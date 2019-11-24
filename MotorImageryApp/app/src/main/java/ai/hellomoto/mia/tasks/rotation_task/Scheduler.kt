package ai.hellomoto.mia.tasks.rotation_task

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class Scheduler(cycle: Long, runnable: () -> Unit) {
    private var mService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var mFuture: ScheduledFuture<*>

    init {
        this.mFuture = this.mService.scheduleAtFixedRate(runnable, 0, cycle, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        this.mFuture.cancel(false)
        this.mService.shutdown()
        this.mService.awaitTermination(1, TimeUnit.SECONDS)
    }
}