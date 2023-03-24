package fr.shining_cat.timer_case_study.domain.usecases

import fr.shining_cat.timer_case_study.domain.models.StepTimerState
import kotlinx.coroutines.flow.StateFlow

interface StepTimerUseCase {

    abstract val timerStateFlow: StateFlow<StepTimerState>
    abstract suspend fun start(totalSeconds: Int)
}