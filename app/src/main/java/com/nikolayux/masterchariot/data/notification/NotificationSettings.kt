package com.nikolayux.masterchariot.data.notification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class NotificationSettings @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _maintenanceNotificationsEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_MAINTENANCE_NOTIFICATIONS_ENABLED, true)
    )
    val maintenanceNotificationsEnabled = _maintenanceNotificationsEnabled.asStateFlow()

    fun setMaintenanceNotificationsEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_MAINTENANCE_NOTIFICATIONS_ENABLED, enabled)
            .apply()
        _maintenanceNotificationsEnabled.value = enabled
    }

    fun areMaintenanceNotificationsEnabled(): Boolean {
        return preferences.getBoolean(KEY_MAINTENANCE_NOTIFICATIONS_ENABLED, true)
    }

    private companion object {
        const val PREFERENCES_NAME = "notification_settings"
        const val KEY_MAINTENANCE_NOTIFICATIONS_ENABLED = "maintenance_notifications_enabled"
    }
}
