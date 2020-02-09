package com.mupper.features

import com.mupper.data.repository.BusRepository
import com.mupper.data.repository.TravelerRepository
import com.mupper.domain.LatLng
import com.mupper.domain.traveler.Traveler
import com.mupper.features.bus.GetActualBusWithTravelers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ShareActualLocation(
    private val getActualBusWithTravelers: GetActualBusWithTravelers,
    private val travelerRepository: TravelerRepository,
    private val busRepository: BusRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun invoke(newLatLng: LatLng) = withContext(dispatcher) {
        val travelingBus = getActualBusWithTravelers.invoke()
        travelingBus?.let {
            val travelersInBus = it.travelers
            if (travelersInBus.isNullOrEmpty()) {
                return@withContext
            }
            val travelerInBus = travelersInBus[0]
            travelerInBus.isTraveling = true
            travelerInBus.currentPosition = newLatLng
            busRepository.shareActualLocation(
                travelingBus, Traveler(
                    travelerInBus.email,
                    newLatLng,
                    true
                )
            )
            travelerRepository.shareActualLocation(travelerInBus)
        }
    }
}