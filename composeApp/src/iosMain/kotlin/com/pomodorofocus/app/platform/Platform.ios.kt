package com.pomodorofocus.app.platform

import platform.UIKit.UIDevice
import platform.UIKit.UIPasteboard
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNNotificationRequest

actual fun platformName(): String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
actual fun copyToClipboard(text: String) { UIPasteboard.generalPasteboard.string = text }
actual class HapticFeedback {
    actual fun light() {}
    actual fun medium() {}
    actual fun heavy() {}
}

actual fun scheduleNotification(title: String, body: String, delaySeconds: Int) {
    val content = UNMutableNotificationContent().apply {
        setTitle(title)
        setBody(body)
        setSound(platform.UserNotifications.UNNotificationSound.defaultSound())
    }
    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(delaySeconds.toDouble(), repeats = false)
    val request = UNNotificationRequest.requestWithIdentifier("timer_$delaySeconds", content, trigger)
    UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { _ -> }
}
