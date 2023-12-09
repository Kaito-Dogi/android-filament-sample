package app.doggy.filamentsample

import android.os.Bundle
import android.view.Surface
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.doggy.filamentsample.ui.theme.FilamentSampleTheme
import com.google.android.filament.Engine
import com.google.android.filament.android.UiHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var engine: Engine

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      FilamentSampleTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          Column {
            AndroidView(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              factory = { context ->
                SurfaceView(context).apply {
                  val uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
                  uiHelper.renderCallback = SurfaceCallback()
                  uiHelper.attachTo(this)
                }
              },
            )
            Text(
              text = "Is engine valid: ${engine.isValid}",
              modifier = Modifier.padding(16.dp),
            )
          }
        }
      }
    }
  }

  inner class SurfaceCallback : UiHelper.RendererCallback {
    override fun onNativeWindowChanged(surface: Surface) {
      // TODO: 実装
    }

    override fun onDetachedFromSurface() {
      // TODO: 実装
    }

    override fun onResized(width: Int, height: Int) {
      // TODO: 実装
    }
  }
}
