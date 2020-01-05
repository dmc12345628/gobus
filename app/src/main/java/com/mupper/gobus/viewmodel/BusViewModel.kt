package com.mupper.gobus.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mupper.commons.scope.ScopedViewModel
import com.mupper.core.database.bus.Bus
import com.mupper.gobus.commons.extension.Event
import com.mupper.gobus.repository.BusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Created by jesus.medina on 12/2019.
 * Mupper
 */
class BusViewModel(private val busRepository: BusRepository) : ScopedViewModel() {
    val pathName = MutableLiveData<String>()

    val pathColor = MutableLiveData<String>()
    val pathColorInt = MutableLiveData<Int>()

    val capacity = MutableLiveData<String>()

    private val _showColorPickerDialog = MutableLiveData<Event<Int?>>()
    val showColorPickerDialog: LiveData<Event<Int?>> get() = _showColorPickerDialog

    fun showColorPickerDialog(defaultColor: Int) {
        if (pathColorInt.value == null) {
            pathColorInt.value = defaultColor
        }
        _showColorPickerDialog.value = Event(pathColorInt.value)
    }

    fun onColorPicked(color: Int) {
        pathColorInt.value = color
        pathColor.value = "#${Integer.toHexString(color)}"
    }

    fun saveNewBus() {
        launch(Dispatchers.Main) {
            val pathName = pathName.value ?: ""
            val pathColor = pathColor.value ?: ""
            val capacity = capacity.value ?: ""
            busRepository.addNewBus(
                Bus(
                    pathName,
                    pathColor,
                    capacity.toInt()
                )
            )
        }
    }
}