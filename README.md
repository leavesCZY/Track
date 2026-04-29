# Track

Track 是一个适用于 Android 的字节码插桩库

包含以下几个功能点：

- View Click 双击防抖
- Jetpack Compose Click 双击防抖
- 收拢应用内所有的 Toast.show 方法，可用于解决在 Android 7.1 系统上 Toast 由于 WindowToken
  失效从而导致应用崩溃的问题
- 收拢应用内所有的 Executors 线程池方法，可用于实现线程整治，统一应用内所有的线程池实例
- 修改指定类的继承关系，将指定父类替换为另一个类
- 修改指定字段的调用链，将指定字段替换为另一个字段
- 修改指定方法的调用链，将指定方法替换为另一个方法

相关联的文章：

- [Android ASM 字节码插桩：实现双击防抖](https://juejin.cn/post/7042328862872567838)
- [Android ASM 字节码插桩：进行线程整治](https://juejin.cn/post/7044339202997092383)
- [Android ASM 字节码插桩：助力隐私合规](https://juejin.cn/post/7046207125785149448)
- [Android ASM 字节码插桩：监控大图加载](https://juejin.cn/post/7074970389188706318)
- [Android ASM 字节码插桩：从 Lambda 表达式讲起](https://juejin.cn/post/7151798531672506398)
- [Android ASM 字节码插桩：Jetpack Compose 实现双击防抖](https://juejin.cn/post/7158061389503250445)
- [Android ASM 字节码插桩：替换字节码指令](https://juejin.cn/post/7422658245933875252)

接入指南：[Wiki](https://github.com/leavesCZY/Track/wiki)