package com.dahee.blockbyblock.data.remote

import com.dahee.blockbyblock.getPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TokenStorage {
    private const val KEY_ACCESS_TOKEN = "bbb_access_token"
    private const val KEY_REFRESH_TOKEN = "bbb_refresh_token"
    private const val KEY_USER_ID = "bbb_user_id"
    private const val KEY_USER_LANG = "bbb_user_lang"

    private val platform by lazy { getPlatform() }

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _refreshToken = MutableStateFlow<String?>(null)
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    private val _userLang = MutableStateFlow<String?>(null)
    val userLang: StateFlow<String?> = _userLang.asStateFlow()

    init {
        try {
            val savedAccess = platform.getPersistentString(KEY_ACCESS_TOKEN)
            val savedRefresh = platform.getPersistentString(KEY_REFRESH_TOKEN)
            val savedId = platform.getPersistentString(KEY_USER_ID)
            val savedLang = platform.getPersistentString(KEY_USER_LANG)
            _accessToken.value = savedAccess
            _refreshToken.value = savedRefresh
            _userId.value = savedId
            _userLang.value = savedLang
        } catch (_: Throwable) {}
    }

    fun getAccessToken(): String? = _accessToken.value
    fun getRefreshToken(): String? = _refreshToken.value
    fun getUserId(): String? = _userId.value
    fun getUserLang(): String? = _userLang.value

    fun setTokens(access: String, refresh: String) {
        _accessToken.value = access
        _refreshToken.value = refresh
        try {
            platform.setPersistentString(KEY_ACCESS_TOKEN, access)
            platform.setPersistentString(KEY_REFRESH_TOKEN, refresh)
        } catch (_: Throwable) {}
    }

    fun setUserInfo(id: String?, lang: String?) {
        _userId.value = id
        _userLang.value = lang
        try {
            platform.setPersistentString(KEY_USER_ID, id)
            platform.setPersistentString(KEY_USER_LANG, lang)
        } catch (_: Throwable) {}
    }

    fun clearTokens() {
        _accessToken.value = null
        _refreshToken.value = null
        _userId.value = null
        _userLang.value = null
        try {
            platform.setPersistentString(KEY_ACCESS_TOKEN, null)
            platform.setPersistentString(KEY_REFRESH_TOKEN, null)
            platform.setPersistentString(KEY_USER_ID, null)
            platform.setPersistentString(KEY_USER_LANG, null)
        } catch (_: Throwable) {}
    }

    val isAuthenticated: Boolean
        get() = !_accessToken.value.isNullOrBlank()
}
