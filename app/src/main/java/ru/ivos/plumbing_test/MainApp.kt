package ru.ivos.plumbing_test

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import ru.ivos.plumbing_test.utils.Utils

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(Utils.API_KEY)
    }
}