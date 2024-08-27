package github.leavesczy.track.replace.field

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.BaseTrackConfigParameters
import github.leavesczy.track.utils.LogPrint
import github.leavesczy.track.utils.replaceDotBySlash
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal abstract class ReplaceFieldAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<BaseTrackConfigParameters, ReplaceFieldConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): BaseTrackClassNode {
        return ReplaceFieldClassVisitor(
            config = trackConfig,
            nextClassVisitor = nextClassVisitor
        )
    }

    override fun isTrackEnabled(classData: ClassData): Boolean {
        return classData.className != trackConfig.proxyOwner
    }

}

private class ReplaceFieldClassVisitor(
    private val config: ReplaceFieldConfig,
    private val nextClassVisitor: ClassVisitor
) : BaseTrackClassNode() {

    override fun visitEnd() {
        super.visitEnd()
        accept(nextClassVisitor)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        return ReplaceFieldMethodVisitor(
            api = api,
            methodVisitor = methodVisitor,
            classNode = this,
            config = config
        )
    }

}

private class ReplaceFieldMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor,
    private val classNode: ClassNode,
    private val config: ReplaceFieldConfig
) : MethodVisitor(api, methodVisitor) {

    private val toOwner = replaceDotBySlash(className = config.proxyOwner)

    override fun visitFieldInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?
    ) {
        val ownerNameDesc = owner + name + descriptor
        val find = config.fields.find {
            it == ownerNameDesc
        }
        if (find != null && opcode == Opcodes.GETSTATIC) {
            super.visitFieldInsn(opcode, toOwner, name, descriptor)
            LogPrint.normal(tag = "ReplaceFieldTrack") {
                "${classNode.name} 发现符合规则的指令：$owner $name $descriptor , 替换为 $toOwner $name $descriptor 完成处理..."
            }
        } else {
            super.visitFieldInsn(opcode, owner, name, descriptor)
        }
    }

}