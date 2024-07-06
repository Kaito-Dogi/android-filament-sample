package app.doggy.filamentsample

import android.app.Application
import com.google.android.filament.Filament
import com.google.android.filament.utils.Utils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FilamentSampleApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    // Filament を初期化
    // ほとんどの API 呼び出しに必要な JNI ライブラリをロードする
    Filament.init()
    Utils.init()
  }
}
