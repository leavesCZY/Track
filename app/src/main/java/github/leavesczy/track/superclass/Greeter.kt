package github.leavesczy.track.superclass

open class OriginGreeter {
    open fun greet(name: String): String {
        return "OriginGreeter: Hello, $name"
    }
}

class AppGreeter : OriginGreeter() {
    override fun greet(name: String): String {
        return super.greet(name) + " (via AppGreeter)"
    }
}

open class ProxyGreeter : OriginGreeter() {
    override fun greet(name: String): String {
        return "ProxyGreeter: Hello, $name"
    }
}

class ExcludedGreeter : OriginGreeter() {
    override fun greet(name: String): String {
        return super.greet(name) + " (via ExcludedGreeter)"
    }
}

open class OriginLogger {
    open fun log(message: String): String {
        return "OriginLogger: $message"
    }
}

class AppLogger : OriginLogger() {
    override fun log(message: String): String {
        return super.log(message) + " (via AppLogger)"
    }
}

/** 不在 OriginLogger 规则的 include 内，父类应保持 OriginLogger。 */
class OutsideLogger : OriginLogger() {
    override fun log(message: String): String {
        return super.log(message) + " (via OutsideLogger)"
    }
}

open class ProxyLogger : OriginLogger() {
    override fun log(message: String): String {
        return "ProxyLogger: $message"
    }
}
