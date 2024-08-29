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
import github.leavesczy.track.replace.instruction.ReplaceInstructionPluginParameter
import github.leavesczy.track.toast.ToastAsmClassVisitorFactory
import github.leavesczy.track.toast.ToastConfig
import github.leavesczy.track.toast.ToastPluginParameter
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
            handleToastTrack(project = project, variant = variant)
            handleReplaceClassTrack(project = project, variant = variant)
            handleOptimizedThreadTrack(
                project = project,
                variant = variant
            )
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
        val config = if (pluginParameter == null) {
            null
        } else {
            ViewClickConfig(
                pluginParameter = pluginParameter,
                extensionName = viewClickTrack
            )
        }
        if (config != null) {
            variant.instrumentation.apply {
                transformClassesWith(
                    ViewClickAsmClassVisitorFactory::class.java,
                    InstrumentationScope.ALL
                ) { params ->
                    params.trackConfig.set(config)
                }
            }
        }
    }

    private fun handleComposeClickTrack(project: Project, variant: Variant) {
        val pluginParameter = project.extensions.findByType(ComposeClickPluginParameter::class.java)
        val config = if (pluginParameter == null) {
            null
        } else {
            ComposeClickConfig(
                pluginParameter = pluginParameter,
                extensionName = composeClickTrack
            )
        }
        if (config != null) {
            variant.instrumentation.apply {
                transformClassesWith(
                    ComposeClickAsmClassVisitorFactory::class.java,
                    InstrumentationScope.ALL
                ) { params ->
                    params.trackConfig.set(config)
                }
            }
        }
    }

    private fun handleToastTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(ToastPluginParameter::class.java)
        val config = if (pluginParameter == null) {
            null
        } else {
            ToastConfig(
                pluginParameter = pluginParameter,
                extensionName = toastTrack
            )
        }
        if (config != null) {
            variant.instrumentation.apply {
                transformClassesWith(
                    ToastAsmClassVisitorFactory::class.java,
                    InstrumentationScope.ALL
                ) { params ->
                    params.trackConfig.set(config)
                }
            }
        }
    }

    private fun handleReplaceClassTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(ReplaceClassPluginParameter::class.java)
        val config = if (pluginParameter == null) {
            null
        } else {
            ReplaceClassConfig(
                pluginParameter = pluginParameter,
                extensionName = replaceClassTrack
            )
        }
        if (config != null) {
            variant.instrumentation.apply {
                transformClassesWith(
                    ReplaceClassAsmClassVisitorFactory::class.java,
                    InstrumentationScope.ALL
                ) { params ->
                    params.trackConfig.set(config)
                }
            }
        }
    }

    private fun handleOptimizedThreadTrack(
        project: Project,
        variant: Variant
    ) {
        val pluginParameter =
            project.extensions.findByType(OptimizedThreadPluginParameter::class.java)
        val config = if (pluginParameter == null) {
            null
        } else {
            ReplaceInstructionConfig(
                pluginParameter = pluginParameter,
                extensionName = optimizedThreadTrack
            )
        }
        if (config != null) {
            handleReplaceInstructionTrack(
                variant = variant,
                config = config
            )
        }
    }

    private fun handleReplaceInstructionTrack(
        project: Project,
        variant: Variant,
        extensionName: String
    ) {
        val pluginParameter =
            project.extensions.findByName(extensionName) as? ReplaceInstructionPluginParameter
        val config = if (pluginParameter == null) {
            null
        } else {
            ReplaceInstructionConfig(
                pluginParameter = pluginParameter,
                extensionName = extensionName
            )
        }
        if (config != null) {
            handleReplaceInstructionTrack(
                variant = variant,
                config = config
            )
        }
    }

    private fun handleReplaceInstructionTrack(
        variant: Variant,
        config: ReplaceInstructionConfig
    ) {
        variant.instrumentation.apply {
            transformClassesWith(
                ReplaceInstructionAsmClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ) { params ->
                params.trackConfig.set(config)
            }
        }
    }

}