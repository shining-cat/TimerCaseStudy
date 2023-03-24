package fr.shining_cat.timer_case_study.ui.timersate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.shining_cat.timer_case_study.di.DefaultDispatcher
import fr.shining_cat.timer_case_study.domain.usecases.StepTimerUseCase
import fr.shining_cat.timer_case_study.utils.HiitLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerSateViewModel @Inject constructor(
    private val stepTimerUseCase: StepTimerUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val hiitLogger: HiitLogger
) : ViewModel() {


}
