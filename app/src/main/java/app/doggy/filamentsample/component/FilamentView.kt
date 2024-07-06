package app.doggy.filamentsample.component

import android.view.TextureView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
internal fun FilamentView(
  modifier: Modifier = Modifier,
) {
  AndroidView(
    factory = { context ->
      TextureView(context)
    },
    modifier = modifier.fillMaxSize(),
  )
}
