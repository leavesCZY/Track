# Track

Track 是一个适用于 Android 的字节码插桩库

包含以下几个功能点：

- View Click 双击防抖
- Jetpack Compose Click 双击防抖
- 收拢应用内所有的 Toast.show 方法。可用于解决在 Android 7.1 系统上 Toast 由于 WindowToken 失效从而导致应用崩溃的问题
- 收拢应用内所有的 Executors 线程池方法。可用于实现线程整治，统一应用内所有的线程池实例
- 修改指定类的继承关系，将指定父类替换为另一个类
- 修改指定字段的调用链，将指定字段替换为另一个字段
- 修改指定方法的调用链，将指定方法替换为另一个方法

接入指南：[Wiki](https://github.com/leavesCZY/Track/wiki)