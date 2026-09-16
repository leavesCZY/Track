package github.leavesczy.track

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import github.leavesczy.track.click.compose.ComposeClickAsmClassVisitorFactory
import github.leavesczy.track.click.compose.ComposeClickConfig
import github.leavesczy.track.click.compose.ComposeClickTrackPluginParameter
import github.leavesczy.track.click.view.ViewClickAsmClassVisitorFactory
import github.leavesczy.track.click.view.ViewClickConfig
import github.leavesczy.track.click.view.ViewClickTrackPluginParameter
import github.leavesczy.track.replace.inheritance.ReplaceClassAsmClassVisitorFactory
import github.leavesczy.track.replace.inheritance.ReplaceClassConfig
import github.leavesczy.track.replace.inheritance.ReplaceClassTrackPluginParameter
import github.leavesczy.track.replace.rule.OptimizedThreadTrackPluginParameter
import github.leavesczy.track.replace.rule.ReplaceFieldRule
import github.leavesczy.track.replace.rule.ReplaceFieldTrackPluginParameter
import github.leavesczy.track.replace.rule.ReplaceMethodRule
import github.leavesczy.track.replace.rule.ReplaceMethodTrackPluginParameter
import github.leavesczy.track.replace.rule.ReplaceRuleAsmClassVisitorFactory
import github.leavesczy.track.replace.rule.ReplaceRuleConfig
import github.leavesczy.track.replace.rule.ReplaceRuleConfig.ReplaceRuleParameter
import github.leavesczy.track.replace.rule.ToastTrackPluginParameter
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
                ViewClickTrackPluginParameter::class.java
            )
            extensions.create(
                composeClickTrack,
                ComposeClickTrackPluginParameter::class.java
            )
            extensions.create(
                toastTrack,
                ToastTrackPluginParameter::class.java
            )
            extensions.create(
                replaceClassTrack,
                ReplaceClassTrackPluginParameter::class.java
            )
            extensions.create(
                optimizedThreadTrack,
                OptimizedThreadTrackPluginParameter::class.java
            )
            extensions.create(
                replaceFieldTrack,
                ReplaceFieldTrackPluginParameter::class.java
            )
            extensions.create(
                replaceMethodTrack,
                ReplaceMethodTrackPluginParameter::class.java
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
            handleReplaceFieldTrack(project = project, variant = variant)
            handleReplaceMethodTrack(project = project, variant = variant)
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
        }
    }

    private fun handleViewClickTrack(project: Project, variant: Variant) {
        val pluginParameter = project.extensions.findByType(ViewClickTrackPluginParameter::class.java)
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
            variant.instrumentation.apply {
                transformClassesWith(
                    classVisitorFactoryImplClass = ViewClickAsmClassVisitorFactory::class.java,
                    scope = InstrumentationScope.ALL
                ) { params ->
                    params.trackConfig.set(
                        ViewClickConfig(
                            include = include,
                            exclude = exclude,
                            extensionName = viewClickTrack,
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
        val pluginParameter = project.extensions.findByType(ComposeClickTrackPluginParameter::class.java)
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
                            clickWrapperClass = clickWrapperClass,
                            skipOnClickLabel = skipOnClickLabel
                        )
                    )
                }
            }
        }
    }

    private fun handleReplaceClassTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(ReplaceClassTrackPluginParameter::class.java)
                ?: return
        val originClass = pluginParameter.originClass
        val targetClass = pluginParameter.targetClass
        val include = pluginParameter.include
        val exclude = pluginParameter.exclude
        val hasAnyConfig = originClass.isNotBlank() ||
                targetClass.isNotBlank() ||
                include.isNotEmpty() ||
                exclude.isNotEmpty()
        val isComplete = originClass.isNotBlank() && targetClass.isNotBlank()
        guardTrackConfig(
            extensionName = replaceClassTrack,
            hasAnyConfig = hasAnyConfig,
            isComplete = isComplete,
            missingDetail = "缺少必填参数 originClass / targetClass"
        ) {
            variant.instrumentation.apply {
                transformClassesWith(
                    classVisitorFactoryImplClass = ReplaceClassAsmClassVisitorFactory::class.java,
                    scope = InstrumentationScope.ALL
                ) { params ->
                    params.trackConfig.set(
                        ReplaceClassConfig(
                            include = include,
                            exclude = exclude,
                            extensionName = replaceClassTrack,
                            originClass = originClass,
                            targetClass = targetClass
                        )
                    )
                }
            }
        }
    }

    private fun handleToastTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(ToastTrackPluginParameter::class.java)
                ?: return
        val proxyClass = pluginParameter.proxyClass
        val include = pluginParameter.include
        val exclude = pluginParameter.exclude
        val hasAnyConfig = proxyClass.isNotBlank() ||
                include.isNotEmpty() ||
                exclude.isNotEmpty()
        val isComplete = proxyClass.isNotBlank()
        guardTrackConfig(
            extensionName = toastTrack,
            hasAnyConfig = hasAnyConfig,
            isComplete = isComplete,
            missingDetail = "缺少必填参数 proxyClass"
        ) {
            handleReplaceRuleTrack(
                variant = variant,
                extensionName = toastTrack,
                include = include,
                exclude = exclude,
                replacements = setOf(
                    element = ReplaceRuleParameter(
                        ownerClass = "android/widget/Toast",
                        memberName = "show",
                        descriptor = "()V",
                        proxyClass = proxyClass
                    )
                )
            )
        }
    }

    private fun handleOptimizedThreadTrack(
        project: Project,
        variant: Variant
    ) {
        val pluginParameter =
            project.extensions.findByType(OptimizedThreadTrackPluginParameter::class.java)
                ?: return
        val proxyClass = pluginParameter.proxyClass
        val methodNames = pluginParameter.methodNames
        val include = pluginParameter.include
        val exclude = pluginParameter.exclude
        val hasAnyConfig = proxyClass.isNotBlank() ||
                methodNames.isNotEmpty() ||
                include.isNotEmpty() ||
                exclude.isNotEmpty()
        val isComplete = proxyClass.isNotBlank() &&
                methodNames.isNotEmpty() &&
                methodNames.all { it.isNotBlank() }
        guardTrackConfig(
            extensionName = optimizedThreadTrack,
            hasAnyConfig = hasAnyConfig,
            isComplete = isComplete,
            missingDetail = "缺少必填参数 proxyClass / methodNames"
        ) {
            handleReplaceRuleTrack(
                variant = variant,
                extensionName = optimizedThreadTrack,
                include = include,
                exclude = exclude,
                replacements = methodNames.map {
                    ReplaceRuleParameter(
                        ownerClass = "java/util/concurrent/Executors",
                        memberName = it,
                        descriptor = ReplaceMethodRule.MATCH_ALL_METHOD_DESCRIPTORS,
                        proxyClass = proxyClass
                    )
                }.toSet()
            )
        }
    }

    private fun handleReplaceFieldTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(ReplaceFieldTrackPluginParameter::class.java)
                ?: return
        val replacements = pluginParameter.replacements
        val include = pluginParameter.include
        val exclude = pluginParameter.exclude
        val hasAnyConfig = replacements.isNotEmpty() ||
                include.isNotEmpty() ||
                exclude.isNotEmpty()
        val isComplete = replacements.isNotEmpty()
        guardTrackConfig(
            extensionName = replaceFieldTrack,
            hasAnyConfig = hasAnyConfig,
            isComplete = isComplete,
            missingDetail = "缺少必填参数 replacements"
        ) {
            val validatedReplacements = replacements.map { rule ->
                validateReplaceFieldRule(
                    extensionName = replaceFieldTrack,
                    rule = rule
                )
            }.toSet()
            handleReplaceRuleTrack(
                variant = variant,
                extensionName = replaceFieldTrack,
                include = include,
                exclude = exclude,
                replacements = validatedReplacements
            )
        }
    }

    private fun handleReplaceMethodTrack(project: Project, variant: Variant) {
        val pluginParameter =
            project.extensions.findByType(ReplaceMethodTrackPluginParameter::class.java)
                ?: return
        val replacements = pluginParameter.replacements
        val include = pluginParameter.include
        val exclude = pluginParameter.exclude
        val hasAnyConfig = replacements.isNotEmpty() ||
                include.isNotEmpty() ||
                exclude.isNotEmpty()
        val isComplete = replacements.isNotEmpty()
        guardTrackConfig(
            extensionName = replaceMethodTrack,
            hasAnyConfig = hasAnyConfig,
            isComplete = isComplete,
            missingDetail = "缺少必填参数 replacements"
        ) {
            val validatedReplacements = replacements.map { rule ->
                validateReplaceMethodRule(
                    extensionName = replaceMethodTrack,
                    rule = rule
                )
            }.toSet()
            handleReplaceRuleTrack(
                variant = variant,
                extensionName = replaceMethodTrack,
                include = include,
                exclude = exclude,
                replacements = validatedReplacements
            )
        }
    }

    private fun validateReplaceFieldRule(
        extensionName: String,
        rule: ReplaceFieldRule
    ): ReplaceRuleParameter {
        val ownerClass = rule.ownerClass
        val fieldName = rule.fieldName
        val typeDescriptor = rule.typeDescriptor
        val proxyClass = rule.proxyClass
        if (ownerClass.isBlank() || fieldName.isBlank() || typeDescriptor.isBlank() || proxyClass.isBlank()) {
            throw trackConfigError(
                extensionName = extensionName,
                detail = "replacements 中存在不完整项，ownerClass / fieldName / typeDescriptor / proxyClass 均不能为空；匹配全部类型请使用 typeDescriptor = \"${ReplaceFieldRule.MATCH_ALL_TYPE_DESCRIPTORS}\""
            )
        }
        return ReplaceRuleParameter(
            ownerClass = replacePeriodWithSlash(className = ownerClass),
            memberName = fieldName,
            descriptor = typeDescriptor,
            proxyClass = proxyClass
        )
    }

    private fun validateReplaceMethodRule(
        extensionName: String,
        rule: ReplaceMethodRule
    ): ReplaceRuleParameter {
        val ownerClass = rule.ownerClass
        val methodName = rule.methodName
        val methodDescriptor = rule.methodDescriptor
        val proxyClass = rule.proxyClass
        if (ownerClass.isBlank() || methodName.isBlank() || methodDescriptor.isBlank() || proxyClass.isBlank()) {
            throw trackConfigError(
                extensionName = extensionName,
                detail = "replacements 中存在不完整项，ownerClass / methodName / methodDescriptor / proxyClass 均不能为空；匹配全部重载请使用 methodDescriptor = \"${ReplaceMethodRule.MATCH_ALL_METHOD_DESCRIPTORS}\""
            )
        }
        return ReplaceRuleParameter(
            ownerClass = replacePeriodWithSlash(className = ownerClass),
            memberName = methodName,
            descriptor = methodDescriptor,
            proxyClass = proxyClass
        )
    }

    private fun handleReplaceRuleTrack(
        variant: Variant,
        extensionName: String,
        include: Set<String>,
        exclude: Set<String>,
        replacements: Set<ReplaceRuleParameter>
    ) {
        variant.instrumentation.apply {
            transformClassesWith(
                classVisitorFactoryImplClass = ReplaceRuleAsmClassVisitorFactory::class.java,
                scope = InstrumentationScope.ALL
            ) { params ->
                params.trackConfig.set(
                    ReplaceRuleConfig(
                        include = include,
                        exclude = exclude,
                        extensionName = extensionName,
                        replacements = replacements
                    )
                )
            }
        }
    }

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
