package com.mupper.gobus.data.source.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.mupper.data.source.remote.TravelerRemoteDataSource
import com.mupper.domain.LatLng
import com.mupper.domain.traveler.Traveler
import com.mupper.gobus.commons.COLLECTION_TRAVELER
import com.mupper.gobus.data.mapper.toDomainTraveler
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TravelerFirebaseDataSource(
    private val firestore: FirebaseFirestore
) : TravelerRemoteDataSource {

    private fun getTravelerDocument(email: String) =
        firestore.collection(COLLECTION_TRAVELER).document(email)

    override suspend fun addTraveler(traveler: Traveler): Traveler =
        suspendCancellableCoroutine { continuation ->
            val (email) = traveler
            getTravelerDocument(email).set(traveler).addOnCompleteListener {
                if (it.isSuccessful) {
                    continuation.resume(traveler)
                } else {
                    continuation.resume(
                        Traveler(
                            "",
                            LatLng(0.0, 0.0),
                            false
                        )
                    )
                }
            }
        }

    override suspend fun findTravelerByEmail(email: String): Traveler? =
        suspendCancellableCoroutine { continuation ->
            getTravelerDocument(email).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.data?.let {
                        continuation.resume(it.toDomainTraveler())
                    }
                } else {
                    continuation.resume(
                        Traveler(
                            "",
                            LatLng(0.0, 0.0),
                            false
                        )
                    )
                }
            }
        }

    override suspend fun shareActualLocation(traveler: Traveler) {
        val (email) = traveler
        getTravelerDocument(email).set(traveler)
    }
}
