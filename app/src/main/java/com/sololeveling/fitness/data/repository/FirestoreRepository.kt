package com.sololeveling.fitness.data.repository

import com.google.firebase.firestore.*
import com.google.firebase.auth.FirebaseAuth
import com.sololeveling.fitness.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun signInAnonymously(): String {
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: throw IllegalStateException("Auth failed")
    }

    fun isSignedIn(): Boolean = auth.currentUser != null

    suspend fun createPlayerProfile(userId: String, name: String, friendCode: String) {
        val profile = hashMapOf(
            "id" to userId,
            "name" to name,
            "friendCode" to friendCode,
            "level" to 1,
            "totalXP" to 0,
            "currentXP" to 0,
            "missionsCompleted" to 0,
            "currentStreak" to 0,
            "longestStreak" to 0,
            "stats" to hashMapOf(
                "strength" to 1, "speed" to 1, "endurance" to 1,
                "stamina" to 1, "flexibility" to 1
            ),
            "createdAt" to System.currentTimeMillis(),
            "lastActive" to System.currentTimeMillis()
        )
        firestore.collection("players").document(userId).set(profile).await()
    }

    suspend fun getPlayerProfile(userId: String): Map<String, Any>? {
        val doc = firestore.collection("players").document(userId).get().await()
        return if (doc.exists()) doc.data else null
    }

    suspend fun updatePlayerProfile(userId: String, data: Map<String, Any>) {
        firestore.collection("players").document(userId)
            .update(data + ("lastActive" to System.currentTimeMillis()))
            .await()
    }

    fun globalRankingFlow(limit: Long = 100): Flow<List<RankingEntry>> {
        return firestore.collection("players")
            .orderBy("totalXP", Query.Direction.DESCENDING)
            .limit(limit)
            .asFlow()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    RankingEntry(
                        userId = doc.id,
                        name = data["name"] as? String ?: "?",
                        level = (data["level"] as? Long)?.toInt() ?: 1,
                        totalXP = (data["totalXP"] as? Long)?.toInt() ?: 0,
                        rank = HunterRank.forLevel((data["level"] as? Long)?.toInt() ?: 1),
                        missionsCompleted = (data["missionsCompleted"] as? Long)?.toInt() ?: 0,
                        currentStreak = (data["currentStreak"] as? Long)?.toInt() ?: 0
                    )
                }
            }
    }

    fun findByFriendCode(code: String): Flow<List<Map<String, Any>>> {
        return firestore.collection("players")
            .whereEqualTo("friendCode", code.uppercase())
            .limit(1)
            .asFlow()
            .map { snap -> snap.documents.mapNotNull { it.data } }
    }

    fun myFriendsFlow(userId: String): Flow<List<Friendship>> {
        return firestore.collection("friendships")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "ACCEPTED")
            .asFlow()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    Friendship(
                        id = doc.id,
                        userId = data["userId"] as? String ?: "",
                        friendId = data["friendId"] as? String ?: "",
                        friendName = data["friendName"] as? String ?: "",
                        friendLevel = (data["friendLevel"] as? Long)?.toInt() ?: 1,
                        status = FriendStatus.ACCEPTED
                    )
                }
            }
    }

    suspend fun addFriend(userId: String, friendCode: String): Boolean {
        val query = firestore.collection("players")
            .whereEqualTo("friendCode", friendCode.uppercase())
            .get().await()
        if (query.documents.isEmpty()) return false
        val friendDoc = query.documents.first()
        val friendData = friendDoc.data ?: return false
        val friendship = hashMapOf(
            "userId" to userId,
            "friendId" to friendDoc.id,
            "friendName" to (friendData["name"] as? String ?: "?"),
            "friendLevel" to (friendData["level"] as? Long)?.toInt(),
            "status" to "ACCEPTED",
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("friendships").add(friendship).await()
        return true
    }
}

/** Extensión para convertir Query en Flow reactivo */
private fun Query.asFlow(): Flow<QuerySnapshot> = callbackFlow {
    val listener = addSnapshotListener { value, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        if (value != null) trySend(value)
    }
    awaitClose { listener.remove() }
}
