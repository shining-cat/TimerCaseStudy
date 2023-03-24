package fr.shining_cat.timer_case_study.domain.models

enum class TimerSessionStepType { WORK, REST, PREPARE }

sealed class TimerSessionStep(
    open val durationSeconds: Int,
    open val remainingSessionDurationSecondsAfterMe: Int,
    open val countDownLengthSeconds: Int
) {

    data class PrepareStep(
        override val durationSeconds: Int,
        override val remainingSessionDurationSecondsAfterMe: Int,
        override val countDownLengthSeconds: Int
    ) : TimerSessionStep(
        durationSeconds,
        remainingSessionDurationSecondsAfterMe,
        countDownLengthSeconds
    )

    data class WorkStep(
        override val durationSeconds: Int,
        override val remainingSessionDurationSecondsAfterMe: Int,
        override val countDownLengthSeconds: Int
    ) : TimerSessionStep(
        durationSeconds,
        remainingSessionDurationSecondsAfterMe,
        countDownLengthSeconds
    )

    data class RestStep(
        override val durationSeconds: Int,
        override val remainingSessionDurationSecondsAfterMe: Int,
        override val countDownLengthSeconds: Int
    ) : TimerSessionStep(
        durationSeconds,
        remainingSessionDurationSecondsAfterMe,
        countDownLengthSeconds
    )
}