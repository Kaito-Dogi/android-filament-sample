package app.doggy.filamentsample

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
internal class MainViewModel @Inject constructor() : ViewModel() {
  private val _uiState = MutableStateFlow(MainUiState.InitialValue)
  val uiState = _uiState.asStateFlow()

  fun onTopAppBarIconClick() {
    _uiState.update {
      it.copy(
        isBottomSheetShown = true,
      )
    }
  }

  fun onBottomSheetDismissRequest() {
    _uiState.update {
      it.copy(
        isBottomSheetShown = false,
      )
    }
  }
}
