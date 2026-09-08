package com.dahee.blockbyblock.core.notification

import com.dahee.blockbyblock.core.utils.getCurrentEpochMillis
import com.dahee.blockbyblock.core.utils.getCurrentTimeZone
import com.dahee.blockbyblock.data.remote.service.DeviceApiService
import com.dahee.blockbyblock.data.remote.service.UserApiService
import com.dahee.blockbyblock.getPlatform

object PushNotificationManager {
    private const val PREF_FCM_TOKEN_KEY = "fcm_device_token"

    fun getDeviceFcmToken(): String? {
        return getPlatform().getPersistentString(PREF_FCM_TOKEN_KEY)
    }

    fun setDeviceFcmToken(token: String?) {
        getPlatform().setPersistentString(PREF_FCM_TOKEN_KEY, token)
    }

    fun getOrCreateDeviceFcmToken(): String {
        val existing = getDeviceFcmToken()
        if (!existing.isNullOrBlank()) {
            return existing
        }
        val type = getPlatform().deviceType ?: "MOBILE"
        val newToken = "fcm_${type.lowercase()}_${getCurrentEpochMillis()}"
        setDeviceFcmToken(newToken)
        return newToken
    }

    /**
     * Handles startup and login device registration / timezone synchronization
     * according to platform-specific rules:
     * - Mobile (Android / iOS): Register FCM token & device timezone via POST /api/v1/devices
     * - Web: Do NOT call device registration; instead sync timezone via PATCH /api/v1/users/me/timezone
     */
    suspend fun syncDeviceOrTimezone(
        deviceApiService: DeviceApiService,
        userApiService: UserApiService,
        explicitFcmToken: String? = null
    ): Result<Unit> = runCatching {
        val timezone = getCurrentTimeZone()
        val platform = getPlatform()

        if (platform.isWeb) {
            // Web rule: Never call POST /api/v1/devices. Only sync timezone if changed.
            userApiService.updateTimezone(timezone)
        } else {
            // Mobile rule (Android / iOS): Register FCM token with device timezone
            val token = explicitFcmToken?.ifBlank { null }
                ?: getOrCreateDeviceFcmToken()
            val deviceType = platform.deviceType ?: "ANDROID"

            val result = deviceApiService.registerDevice(
                fcmToken = token,
                deviceType = deviceType,
                timezone = timezone
            )
            result.onSuccess {
                setDeviceFcmToken(token)
            }
        }
    }

    /**
     * Handles logout token removal:
     * - Mobile (Android / iOS): Unregister FCM token from backend via DELETE /api/v1/devices?fcmToken=...
     * - Web: No-op
     */
    suspend fun onLogout(deviceApiService: DeviceApiService): Result<Unit> = runCatching {
        val platform = getPlatform()
        if (!platform.isWeb) {
            val token = getDeviceFcmToken()
            if (!token.isNullOrBlank()) {
                deviceApiService.unregisterDevice(token)
                setDeviceFcmToken(null)
            }
        }
    }
}
