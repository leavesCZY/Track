package github.leavesczy.track.replace.inheritance

class ExcludedGreeter : OriginGreeter() {
    override fun greet(name: String): String {
        return super.greet(name) + " (via ExcludedGreeter)"
    }
}
