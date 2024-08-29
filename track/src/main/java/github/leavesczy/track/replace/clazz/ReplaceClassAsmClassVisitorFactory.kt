package github.leavesczy.track.replace.clazz

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.BaseTrackConfigParameters
import github.leavesczy.track.utils.replaceDotBySlash
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
        val className = classData.className
        val superClasses = classData.superClasses
        return !(className == trackConfig.targetClass || superClasses.first() != trackConfig.originClass)
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
            replaceDotBySlash(className = trackConfig.targetClass),
            interfaces
        )
        nLog {
            "$name 的父类符合 ReplaceClass 规则，完成处理..."
        }
    }

    override fun visitEnd() {
        super.visitEnd()
        accept(nextClassVisitor)
    }

}