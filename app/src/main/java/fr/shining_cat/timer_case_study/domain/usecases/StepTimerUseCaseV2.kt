package fr.shining_cat.timer_case_study.domain.usecases

import fr.shining_cat.timer_case_study.di.TimerDispatcher
import fr.shining_cat.timer_case_study.domain.models.StepTimerState
import fr.shining_cat.timer_case_study.utils.HiitLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StepTimerUseCaseV2 @Inject constructor(
    @TimerDispatcher private val timerDispatcher: CoroutineDispatcher,
    private val hiitLogger: HiitLogger
) : StepTimerUseCase {

    private var _timerStateFlow = MutableStateFlow(StepTimerState())
    override val timerStateFlow: StateFlow<StepTimerState> = _timerStateFlow

    private var loggingTimestamp: Long = 0L
    private var stepStartTimeStamp = 0L

    override suspend fun start(totalSeconds: Int) {
        stepStartTimeStamp = System.currentTimeMillis()
        hiitLogger.d("StepTimerUseCaseV2", "-------------- START for $totalSeconds seconds--------------")
        return withContext(timerDispatcher) {
            loggingTimestamp = System.currentTimeMillis()
            initTimer(totalSeconds)
                .collect {
                    hiitLogger.d(
                        "StepTimerUseCaseV2",
                        "COLLECT: time since step start: ${System.currentTimeMillis() - stepStartTimeStamp}"
                    )
                    _timerStateFlow.emit(it)
                }
        }
    }

    private val timerHeartBeatRatio: Int = 100
    private val oneSecondAsMs: Long = 1000L

    private fun initTimer(totalSeconds: Int): Flow<StepTimerState> =
        ((totalSeconds - 1).times(timerHeartBeatRatio) downTo 0).asFlow() // first emit total - 1 because the total is emitted by onStart
            .onEach { delay(oneSecondAsMs.div(timerHeartBeatRatio)) } // internal heartbeat of oneSecondAsMs/timerHeartBeatRatio
            .conflate() // In case the operation in onTransform takes some time, conflate keeps the time ticking separately
            .onEach { delay(oneSecondAsMs) }//only collect and reemit every second
            .onStart {
                emit(totalSeconds.times(timerHeartBeatRatio))
            } // Emit total seconds immediately, without waiting the specified delay in onEach
            .transform { remainingHeartBeats: Int ->
                val remainingSeconds = remainingHeartBeats.div(timerHeartBeatRatio.toFloat()) //very first emission will be totalSeconds.times(timerHeartBeatRatio).div(timerHeartBeatRatio.toFloat()) = totalSeconds
                    .toInt() // this rounding will hopefully correct for the drift?
                val now = System.currentTimeMillis()
                hiitLogger.d(
                    "StepTimerUseCaseV2",
                    "TICKING: remainingHeartBeats = $remainingHeartBeats | remainingSeconds = $remainingSeconds | tick length = ${now - loggingTimestamp} | time since step start: ${System.currentTimeMillis() - stepStartTimeStamp}"
                )
                loggingTimestamp = now
                emit(StepTimerState(remainingSeconds, totalSeconds))
            }
            .flowOn(timerDispatcher)//actually ensure the operation will flow on the provided dispatcher. This mostly allows testing the usecase by manipulating it

}