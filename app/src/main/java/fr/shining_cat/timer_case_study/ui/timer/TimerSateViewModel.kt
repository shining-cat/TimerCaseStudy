package fr.shining_cat.timer_case_study.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.shining_cat.timer_case_study.di.DefaultDispatcher
import fr.shining_cat.timer_case_study.domain.models.StepTimerState
import fr.shining_cat.timer_case_study.domain.models.TimerSession
import fr.shining_cat.timer_case_study.domain.usecases.StepTimerUseCase
import fr.shining_cat.timer_case_study.domain.usecases.StepTimerUseCaseV1
import fr.shining_cat.timer_case_study.domain.usecases.StepTimerUseCaseV2
import fr.shining_cat.timer_case_study.domain.usecases.StepTimerUseCaseV3
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
    private val stepTimerUseCaseV3: StepTimerUseCaseV3,
    private val stepTimerUseCaseV2: StepTimerUseCaseV2,
    private val stepTimerUseCaseV1: StepTimerUseCaseV1,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val hiitLogger: HiitLogger
) : ViewModel() {


    private val _screenViewState =
        MutableStateFlow<TimerViewState>(TimerViewState.Loading)
    val screenViewState = _screenViewState.asStateFlow()

    //
    private var session: TimerSession = TestTimerSessionProvider().getTestTimerSessionShort()
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
    fun startUseCaseTimerV3() {
        sessionStartTimestamp = System.currentTimeMillis()
        logDriftAnalysis("startViewModelTimer")
        setupTicker(stepTimerUseCaseV3)
        launchSessionStep(stepTimerUseCaseV3)
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
        val remainingSeconds = stepTimerState.secondsRemaining
        logDriftAnalysis("remainingSeconds = $remainingSeconds TICK ${currentStep::class.java.simpleName}")
        hiitLogger.d("TimerSateViewModel", "tick: remainingSeconds = ${remainingSeconds} ")
        if (remainingSeconds == 0) {//step end
            stepTimerJob?.cancel()
            if (session.steps.lastOrNull() == currentStep) {
                emitSessionEndState()
            } else {
                //session is not finished, increment step index and continue
                //we don't emit any state here as we expect the next step's first state to be emitted immediately
                currentSessionStepIndex += 1
                launchSessionStep(stepTimerUseCase)
            }
        } else {//build running step state and emit
            viewModelScope.launch {
                val currentState = mapper.buildState(
                    session = session,
                    currentSessionStepIndex = currentSessionStepIndex,
                    currentState = stepTimerState
                )
                _screenViewState.emit(currentState)
            }
        }
    }

    private fun emitSessionEndState() {
        viewModelScope.launch {
            stepTimerJob?.cancel() // needed for the "whole session" case
            val totalElapsedTimeMs = (System.currentTimeMillis() - sessionStartTimestamp)
            val drift = totalElapsedTimeMs - session.durationSeconds.times(1000)
            hiitLogger.d("TimerSateViewModel", "emitSessionEndState::DRIFT = ${drift}ms")
            _screenViewState.emit(
                TimerViewState.Finished(
                    expectedDuration = "${session.durationSeconds}s",
                    realDuration = "${totalElapsedTimeMs}ms",
                    drift = "${drift}ms",
                )
            )
            for (driftLogged in analysis) {
                hiitLogger.d(
                    "TimerSateViewModel",
                    "time since previous log: ${driftLogged.elapsedTimeSincePrevious} - time since start : ${driftLogged.elapsedTimeSinceStart}  -  ${driftLogged.step}"
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

    /////////////////////////////////////////
    fun startUseCaseTimerV1WholeSessionShort() {
        sessionStartTimestamp = System.currentTimeMillis()
        logDriftAnalysis("startViewModelTimer")
        setupWholeTicker(stepTimerUseCaseV1)
        launchWholeSession(stepTimerUseCaseV1)
    }
    fun startUseCaseTimerV2WholeSessionShort() {
        sessionStartTimestamp = System.currentTimeMillis()
        logDriftAnalysis("startViewModelTimer")
        setupWholeTicker(stepTimerUseCaseV2)
        launchWholeSession(stepTimerUseCaseV2)
    }
    fun startUseCaseTimerV3WholeSessionShort() {
        sessionStartTimestamp = System.currentTimeMillis()
        logDriftAnalysis("startViewModelTimer")
        setupWholeTicker(stepTimerUseCaseV3)
        launchWholeSession(stepTimerUseCaseV3)
    }
    fun startUseCaseTimerV1WholeSessionLong() {
        session = TestTimerSessionProvider().getTestTimerSessionLong()
        sessionStartTimestamp = System.currentTimeMillis()
        logDriftAnalysis("startViewModelTimer")
        setupWholeTicker(stepTimerUseCaseV1)
        launchWholeSession(stepTimerUseCaseV1)
    }
    fun startUseCaseTimerV2WholeSessionLong() {
        session = TestTimerSessionProvider().getTestTimerSessionLong()
        sessionStartTimestamp = System.currentTimeMillis()
        logDriftAnalysis("startViewModelTimer")
        setupWholeTicker(stepTimerUseCaseV2)
        launchWholeSession(stepTimerUseCaseV2)
    }
    fun startUseCaseTimerV3WholeSessionLong() {
        session = TestTimerSessionProvider().getTestTimerSessionLong()
        hiitLogger.d("TimerSateViewModel", "session = $session")
        sessionStartTimestamp = System.currentTimeMillis()
        logDriftAnalysis("startViewModelTimer")
        setupWholeTicker(stepTimerUseCaseV3)
        launchWholeSession(stepTimerUseCaseV3)
    }

    private fun setupWholeTicker(stepTimerUseCase: StepTimerUseCase) {
        viewModelScope.launch {
            stepTimerUseCase.timerStateFlow.collect() { stepTimerState ->
                if (stepTimerState != StepTimerState()) { //excluding first emission with default value
                    tickWhole(stepTimerState = stepTimerState)
                }
            }
        }
    }

    private fun launchWholeSession(stepTimerUseCase: StepTimerUseCase) {
        val stepToStart = session.steps.first()
        logDriftAnalysis("launchSessionStep START ${stepToStart::class.java.simpleName}")
        val wholeSessionDuration = session.durationSeconds
        stepTimerJob = viewModelScope.launch {
            //logDriftAnalysis("launchSessionStep::stepTimerUseCase.start")
            stepTimerUseCase.start(wholeSessionDuration)
        }
    }

    private fun tickWhole(stepTimerState: StepTimerState) {
        val currentStep = session.steps[currentSessionStepIndex]
        val sessionRemainingSeconds = stepTimerState.secondsRemaining
        logDriftAnalysis("remainingSeconds = $sessionRemainingSeconds TICK ${currentStep::class.java.simpleName}")
        hiitLogger.d("TimerSateViewModel", "tickWhole: sessionRemainingSeconds = $sessionRemainingSeconds - ${currentStep::class.java.simpleName}")
        if (sessionRemainingSeconds == 0) {//whole session end
            if (session.steps.lastOrNull() == currentStep) {
                hiitLogger.d("TimerSateViewModel", "tickWhole: SESSION FINISHED, current step is LAST")
                emitSessionEndState()
            } else {
                hiitLogger.e("TimerSateViewModel", "tickWhole: SESSION FINISHED, current step is NOT LAST")
            }
        } else {//build current running step state and emit
            val timeRemainingTriggerNextStep = currentStep.remainingSessionDurationSecondsAfterMe
            hiitLogger.d("TimerSateViewModel", "tickWhole: timeRemainingTriggerNextStep = $timeRemainingTriggerNextStep")
            if(sessionRemainingSeconds <= timeRemainingTriggerNextStep){
                hiitLogger.d("TimerSateViewModel", "tickWhole: step $currentStep has ended, incrementing currentSessionStepIndex")
                currentSessionStepIndex += 1
            }
            viewModelScope.launch {
                val currentState = mapper.buildStateWholeSession(
                    session = session,
                    currentSessionStepIndex = currentSessionStepIndex,
                    currentState = stepTimerState
                )
                _screenViewState.emit(currentState)
            }
        }
    }


    /////////////////////////////////////////
    override fun onCleared() {
        super.onCleared()
        stepTimerJob?.cancel()
    }

}
