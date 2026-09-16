package com.imi.smartedge.sidebar.panel

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.concurrent.CopyOnWriteArraySet

class NotificationTrackingService : NotificationListenerService() {
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }
    companion object {
        @Volatile private var packages: List<String> = emptyList()
        private val listeners = CopyOnWriteArraySet<() -> Unit>()
        fun getActiveNotificationPackages(): List<String> = packages.toList()
        fun subscribe(listener: () -> Unit): () -> Unit {
            listeners.add(listener)
            return { listeners.remove(listener) }
        }
        private fun publish(next: List<String>) {
            if (packages == next) return
            packages = next
            listeners.forEach { it() }
        }
    }
    override fun onListenerConnected() { super.onListenerConnected(); updateActiveNotifications() }
    override fun onNotificationPosted(sbn: StatusBarNotification?) { updateActiveNotifications() }
    override fun onNotificationRemoved(sbn: StatusBarNotification?) { updateActiveNotifications() }
    override fun onListenerDisconnected() { publish(emptyList()); super.onListenerDisconnected() }
    override fun onDestroy() { publish(emptyList()); super.onDestroy() }
    private fun updateActiveNotifications() {
        val next = try { activeNotifications?.map { it.packageName }?.distinct() ?: emptyList() }
            catch (_: Exception) { emptyList() }
        publish(next)
    }
}
