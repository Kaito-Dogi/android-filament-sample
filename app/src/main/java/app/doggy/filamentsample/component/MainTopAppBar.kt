package app.doggy.filamentsample.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainTopAppBar(
  onIconClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  TopAppBar(
    title = {
      Text(text = "Filament Sample")
    },
    modifier = modifier,
    actions = {
      IconButton(onClick = onIconClick) {
        Icon(
          imageVector = Icons.Outlined.Info,
          contentDescription = "Bottom Sheet を開く",
        )
      }
    },
  )
}
