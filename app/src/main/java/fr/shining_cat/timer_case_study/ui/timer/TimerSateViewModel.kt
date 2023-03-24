package fr.shining_cat.timer_case_study.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.shining_cat.timer_case_study.di.DefaultDispatcher
import fr.shining_cat.timer_case_study.domain.models.StepTimerState
import fr.shining_cat.timer_case_study.domain.models.TimerSession
import fr.shining_cat.timer_case_study.domain.models.TimerSessionStep.RestStep
import fr.shining_cat.timer_case_study.domain.models.TimerSessionStep.WorkStep
import fr.shining_cat.timer_case_study.domain.usecases.StepTimerUseCase
import fr.shining_cat.timer_case_study.domain.usecases.StepTimerUseCaseV1
import fr.shining_cat.timer_case_study.domain.usecases.StepTimerUseCaseV2
import fr.shining_cat.timer_case_study.utils.HiitLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerSateViewModel @Inject constructor(
    private val mapper: TimerSessionMapper,
    private val stepTimerUseCaseV2: StepTimerUseCaseV2,
    private val stepTimerUseCaseV1: StepTimerUseCaseV1,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val hiitLogger: HiitLogger
) : ViewModel() {


    private val _screenViewState =
        MutableStateFlow<TimerViewState>(TimerViewState.Loading)
    val screenViewState = _screenViewState.asStateFlow()

    //
    private var session: TimerSession = TestTimerSessionProvider().getTestTimerSession()
    private var currentSessionStepIndex = 0
    private var stepTimerJob: Job? = null

    // drift analysis:
    private var sessionStartTimestamp = 0L
    private data class DriftAnalysis(
        val step: String,
        val elapsedTimeSincePrevious: Long,
        val elapsedTimeSinceStart: Long
    )
    private val analysis = mutableListOf<DriftAnalysis>()
    private fun logDriftAnalysis(step: String) {
        val elapsedTotal = System.currentTimeMillis() - sessionStartTimestamp
        val elapsedSinceLast = elapsedTotal - (analysis.lastOrNull()?.elapsedTimeSinceStart ?: 0L)
        analysis.add(
            DriftAnalysis(
                step = step,
                elapsedTimeSincePrevious = elapsedSinceLast,
                elapsedTimeSinceStart = elapsedTotal
            )
        )
    }


    fun startUseCaseTimerV1() {
        sessionStartTimestamp = System.currentTimeMillis()
        logDriftAnalysis("startViewModelTimer")
        setupTicker(stepTimerUseCaseV1)
        launchSessionStep(stepTimerUseCaseV1)
    }

    fun startUseCaseTimerV2() {
        sessionStartTimestamp = System.currentTimeMillis()
        logDriftAnalysis("startViewModelTimer")
        setupTicker(stepTimerUseCaseV2)
        launchSessionStep(stepTimerUseCaseV2)
    }

    private fun setupTicker(stepTimerUseCase: StepTimerUseCase) {
        viewModelScope.launch {
            stepTimerUseCase.timerStateFlow.collect() { stepTimerState ->
                if (stepTimerState != StepTimerState()) { //excluding first emission with default value
                    tick(stepTimerUseCase, stepTimerState = stepTimerState)
                }
            }
        }
    }

    private fun tick(stepTimerUseCase: StepTimerUseCase, stepTimerState: StepTimerState) {
        val currentStep = session.steps[currentSessionStepIndex]
        logDriftAnalysis("tick currentStep: ${currentStep::class.java.simpleName}")
        val remainingSeconds = stepTimerState.secondsRemaining
        if (remainingSeconds == 0) {//step end
            stepTimerJob?.cancel()
            if (session.steps.lastOrNull() == currentStep) {
                emitSessionEndState()
            } else {
                //session is not finished, increment step index and continue
                //we don't emit any state here as we expect the next step's first state to be emitted immediately
                //logDriftAnalysis("tick::STEP ended")
                currentSessionStepIndex += 1
                launchSessionStep(stepTimerUseCase)
            }
        } else {//build running step state and emit
            viewModelScope.launch {
                //logDriftAnalysis("tick::normal ticking - mapper.buildState START")
                val currentState = mapper.buildState(
                    session = session,
                    currentSessionStepIndex = currentSessionStepIndex,
                    currentState = stepTimerState
                )
                //logDriftAnalysis("tick::normal ticking - mapper.buildState END -> viewstate emission")
                _screenViewState.emit(currentState)
            }
        }
    }

    private fun emitSessionEndState() {
        viewModelScope.launch {
            val totalElapsedTimeMs = (System.currentTimeMillis() - sessionStartTimestamp)
            hiitLogger.d(
                "TimerSateViewModel",
                "emitSessionEndState::drift = ${
                    totalElapsedTimeMs - session.durationSeconds.times(
                        1000L
                    )
                }"
            )
            if (session.steps.last() is RestStep) {
                //not counting the last Rest step for aborted session as it doesn't make much sense:
                currentSessionStepIndex -= 1
            }
            val restStepsDone = session.steps
                .take(currentSessionStepIndex + 1) // we want to include the last step
                .filterIsInstance<RestStep>()
            val workingStepsDone = session.steps
                .take(currentSessionStepIndex + 1) // we want to include the last step
                .filterIsInstance<WorkStep>()
            val actualSessionLength =
                if (restStepsDone.isNotEmpty() && workingStepsDone.isNotEmpty()) {
                    restStepsDone.size.times(restStepsDone[0].durationSeconds).plus(
                        workingStepsDone.size.times(workingStepsDone[0].durationSeconds)
                    )
                } else 0L
            hiitLogger.d(
                "TimerSateViewModel",
                "emitSessionEndState::workingStepsDone = ${workingStepsDone.size} | restStepsDone = ${restStepsDone.size} | total steps = ${workingStepsDone.size + restStepsDone.size}"
            )
            hiitLogger.d(
                "TimerSateViewModel",
                "emitSessionEndState::actualSessionLength = $actualSessionLength"
            )
            val actualSessionLengthFormatted = "${actualSessionLength}s"
            //logDriftAnalysis("emitSessionEndState END -> viewstate emission")
            _screenViewState.emit(
                TimerViewState.Finished(totalDuration = actualSessionLengthFormatted)
            )
            for (drift in analysis) {
                hiitLogger.d(
                    "TimerSateViewModel",
                    "time since previous log: ${drift.elapsedTimeSincePrevious} - time since start : ${drift.elapsedTimeSinceStart}  -  ${drift.step}"
                )
            }
        }
    }

    private fun launchSessionStep(stepTimerUseCase: StepTimerUseCase) {
        val stepToStart = session.steps[currentSessionStepIndex]
        logDriftAnalysis("launchSessionStep START ${stepToStart::class.java.simpleName}")
        val stepDurationS = stepToStart.durationSeconds
        stepTimerJob = viewModelScope.launch {
            //logDriftAnalysis("launchSessionStep::stepTimerUseCase.start")
            stepTimerUseCase.start(stepDurationS)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepTimerJob?.cancel()
    }

}
