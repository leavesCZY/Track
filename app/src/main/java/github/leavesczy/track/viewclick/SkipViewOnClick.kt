package github.leavesczy.track.viewclick

/** ASM 从 class 文件读取注解，必须使用 BINARY 保留策略。 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class SkipViewOnClick
