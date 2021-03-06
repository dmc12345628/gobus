package com.mupper.gobus.viewmodel

import com.mupper.domain.LatLng
import com.mupper.gobus.commons.scope.ScopedViewModel
import com.mupper.usecase.ShareActualLocation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

/**
 * Created by jesus.medina on 12/2019.
 * Mupper
 */
class TravelerViewModel(
    private val shareActualLocation: ShareActualLocation,
    uiDispatcher: CoroutineDispatcher
) : ScopedViewModel(uiDispatcher) {
    fun shareActualLocation(newLocation: LatLng) {
        launch {
            shareActualLocation.invoke(newLocation)
        }
    }
}
