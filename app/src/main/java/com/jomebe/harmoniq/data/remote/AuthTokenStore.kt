package com.jomebe.harmoniq.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthTokenStore {
    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    fun update(value: String?) {
        _token.value = value
    }
}
