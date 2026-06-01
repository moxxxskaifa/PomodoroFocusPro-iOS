package com.pomodorofocus.app.platform

import android.app.Application

object AppContextHolder {
    lateinit var instance: Application; private set
    fun init(app: Application) { instance = app }
}
fun getAppContext(): Application = AppContextHolder.instance
class AndroidApp : Application() {
    override fun onCreate() { super.onCreate(); AppContextHolder.init(this) }
}
