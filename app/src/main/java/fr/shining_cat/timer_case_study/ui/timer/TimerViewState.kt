package fr.shining_cat.timer_case_study.ui.timer

sealed class TimerViewState {

    object Loading : TimerViewState()

    data class InitialCountDown(val countDown: CountDown) : TimerViewState()

    data class WorkNominal(
        val stepRemainingTime: String,
        val stepProgress: Float,
        val totalRemainingTime: String,
        val totalProgress: Float,
        val countDown: CountDown? = null
    ) : TimerViewState()

    data class RestNominal(
        val stepRemainingTime: String,
        val stepProgress: Float,
        val totalRemainingTime: String,
        val totalProgress: Float,
        val countDown: CountDown? = null
    ) : TimerViewState()

    data class Finished(val totalDuration: String) : TimerViewState()

    data class Error(val errorCode: String) : TimerViewState()
}

data class CountDown(
    val secondsDisplay: String,
    val progress: Float
)
