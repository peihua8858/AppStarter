package org.jay.appstarter.utils

import android.util.Log
import org.jay.appstarter.BuildConfig

// 日志打印器
object DispatcherLog {

    @JvmStatic
    var isDebug = BuildConfig.DEBUG

    @JvmStatic
    fun i(msg: String?) {
        if (!isDebug) {
            return
        }
        Log.i("TaskDispatcher", msg!!)
    }

    @JvmStatic
    fun e(msg: String?, e: Throwable?) {
        Log.e("TaskDispatcher", msg ?: "", e)
    }

}