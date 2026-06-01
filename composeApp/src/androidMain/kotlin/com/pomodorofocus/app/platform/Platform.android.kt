package com.pomodorofocus.app.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

actual fun platformName(): String = "Android ${Build.VERSION.SDK_INT}"

actual fun copyToClipboard(text: String) {
    val ctx = getAppContext()
    val clip = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clip.setPrimaryClip(ClipData.newPlainText("label", text))
}

actual class HapticFeedback {
    actual fun light() { vibrate(10) }
    actual fun medium() { vibrate(20) }
    actual fun heavy() { vibrate(40) }
    private fun vibrate(ms: Long) {
        val ctx = getAppContext()
        val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else { @Suppress("DEPRECATION") ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        else @Suppress("DEPRECATION") v.vibrate(ms)
    }
}

actual fun scheduleNotification(title: String, body: String, delaySeconds: Int) {
    // Android notification scheduling
}
