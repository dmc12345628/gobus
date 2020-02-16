package com.mupper.gobus

import android.app.Application
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.BitmapDescriptor
import com.mupper.data.repository.BusRepositoryDerived
import com.mupper.data.repository.MapResourcesRepository
import com.mupper.data.repository.MapResourcesRepositoryDerived
import com.mupper.data.repository.TravelerRepositoryDerived
import com.mupper.data.source.local.BusLocalDataSource
import com.mupper.data.source.local.TravelerLocalDataSource
import com.mupper.data.source.location.LocationDataSource
import com.mupper.data.source.remote.BusRemoteDataSource
import com.mupper.data.source.remote.TravelerRemoteDataSource
import com.mupper.data.source.resources.MapResourcesDataSource
import com.mupper.features.ShareActualLocation
import com.mupper.features.bus.AddNewBusWithTravelers
import com.mupper.features.bus.GetActualBusWithTravelers
import com.mupper.features.traveler.GetActualTraveler
import com.mupper.gobus.data.database.GobusDatabase
import com.mupper.gobus.data.source.firebase.BusFirebaseDataSource
import com.mupper.gobus.data.source.firebase.TravelerFirebaseDataSource
import com.mupper.gobus.data.source.location.PlayServicesLocationDataSource
import com.mupper.gobus.data.source.resources.MapBitmapDescriptorDataSource
import com.mupper.gobus.data.source.room.BusRoomDataSource
import com.mupper.gobus.data.source.room.TravelerRoomDataSource
import com.mupper.gobus.model.TravelControl
import com.mupper.gobus.viewmodel.BusViewModel
import com.mupper.gobus.viewmodel.MapViewModel
import com.mupper.gobus.viewmodel.TravelViewModel
import com.mupper.gobus.viewmodel.TravelerViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun Application.initDI() {
    startKoin {
        androidLogger()
        androidContext(this@initDI)
        modules(
            listOf(
                appModule,
                dataSourceModule,
                repositoryModule,
                featureModule,
                viewModelModule
            )
        )
    }
}

const val STATIC_USER_EMAIL = "dmc12345628@gmail.com"
const val DEPENDENCY_NAME_STATIC_USER_EMAIL = "staticUserEmail"
const val DEPENDENCY_NAME_UI_DISPATCHER = "uiDispatcher"
const val DEPENDENCY_NAME_IO_DISPATCHER = "ioDispatcher"

private val appModule = module {
    single { GobusDatabase.getInstance(get()) }
    single<CoroutineDispatcher>(named(DEPENDENCY_NAME_UI_DISPATCHER)) { Dispatchers.Main }
    single(named(DEPENDENCY_NAME_IO_DISPATCHER)) { Dispatchers.IO }
}

val dataSourceModule = module {
    single(named(DEPENDENCY_NAME_STATIC_USER_EMAIL)) { STATIC_USER_EMAIL }
    factory<MapResourcesDataSource<BitmapDescriptor>> { MapBitmapDescriptorDataSource(get()) }
    factory { TravelControl(get()) }
    factory<LocationDataSource<LocationRequest, LocationCallback>> { PlayServicesLocationDataSource(get()) }
    factory<BusLocalDataSource> {
        BusRoomDataSource(
            get()
        )
    }
    factory<BusRemoteDataSource> { BusFirebaseDataSource() }
    factory<TravelerLocalDataSource> {
        TravelerRoomDataSource(
            get()
        )
    }
    factory<TravelerRemoteDataSource> { TravelerFirebaseDataSource() }
}

val repositoryModule = module {
    factory<MapResourcesRepository<BitmapDescriptor>> { MapResourcesRepositoryDerived(get()) }
    factory { BusRepositoryDerived(get(), get()) }
    factory { TravelerRepositoryDerived(get(named(DEPENDENCY_NAME_STATIC_USER_EMAIL)), get(), get()) }
}

private val featureModule = module {
    factory { GetActualTraveler(get()) }
    factory { AddNewBusWithTravelers(get(), get(), get(named(DEPENDENCY_NAME_IO_DISPATCHER))) }
    factory { GetActualBusWithTravelers(get()) }
    factory {
        ShareActualLocation(get(), get(), get(), get(named(DEPENDENCY_NAME_IO_DISPATCHER)))
    }
}

private val viewModelModule = module {
    factory { MapViewModel(get(), get(), get(named(DEPENDENCY_NAME_UI_DISPATCHER))) }
    factory { TravelerViewModel(get(), get(named(DEPENDENCY_NAME_UI_DISPATCHER))) }
    factory { TravelViewModel(get(), get(named(DEPENDENCY_NAME_UI_DISPATCHER))) }
    factory { BusViewModel(get(), get(named(DEPENDENCY_NAME_UI_DISPATCHER))) }
}