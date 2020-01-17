package com.mupper.gobus.ui.main

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavDirections
import com.google.android.gms.maps.SupportMapFragment
import com.mupper.commons.scope.ScoppedFragment
import com.mupper.features.ShareActualLocation
import com.mupper.features.bus.AddNewBusWithTravelers
import com.mupper.features.bus.GetTravelingBus
import com.mupper.features.traveler.GetActualTraveler
import com.mupper.gobus.PermissionRequester
import com.mupper.gobus.R
import com.mupper.gobus.commons.EventObserver
import com.mupper.gobus.commons.extension.*
import com.mupper.gobus.data.source.traveler.TravelerRoomDataSource
import com.mupper.gobus.data.source.bus.BusFirebaseDataSource
import com.mupper.gobus.data.source.bus.BusRoomDataSource
import com.mupper.gobus.data.source.traveler.TravelerFirebaseDataSource
import com.mupper.gobus.databinding.FragmentMapsBinding
import com.mupper.gobus.model.TravelControl
import com.mupper.gobus.repository.LocationRepository
import com.mupper.gobus.viewmodel.BusViewModel
import com.mupper.gobus.viewmodel.MapsViewModel
import com.mupper.gobus.viewmodel.MapsViewModel.MapsModel
import com.mupper.gobus.viewmodel.TravelViewModel
import com.mupper.gobus.viewmodel.TravelerViewModel

class MapsFragment : ScoppedFragment() {

    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var travelViewModel: TravelViewModel
    private lateinit var travelerViewModel: TravelerViewModel
    private lateinit var busViewModel: BusViewModel

    private lateinit var mapFragment: SupportMapFragment

    private val locationPermissionRequester by lazy {
        PermissionRequester(
            activity!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private var binding: FragmentMapsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = container?.bindingInflate(R.layout.fragment_maps, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().supportFragmentManager.findFragmentById(R.id.map).let {
            mapFragment = it as SupportMapFragment
        }

        mapsViewModel =
            getViewModel { MapsViewModel(LocationRepository(app)) }
        travelerViewModel =
            getViewModel {
                TravelerViewModel(
                    ShareActualLocation(
                        GetTravelingBus(
                            BusRoomDataSource(app.db),
                            BusFirebaseDataSource()
                        ),
                        TravelerRoomDataSource(
                            app.db
                        ),
                        TravelerFirebaseDataSource(),
                        BusRoomDataSource(app.db),
                        BusFirebaseDataSource()
                    )
                )
            }
        travelViewModel =
            getViewModel { TravelViewModel(TravelControl(requireContext())) }
        busViewModel =
            getViewModel {
                BusViewModel(
                    AddNewBusWithTravelers(
                        GetActualTraveler(
                            TravelerRoomDataSource(
                                app.db
                            ),
                            TravelerFirebaseDataSource()
                        ),
                        BusRoomDataSource(app.db),
                        BusFirebaseDataSource()
                    )
                )
            }

        mapsViewModel.model.observe(
            this,
            EventObserver(::onMapsModelChange)
        )

        mapsViewModel.requestLocationPermission.observe(this,
            EventObserver {
                locationPermissionRequester.request {
                    if (it)
                        mapsViewModel.onPermissionsRequested()
                }
            })

        travelViewModel.navigateToStartTravelDialog.observe(this,
            EventObserver {
                val toStartTravel: NavDirections =
                    MapsFragmentDirections.actionMapsFragmentToStartTravelFragment()
                navigate(toStartTravel)
            })
        travelViewModel.navigateToStopTravelDialog.observe(this,
            EventObserver {
                val toStopTravel: NavDirections =
                    MapsFragmentDirections.actionMapsFragmentToStopTravelFragment()
                navigate(toStopTravel)
            })

        travelViewModel.travelState.observe(
            this,
            EventObserver(::onTravelModelChange)
        )

        binding?.apply {
            travel = travelViewModel
            lifecycleOwner = this@MapsFragment
        }
    }

    override fun onResume() {
        super.onResume()
        mapsViewModel.requestLocationPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapsViewModel.clearMap()
        supportFragmentManager.beginTransaction().remove(mapFragment).commit()
    }

    private fun onMapsModelChange(model: MapsModel) {
        when (model) {
            is MapsModel.MapReady -> mapFragment.getMapAsync(model.onMapReady)
            is MapsModel.RequestNewLocation -> mapsViewModel.onNewLocationRequested()
            is MapsModel.NewLocation -> with(model.lastLocation) {
                if (model.isTraveling) {
                    travelerViewModel.shareActualLocation(this)
                }
            }
        }
    }

    private fun onTravelModelChange(state: TravelViewModel.TravelState) {
        when (state) {
            is TravelViewModel.TravelState.Walking -> {
                mapsViewModel.stopTravel()
                travelViewModel.setFabToStart()
            }
            is TravelViewModel.TravelState.OnWay -> {
                mapsViewModel.startTravel()
                travelViewModel.setFabToStop()
            }
        }
    }
}
