package com.geekzforwarder.notifikator

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return

        // Hanya proses jika diizinkan oleh whitelist / mode all
        if (!AllowedAppsStore.isAllowed(this, pkg)) {
            Log.d("NotifService", "Ignored notification from $pkg")
            return
        }

        Log.d("NotifService", "Processing notification from $pkg")
        // TODO: panggil logic pembacaan / handling notifikasi di sini
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // optional
    }
}
