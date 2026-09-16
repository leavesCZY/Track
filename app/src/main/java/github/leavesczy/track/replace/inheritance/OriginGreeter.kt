package github.leavesczy.track.replace.inheritance

open class OriginGreeter {
    open fun greet(name: String): String {
        return "OriginGreeter: Hello, $name"
    }
}
