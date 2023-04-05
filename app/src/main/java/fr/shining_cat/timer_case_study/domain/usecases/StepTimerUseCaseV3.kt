package fr.shining_cat.timer_case_study.domain.usecases

import fr.shining_cat.timer_case_study.di.TimerDispatcher
import fr.shining_cat.timer_case_study.domain.models.StepTimerState
import fr.shining_cat.timer_case_study.utils.HiitLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StepTimerUseCaseV3 @Inject constructor(
    @TimerDispatcher private val timerDispatcher: CoroutineDispatcher,
    private val hiitLogger: HiitLogger
) : StepTimerUseCase {

    private var _timerStateFlow = MutableStateFlow(StepTimerState())
    override val timerStateFlow: StateFlow<StepTimerState> = _timerStateFlow

    private var loggingTimestamp: Long = 0L
    private var stepStartTimeStamp = 0L
    private var nextTickTimeStamp = 0L

    override suspend fun start(totalSeconds: Int) {
        hiitLogger.d(
            "StepTimerUseCaseV3",
            "-------------- START for $totalSeconds seconds--------------"
        )
        return withContext(timerDispatcher) {
            loggingTimestamp = System.currentTimeMillis()
            initTimer(totalSeconds)//any work done in that Flow will be cancelled if the coroutine is cancelled
                .collect {
                    hiitLogger.d(
                        "Ste{pTimerUseCaseV3",
                        "COLLECT: time since step start: ${System.currentTimeMillis() - stepStartTimeStamp}"
                    )
                    _timerStateFlow.emit(it)
                }
        }
    }

    private val oneSecondAsMs: Long = 1000L

    private fun initTimer(totalSeconds: Int): Flow<StepTimerState> = flow {
        hiitLogger.d("StepTimerUseCaseV3", "initTimer:: totalSeconds $totalSeconds")
        stepStartTimeStamp = System.currentTimeMillis()
        val expectedEndTimeMillis = stepStartTimeStamp + totalSeconds.times(oneSecondAsMs)
        //emit starting state
        emit(
            StepTimerState(
                secondsRemaining = totalSeconds,
                totalSeconds = totalSeconds
            )
        )
        var totalSecondsReached = false
        var remainingSeconds = totalSeconds
        while (!totalSecondsReached) {
            nextTickTimeStamp = System.currentTimeMillis() + oneSecondAsMs
            hiitLogger.d("StepTimerUseCaseV3", "nextTickTimeStamp: $nextTickTimeStamp")
            var secondComplete = false
            while (!secondComplete) {
                secondComplete = System.currentTimeMillis() >= nextTickTimeStamp
                //hiitLogger.d("StepTimerUseCaseV3","initTimer:: currentTimeMillis: ${System.currentTimeMillis() - nextTickTimeStamp}")
            }
            remainingSeconds--
            hiitLogger.d("StepTimerUseCaseV3", "remainingSeconds: $remainingSeconds")
            //emit every second
            emit(
                StepTimerState(
                    secondsRemaining = remainingSeconds,
                    totalSeconds = totalSeconds
                )
            )
            totalSecondsReached = System.currentTimeMillis() >= expectedEndTimeMillis
            hiitLogger.d("StepTimerUseCaseV3", "totalSecondsReached: $totalSecondsReached")
        }
        //emit finish state
        emit(
            StepTimerState(
                secondsRemaining = 0,
                totalSeconds = totalSeconds
            )
        )
    }
}