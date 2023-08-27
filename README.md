# Trace

一个适用于 Android 的字节码插桩库

目前一共包含四个功能点：

- View Click 双击防抖。适用于 View 体系
- Compose Click 双击防抖。适用于 Jetpack Compose
- 替换 Class 的继承关系。可用于非侵入式地实现监控大图加载的功能
- 修复 Toast 在 Android 7.1 上的系统 bug。用于解决在 Android 7.1 系统上 Toast 由于 WindowToken 失效从而导致应用崩溃的问题

接入指南：[Wiki](https://github.com/leavesCZY/Trace/wiki)