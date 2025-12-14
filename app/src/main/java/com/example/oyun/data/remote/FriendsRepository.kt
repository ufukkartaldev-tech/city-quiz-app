package com.example.oyun.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val score: Int = 0,
    val level: Int = 1,
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)

data class FriendRequest(
    val id: String = "",
    val fromUid: String = "",
    val fromUsername: String = "",
    val fromPhotoUrl: String = "",
    val toUid: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: RequestStatus = RequestStatus.PENDING
)

enum class RequestStatus {
    PENDING, ACCEPTED, REJECTED
}

data class Friend(
    val uid: String = "",
    val username: String = "",
    val photoUrl: String = "",
    val score: Int = 0,
    val level: Int = 1,
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val friendsSince: Long = System.currentTimeMillis()
)

@Singleton
class FriendsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val usersCollection = firestore.collection("users")
    private val friendRequestsCollection = firestore.collection("friend_requests")
    
    /**
     * Kullanıcı arama (username veya email ile)
     */
    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            
            // Username ile arama
            val usernameResults = usersCollection
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .limit(10)
                .get()
                .await()
            
            val users = usernameResults.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(uid = doc.id)
            }.filter { it.uid != currentUid } // Kendini gösterme
            
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Arkadaşlık isteği gönder
     */
    suspend fun sendFriendRequest(toUid: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
            val currentUid = currentUser.uid
            
            // Zaten arkadaş mı kontrol et
            val isAlreadyFriend = checkIfFriends(currentUid, toUid)
            if (isAlreadyFriend) {
                return Result.failure(Exception("Already friends"))
            }
            
            // Bekleyen istek var mı kontrol et
            val existingRequest = friendRequestsCollection
                .whereEqualTo("fromUid", currentUid)
                .whereEqualTo("toUid", toUid)
                .whereEqualTo("status", RequestStatus.PENDING.name)
                .get()
                .await()
            
            if (!existingRequest.isEmpty) {
                return Result.failure(Exception("Request already sent"))
            }
            
            // Kullanıcı bilgilerini al
            val currentUserDoc = usersCollection.document(currentUid).get().await()
            val currentUsername = currentUserDoc.getString("username") ?: "Unknown"
            val currentPhotoUrl = currentUserDoc.getString("photoUrl") ?: ""
            
            // İstek oluştur
            val request = FriendRequest(
                fromUid = currentUid,
                fromUsername = currentUsername,
                fromPhotoUrl = currentPhotoUrl,
                toUid = toUid,
                timestamp = System.currentTimeMillis(),
                status = RequestStatus.PENDING
            )
            
            friendRequestsCollection.add(request).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gelen arkadaşlık isteklerini dinle (real-time)
     */
    fun getIncomingRequests(): Flow<List<FriendRequest>> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            close(Exception("Not logged in"))
            return@callbackFlow
        }
        
        val listener: ListenerRegistration = friendRequestsCollection
            .whereEqualTo("toUid", currentUid)
            .whereEqualTo("status", RequestStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val requests = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FriendRequest::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(requests)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Arkadaşlık isteğini kabul et
     */
    suspend fun acceptFriendRequest(requestId: String): Result<Unit> {
        return try {
            val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            
            // İsteği al
            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val request = requestDoc.toObject(FriendRequest::class.java)
                ?: return Result.failure(Exception("Request not found"))
            
            // İsteği güncelle
            friendRequestsCollection.document(requestId)
                .update("status", RequestStatus.ACCEPTED.name)
                .await()
            
            // Her iki kullanıcının da arkadaş listesine ekle
            val batch = firestore.batch()
            
            // Kullanıcı 1'in arkadaş listesine kullanıcı 2'yi ekle
            val friend1Ref = usersCollection.document(currentUid)
                .collection("friends")
                .document(request.fromUid)
            
            // Kullanıcı 2'nin arkadaş listesine kullanıcı 1'i ekle
            val friend2Ref = usersCollection.document(request.fromUid)
                .collection("friends")
                .document(currentUid)
            
            // Kullanıcı bilgilerini al
            val user1Doc = usersCollection.document(currentUid).get().await()
            val user2Doc = usersCollection.document(request.fromUid).get().await()
            
            val friend1 = Friend(
                uid = request.fromUid,
                username = user2Doc.getString("username") ?: "",
                photoUrl = user2Doc.getString("photoUrl") ?: "",
                score = user2Doc.getLong("score")?.toInt() ?: 0,
                level = user2Doc.getLong("level")?.toInt() ?: 1,
                isOnline = user2Doc.getBoolean("isOnline") ?: false,
                lastSeen = user2Doc.getLong("lastSeen") ?: System.currentTimeMillis(),
                friendsSince = System.currentTimeMillis()
            )
            
            val friend2 = Friend(
                uid = currentUid,
                username = user1Doc.getString("username") ?: "",
                photoUrl = user1Doc.getString("photoUrl") ?: "",
                score = user1Doc.getLong("score")?.toInt() ?: 0,
                level = user1Doc.getLong("level")?.toInt() ?: 1,
                isOnline = user1Doc.getBoolean("isOnline") ?: false,
                lastSeen = user1Doc.getLong("lastSeen") ?: System.currentTimeMillis(),
                friendsSince = System.currentTimeMillis()
            )
            
            batch.set(friend1Ref, friend1)
            batch.set(friend2Ref, friend2)
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Arkadaşlık isteğini reddet
     */
    suspend fun rejectFriendRequest(requestId: String): Result<Unit> {
        return try {
            friendRequestsCollection.document(requestId)
                .update("status", RequestStatus.REJECTED.name)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Arkadaş listesini dinle (real-time)
     */
    fun getFriends(): Flow<List<Friend>> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            close(Exception("Not logged in"))
            return@callbackFlow
        }
        
        val listener: ListenerRegistration = usersCollection
            .document(currentUid)
            .collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val friends = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Friend::class.java)
                } ?: emptyList()
                
                trySend(friends)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Arkadaşı sil
     */
    suspend fun removeFriend(friendUid: String): Result<Unit> {
        return try {
            val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            
            val batch = firestore.batch()
            
            // Her iki taraftan da sil
            val friend1Ref = usersCollection.document(currentUid)
                .collection("friends")
                .document(friendUid)
            
            val friend2Ref = usersCollection.document(friendUid)
                .collection("friends")
                .document(currentUid)
            
            batch.delete(friend1Ref)
            batch.delete(friend2Ref)
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Online durumunu güncelle
     */
    suspend fun updateOnlineStatus(isOnline: Boolean) {
        try {
            val currentUid = auth.currentUser?.uid ?: return
            
            usersCollection.document(currentUid).update(
                mapOf(
                    "isOnline" to isOnline,
                    "lastSeen" to System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
            // Hata logla ama crash etme
        }
    }
    
    /**
     * Kullanıcı profilini güncelle
     */
    suspend fun updateUserProfile(username: String, photoUrl: String, score: Int, level: Int) {
        try {
            val currentUid = auth.currentUser?.uid ?: return
            
            usersCollection.document(currentUid).update(
                mapOf(
                    "username" to username,
                    "photoUrl" to photoUrl,
                    "score" to score,
                    "level" to level
                )
            ).await()
        } catch (e: Exception) {
            // Hata logla
        }
    }
    
    /**
     * İki kullanıcı arkadaş mı kontrol et
     */
    private suspend fun checkIfFriends(uid1: String, uid2: String): Boolean {
        return try {
            val doc = usersCollection.document(uid1)
                .collection("friends")
                .document(uid2)
                .get()
                .await()
            
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
}
