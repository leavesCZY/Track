package github.leavesczy.track.replace.inheritance

open class ProxyGreeter : OriginGreeter() {
    override fun greet(name: String): String {
        return "ProxyGreeter: Hello, $name"
    }
}
