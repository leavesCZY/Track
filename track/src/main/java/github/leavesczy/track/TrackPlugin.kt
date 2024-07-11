package github.leavesczy.track

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import github.leavesczy.track.click.compose.ComposeClickClassVisitorFactory
import github.leavesczy.track.click.compose.ComposeClickConfig
import github.leavesczy.track.click.compose.ComposeClickPluginParameter
import github.leavesczy.track.click.view.ViewClickClassVisitorFactory
import github.leavesczy.track.click.view.ViewClickConfig
import github.leavesczy.track.click.view.ViewClickPluginParameter
import github.leavesczy.track.replace.ReplaceClassClassVisitorFactory
import github.leavesczy.track.replace.ReplaceClassConfig
import github.leavesczy.track.replace.ReplaceClassPluginParameter
import github.leavesczy.track.toast.ToastClassVisitorFactory
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

    override fun apply(project: Project) {
        project.run {
            extensions.create(
                "viewClickTrack",
                ViewClickPluginParameter::class.java
            )
            extensions.create(
                "composeClickTrack",
                ComposeClickPluginParameter::class.java
            )
            extensions.create(
                "replaceClassTrack",
                ReplaceClassPluginParameter::class.java
            )
            extensions.create(
                "toastTrack",
                ToastPluginParameter::class.java
            )
        }
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            handleViewClickTrack(project = project, variant = variant)
            handleComposeClickTrack(project = project, variant = variant)
            handleReplaceClassTrack(project = project, variant = variant)
            handleToastTrack(project = project, variant = variant)
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
        }
    }

    private fun handleViewClickTrack(project: Project, variant: Variant) {
        val pluginParameter = project.extensions.findByType(ViewClickPluginParameter::class.java)
        val viewClickConfig = if (pluginParameter == null) {
            null
        } else {
            ViewClickConfig(pluginParameter = pluginParameter)
        }
        if (viewClickConfig != null) {
            variant.instrumentation.apply {
                transformClassesWith(
                    ViewClickClassVisitorFactory::class.java,
                    InstrumentationScope.ALL
                ) { params ->
                    params.config.set(viewClickConfig)
                }
            }
        }
    }

    private fun handleComposeClickTrack(project: Project, variant: Variant) {
        val pluginParameter = project.extensions.findByType(ComposeClickPluginParameter::class.java)
        val composeClickConfig = if (pluginParameter == null) {
            null
        } else {
            ComposeClickConfig(pluginParameter = pluginParameter)
        }
        if (composeClickConfig != null) {
            variant.instrumentation.apply {
                transformClassesWith(
                    ComposeClickClassVisitorFactory::class.java,
                    InstrumentationScope.ALL
                ) { params ->
                    params.config.set(composeClickConfig)
                }
            }
        }
    }

    private fun handleReplaceClassTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(ReplaceClassPluginParameter::class.java)
        val replaceClassConfig = if (pluginParameter == null) {
            null
        } else {
            ReplaceClassConfig(pluginParameter = pluginParameter)
        }
        if (replaceClassConfig != null) {
            variant.instrumentation.apply {
                transformClassesWith(
                    ReplaceClassClassVisitorFactory::class.java,
                    InstrumentationScope.ALL
                ) { params ->
                    params.config.set(replaceClassConfig)
                }
            }
        }
    }

    private fun handleToastTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(ToastPluginParameter::class.java)
        val toastConfig = if (pluginParameter == null) {
            null
        } else {
            ToastConfig(pluginParameter = pluginParameter)
        }
        if (toastConfig != null) {
            variant.instrumentation.apply {
                transformClassesWith(
                    ToastClassVisitorFactory::class.java,
                    InstrumentationScope.ALL
                ) { params ->
                    params.config.set(toastConfig)
                }
            }
        }
    }

}