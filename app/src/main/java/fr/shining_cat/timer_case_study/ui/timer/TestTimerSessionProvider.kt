package fr.shining_cat.timer_case_study.ui.timer

import fr.shining_cat.timer_case_study.domain.models.TimerSession
import fr.shining_cat.timer_case_study.domain.models.TimerSessionStep

class TestTimerSessionProvider {
    
    fun getTestTimerSession() = TimerSession(
        steps = listOf(
            TimerSessionStep.PrepareStep(
                durationSeconds = 5,
                remainingSessionDurationSecondsAfterMe = 150,
                countDownLengthSeconds = 5
            ),
            TimerSessionStep.RestStep(
                durationSeconds = 10,
                remainingSessionDurationSecondsAfterMe = 140,
                countDownLengthSeconds = 3
            ),
            TimerSessionStep.WorkStep(
                durationSeconds = 20,
                remainingSessionDurationSecondsAfterMe = 120,
                countDownLengthSeconds = 3
            ),
            TimerSessionStep.RestStep(
                durationSeconds = 10,
                remainingSessionDurationSecondsAfterMe = 110,
                countDownLengthSeconds = 3
            ),
            TimerSessionStep.WorkStep(
                durationSeconds = 20,
                remainingSessionDurationSecondsAfterMe = 90,
                countDownLengthSeconds = 3
            ),
            TimerSessionStep.RestStep(
                durationSeconds = 10,
                remainingSessionDurationSecondsAfterMe = 80,
                countDownLengthSeconds = 3
            ),
            TimerSessionStep.WorkStep(
                durationSeconds = 20,
                remainingSessionDurationSecondsAfterMe = 60,
                countDownLengthSeconds = 3
            ),
            TimerSessionStep.RestStep(
                durationSeconds = 10,
                remainingSessionDurationSecondsAfterMe = 50,
                countDownLengthSeconds = 3
            ),
            TimerSessionStep.WorkStep(
                durationSeconds = 20,
                remainingSessionDurationSecondsAfterMe = 30,
                countDownLengthSeconds = 3
            ),
            TimerSessionStep.RestStep(
                durationSeconds = 10,
                remainingSessionDurationSecondsAfterMe = 20,
                countDownLengthSeconds = 3
            ),
            TimerSessionStep.WorkStep(
                durationSeconds = 20,
                remainingSessionDurationSecondsAfterMe = 0,
                countDownLengthSeconds = 3
            )
        ),
        durationSeconds = 155
    )
}