package org.jay.appstarter

import android.os.Looper
import android.os.MessageQueue.IdleHandler
import java.util.LinkedList
import java.util.Queue

/**
 * 延迟初始化分发器
 */
class DelayInitDispatcher {
    private val mDelayTasks: Queue<Task> = LinkedList<Task>()

    private val mIdleHandler = IdleHandler {
        if (!mDelayTasks.isEmpty()) {
            val task = mDelayTasks.poll()
            if (task != null) {
                DispatchRunnable(task).run()
            }
        }
        !mDelayTasks.isEmpty()
    }

    fun addTask(task: Task): DelayInitDispatcher {
        mDelayTasks.add(task)
        return this
    }

    fun start() {
        Looper.myQueue().addIdleHandler(mIdleHandler)
    }
}
