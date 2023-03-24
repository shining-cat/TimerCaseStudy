package fr.shining_cat.timer_case_study.domain.usecases

import fr.shining_cat.timer_case_study.domain.models.StepTimerState
import kotlinx.coroutines.flow.StateFlow

interface StepTimerUseCase {

    val timerStateFlow: StateFlow<StepTimerState>
    suspend fun start(totalSeconds: Int)
}