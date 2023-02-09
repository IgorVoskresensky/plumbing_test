package ru.ivos.plumbing_test.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import ru.ivos.plumbing_test.R
import ru.ivos.plumbing_test.data.PreferencesRepo
import ru.ivos.plumbing_test.databinding.FragmentHomeBinding
import ru.ivos.plumbing_test.utils.Utils

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = _binding ?: throw java.lang.RuntimeException("Binding is empty")

    private lateinit var mapView: MapView
    private lateinit var mapKit: MapKit
    private lateinit var userLocationLayer: UserLocationLayer

    private lateinit var reset: AppCompatImageButton
    private lateinit var tvResetOn: TextView
    private lateinit var noCoordinates: TextView
    private lateinit var textActuallyCoordinates: TextView
    private lateinit var actuallyCoordinates: CardView
    private lateinit var setDestination: AppCompatButton
    private lateinit var latitude: TextView
    private lateinit var longitude: TextView

    private lateinit var preferencesRepo: PreferencesRepo

    private lateinit var latFromPref: String
    private lateinit var lonFromPref: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferencesRepo = PreferencesRepo(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
        latFromPref = preferencesRepo.getStartLatitude().orEmpty()
        lonFromPref = preferencesRepo.getStartLongitude().orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mvHome

        setupCameraPosition()

        setupViews()

        mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = false

        homeInfo()

        reset.setOnClickListener {
            Utils.latitude = ""
            Utils.longitude = ""
            latFromPref = ""
            preferencesRepo.setStartLatitude("")
            preferencesRepo.setStartLongitude("")
            homeInfo()
            setupCameraPosition()
        }

        setDestination.setOnClickListener {
            val fragment = MapFragment()
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onStart() {
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun homeInfo() {
        if (Utils.latitude != "") {
            actuallyCoordinates.visibility = View.VISIBLE
            latitude.visibility = View.VISIBLE
            longitude.visibility = View.VISIBLE
            textActuallyCoordinates.visibility = View.VISIBLE
            noCoordinates.visibility = View.GONE
        } else if (latFromPref != "") {
            actuallyCoordinates.visibility = View.VISIBLE
            latitude.visibility = View.VISIBLE
            latitude.text = latFromPref
            longitude.visibility = View.VISIBLE
            longitude.text = lonFromPref
            textActuallyCoordinates.visibility = View.VISIBLE
            noCoordinates.visibility = View.GONE
        } else {
            actuallyCoordinates.visibility = View.GONE
            latitude.visibility = View.GONE
            longitude.visibility = View.GONE
            textActuallyCoordinates.visibility = View.GONE
            noCoordinates.visibility = View.VISIBLE
        }
    }

    private fun setupViews() = with(binding) {
        reset = btnResetCoordinates
        tvResetOn = tvReset
        noCoordinates = tvNoCoordinates
        actuallyCoordinates = cvActuallyCoordinates
        latitude = tvLatitudeHome
        latitude.text = Utils.latitude
        longitude = tvLongitudeHome
        longitude.text = Utils.longitude
        setDestination = btnSetDestinationHome
        textActuallyCoordinates = tvActuallyDestination
    }

    private fun setupCameraPosition() {
        var zoom = 11.0f

        latFromPref = preferencesRepo.getStartLatitude().orEmpty()
        lonFromPref = preferencesRepo.getStartLongitude().orEmpty()

        var startLatitude = Utils.START_LATITUDE
        if (latFromPref.isNotEmpty()) {
            startLatitude = latFromPref.toDouble()
            zoom = 14.0f
        }
        if (Utils.latitude != "") {
            startLatitude = Utils.latitude.toDouble()
            zoom = 14.0f
        }

        var startLongitude = if (lonFromPref.isNotEmpty()) {
            lonFromPref.toDouble()
        } else {
            Utils.START_LONGITUDE
        }
        if (Utils.longitude != "") {
            startLongitude = Utils.longitude.toDouble()
        }

        mapView.map.move(
            CameraPosition(Point(startLatitude, startLongitude), zoom, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f), null
        )
    }
}