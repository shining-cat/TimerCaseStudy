package fr.shining_cat.timer_case_study.ui.timer

import fr.shining_cat.timer_case_study.domain.models.TimerSession
import fr.shining_cat.timer_case_study.domain.models.TimerSessionStep
import java.security.InvalidParameterException

class TestTimerSessionProvider {

    private val restStepLentgh = 10
    private val workStepLentgh = 20
    private val prepareStepLentgh = 15
    private val countDownLength = 5

    fun getTestTimerSessionShort() = TimerSession(
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


    private fun addStep(previousStep: TimerSessionStep):TimerSessionStep{
        return when(previousStep){
            is TimerSessionStep.RestStep -> TimerSessionStep.WorkStep(
                durationSeconds = workStepLentgh,
                remainingSessionDurationSecondsAfterMe = previousStep.remainingSessionDurationSecondsAfterMe.plus(previousStep.durationSeconds),
                countDownLengthSeconds = countDownLength
            )
            is TimerSessionStep.WorkStep -> TimerSessionStep.RestStep(
                durationSeconds = restStepLentgh,
                remainingSessionDurationSecondsAfterMe = previousStep.remainingSessionDurationSecondsAfterMe.plus(previousStep.durationSeconds),
                countDownLengthSeconds = countDownLength
            )
            is TimerSessionStep.PrepareStep -> throw InvalidParameterException("This case should not happen!")
        }
    }
    fun getTestTimerSessionLong():TimerSession{
        val lastStepAddedFirst = TimerSessionStep.WorkStep(
            durationSeconds = workStepLentgh,
            remainingSessionDurationSecondsAfterMe = 0,
            countDownLengthSeconds = countDownLength
        )
        val steps = mutableListOf<TimerSessionStep>(lastStepAddedFirst)
        for(i in 1..101){//odd number to end on the correct type
            val stepToAdd = addStep(steps.last())
            println("getTestTimerSessionLong::stepToAdd = $stepToAdd")
            steps.add(stepToAdd)
        }
        val firstStepAddedLast = TimerSessionStep.PrepareStep(
            durationSeconds = prepareStepLentgh,
            remainingSessionDurationSecondsAfterMe = steps.last().remainingSessionDurationSecondsAfterMe.plus(steps.last().durationSeconds),
            countDownLengthSeconds = countDownLength
        )
        steps.add(firstStepAddedLast)
        steps.reverse()
        println("getTestTimerSessionLong::steps = $steps")

        val totalDuration = firstStepAddedLast.remainingSessionDurationSecondsAfterMe.plus(firstStepAddedLast.durationSeconds)
        return TimerSession(
            steps = steps,
            durationSeconds = totalDuration
        )
    }

}