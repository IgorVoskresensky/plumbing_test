package ru.ivos.plumbing_test.presentation.fragments

import android.content.Context
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapEvent
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.GeoObjectSelectionMetadata
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.ivos.plumbing_test.R
import ru.ivos.plumbing_test.data.PreferencesRepo
import ru.ivos.plumbing_test.databinding.FragmentMapBinding
import ru.ivos.plumbing_test.utils.Utils


class MapFragment : Fragment(), UserLocationObjectListener, GeoObjectTapListener, InputListener {

    private var _binding: FragmentMapBinding? = null
    private val binding: FragmentMapBinding
        get() = _binding ?: throw java.lang.RuntimeException("Binding is empty")

    private lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var findUserLocation: AppCompatImageButton
    private lateinit var coordinates: LinearLayout
    private lateinit var latitude: TextView
    private lateinit var longitude: TextView
    private lateinit var instruction: TextView
    private lateinit var destination: AppCompatButton

    private lateinit var preferencesRepo: PreferencesRepo

    private val mapKit = MapKitFactory.getInstance()

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
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMapView()

        setupViews()

        destination.setOnClickListener {
            Utils.latitude = latitude.text.toString()
            Utils.longitude = longitude.text.toString()

            preferencesRepo.setStartLatitude(latitude.text.toString())
            preferencesRepo.setStartLongitude(longitude.text.toString())

            val fragment = HomeFragment()
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
        findUserLocation.setOnClickListener {
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.please_wait),
                Toast.LENGTH_LONG
            ).show()
            userLocationLayer.isVisible = true
        }

        setupUserLocationLayer()

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

    override fun onObjectAdded(userLocationView: UserLocationView) {
        userLocationLayer.setAnchor(
            PointF((mapView.width() * 0.5f), (mapView.height() * 0.5f)),
            PointF((mapView.width() * 0.5f), (mapView.height() * 0.83f))
        )

        /**
        You will see the user's label on the map,
        because when you try to hide it,
        the SDK sets the default
         */
        userLocationView.arrow.setIcon(
            ImageProvider.fromResource(
                requireContext(),
                R.drawable.ic_arrow
            )
        )
//        userLocationView.arrow.isVisible = false
        val pinIcon = userLocationView.pin.useCompositeIcon()
        pinIcon.setIcon(
            "icon",
            ImageProvider.fromResource(requireContext(), R.drawable.ic_location),
            IconStyle().setAnchor(PointF(0f, 0f))
                .setRotationType(RotationType.ROTATE)
                .setZIndex(0f)
                .setScale(1f)
        )
        pinIcon.setIcon(
            "pin",
            ImageProvider.fromResource(requireContext(), R.drawable.ic_location),
            IconStyle().setAnchor(PointF(0.5f, 0.5f))
                .setRotationType(RotationType.ROTATE)
                .setZIndex(1f)
                .setScale(0.5f)
        )
    }

    override fun onObjectRemoved(p0: UserLocationView) {
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
    }

    override fun onObjectTap(geoObjectTapEvent: GeoObjectTapEvent): Boolean {
        val selectionMetadata = geoObjectTapEvent
            .geoObject
            .metadataContainer
            .getItem(GeoObjectSelectionMetadata::class.java)
        if (selectionMetadata != null) {
            mapView.map.selectGeoObject(selectionMetadata.id, selectionMetadata.layerId)
        }

        coordinates.visibility = View.GONE
        instruction.visibility = View.VISIBLE

        return selectionMetadata != null
    }

    override fun onMapTap(p0: Map, p1: Point) {
        mapView.map.deselectGeoObject()
        coordinates.visibility = View.GONE
        findUserLocation.visibility = View.VISIBLE
        instruction.text = requireContext().getString(R.string.instruction)
        userLocationLayer.isVisible = false
    }

    override fun onMapLongTap(p0: Map, p1: Point) {
        coordinates.visibility = View.VISIBLE
        findUserLocation.visibility = View.GONE
        instruction.text = requireContext().getString(R.string.tap_to_reset)
        latitude.text = p1.latitude.toString()
        longitude.text = p1.longitude.toString()
    }

    private fun setupViews() = with(binding) {
        coordinates = llCoordinates
        findUserLocation = binding.ibUserLocationSearch
        destination = btnSetDestination
        latitude = tvLatitude
        longitude = tvLongitude
        instruction = tvInstruction
    }

    private fun setupMapView() {
        mapView = binding.mvMap

        var zoom = 11.0f

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
            Animation(Animation.Type.SMOOTH, 2f), null
        )

        mapView.map.addTapListener(this)
        mapView.map.addInputListener(this)
    }

    private fun setupUserLocationLayer() {
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = false
        userLocationLayer.setObjectListener(this)
        mapView.map.isRotateGesturesEnabled = false
    }
}
