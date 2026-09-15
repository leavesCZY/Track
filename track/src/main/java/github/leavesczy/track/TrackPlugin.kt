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
import github.leavesczy.track.replace.instruction.ReplaceInstruction
import github.leavesczy.track.replace.instruction.ReplaceInstructionAsmClassVisitorFactory
import github.leavesczy.track.replace.instruction.ReplaceInstructionConfig
import github.leavesczy.track.replace.instruction.ReplaceInstructionConfig.ReplaceInstructionParameter
import github.leavesczy.track.replace.instruction.ReplaceInstructionPluginParameter
import github.leavesczy.track.replace.instruction.ToastPluginParameter
import github.leavesczy.track.utils.replacePeriodWithSlash
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

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
            handleToastTrack(project = project, variant = variant)
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
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
        }
    }

    private fun handleViewClickTrack(project: Project, variant: Variant) {
        val pluginParameter = project.extensions.findByType(ViewClickPluginParameter::class.java)
            ?: return
        val hasConfigIntent = pluginParameter.onClickClass.isNotBlank() ||
                pluginParameter.onClickMethodName.isNotBlank() ||
                pluginParameter.uncheckViewOnClickAnnotation.isNotBlank() ||
                pluginParameter.include.isNotEmpty() ||
                pluginParameter.exclude.isNotEmpty()
        if (!hasConfigIntent) {
            return
        }
        val onClickClass = pluginParameter.onClickClass
        val onClickMethodName = pluginParameter.onClickMethodName
        if (onClickClass.isBlank() || onClickMethodName.isBlank()) {
            throw trackConfigError(
                extensionName = viewClickTrack,
                detail = "已配置但缺少必填参数 onClickClass / onClickMethodName"
            )
        }
        variant.instrumentation.apply {
            transformClassesWith(
                classVisitorFactoryImplClass = ViewClickAsmClassVisitorFactory::class.java,
                scope = InstrumentationScope.ALL
            ) { params ->
                params.trackConfig.set(
                    ViewClickConfig(
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
            ?: return
        val hasConfigIntent = pluginParameter.onClickClass.isNotBlank() ||
                pluginParameter.uncheckOnClickLabel.isNotBlank()
        if (!hasConfigIntent) {
            return
        }
        val onClickClass = pluginParameter.onClickClass
        if (onClickClass.isBlank()) {
            throw trackConfigError(
                extensionName = composeClickTrack,
                detail = "已配置但缺少必填参数 onClickClass"
            )
        }
        variant.instrumentation.apply {
            transformClassesWith(
                classVisitorFactoryImplClass = ComposeClickAsmClassVisitorFactory::class.java,
                scope = InstrumentationScope.ALL
            ) { params ->
                params.trackConfig.set(
                    ComposeClickConfig(
                        include = emptySet(),
                        exclude = emptySet(),
                        extensionName = composeClickTrack,
                        onClickClass = onClickClass,
                        uncheckOnClickLabel = pluginParameter.uncheckOnClickLabel
                    )
                )
            }
        }
    }

    private fun handleReplaceClassTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(ReplaceClassPluginParameter::class.java)
                ?: return
        val hasConfigIntent = pluginParameter.originClass.isNotBlank() ||
                pluginParameter.targetClass.isNotBlank() ||
                pluginParameter.include.isNotEmpty() ||
                pluginParameter.exclude.isNotEmpty()
        if (!hasConfigIntent) {
            return
        }
        val originClass = pluginParameter.originClass
        val targetClass = pluginParameter.targetClass
        if (originClass.isBlank() || targetClass.isBlank()) {
            throw trackConfigError(
                extensionName = replaceClassTrack,
                detail = "已配置但缺少必填参数 originClass / targetClass"
            )
        }
        variant.instrumentation.apply {
            transformClassesWith(
                classVisitorFactoryImplClass = ReplaceClassAsmClassVisitorFactory::class.java,
                scope = InstrumentationScope.ALL
            ) { params ->
                params.trackConfig.set(
                    ReplaceClassConfig(
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
                ?: return
        val hasConfigIntent = pluginParameter.proxyOwner.isNotBlank() ||
                pluginParameter.include.isNotEmpty() ||
                pluginParameter.exclude.isNotEmpty()
        if (!hasConfigIntent) {
            return
        }
        val proxyOwner = pluginParameter.proxyOwner
        if (proxyOwner.isBlank()) {
            throw trackConfigError(
                extensionName = toastTrack,
                detail = "已配置但缺少必填参数 proxyOwner"
            )
        }
        handleReplaceInstructionTrack(
            variant = variant,
            extensionName = toastTrack,
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
                ?: return
        val hasConfigIntent = pluginParameter.proxyOwner.isNotBlank() ||
                pluginParameter.methods.isNotEmpty() ||
                pluginParameter.include.isNotEmpty() ||
                pluginParameter.exclude.isNotEmpty()
        if (!hasConfigIntent) {
            return
        }
        val proxyOwner = pluginParameter.proxyOwner
        val methods = pluginParameter.methods
        if (proxyOwner.isBlank() || methods.isEmpty()) {
            throw trackConfigError(
                extensionName = optimizedThreadTrack,
                detail = "已配置但缺少必填参数 proxyOwner / methods"
            )
        }
        handleReplaceInstructionTrack(
            variant = variant,
            extensionName = optimizedThreadTrack,
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
                ?: return
        val hasConfigIntent = pluginParameter.instructions.isNotEmpty() ||
                pluginParameter.include.isNotEmpty() ||
                pluginParameter.exclude.isNotEmpty()
        if (!hasConfigIntent) {
            return
        }
        if (pluginParameter.instructions.isEmpty()) {
            throw trackConfigError(
                extensionName = extensionName,
                detail = "已配置但缺少必填参数 instructions"
            )
        }
        val instructions = pluginParameter.instructions.map { instruction ->
            validateReplaceInstruction(
                extensionName = extensionName,
                instruction = instruction
            )
        }.toSet()
        handleReplaceInstructionTrack(
            variant = variant,
            extensionName = extensionName,
            include = pluginParameter.include,
            exclude = pluginParameter.exclude,
            instructions = instructions
        )
    }

    private fun validateReplaceInstruction(
        extensionName: String,
        instruction: ReplaceInstruction
    ): ReplaceInstructionParameter {
        val owner = instruction.owner
        val name = instruction.name
        val proxyOwner = instruction.proxyOwner
        if (owner.isBlank() || name.isBlank() || proxyOwner.isBlank()) {
            throw trackConfigError(
                extensionName = extensionName,
                detail = "instructions 中存在不完整项，owner / name / proxyOwner 均不能为空"
            )
        }
        return ReplaceInstructionParameter(
            owner = replacePeriodWithSlash(className = owner),
            name = name,
            descriptor = instruction.descriptor,
            proxyOwner = proxyOwner
        )
    }

    private fun handleReplaceInstructionTrack(
        variant: Variant,
        extensionName: String,
        include: Set<String>,
        exclude: Set<String>,
        instructions: Set<ReplaceInstructionParameter>
    ) {
        if (instructions.isEmpty()) {
            return
        }
        variant.instrumentation.apply {
            transformClassesWith(
                classVisitorFactoryImplClass = ReplaceInstructionAsmClassVisitorFactory::class.java,
                scope = InstrumentationScope.ALL
            ) { params ->
                params.trackConfig.set(
                    ReplaceInstructionConfig(
                        include = include,
                        exclude = exclude,
                        extensionName = extensionName,
                        instructions = instructions
                    )
                )
            }
        }
    }

    private fun trackConfigError(extensionName: String, detail: String): GradleException {
        return GradleException("$extensionName 配置无效：$detail，已拒绝注册插桩。")
    }

}