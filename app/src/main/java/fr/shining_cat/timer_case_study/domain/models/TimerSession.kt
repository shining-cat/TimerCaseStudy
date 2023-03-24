package fr.shining_cat.timer_case_study.domain.models

data class TimerSession(
    val steps: List<TimerSessionStep>,
    val durationSeconds: Int
)