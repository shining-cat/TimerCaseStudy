package fr.shining_cat.timer_case_study.domain.usecases

import fr.shining_cat.timer_case_study.di.TimerDispatcher
import fr.shining_cat.timer_case_study.domain.models.StepTimerState
import fr.shining_cat.timer_case_study.utils.HiitLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StepTimerUseCaseV1 @Inject constructor(
    @TimerDispatcher private val timerDispatcher: CoroutineDispatcher,
    private val hiitLogger: HiitLogger
) : StepTimerUseCase {

    private var _timerStateFlow = MutableStateFlow(StepTimerState(-1))
    override val timerStateFlow: StateFlow<StepTimerState> = _timerStateFlow

    private var loggingTimestamp: Long = 0L
    private var stepStartTimeStamp = 0L

    override suspend fun start(totalSeconds: Int) {
        hiitLogger.d("StepTimerUseCaseV1", "-------------- START for $totalSeconds seconds--------------")
        stepStartTimeStamp = System.currentTimeMillis()
        loggingTimestamp = System.currentTimeMillis()
        return withContext(timerDispatcher) {
            initTimer(totalSeconds)
                .collect {
                    hiitLogger.d(
                        "StepTimerUseCaseV1",
                        "COLLECT: time since step start: ${System.currentTimeMillis() - stepStartTimeStamp}"
                    )
                    _timerStateFlow.emit(it)
                }
        }
    }

    private fun initTimer(totalSeconds: Int): Flow<StepTimerState> =
        (totalSeconds - 1 downTo 0).asFlow() // Emit total - 1 because the first will be emitted onStart
            .onEach { delay(1000L) } // Each second later emit a number
            .onStart {
                emit(totalSeconds)
            } // Emit total seconds immediately
            .conflate() // In case the operation onTick takes some time, conflate keeps the time ticking separately
            .transform { remainingSeconds: Int ->
                val now = System.currentTimeMillis()
                hiitLogger.d(
                    "StepTimerUseCaseV1",
                    "TICKING: remainingSeconds = $remainingSeconds | tick length = ${now - loggingTimestamp} | time since step start: ${System.currentTimeMillis() - stepStartTimeStamp}"
                )
                loggingTimestamp = now
                emit(StepTimerState(remainingSeconds, totalSeconds))
            }
            .flowOn(timerDispatcher)//actually ensure the operation will flow on the provided dispatcher. This mostly allows testing the usecase by manipulating it
}