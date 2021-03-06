package com.mupper.gobus.viewmodel

import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.GoogleMap
import com.mupper.data.source.location.LocationDataSource
import com.mupper.gobus.utils.any
import com.mupper.gobus.commons.Event
import com.mupper.gobus.commons.extension.getOrAwaitValue
import com.mupper.gobus.data.source.resources.TravelerMapMarkerDataSource
import com.mupper.sharedtestcode.fakeLatLng
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.willDoNothing
import org.mockito.BDDMockito.willReturn
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MapViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    lateinit var mockLocationDataSource: LocationDataSource<LocationRequest, LocationCallback>

    @Mock
    lateinit var mockTravelerMapMarkerDataSource: TravelerMapMarkerDataSource

    @Mock
    lateinit var mockMapEventLiveDataObserver: Observer<Event<MapViewModel.MapModel>>

    @Mock
    lateinit var mockEventObserver: Observer<Event<Unit>>

    @Mock
    lateinit var mockGoogleMap: GoogleMap

    private lateinit var mapViewModel: MapViewModel

    @Before
    fun setUp() {
        mapViewModel =
            MapViewModel(
                mockLocationDataSource,
                mockTravelerMapMarkerDataSource,
                Dispatchers.Unconfined
            )
    }

    @Test
    fun `MapViewModel should not interact with mapsEventLiveData on init`() {
        // GIVEN
        mapViewModel.mapEventLiveData.observeForever(mockMapEventLiveDataObserver)

        // WHEN MapViewModel init

        // THEN
        verifyZeroInteractions(mockMapEventLiveDataObserver)
    }

    @Test
    fun `requestLocationPermission should call requestLocationPermissionEventLiveData event`() {
        runBlocking {
            with(mapViewModel) {
                // GIVEN
                requestLocationPermissionEventLiveData.observeForever(mockEventObserver)

                // WHEN
                requestLocationPermission()

                // THEN
                val expectedRequestLocationPermissionEventLiveData =
                    requestLocationPermissionEventLiveData.value
                verify(mockEventObserver).onChanged(expectedRequestLocationPermissionEventLiveData)
            }
        }
    }

    @Test
    fun `onPermissionRequest should call mapEventLiveData with MapReady event`() {
        with(mapViewModel) {
            // GIVEN
            mapEventLiveData.observeForever(mockMapEventLiveDataObserver)

            // WHEN
            onPermissionsRequested()

            // THEN
            val expectedMapsModelEvent = mapEventLiveData.value
            verify(mockMapEventLiveDataObserver).onChanged(expectedMapsModelEvent)
        }
    }

    @Test
    fun `onPermissionRequest should call initGoogleMap with expected GoogleMap`() {
        runBlocking {
            // GIVEN
            val spyMapViewModel = spy(mapViewModel)
            with(spyMapViewModel) {
                // WHEN
                onPermissionsRequested()
                val mapsEvent =
                    mapEventLiveData.getOrAwaitValue().peekContent() as MapViewModel.MapModel.MapReady
                mapsEvent.onMapReady.onMapReady(mockGoogleMap)

                // THEN
                verify(spyMapViewModel).initGoogleMap(mockGoogleMap)
            }
        }
    }

    @Test
    fun `onPermissionRequest should call requestLocationUpdates of locationDataSource with any arguments`() {
        runBlocking {
            // GIVEN
            val spyMapViewModel = spy(mapViewModel)
            with(spyMapViewModel) {
                // WHEN
                onPermissionsRequested()
                val mapsEvent =
                    mapEventLiveData.getOrAwaitValue().peekContent() as MapViewModel.MapModel.MapReady
                mapsEvent.onMapReady.onMapReady(mockGoogleMap)

                // THEN
                verify(mockLocationDataSource).requestLocationUpdates(
                    any(),
                    any()
                )
            }
        }
    }

    @Test
    fun `onNewLocationRequested should call findLastLocation of locationDataSource`() {
        runBlocking {
            // GIVEN
            with(mapViewModel) {
                // WHEN
                onNewLocationRequested()

                // THEN
                verify(mockLocationDataSource).findLastLocation()
            }
        }
    }

    @Test
    fun `onNewLocationRequest should call mapEventLiveData with NewLocation event`() {
        runBlocking {
            // GIVEN
            with(mapViewModel) {
                val expectedLatLng = fakeLatLng.copy()
                given(mockLocationDataSource.findLastLocation()).willReturn(expectedLatLng)

                // WHEN
                onNewLocationRequested()

                // THEN
                val mapsEvent = mapEventLiveData.getOrAwaitValue().peekContent()
                assertThat(mapsEvent, instanceOf(MapViewModel.MapModel.NewLocation::class.java))
            }
        }
    }

    @Test
    fun `onNewLocationRequest should call mapEventLiveData with NewLocation event with expected LatLng`() {
        runBlocking {
            // GIVEN
            with(mapViewModel) {
                val expectedLatLng = fakeLatLng.copy(2.0, 2.0)
                given(mockLocationDataSource.findLastLocation()).willReturn(expectedLatLng)

                // WHEN
                onNewLocationRequested()

                // THEN
                val mapsEvent =
                    mapEventLiveData.getOrAwaitValue().peekContent() as MapViewModel.MapModel.NewLocation
                assertThat(mapsEvent.lastLocation, `is`(expectedLatLng))
            }
        }
    }

    @Test
    fun `onNewLocationRequest should call mapEventLiveData with NewLocation event with isTraveling property as false`() {
        runBlocking {
            // GIVEN
            with(mapViewModel) {
                val expectedLatLng = fakeLatLng.copy()
                given(mockLocationDataSource.findLastLocation()).willReturn(expectedLatLng)
                stopTravel()

                // WHEN
                onNewLocationRequested()

                // THEN
                val mapsEvent =
                    mapEventLiveData.getOrAwaitValue().peekContent() as MapViewModel.MapModel.NewLocation
                assertThat(mapsEvent.isTraveling, `is`(false))
            }
        }
    }

    @Test
    fun `onNewLocationRequest should call mapEventLiveData with NewLocation event with isTraveling property as true`() {
        runBlocking {
            // GIVEN
            val spyMapViewModel = spy(mapViewModel)
            with(spyMapViewModel) {
                val expectedLatLng = fakeLatLng.copy()
                given(mockLocationDataSource.findLastLocation()).willReturn(expectedLatLng)
                val expectedNewLatLngZoom: CameraUpdate = mock()
                willReturn(expectedNewLatLngZoom).given(this).generateNewLatLngZoom(expectedLatLng)
                willReturn(true).given(mockTravelerMapMarkerDataSource).visibleForMap
                willDoNothing().given(mockTravelerMapMarkerDataSource).visibleForMap = true
                startTravel()

                // WHEN
                onNewLocationRequested()

                // THEN
                val mapsEvent =
                    mapEventLiveData.getOrAwaitValue().peekContent() as MapViewModel.MapModel.NewLocation
                assertThat(mapsEvent.isTraveling, `is`(true))
            }
        }
    }

    @Test
    fun `onNewLocationRequest should call googleMap animateCamera with any argument`() {
        runBlocking {
            // GIVEN
            val spyMapViewModel = spy(mapViewModel)
            with(spyMapViewModel) {
                // Map is ready
                onPermissionsRequested()
                val mapsEvent =
                    mapEventLiveData.getOrAwaitValue().peekContent() as MapViewModel.MapModel.MapReady
                mapsEvent.onMapReady.onMapReady(mockGoogleMap)
                // findLastLocation of locationDataSource return expectedLatLng
                val expectedLatLng = fakeLatLng.copy()
                given(mockLocationDataSource.findLastLocation()).willReturn(expectedLatLng)
                // generateNewLatLngZoom return expectedNewLatLngZoom
                val expectedNewLatLngZoom: CameraUpdate = mock()
                willReturn(expectedNewLatLngZoom).given(this).generateNewLatLngZoom(expectedLatLng)

                // WHEN
                onNewLocationRequested()

                // THEN
                verify(mockGoogleMap).animateCamera(any())
            }
        }
    }

    @Test
    fun `clearMap should set googleMap as null`() {
        // GIVEN
        with(mapViewModel) {
            // Map is ready
            onPermissionsRequested()
            val mapsEvent =
                mapEventLiveData.getOrAwaitValue().peekContent() as MapViewModel.MapModel.MapReady
            mapsEvent.onMapReady.onMapReady(mockGoogleMap)

            //WHEN
            clearMap()

            // THEN
            assertThat(googleMap, `is`(nullValue()))
        }
    }

    @Test
    fun `clearMap should call clearMarker of travelerMapMarkerDataSource`() {
        // GIVEN
        with(mapViewModel) {
            //WHEN
            clearMap()

            // THEN
            verify(mockTravelerMapMarkerDataSource).clearMarker()
        }
    }

    @Test
    fun `onLocationResult should not call mapEventLiveData with NewLocation event`() {
        runBlocking {
            // GIVEN
            val expectedLocationResult = null

            // WHEN
            val spyMapViewModel = spy(mapViewModel)
            with(spyMapViewModel) {
                onLocationResult(expectedLocationResult)

                val mapsEvent = mapEventLiveData.value
                assertThat(mapsEvent, `is`(nullValue()))
            }
        }
    }

    @Test
    fun `onLocationResult should call mapEventLiveData with NewLocation event`() {
        runBlocking {
            // GIVEN
            with(mapViewModel) {
                val mockLocation: Location = mock()
                val expectedLocationResult = LocationResult.create(listOf(mockLocation))

                // WHEN
                launch {
                    onLocationResult(expectedLocationResult)
                }

                val mapsEvent = mapEventLiveData.getOrAwaitValue().peekContent()
                assertThat(mapsEvent, instanceOf(MapViewModel.MapModel.NewLocation::class.java))
            }
        }
    }
}