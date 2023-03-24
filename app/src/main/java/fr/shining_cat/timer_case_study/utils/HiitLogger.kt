package fr.shining_cat.timer_case_study.utils


interface HiitLogger {

    fun d(tag: String, msg: String)

    fun e(tag: String, msg: String, throwable: Throwable? = null)
}