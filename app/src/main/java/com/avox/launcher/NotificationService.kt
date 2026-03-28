package com.avox.launcher

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        updateHolder()
        broadcastUpdate()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        updateHolder()
        broadcastUpdate()
    }

    override fun onListenerConnected() {
        NotificationHolder.service = this
        updateHolder()
        broadcastUpdate()
    }

    override fun onListenerDisconnected() {
        NotificationHolder.service = null
    }

    fun cancelNotificationsForPackage(pkg: String) {
        try {
            activeNotifications?.filter { it.packageName == pkg }?.forEach {
                cancelNotification(it.key)
            }
        } catch (_: Exception) {}
    }

    private fun updateHolder() {
        NotificationHolder.activeNotifications = try {
            activeNotifications ?: emptyArray()
        } catch (_: Exception) {
            emptyArray()
        }
    }

    private fun broadcastUpdate() {
        val intent = Intent(ACTION_NOTIFICATION_UPDATE)
        intent.setPackage(packageName)
        sendBroadcast(intent)
    }

    companion object {
        const val ACTION_NOTIFICATION_UPDATE = "com.avox.launcher.NOTIFICATION_UPDATE"
    }
}
