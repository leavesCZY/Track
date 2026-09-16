package github.leavesczy.track.replace.inheritance

class AppGreeter : OriginGreeter() {
    override fun greet(name: String): String {
        return super.greet(name) + " (via AppGreeter)"
    }
}
