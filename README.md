# Track

A bytecode instrumentation library for Android

Currently, it includes four features:

- View Click debounce
- Jetpack Compose Click debounce
- Replace the inheritance of a Class. Application scenarios include: non-intrusive implementation of
  monitoring large image loading
- Fix the system bug of Toast on Android 7.1. This is used to solve the problem where Toast causes
  the application to crash due to WindowToken invalidation on Android 7.1

Integration guide: [Wiki](https://github.com/leavesCZY/Track/wiki)

一个适用于 Android 的字节码插桩库

目前一共包含四个功能点：

- View Click 双击防抖
- Jetpack Compose Click 双击防抖
- 替换 Class 的继承关系。应用场景包括：非侵入式地实现监控大图加载的功能
- 修复 Toast 在 Android 7.1 上的系统 bug。用于解决在 Android 7.1 系统上 Toast 由于 WindowToken
  失效从而导致应用崩溃的问题

接入指南：[Wiki](https://github.com/leavesCZY/Track/wiki)