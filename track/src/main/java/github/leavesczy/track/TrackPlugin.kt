package github.leavesczy.track

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import github.leavesczy.track.composeclick.ComposeClickAsmClassVisitorFactory
import github.leavesczy.track.composeclick.ComposeClickConfig
import github.leavesczy.track.composeclick.ComposeClickTrackPluginParameter
import github.leavesczy.track.member.MATCH_ALL_DESCRIPTORS
import github.leavesczy.track.member.MemberAsmClassVisitorFactory
import github.leavesczy.track.member.MemberConfig
import github.leavesczy.track.member.MemberConfig.MemberReplacement
import github.leavesczy.track.member.MemberFieldRule
import github.leavesczy.track.member.MemberKind
import github.leavesczy.track.member.MemberMethodRule
import github.leavesczy.track.member.MemberTrackPluginParameter
import github.leavesczy.track.superclass.SuperclassAsmClassVisitorFactory
import github.leavesczy.track.superclass.SuperclassConfig
import github.leavesczy.track.superclass.SuperclassConfig.SuperclassReplacement
import github.leavesczy.track.superclass.SuperclassRule
import github.leavesczy.track.superclass.SuperclassTrackPluginParameter
import github.leavesczy.track.utils.replacePeriodWithSlash
import github.leavesczy.track.viewclick.ViewClickAsmClassVisitorFactory
import github.leavesczy.track.viewclick.ViewClickConfig
import github.leavesczy.track.viewclick.ViewClickTrackPluginParameter
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Track 插件入口：注册四套 Gradle Extension，并在各 Variant 上挂载对应的 ASM ClassVisitor。
 *
 * 未配置的能力不会注册插桩；已写部分字段但不完整时直接失败，避免半残配置悄悄放行。
 */
class TrackPlugin : Plugin<Project> {

    private val viewClickTrack = "viewClickTrack"

    private val composeClickTrack = "composeClickTrack"

    private val superclassTrack = "superclassTrack"

    private val memberTrack = "memberTrack"

    override fun apply(project: Project) {
        project.run {
            extensions.create(
                viewClickTrack,
                ViewClickTrackPluginParameter::class.java
            )
            extensions.create(
                composeClickTrack,
                ComposeClickTrackPluginParameter::class.java
            )
            extensions.create(
                superclassTrack,
                SuperclassTrackPluginParameter::class.java
            )
            extensions.create(
                memberTrack,
                MemberTrackPluginParameter::class.java
            )
        }
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            handleViewClickTrack(project = project, variant = variant)
            handleComposeClickTrack(project = project, variant = variant)
            handleSuperclassTrack(project = project, variant = variant)
            handleMemberTrack(project = project, variant = variant)
            // 插桩后栈帧可能失效，仅对改过的方法重算，避免全量 COMPUTE_MAXS 的额外开销。
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
        }
    }

    private fun handleViewClickTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(ViewClickTrackPluginParameter::class.java)
                ?: return
        val clickHandlerClass = pluginParameter.clickHandlerClass
        val clickMethodName = pluginParameter.clickMethodName
        val skipOnClickAnnotation = pluginParameter.skipOnClickAnnotation
        val include = pluginParameter.include
        val exclude = pluginParameter.exclude
        val hasAnyConfig = clickHandlerClass.isNotBlank() ||
                clickMethodName.isNotBlank() ||
                skipOnClickAnnotation.isNotBlank() ||
                include.isNotEmpty() ||
                exclude.isNotEmpty()
        val isComplete = clickHandlerClass.isNotBlank() && clickMethodName.isNotBlank()
        guardTrackConfig(
            extensionName = viewClickTrack,
            hasAnyConfig = hasAnyConfig,
            isComplete = isComplete,
            missingDetail = "缺少必填参数 clickHandlerClass / clickMethodName"
        ) {
            // ALL：匿名 OnClickListener / lambda 生成类不一定在 project 模块内。
            variant.instrumentation.apply {
                transformClassesWith(
                    classVisitorFactoryImplClass = ViewClickAsmClassVisitorFactory::class.java,
                    scope = InstrumentationScope.ALL
                ) { params ->
                    params.trackConfig.set(
                        ViewClickConfig(
                            include = include,
                            exclude = exclude,
                            clickHandlerClass = clickHandlerClass,
                            clickMethodName = clickMethodName,
                            skipOnClickAnnotation = skipOnClickAnnotation
                        )
                    )
                }
            }
        }
    }

    private fun handleComposeClickTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(ComposeClickTrackPluginParameter::class.java)
                ?: return
        val clickWrapperClass = pluginParameter.clickWrapperClass
        val skipOnClickLabel = pluginParameter.skipOnClickLabel
        val hasAnyConfig = clickWrapperClass.isNotBlank() || skipOnClickLabel.isNotBlank()
        val isComplete = clickWrapperClass.isNotBlank()
        guardTrackConfig(
            extensionName = composeClickTrack,
            hasAnyConfig = hasAnyConfig,
            isComplete = isComplete,
            missingDetail = "缺少必填参数 clickWrapperClass"
        ) {
            // 目标是 Compose Foundation 内部类，必须扫依赖；include/exclude 由 isTrackEnabled 收窄。
            variant.instrumentation.apply {
                transformClassesWith(
                    classVisitorFactoryImplClass = ComposeClickAsmClassVisitorFactory::class.java,
                    scope = InstrumentationScope.ALL
                ) { params ->
                    params.trackConfig.set(
                        ComposeClickConfig(
                            include = emptySet(),
                            exclude = emptySet(),
                            clickWrapperClass = clickWrapperClass,
                            skipOnClickLabel = skipOnClickLabel
                        )
                    )
                }
            }
        }
    }

    private fun handleSuperclassTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(SuperclassTrackPluginParameter::class.java)
                ?: return
        val rules = pluginParameter.rules
        val hasAnyConfig = rules.isNotEmpty()
        guardTrackConfig(
            extensionName = superclassTrack,
            hasAnyConfig = hasAnyConfig,
            isComplete = hasAnyConfig,
            missingDetail = "缺少必填参数 rules"
        ) {
            // 同一 include/exclude 合并为一次 transform；不同过滤条件分桶注册，避免互相覆盖。
            val buckets =
                linkedMapOf<Pair<Set<String>, Set<String>>, MutableSet<SuperclassReplacement>>()
            rules.forEach { rule ->
                val replacement = validateSuperclassRule(rule = rule)
                val bucket = buckets.getOrPut(rule.include to rule.exclude) { mutableSetOf() }
                val duplicated = bucket.find { it.originClass == replacement.originClass }
                if (duplicated != null) {
                    throw trackConfigError(
                        extensionName = superclassTrack,
                        detail = "同一 include/exclude 下 originClass 重复：${replacement.originClass}（已映射到 ${duplicated.targetClass}，又配置为 ${replacement.targetClass}）"
                    )
                }
                bucket.add(replacement)
            }
            buckets.forEach { (filter, replacements) ->
                val (include, exclude) = filter
                registerSuperclassTrack(
                    variant = variant,
                    include = include,
                    exclude = exclude,
                    replacements = replacements
                )
            }
        }
    }

    private fun validateSuperclassRule(rule: SuperclassRule): SuperclassReplacement {
        val originClass = rule.originClass
        val targetClass = rule.targetClass
        if (originClass.isBlank() || targetClass.isBlank()) {
            throw trackConfigError(
                extensionName = superclassTrack,
                detail = "rules 中存在不完整项，originClass / targetClass 均不能为空"
            )
        }
        if (originClass == targetClass) {
            throw trackConfigError(
                extensionName = superclassTrack,
                detail = "rules 中 originClass 与 targetClass 不能相同：$originClass"
            )
        }
        return SuperclassReplacement(
            originClass = originClass,
            targetClass = targetClass
        )
    }

    private fun registerSuperclassTrack(
        variant: Variant,
        include: Set<String>,
        exclude: Set<String>,
        replacements: Set<SuperclassReplacement>
    ) {
        variant.instrumentation.apply {
            transformClassesWith(
                classVisitorFactoryImplClass = SuperclassAsmClassVisitorFactory::class.java,
                scope = InstrumentationScope.ALL
            ) { params ->
                params.trackConfig.set(
                    SuperclassConfig(
                        include = include,
                        exclude = exclude,
                        replacements = replacements
                    )
                )
            }
        }
    }

    private fun handleMemberTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(MemberTrackPluginParameter::class.java)
                ?: return
        val methods = pluginParameter.methods
        val fields = pluginParameter.fields
        val hasAnyConfig = methods.isNotEmpty() || fields.isNotEmpty()
        guardTrackConfig(
            extensionName = memberTrack,
            hasAnyConfig = hasAnyConfig,
            isComplete = hasAnyConfig,
            missingDetail = "缺少必填参数 methods / fields"
        ) {
            // 与 superclass 相同：按 include/exclude 分桶，桶内校验成员规则是否冲突。
            val buckets =
                linkedMapOf<Pair<Set<String>, Set<String>>, MutableSet<MemberReplacement>>()
            methods.forEach { rule ->
                addMemberReplacement(
                    bucket = buckets.getOrPut(rule.include to rule.exclude) { mutableSetOf() },
                    replacement = validateMemberMethodRule(rule = rule)
                )
            }
            fields.forEach { rule ->
                addMemberReplacement(
                    bucket = buckets.getOrPut(rule.include to rule.exclude) { mutableSetOf() },
                    replacement = validateMemberFieldRule(rule = rule)
                )
            }
            buckets.forEach { (filter, replacements) ->
                val (include, exclude) = filter
                registerMemberTrack(
                    variant = variant,
                    include = include,
                    exclude = exclude,
                    replacements = replacements
                )
            }
        }
    }

    private fun addMemberReplacement(
        bucket: MutableSet<MemberReplacement>,
        replacement: MemberReplacement
    ) {
        val duplicated = bucket.find { existing ->
            existing.conflictsWith(other = replacement)
        }
        if (duplicated != null) {
            throw trackConfigError(
                extensionName = memberTrack,
                detail = "同一 include/exclude 下成员规则冲突：" +
                        "${replacement.kind} ${replacement.ownerClass}.${replacement.memberName} " +
                        "descriptor=${replacement.descriptor} → ${replacement.proxyClass}，" +
                        "与已有规则 descriptor=${duplicated.descriptor} → ${duplicated.proxyClass} 重叠"
            )
        }
        bucket.add(replacement)
    }

    private fun MemberReplacement.conflictsWith(other: MemberReplacement): Boolean {
        if (kind != other.kind ||
            ownerClass != other.ownerClass ||
            memberName != other.memberName
        ) {
            return false
        }
        if (descriptor == other.descriptor) {
            return true
        }
        // "*" 会匹配全部重载/类型，不能再与同名具体 descriptor 并存。
        return descriptor == MATCH_ALL_DESCRIPTORS || other.descriptor == MATCH_ALL_DESCRIPTORS
    }

    private fun validateMemberFieldRule(rule: MemberFieldRule): MemberReplacement {
        val ownerClass = rule.ownerClass
        val fieldName = rule.fieldName
        val typeDescriptor = rule.typeDescriptor
        val proxyClass = rule.proxyClass
        if (ownerClass.isBlank() || fieldName.isBlank() || typeDescriptor.isBlank() || proxyClass.isBlank()) {
            throw trackConfigError(
                extensionName = memberTrack,
                detail = "fields 中存在不完整项，ownerClass / fieldName / typeDescriptor / proxyClass 均不能为空；匹配全部类型请使用 typeDescriptor = \"${MemberFieldRule.MATCH_ALL_TYPE_DESCRIPTORS}\""
            )
        }
        return MemberReplacement(
            kind = MemberKind.FIELD,
            // 字节码 owner 使用内部名（斜杠分隔），与 visitFieldInsn 对齐。
            ownerClass = replacePeriodWithSlash(className = ownerClass),
            memberName = fieldName,
            descriptor = typeDescriptor,
            proxyClass = proxyClass
        )
    }

    private fun validateMemberMethodRule(rule: MemberMethodRule): MemberReplacement {
        val ownerClass = rule.ownerClass
        val methodName = rule.methodName
        val methodDescriptor = rule.methodDescriptor
        val proxyClass = rule.proxyClass
        if (ownerClass.isBlank() || methodName.isBlank() || methodDescriptor.isBlank() || proxyClass.isBlank()) {
            throw trackConfigError(
                extensionName = memberTrack,
                detail = "methods 中存在不完整项，ownerClass / methodName / methodDescriptor / proxyClass 均不能为空；匹配全部重载请使用 methodDescriptor = \"${MemberMethodRule.MATCH_ALL_METHOD_DESCRIPTORS}\""
            )
        }
        return MemberReplacement(
            kind = MemberKind.METHOD,
            ownerClass = replacePeriodWithSlash(className = ownerClass),
            memberName = methodName,
            descriptor = methodDescriptor,
            proxyClass = proxyClass
        )
    }

    private fun registerMemberTrack(
        variant: Variant,
        include: Set<String>,
        exclude: Set<String>,
        replacements: Set<MemberReplacement>
    ) {
        variant.instrumentation.apply {
            transformClassesWith(
                classVisitorFactoryImplClass = MemberAsmClassVisitorFactory::class.java,
                scope = InstrumentationScope.ALL
            ) { params ->
                params.trackConfig.set(
                    MemberConfig(
                        include = include,
                        exclude = exclude,
                        replacements = replacements
                    )
                )
            }
        }
    }

    /**
     * 未配置 → 跳过；配置不完整 → 抛错；完整 → 注册插桩。
     */
    private inline fun guardTrackConfig(
        extensionName: String,
        hasAnyConfig: Boolean,
        isComplete: Boolean,
        missingDetail: String,
        register: () -> Unit
    ) {
        if (!hasAnyConfig) {
            return
        }
        if (!isComplete) {
            throw trackConfigError(
                extensionName = extensionName,
                detail = "已配置但$missingDetail"
            )
        }
        register()
    }

    private fun trackConfigError(extensionName: String, detail: String): GradleException {
        return GradleException("$extensionName 配置无效：$detail，已拒绝注册插桩。")
    }

}
