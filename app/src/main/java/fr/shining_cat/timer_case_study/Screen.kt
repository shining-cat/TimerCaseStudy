package fr.shining_cat.timer_case_study

sealed class Screen(val route: String){
    object Home: Screen(route = "home")
}
