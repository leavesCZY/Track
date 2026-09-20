package github.leavesczy.track

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import github.leavesczy.track.utils.ASM_API
import github.leavesczy.track.utils.LogPrint
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.tree.ClassNode
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

/** Tree API 基类：先收集完整 ClassNode，再在 visitEnd 里改写后 accept 下游。 */
internal abstract class BaseTrackClassNode(
    protected open val trackConfig: BaseTrackConfig,
    private val logTag: String
) : ClassNode(ASM_API) {

    fun log(msg: () -> String) {
        LogPrint.normal(tag = logTag, msg = msg)
    }

}

internal interface BaseTrackConfig : Serializable {

    /** 类名正则；空表示不限制（仍受 exclude / isTrackEnabled 约束）。 */
    val include: Set<String>

    /** 类名正则；命中则跳过。 */
    val exclude: Set<String>

}

internal interface BaseTrackConfigParameters<TrackConfig : BaseTrackConfig> :
    InstrumentationParameters {

    @get:Input
    val trackConfig: Property<TrackConfig>

}

/**
 * 各插桩 Factory 的公共过滤：先按 include/exclude 筛类名，再交给 [isTrackEnabled]。
 *
 * - 仅 exclude：排除名单外全部可进
 * - 仅 include：必须命中 include
 * - 两者都有：须命中 include 且不命中 exclude
 */
internal interface BaseTrackAsmClassVisitorFactory<
        Parameters : BaseTrackConfigParameters<TrackConfig>,
        TrackConfig : BaseTrackConfig
        > : AsmClassVisitorFactory<Parameters> {

    @get:Input
    val trackConfig: TrackConfig
        get() = parameters.get().trackConfig.get()

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor

    override fun isInstrumentable(classData: ClassData): Boolean {
        val include = RegexPatternCache.compileAll(patterns = trackConfig.include)
        val exclude = RegexPatternCache.compileAll(patterns = trackConfig.exclude)
        if (include.isEmpty()) {
            if (classData.matches(rules = exclude)) {
                return false
            }
        } else if (exclude.isEmpty()) {
            if (!classData.matches(rules = include)) {
                return false
            }
        } else if (!classData.matches(rules = include) || classData.matches(rules = exclude)) {
            return false
        }
        return isTrackEnabled(classData = classData)
    }

    private fun ClassData.matches(rules: List<Regex>): Boolean {
        for (rule in rules) {
            if (className.matches(regex = rule)) {
                return true
            }
        }
        return false
    }

    /** 业务侧额外过滤（如只处理特定类、跳过 proxy）。 */
    fun isTrackEnabled(classData: ClassData): Boolean

}

/**
 * 跨 class 复用已编译的 [Regex]，避免 ALL scope 下每个类都重新编译同一批 pattern。
 */
private object RegexPatternCache {

    private val patternCache = ConcurrentHashMap<String, Regex>()

    private val patternSetCache = ConcurrentHashMap<Set<String>, List<Regex>>()

    fun compileAll(patterns: Set<String>): List<Regex> {
        if (patterns.isEmpty()) {
            return emptyList()
        }
        return patternSetCache.getOrPut(patterns) {
            patterns.map { pattern ->
                patternCache.getOrPut(pattern) { Regex(pattern) }
            }
        }
    }

}
