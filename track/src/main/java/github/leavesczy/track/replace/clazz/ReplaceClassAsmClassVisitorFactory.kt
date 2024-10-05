package github.leavesczy.track.replace.clazz

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.BaseTrackConfigParameters
import github.leavesczy.track.utils.replacePeriodWithSlash
import org.objectweb.asm.ClassVisitor

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal abstract class ReplaceClassAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<BaseTrackConfigParameters, ReplaceClassConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): BaseTrackClassNode {
        return ReplaceClassClassVisitor(
            nextClassVisitor = nextClassVisitor,
            trackConfig = trackConfig
        )
    }

    override fun isTrackEnabled(classData: ClassData): Boolean {
        val superClasses = classData.superClasses
        if (classData.className == trackConfig.targetClass || superClasses.isEmpty()) {
            return false
        }
        return superClasses.first() == trackConfig.originClass
    }

}

private class ReplaceClassClassVisitor(
    private val nextClassVisitor: ClassVisitor,
    override val trackConfig: ReplaceClassConfig
) : BaseTrackClassNode(trackConfig = trackConfig) {

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(
            version,
            access,
            name,
            signature,
            replacePeriodWithSlash(className = trackConfig.targetClass),
            interfaces
        )
        log {
            "$name 的父类符合规则，完成处理..."
        }
    }

    override fun visitEnd() {
        super.visitEnd()
        accept(nextClassVisitor)
    }

}