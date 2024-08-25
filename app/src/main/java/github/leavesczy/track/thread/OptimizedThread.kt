package github.leavesczy.track.thread

import java.util.concurrent.atomic.AtomicInteger

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal class OptimizedThread(runnable: Runnable?, name: String?, className: String) :
    Thread(runnable, generateThreadName(name, className)) {

    companion object {

        private val threadId = AtomicInteger(0)

        private fun generateThreadName(originalThreadName: String?, className: String): String {
            val threadName = if (originalThreadName.isNullOrBlank()) {
                "emptyThreadName"
            } else {
                originalThreadName
            }
            return className + "-" + threadId.getAndIncrement() + "-" + threadName
        }

    }

    constructor(runnable: Runnable, className: String) : this(runnable, null, className)

    constructor(name: String, className: String) : this(null, name, className)

}