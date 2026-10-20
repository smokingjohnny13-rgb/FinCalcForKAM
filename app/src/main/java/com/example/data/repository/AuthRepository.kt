package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUserFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { currentAuth ->
            trySend(currentAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
        return try {
            val webClientId = try {
                context.getString(R.string.default_web_client_id)
            } catch (e: Exception) {
                // Fallback client ID from google-services.json if resource not generated
                "473324426230-90cnc6tq0mc99chofqmu688i355p112e.apps.googleusercontent.com"
            }

            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: error("Firebase sign-in completed without user")
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Unexpected credential type: ${credential.javaClass.name}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("BigDealAuth", "Sign-in was cancelled by user: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("BigDealAuth", "Sign-in failure: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
