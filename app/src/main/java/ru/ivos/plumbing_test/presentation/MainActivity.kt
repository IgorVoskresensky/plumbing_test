package ru.ivos.plumbing_test.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.ivos.plumbing_test.R
import ru.ivos.plumbing_test.databinding.ActivityMainBinding
import ru.ivos.plumbing_test.presentation.fragments.AboutFragment
import ru.ivos.plumbing_test.presentation.fragments.HomeFragment
import ru.ivos.plumbing_test.presentation.fragments.MapFragment

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_splash)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            setContentView(binding.root)
            setupBottomNavigationView()
        }

        checkLocationPermission()
    }


    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 99
            )
            return
        }
    }

    /**
    I had to give up using Jetpack Navigation Component
    because backstack doesn't work in its latest versions.
    I couldn't find a solution to this problem
    and set up the BottomNavigationView the old way
     */
    private fun setupBottomNavigationView() {
        binding.bnvMain.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.mapFragment -> {
                    replaceFragment(MapFragment())
                    true
                }
                else -> {
                    replaceFragment(AboutFragment())
                    true
                }

            }
        }
    }

    private fun replaceFragment(newFragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, newFragment)
            .addToBackStack(null)
            .commit()
    }
}