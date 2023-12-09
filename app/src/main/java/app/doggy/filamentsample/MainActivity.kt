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
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.android.UiHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private lateinit var engine: Engine
  private lateinit var surfaceView: SurfaceView
  private lateinit var uiHelper: UiHelper

  private lateinit var renderer: Renderer
  private lateinit var scene: Scene
  private lateinit var view: View
  private lateinit var camera: Camera

  private var _swapChain: SwapChain? = null
  private val swapChain get() = _swapChain!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setUpFilament()
    setupSurface()
    setupView()

    setContent {
      FilamentSampleTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          Column {
            AndroidView(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              factory = { surfaceView },
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

  private fun setUpFilament() {
    engine = Engine.create()

    renderer = engine.createRenderer()
    scene = engine.createScene()
    view = engine.createView()
    camera = engine.createCamera()
  }

  private fun setupSurface() {
    surfaceView = SurfaceView(this@MainActivity)

    uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    uiHelper.renderCallback = SurfaceCallback()
    uiHelper.attachTo(surfaceView)
  }

  private fun setupView() {
    // TODO: Skybox を生成する
    scene.skybox = null

    view.camera = camera
    view.scene = scene
  }

  inner class SurfaceCallback : UiHelper.RendererCallback {

    // SurfaceView が生成されたとき
    override fun onNativeWindowChanged(surface: Surface) {
      println("XXX: onNativeWindowChanged")

      engine.destroySwapChain(swapChain)
      _swapChain = engine.createSwapChain(surface)
    }

    // SurfaceView が破棄されたとき
    override fun onDetachedFromSurface() {
      println("XXX: onDetachedFromSurface")

      engine.destroySwapChain(swapChain)
      engine.flushAndWait()
      _swapChain = null
    }

    // SurfaceView の大きさが変更されたとき
    override fun onResized(width: Int, height: Int) {
      println("XXX: onResized")

      view.viewport = Viewport(0, 0, width, height)
    }
  }
}
