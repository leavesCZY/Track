# Trace

Trace 是一个适用于 Android 的字节码插桩库

目前一共包含三个功能点：

- 应用双击防抖。包括 Android 原生的 View 体系以及目前流行的 Jetpack Compose
- 替换 Class 的继承关系。可用于非侵入式地实现监控大图加载的功能
- 修复 Toast 在 Android 7.1 上的系统 bug。用于解决在 Android 7.1 系统上 Toast 由于 WindowToken 失效从而导致应用崩溃的问题

接入指南：[Wiki](https://github.com/leavesCZY/Trace/wiki)