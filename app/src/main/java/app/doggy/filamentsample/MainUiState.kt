package app.doggy.filamentsample

import androidx.compose.runtime.Immutable

@Immutable
internal data class MainUiState(
  val isBottomSheetShown: Boolean,
) {
  companion object {
    val InitialValue = MainUiState(
      isBottomSheetShown = false,
    )
  }
}
