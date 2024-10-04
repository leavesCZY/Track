package github.leavesczy.track

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import github.leavesczy.track.click.compose.ComposeClickAsmClassVisitorFactory
import github.leavesczy.track.click.compose.ComposeClickConfig
import github.leavesczy.track.click.compose.ComposeClickPluginParameter
import github.leavesczy.track.click.view.ViewClickAsmClassVisitorFactory
import github.leavesczy.track.click.view.ViewClickConfig
import github.leavesczy.track.click.view.ViewClickPluginParameter
import github.leavesczy.track.replace.clazz.ReplaceClassAsmClassVisitorFactory
import github.leavesczy.track.replace.clazz.ReplaceClassConfig
import github.leavesczy.track.replace.clazz.ReplaceClassPluginParameter
import github.leavesczy.track.replace.instruction.OptimizedThreadPluginParameter
import github.leavesczy.track.replace.instruction.ReplaceInstructionAsmClassVisitorFactory
import github.leavesczy.track.replace.instruction.ReplaceInstructionConfig
import github.leavesczy.track.replace.instruction.ReplaceInstructionConfig.ReplaceInstructionParameter
import github.leavesczy.track.replace.instruction.ReplaceInstructionPluginParameter
import github.leavesczy.track.replace.instruction.ToastPluginParameter
import github.leavesczy.track.utils.replaceDotBySlash
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
class TrackPlugin : Plugin<Project> {

    private val viewClickTrack = "viewClickTrack"

    private val composeClickTrack = "composeClickTrack"

    private val toastTrack = "toastTrack"

    private val replaceClassTrack = "replaceClassTrack"

    private val optimizedThreadTrack = "optimizedThreadTrack"

    private val replaceFieldTrack = "replaceFieldTrack"

    private val replaceMethodTrack = "replaceMethodTrack"

    override fun apply(project: Project) {
        project.run {
            extensions.create(
                viewClickTrack,
                ViewClickPluginParameter::class.java
            )
            extensions.create(
                composeClickTrack,
                ComposeClickPluginParameter::class.java
            )
            extensions.create(
                toastTrack,
                ToastPluginParameter::class.java
            )
            extensions.create(
                replaceClassTrack,
                ReplaceClassPluginParameter::class.java
            )
            extensions.create(
                optimizedThreadTrack,
                OptimizedThreadPluginParameter::class.java
            )
            extensions.create(
                replaceFieldTrack,
                ReplaceInstructionPluginParameter::class.java
            )
            extensions.create(
                replaceMethodTrack,
                ReplaceInstructionPluginParameter::class.java
            )
        }
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            handleViewClickTrack(project = project, variant = variant)
            handleComposeClickTrack(project = project, variant = variant)
            handleReplaceClassTrack(project = project, variant = variant)
            handleOptimizedThreadTrack(
                project = project,
                variant = variant
            )
            handleToastTrack(project = project, variant = variant)
            handleReplaceInstructionTrack(
                project = project,
                variant = variant,
                extensionName = replaceFieldTrack
            )
            handleReplaceInstructionTrack(
                project = project,
                variant = variant,
                extensionName = replaceMethodTrack
            )
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
        }
    }

    private fun handleViewClickTrack(project: Project, variant: Variant) {
        val pluginParameter = project.extensions.findByType(ViewClickPluginParameter::class.java)
        val onClickClass = pluginParameter?.onClickClass
        val onClickMethodName = pluginParameter?.onClickMethodName
        if (onClickClass.isNullOrBlank() || onClickMethodName.isNullOrBlank()) {
            return
        }
        variant.instrumentation.apply {
            transformClassesWith(
                ViewClickAsmClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ) { params ->
                params.trackConfig.set(
                    ViewClickConfig(
                        isEnabled = pluginParameter.isEnabled,
                        include = pluginParameter.include,
                        exclude = pluginParameter.exclude,
                        extensionName = viewClickTrack,
                        onClickClass = onClickClass,
                        onClickMethodName = onClickMethodName,
                        uncheckViewOnClickAnnotation = pluginParameter.uncheckViewOnClickAnnotation
                    )
                )
            }
        }
    }

    private fun handleComposeClickTrack(project: Project, variant: Variant) {
        val pluginParameter = project.extensions.findByType(ComposeClickPluginParameter::class.java)
        val onClickClass = pluginParameter?.onClickClass
        if (onClickClass.isNullOrBlank()) {
            return
        }
        variant.instrumentation.apply {
            transformClassesWith(
                ComposeClickAsmClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ) { params ->
                params.trackConfig.set(
                    ComposeClickConfig(
                        isEnabled = pluginParameter.isEnabled,
                        include = emptySet(),
                        exclude = emptySet(),
                        extensionName = composeClickTrack,
                        onClickClass = onClickClass,
                        onClickWhiteList = pluginParameter.onClickWhiteList
                    )
                )
            }
        }
    }

    private fun handleReplaceClassTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(ReplaceClassPluginParameter::class.java)
        val originClass = pluginParameter?.originClass
        val targetClass = pluginParameter?.targetClass
        if (originClass.isNullOrBlank() || targetClass.isNullOrBlank()) {
            return
        }
        variant.instrumentation.apply {
            transformClassesWith(
                ReplaceClassAsmClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ) { params ->
                params.trackConfig.set(
                    ReplaceClassConfig(
                        isEnabled = pluginParameter.isEnabled,
                        include = pluginParameter.include,
                        exclude = pluginParameter.exclude,
                        extensionName = replaceClassTrack,
                        originClass = originClass,
                        targetClass = targetClass
                    )
                )
            }
        }
    }

    private fun handleToastTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(ToastPluginParameter::class.java)
        val proxyOwner = pluginParameter?.proxyOwner
        if (proxyOwner.isNullOrBlank()) {
            return
        }
        handleReplaceInstructionTrack(
            variant = variant,
            extensionName = toastTrack,
            isEnabled = pluginParameter.isEnabled,
            include = pluginParameter.include,
            exclude = pluginParameter.exclude,
            instructions = setOf(
                element = ReplaceInstructionParameter(
                    owner = "android/widget/Toast",
                    name = "show",
                    descriptor = "()V",
                    proxyOwner = proxyOwner
                )
            )
        )
    }

    private fun handleOptimizedThreadTrack(
        project: Project,
        variant: Variant
    ) {
        val pluginParameter =
            project.extensions.findByType(OptimizedThreadPluginParameter::class.java)
        val proxyOwner = pluginParameter?.proxyOwner
        val methods = pluginParameter?.methods
        if (proxyOwner.isNullOrBlank() || methods.isNullOrEmpty()) {
            return
        }
        handleReplaceInstructionTrack(
            variant = variant,
            extensionName = optimizedThreadTrack,
            isEnabled = pluginParameter.isEnabled,
            include = pluginParameter.include,
            exclude = pluginParameter.exclude,
            instructions = methods.map {
                ReplaceInstructionParameter(
                    owner = "java/util/concurrent/Executors",
                    name = it,
                    descriptor = "",
                    proxyOwner = proxyOwner
                )
            }.toSet()
        )
    }

    private fun handleReplaceInstructionTrack(
        project: Project,
        variant: Variant,
        extensionName: String
    ) {
        val pluginParameter =
            project.extensions.findByName(extensionName) as? ReplaceInstructionPluginParameter
        val instructions = pluginParameter?.instructions?.mapNotNull {
            val owner = it.owner
            val name = it.name
            val descriptor = it.descriptor
            val proxyOwner = it.proxyOwner
            if (owner.isBlank() || name.isBlank() || proxyOwner.isBlank()) {
                null
            } else {
                ReplaceInstructionParameter(
                    owner = replaceDotBySlash(className = owner),
                    name = name,
                    descriptor = descriptor,
                    proxyOwner = proxyOwner
                )
            }
        }?.toSet()
        if (instructions.isNullOrEmpty()) {
            return
        }
        handleReplaceInstructionTrack(
            variant = variant,
            extensionName = extensionName,
            isEnabled = pluginParameter.isEnabled,
            include = pluginParameter.include,
            exclude = pluginParameter.exclude,
            instructions = instructions
        )
    }

    private fun handleReplaceInstructionTrack(
        variant: Variant,
        extensionName: String,
        isEnabled: Boolean,
        include: Set<String>,
        exclude: Set<String>,
        instructions: Set<ReplaceInstructionParameter>
    ) {
        if (instructions.isEmpty()) {
            return
        }
        variant.instrumentation.apply {
            transformClassesWith(
                ReplaceInstructionAsmClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ) { params ->
                params.trackConfig.set(
                    ReplaceInstructionConfig(
                        isEnabled = isEnabled,
                        include = include,
                        exclude = exclude,
                        extensionName = extensionName,
                        instructions = instructions
                    )
                )
            }
        }
    }

}