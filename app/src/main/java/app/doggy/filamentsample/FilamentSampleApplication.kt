package app.doggy.filamentsample

import android.app.Application
import com.google.android.filament.Filament
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FilamentSampleApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    // Filament を初期化
    Filament.init()
  }
}
