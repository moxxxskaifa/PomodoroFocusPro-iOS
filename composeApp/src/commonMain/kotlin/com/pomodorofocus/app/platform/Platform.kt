package com.pomodorofocus.app.platform

expect fun platformName(): String
expect fun copyToClipboard(text: String)
expect class HapticFeedback() {
    fun light()
    fun medium()
    fun heavy()
}
expect fun scheduleNotification(title: String, body: String, delaySeconds: Int)
