package github.leavesczy.track.thread

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal object OptimizedExecutors {

    private const val DEFAULT_THREAD_KEEP_ALIVE_TIME = 3000L

    @JvmStatic
    @JvmOverloads
    fun newSingleThreadExecutor(
        threadFactory: ThreadFactory? = null,
        className: String
    ): ExecutorService {
        return getOptimizedExecutorService(
            corePoolSize = 1,
            maximumPoolSize = 1,
            keepAliveTime = 0L,
            unit = TimeUnit.MILLISECONDS,
            workQueue = LinkedBlockingQueue(),
            threadFactory = threadFactory,
            className = className
        )
    }

    @JvmStatic
    @JvmOverloads
    fun newCachedThreadPool(
        threadFactory: ThreadFactory? = null,
        className: String
    ): ExecutorService {
        return getOptimizedExecutorService(
            corePoolSize = 0,
            maximumPoolSize = Integer.MAX_VALUE,
            keepAliveTime = 60L,
            unit = TimeUnit.SECONDS,
            workQueue = SynchronousQueue(),
            threadFactory = threadFactory,
            className = className
        )
    }

    @JvmStatic
    @JvmOverloads
    fun newFixedThreadPool(
        corePoolSize: Int,
        threadFactory: ThreadFactory? = null,
        className: String
    ): ExecutorService {
        return getOptimizedExecutorService(
            corePoolSize = corePoolSize,
            maximumPoolSize = corePoolSize,
            keepAliveTime = 0L,
            unit = TimeUnit.MILLISECONDS,
            workQueue = LinkedBlockingQueue(),
            threadFactory = threadFactory,
            className = className
        )
    }

    @JvmStatic
    @JvmOverloads
    fun newScheduledThreadPool(
        corePoolSize: Int,
        threadFactory: ThreadFactory? = null,
        className: String
    ): ScheduledExecutorService {
        return getOptimizedScheduledExecutorService(
            corePoolSize = corePoolSize,
            threadFactory = threadFactory,
            className = className
        )
    }

    @JvmStatic
    @JvmOverloads
    fun newSingleThreadScheduledExecutor(
        threadFactory: ThreadFactory? = null,
        className: String
    ): ScheduledExecutorService {
        return newScheduledThreadPool(
            corePoolSize = 1,
            threadFactory = threadFactory,
            className = className
        )
    }

    private fun getOptimizedExecutorService(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit,
        workQueue: BlockingQueue<Runnable>,
        threadFactory: ThreadFactory?,
        className: String
    ): ExecutorService {
        val executor = ThreadPoolExecutor(
            corePoolSize, maximumPoolSize,
            keepAliveTime, unit,
            workQueue,
            NamedThreadFactory(threadFactory, className)
        )
        executor.setKeepAliveTime(DEFAULT_THREAD_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS)
        executor.allowCoreThreadTimeOut(true)
        return executor
    }

    private fun getOptimizedScheduledExecutorService(
        corePoolSize: Int,
        threadFactory: ThreadFactory?,
        className: String
    ): ScheduledExecutorService {
        val executor = ScheduledThreadPoolExecutor(
            corePoolSize,
            NamedThreadFactory(threadFactory, className)
        )
        executor.setKeepAliveTime(DEFAULT_THREAD_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS)
        executor.allowCoreThreadTimeOut(true)
        return executor
    }

    private class NamedThreadFactory(
        private val threadFactory: ThreadFactory?,
        private val className: String
    ) : ThreadFactory {

        private val threadId = AtomicInteger(0)

        override fun newThread(runnable: Runnable): Thread {
            val thread = threadFactory?.newThread(runnable) ?: Thread(runnable)
            val threadName = buildString {
                append("[className : $className]")
                append(" - ")
                append("[threadId : ${threadId.getAndIncrement()}]")
                append(" - ")
                append("[threadName : ${thread.name}]")
            }
            thread.name = threadName
            if (thread.isDaemon) {
                thread.isDaemon = false
            }
            if (thread.priority != Thread.NORM_PRIORITY) {
                thread.priority = Thread.NORM_PRIORITY
            }
            return thread
        }

    }

}