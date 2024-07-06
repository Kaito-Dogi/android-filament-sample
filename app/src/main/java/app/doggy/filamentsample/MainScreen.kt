package app.doggy.filamentsample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.doggy.filamentsample.component.MainTopAppBar
import app.doggy.filamentsample.component.SampleSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(
  viewModel: MainViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      MainTopAppBar(onIconClick = viewModel::onTopAppBarIconClick)
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
      contentAlignment = Alignment.Center,
    ) {
      Text(text = "3D モデルをドラッグで回転させたい")
    }
  }

  if (uiState.isBottomSheetShown) {
    ModalBottomSheet(
      onDismissRequest = viewModel::onBottomSheetDismissRequest,
    ) {
      SampleSection(
        modifier = Modifier.padding(16.dp),
      )
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
