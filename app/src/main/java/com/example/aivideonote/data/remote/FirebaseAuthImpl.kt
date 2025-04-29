package com.example.aivideonote.data.remote

import android.util.Log
import com.example.aivideonote.domain.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepoImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    private companion object {
        const val TAG = "FirebaseAuthImpl"
    }

    private val usersRef = firestore.collection("users")
    private val usernamesRef = firestore.collection("usernames")

    suspend fun addQuestionToUser(questions: String) {
        withContext(dispatcher) {
            if (questions.isBlank()) return@withContext
            firestore.runBatch { batch ->
                batch.update(
                    usersRef.document(auth.uid!!),
                    "mcqs",
                    FieldValue.arrayUnion(questions)
                )
            }.await()
        }
    }

    override suspend fun register(request: RegisterRequest): AuthResult<AuthUser> {
        return withContext(dispatcher) {
            try {
                // 1. Check username availability
                if (!checkUsernameAvailability(request.username).first()) {
                    return@withContext AuthResult.Failure(AuthError.UsernameTaken)
                }

                // 2. Create Firebase Auth user
                val authResult = auth.createUserWithEmailAndPassword(
                    request.email,
                    request.password
                ).await()

                val user = authResult.user
                    ?: return@withContext AuthResult.Failure(AuthError.InvalidCredentials)

                // 3. Create user document in Firestore
                val userData = hashMapOf(
                    "uid" to user.uid,
                    "username" to request.username.lowercase(),
                    "name" to request.name,
                    "email" to request.email,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "apikey" to request.key
                )

                // 4. Create username reservation document
                val usernameData = hashMapOf(
                    "userId" to user.uid,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                // 5. Atomic batch write
                firestore.runBatch { batch ->
                    batch.set(usersRef.document(user.uid), userData)
                    batch.set(usernamesRef.document(request.username.lowercase()), usernameData)
                }.await()

                AuthResult.Success(
                    AuthUser(
                        uid = user.uid,
                        username = request.username,
                        name = request.name,
                        email = user.email ?: request.email,
                        key = request.key
                    )
                )
            } catch (e: FirebaseAuthUserCollisionException) {
                AuthResult.Failure(AuthError.EmailInUse)
            } catch (e: FirebaseAuthWeakPasswordException) {
                AuthResult.Failure(AuthError.WeakPassword)
            } catch (e: FirebaseNetworkException) {
                AuthResult.Failure(AuthError.NetworkError)
            } catch (e: Exception) {
                Log.e(TAG, "Registration failed", e)
                AuthResult.Failure(AuthError.UnknownError(e.message ?: "Registration failed"))
            }
        }
    }

    override suspend fun login(request: LoginRequest): AuthResult<AuthUser> {
        return withContext(dispatcher) {
            try {
                val authResult = auth.signInWithEmailAndPassword(
                    request.email,
                    request.password
                ).await()

                val user = authResult.user
                    ?: return@withContext AuthResult.Failure(AuthError.InvalidCredentials)

                val snapshot = usersRef.document(user.uid).get().await()
                if (!snapshot.exists()) {
                    return@withContext AuthResult.Failure(AuthError.UserDataNotFound)
                }

                AuthResult.Success(
                    AuthUser(
                        uid = user.uid,
                        username = snapshot.getString("username") ?: "",
                        name = snapshot.getString("name") ?: "",
                        email = user.email ?: "",
                        key = snapshot.getString("apikey") ?: ""
                    )
                )
            } catch (e: FirebaseAuthInvalidUserException) {
                AuthResult.Failure(AuthError.InvalidCredentials)
            } catch (e: FirebaseNetworkException) {
                AuthResult.Failure(AuthError.NetworkError)
            } catch (e: Exception) {
                Log.e(TAG, "Login failed", e)
                AuthResult.Failure(AuthError.UnknownError(e.message ?: "Login failed"))
            }
        }
    }

    override suspend fun getCurrentUser(): AuthResult<AuthUser> {
        return withContext(dispatcher) {
            try {
                val user =
                    auth.currentUser ?: return@withContext AuthResult.Failure(AuthError.NotLoggedIn)

                val snapshot = usersRef.document(user.uid).get().await()
                if (!snapshot.exists()) {
                    return@withContext AuthResult.Failure(AuthError.UserDataNotFound)
                }

                AuthResult.Success(
                    AuthUser(
                        uid = user.uid,
                        username = snapshot.getString("username") ?: "",
                        name = snapshot.getString("name") ?: "",
                        email = user.email ?: "",
                        key = snapshot.getString("apikey") ?: ""
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get current user", e)
                AuthResult.Failure(
                    AuthError.UnknownError(
                        e.message ?: "Failed to get current user"
                    )
                )
            }
        }
    }

    override suspend fun logout(): AuthResult<Unit> {
        return try {
            auth.signOut()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed", e)
            AuthResult.Failure(AuthError.UnknownError("Logout failed"))
        }
    }

    override fun checkUsernameAvailability(username: String): Flow<Boolean> {
        require(username.isNotBlank()) { "Username cannot be empty" }

        return usernamesRef.document(username.trim().lowercase())
            .snapshots()
            .map {
                !it.exists()
            }
            .catch { e ->
                when (e) {
                    is FirebaseFirestoreException -> {
                        Log.w(TAG, "Username check failed", e)
                        emit(false)
                    }

                    else -> throw e
                }
            }
            .distinctUntilChanged()
            .flowOn(dispatcher)
    }
}