package com.example.marshal.presentation

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // 👇 Paste your Web Client ID right here
    private val WEB_CLIENT_ID = "896509150965-kmbbbtbco0po5vlskr8vdl2a03o9vac9.apps.googleusercontent.com"

    // Expose the auth state to the UI (true if logged in, false if not)
    private val _isUserAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated

    fun signInWithGoogle(context: Context, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)

                // 1. Build the Google Sign-In request using your Web Client ID
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // 2. Launch the bottom sheet UI and wait for the user to select an account
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                // 3. Extract the Google token and sign into Firebase
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                    auth.signInWithCredential(authCredential).await()
                    _isUserAuthenticated.value = true
                    onResult(true, null)
                } else {
                    onResult(false, "Unrecognized credential type")
                }
            } catch (e: Exception) {
                // Catch cancellations or network errors
                onResult(false, e.localizedMessage)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _isUserAuthenticated.value = false
    }
}