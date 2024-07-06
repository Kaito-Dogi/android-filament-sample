package app.doggy.filamentsample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.doggy.filamentsample.component.SampleSection

@Composable
internal fun MainScreen() {
  Surface(
    modifier = Modifier.fillMaxSize(),
  ) {
    Column {
      SampleSection()
    }
  }
}

@Preview
@Composable
private fun MainScreenPreview() {
  MaterialTheme {
    MainScreen()
  }
}
