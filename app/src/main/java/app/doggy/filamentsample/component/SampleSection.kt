package app.doggy.filamentsample.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.doggy.filamentsample.sample.GltfViewerActivity
import app.doggy.filamentsample.sample.HelloTriangleActivity
import app.doggy.filamentsample.sample.TextureViewActivity

@Composable
internal fun SampleSection(
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Button(onClick = { context.startActivity(HelloTriangleActivity.newIntent(context)) }) {
      Text(text = "hello-triangle")
    }
    Button(onClick = { context.startActivity(TextureViewActivity.newIntent(context)) }) {
      Text(text = "texture-view")
    }
    Button(onClick = { context.startActivity(GltfViewerActivity.newIntent(context)) }) {
      Text(text = "gltf-viewer")
    }
  }
}
