package com.dahee.blockbyblock.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

class JsGoogleAuthProvider : GoogleAuthProvider {

    private fun ensureJsHelper() {
        js("""
        if (!window.__startGoogleSignIn) {
            window.__googleSignInState = { status: 'idle', token: '', error: '' };
            window.__ensureGoogleSdk = function(callback) {
                if (window.google && window.google.accounts && window.google.accounts.id) {
                    callback();
                    return;
                }
                var script = document.getElementById('google-gsi-client-script');
                if (!script) {
                    script = document.createElement('script');
                    script.id = 'google-gsi-client-script';
                    script.src = 'https://accounts.google.com/gsi/client';
                    script.async = true;
                    script.defer = true;
                    script.onload = callback;
                    script.onerror = function() {
                        window.__googleSignInState = { status: 'error', token: '', error: 'Google SDK script load failed' };
                    };
                    document.head.appendChild(script);
                } else {
                    var count = 0;
                    var interval = setInterval(function() {
                        count++;
                        if (window.google && window.google.accounts && window.google.accounts.id) {
                            clearInterval(interval);
                            callback();
                        } else if (count > 50) {
                            clearInterval(interval);
                            window.__googleSignInState = { status: 'error', token: '', error: 'Google SDK load timeout' };
                        }
                    }, 100);
                }
            };
            window.__ensureGoogleModal = function() {
                var modal = document.getElementById('google-signin-modal');
                if (!modal) {
                    modal = document.createElement('div');
                    modal.id = 'google-signin-modal';
                    modal.style.cssText = 'display:none;position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:100000;justify-content:center;align-items:center;backdrop-filter:blur(3px);user-select:auto;';
                    modal.innerHTML = 
                        '<div style="background:#fff;border-radius:20px;padding:28px 24px 20px;max-width:320px;width:88%;box-shadow:0 12px 36px rgba(0,0,0,0.25);text-align:center;font-family:-apple-system,BlinkMacSystemFont,\'Segoe UI\',Roboto,sans-serif;user-select:auto;">' +
                            '<div style="width:44px;height:44px;border-radius:50%;background:#EFF6FF;display:flex;align-items:center;justify-content:center;margin:0 auto 12px;">' +
                                '<svg width="24" height="24" viewBox="0 0 24 24"><path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/><path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/><path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05"/><path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" fill="#EA4335"/></svg>' +
                            '</div>' +
                            '<h3 style="margin:0 0 6px 0;font-size:18px;font-weight:700;color:#111827;">Google 로그인</h3>' +
                            '<p style="margin:0 0 20px 0;font-size:13px;color:#6B7280;line-height:1.4;">계정을 선택하여 로그인을 진행해 주세요.</p>' +
                            '<div id="google-signin-button-slot" style="display:flex;justify-content:center;min-height:44px;margin-bottom:16px;"></div>' +
                            '<button id="google-signin-close-btn" style="background:#F3F4F6;border:none;border-radius:10px;padding:10px;font-size:13px;font-weight:600;color:#4B5563;cursor:pointer;width:100%;">취소</button>' +
                        '</div>';
                    document.body.appendChild(modal);
                }
                return modal;
            };
            window.__startGoogleSignIn = function(clientId) {
                window.__googleSignInState = { status: 'pending', token: '', error: '' };
                window.__ensureGoogleSdk(function() {
                    var modal = window.__ensureGoogleModal();
                    var slot = document.getElementById('google-signin-button-slot');
                    var closeBtn = document.getElementById('google-signin-close-btn');
                    function closeModal(reason) {
                        if (modal) modal.style.display = 'none';
                        if (window.__googleSignInState.status === 'pending') {
                            window.__googleSignInState = { status: reason || 'cancelled', token: '', error: '' };
                        }
                    }
                    if (closeBtn) closeBtn.onclick = function() { closeModal('cancelled'); };
                    if (modal) {
                        modal.onclick = function(e) { if (e.target === modal) closeModal('cancelled'); };
                        modal.style.display = 'flex';
                    }
                    window.google.accounts.id.initialize({
                        client_id: clientId,
                        callback: function(response) {
                            if (modal) modal.style.display = 'none';
                            if (response && response.credential) {
                                window.__googleSignInState = { status: 'success', token: response.credential, error: '' };
                            } else {
                                window.__googleSignInState = { status: 'error', token: '', error: 'ID Token을 수신하지 못했습니다.' };
                            }
                        }
                    });
                    if (slot) {
                        slot.innerHTML = '';
                        window.google.accounts.id.renderButton(slot, {
                            type: 'standard',
                            shape: 'rectangular',
                            theme: 'outline',
                            text: 'signin_with',
                            size: 'large',
                            logo_alignment: 'left'
                        });
                    }
                    try { window.google.accounts.id.prompt(); } catch(e) {}
                });
            };
            window.__getGoogleSignInStatus = function() { return (window.__googleSignInState && window.__googleSignInState.status) || 'idle'; };
            window.__getGoogleSignInToken = function() { return (window.__googleSignInState && window.__googleSignInState.token) || ''; };
            window.__getGoogleSignInError = function() { return (window.__googleSignInState && window.__googleSignInState.error) || ''; };
        }
        """)
    }

    override suspend fun signIn(): GoogleAuthResult {
        ensureJsHelper()
        val clientId = GoogleAuthConfig.WEB_CLIENT_ID
        js("window.__startGoogleSignIn(clientId)")
        while (true) {
            delay(150)
            val status = js("window.__getGoogleSignInStatus()") as String
            when (status) {
                "success" -> {
                    val token = js("window.__getGoogleSignInToken()") as String
                    return if (token.isNotBlank()) {
                        GoogleAuthResult.Success(token)
                    } else {
                        GoogleAuthResult.Failure("Empty ID token received")
                    }
                }
                "cancelled" -> {
                    return GoogleAuthResult.Cancelled
                }
                "error" -> {
                    val err = js("window.__getGoogleSignInError()") as String
                    return GoogleAuthResult.Failure(if (err.isNotBlank()) err else "Google Sign-In failed")
                }
            }
        }
    }
}

@Composable
actual fun rememberGoogleAuthProvider(): GoogleAuthProvider {
    return remember { JsGoogleAuthProvider() }
}
