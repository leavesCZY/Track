package github.leavesczy.trace

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import github.leavesczy.trace.click.ClickConfig
import github.leavesczy.trace.click.ClickPluginParameter
import github.leavesczy.trace.click.ComposeClickClassVisitorFactory
import github.leavesczy.trace.click.ViewClickClassVisitorFactory
import github.leavesczy.trace.replace.ReplaceClassClassVisitorFactory
import github.leavesczy.trace.replace.ReplaceClassConfig
import github.leavesczy.trace.replace.ReplaceClassPluginParameter
import github.leavesczy.trace.toast.ToastClassVisitorFactory
import github.leavesczy.trace.toast.ToastConfig
import github.leavesczy.trace.toast.ToastPluginParameter
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
class TracePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create(
            "clickTrace",
            ClickPluginParameter::class.java
        )
        project.extensions.create(
            "replaceClassTrace",
            ReplaceClassPluginParameter::class.java
        )
        project.extensions.create(
            "toastTrace",
            ToastPluginParameter::class.java
        )
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            handleClickTrace(project = project, variant = variant)
            handleReplaceClassTrace(project = project, variant = variant)
            handleToastTrace(project = project, variant = variant)
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
        }
    }

    private fun handleClickTrace(project: Project, variant: Variant) {
        val pluginParameter = project.extensions.findByType(ClickPluginParameter::class.java)
        val clickConfig = if (pluginParameter == null) {
            null
        } else {
            ClickConfig(pluginParameter = pluginParameter)
        }
        if (clickConfig != null) {
            val viewClickConfig = clickConfig.viewClickConfig
            val composeClickConfig = clickConfig.composeClickConfig
            variant.instrumentation.apply {
                if (viewClickConfig != null) {
                    transformClassesWith(
                        ViewClickClassVisitorFactory::class.java,
                        InstrumentationScope.ALL
                    ) { params ->
                        params.config.set(viewClickConfig)
                    }
                }
                if (composeClickConfig != null) {
                    transformClassesWith(
                        ComposeClickClassVisitorFactory::class.java,
                        InstrumentationScope.ALL
                    ) { params ->
                        params.config.set(composeClickConfig)
                    }
                }
            }
        }
    }

    private fun handleReplaceClassTrace(project: Project, variant: Variant) {
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

    private fun handleToastTrace(project: Project, variant: Variant) {
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