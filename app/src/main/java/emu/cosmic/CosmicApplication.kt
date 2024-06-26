package emu.cosmic

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Singleton

@HiltAndroidApp
class CosmicApplication : Application() {
    init {
        app = this
    }
    override fun onCreate() {
        app = this

        super.onCreate()
        // Applies dynamic colors to your application
        val colorsOption = DynamicColorsOptions.Builder().setPrecondition { _, _ -> true }.build()
        DynamicColors.applyToActivitiesIfAvailable(this, colorsOption)
        System.loadLibrary("cosmic")
    }
    companion object {
        lateinit var app: CosmicApplication
            private set
        val context : Context get() = app.applicationContext
    }
}

// We won't be using Protocol Buffers, but instead, Jetpack DataStore, as SharedPreferences has been
// deprecated in the new Android versions
@Singleton
val Context.dataSettings: DataStore<Preferences> by preferencesDataStore(name = "cosmicSettings")