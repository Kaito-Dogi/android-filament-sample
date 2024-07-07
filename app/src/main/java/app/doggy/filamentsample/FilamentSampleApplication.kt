package app.doggy.filamentsample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
internal class FilamentSampleApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    // Filament を初期化
    // ほとんどの API 呼び出しに必要な JNI ライブラリをロードする
    // Filament.init()
    // Utils.init()
  }
}
