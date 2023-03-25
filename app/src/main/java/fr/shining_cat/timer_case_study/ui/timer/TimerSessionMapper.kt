package fr.shining_cat.timer_case_study.ui.timer

import fr.shining_cat.timer_case_study.di.DefaultDispatcher
import fr.shining_cat.timer_case_study.domain.models.StepTimerState
import fr.shining_cat.timer_case_study.domain.models.TimerSession
import fr.shining_cat.timer_case_study.domain.models.TimerSessionStep
import fr.shining_cat.timer_case_study.utils.HiitLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TimerSessionMapper @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val hiitLogger: HiitLogger
) {

    suspend fun buildState(
        session: TimerSession,
        currentSessionStepIndex: Int,
        currentState: StepTimerState
    ): TimerViewState {
        return withContext(defaultDispatcher) {
            val currentStep = session.steps[currentSessionStepIndex]
            val remainingSeconds = currentState.secondsRemaining
            val countdownS = currentStep.countDownLengthSeconds
            val countDown = if (remainingSeconds <= countdownS) {
                CountDown(
                    secondsDisplay = "${currentState.secondsRemaining}s",
                    progress = remainingSeconds.div(countdownS.toFloat())
                )
            } else null
            val stepRemainingSeconds = currentState.secondsRemaining
            val stepRemainingSecondsString = "${stepRemainingSeconds}s"
            val sessionRemainingSeconds = stepRemainingSeconds + currentStep.remainingSessionDurationSecondsAfterMe
            val sessionRemainingSecondsString = "${sessionRemainingSeconds}s"
              
            val sessionRemainingPercentage = sessionRemainingSeconds.div(session.durationSeconds.toFloat())
            when (currentStep) {
                is TimerSessionStep.WorkStep -> TimerViewState.WorkNominal(
                    stepRemainingTime = stepRemainingSecondsString,
                    stepProgress = currentState.remainingPercentage,
                    totalRemainingTime = sessionRemainingSecondsString,
                    totalProgress = sessionRemainingPercentage,
                    countDown = countDown,
                )
                is TimerSessionStep.RestStep -> TimerViewState.RestNominal(
                    stepRemainingTime = stepRemainingSecondsString,
                    stepProgress = currentState.remainingPercentage,
                    totalRemainingTime = sessionRemainingSecondsString,
                    totalProgress = sessionRemainingPercentage,
                    countDown = countDown,
                )
                is TimerSessionStep.PrepareStep -> {
                    if (countDown == null) {
                        TimerViewState.Error("LAUNCH_SESSION")
                    } else {
                        TimerViewState.InitialCountDown(countDown = countDown)
                    }
                }
            }
        }
    }

    suspend fun buildStateWholeSession(
        session: TimerSession,
        currentSessionStepIndex: Int,
        currentState: StepTimerState
    ): TimerViewState {
        return withContext(defaultDispatcher) {
            val currentStep = session.steps[currentSessionStepIndex]
            val stepRemainingSeconds = currentState.secondsRemaining.minus(currentStep.remainingSessionDurationSecondsAfterMe)
            val stepRemainingPercentage = stepRemainingSeconds.div(currentStep.durationSeconds.toFloat())
            val stepRemainingSecondsString = "${stepRemainingSeconds}s"
            val countdownS = currentStep.countDownLengthSeconds
            val countDown = if (stepRemainingSeconds <= countdownS) {
                CountDown(
                    secondsDisplay = "${stepRemainingSeconds}s",
                    progress = stepRemainingSeconds.div(countdownS.toFloat())
                )
            } else null
            val sessionRemainingSeconds = currentState.secondsRemaining
            val sessionRemainingSecondsString = "${sessionRemainingSeconds}s"
            when (currentStep) {
                is TimerSessionStep.WorkStep -> TimerViewState.WorkNominal(
                    stepRemainingTime = stepRemainingSecondsString,
                    stepProgress = stepRemainingPercentage,
                    totalRemainingTime = sessionRemainingSecondsString,
                    totalProgress = currentState.remainingPercentage,
                    countDown = countDown,
                )
                is TimerSessionStep.RestStep -> TimerViewState.RestNominal(
                    stepRemainingTime = stepRemainingSecondsString,
                    stepProgress = stepRemainingPercentage,
                    totalRemainingTime = sessionRemainingSecondsString,
                    totalProgress = currentState.remainingPercentage,
                    countDown = countDown,
                )
                is TimerSessionStep.PrepareStep -> {
                    if (countDown == null) {
                        hiitLogger.e("TimerSessionMapper", "buildStateWholeSession::currentStep = $currentStep")
                        TimerViewState.Error("LAUNCH_SESSION")
                    } else {
                        TimerViewState.InitialCountDown(countDown = countDown)
                    }
                }
            }
        }
    }
}