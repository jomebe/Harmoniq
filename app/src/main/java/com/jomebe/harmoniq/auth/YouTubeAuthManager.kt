package com.jomebe.harmoniq.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.jomebe.harmoniq.data.remote.AuthTokenStore
import com.jomebe.harmoniq.domain.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

sealed interface AuthState {
    data object SignedOut : AuthState
    data object Loading : AuthState
    data class SignedIn(val profile: UserProfile) : AuthState
    data class RecoveryRequired(val intent: Intent) : AuthState
    data class Error(val message: String) : AuthState
}

class YouTubeAuthManager(
    private val context: Context,
    private val tokenStore: AuthTokenStore
) {
    companion object {
        const val YOUTUBE_READONLY = "https://www.googleapis.com/auth/youtube.readonly"
    }

    private val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(YOUTUBE_READONLY))
        .build()

    private val client = GoogleSignIn.getClient(context, options)
    private val _state = MutableStateFlow<AuthState>(AuthState.SignedOut)
    val state: StateFlow<AuthState> = _state

    fun signInIntent(): Intent = client.signInIntent

    suspend fun restoreSession() {
        GoogleSignIn.getLastSignedInAccount(context)?.let { acquireToken(it) }
    }

    suspend fun completeSignIn(data: Intent?): Result<Unit> {
        _state.value = AuthState.Loading
        return runCatching {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).await()
            acquireToken(account)
        }.onFailure { _state.value = AuthState.Error(it.message ?: "Google 로그인에 실패했습니다.") }
    }

    suspend fun retryToken() {
        GoogleSignIn.getLastSignedInAccount(context)?.let { acquireToken(it) }
    }

    suspend fun signOut() {
        tokenStore.token.value?.let { token ->
            withContext(Dispatchers.IO) { runCatching { GoogleAuthUtil.clearToken(context, token) } }
        }
        client.signOut().await()
        tokenStore.update(null)
        _state.value = AuthState.SignedOut
    }

    private suspend fun acquireToken(account: GoogleSignInAccount) {
        _state.value = AuthState.Loading
        try {
            val androidAccount = requireNotNull(account.account)
            val token = withContext(Dispatchers.IO) {
                GoogleAuthUtil.getToken(context, androidAccount, "oauth2:$YOUTUBE_READONLY")
            }
            tokenStore.update(token)
            _state.value = AuthState.SignedIn(
                UserProfile(
                    displayName = account.displayName ?: "YouTube 사용자",
                    email = account.email.orEmpty(),
                    photoUrl = account.photoUrl?.toString()
                )
            )
        } catch (error: UserRecoverableAuthException) {
            _state.value = error.intent?.let(AuthState::RecoveryRequired)
                ?: AuthState.Error("YouTube 권한 승인 화면을 열지 못했습니다.")
        } catch (error: IOException) {
            _state.value = AuthState.Error("네트워크 연결을 확인해주세요.")
        } catch (error: GoogleAuthException) {
            _state.value = AuthState.Error(error.message ?: "YouTube 권한을 가져오지 못했습니다.")
        }
    }
}
