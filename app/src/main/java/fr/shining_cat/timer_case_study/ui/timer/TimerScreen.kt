package fr.shining_cat.timer_case_study.ui.timer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TimerScreen(
    viewModel: TimerSateViewModel = hiltViewModel()
) {
    val screenViewState = viewModel.screenViewState.collectAsState().value
    //
    Column(Modifier.padding(64.dp).fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.startUseCaseTimerV2() }) {
                Text(text = "launch V1")
            }
            Button(onClick = { viewModel.startUseCaseTimerV2() }) {
                Text(text = "launch V2")
            }
        }
        when (screenViewState) {
            TimerViewState.Loading -> CircularProgressIndicator()
            is TimerViewState.Error -> Text("whoups error ${screenViewState.errorCode}")
            is TimerViewState.Finished -> Text("Finished! total = ${screenViewState.totalDuration}")
            is TimerViewState.InitialCountDown -> CountDownComponent(
                screenViewState.countDown.secondsDisplay,
                screenViewState.countDown.progress
            )
            is TimerViewState.RestNominal -> StepComponent(
                screenViewState.stepRemainingTime,
                screenViewState.stepProgress,
                screenViewState.totalRemainingTime,
                screenViewState.totalProgress,
                screenViewState.countDown
            )
            is TimerViewState.WorkNominal -> StepComponent(
                screenViewState.stepRemainingTime,
                screenViewState.stepProgress,
                screenViewState.totalRemainingTime,
                screenViewState.totalProgress,
                screenViewState.countDown
            )
        }
    }
}

@Composable
fun StepComponent(
    stepSeconds: String,
    stepProgress: Float,
    totalSeconds: String,
    totalProgress: Float,
    countDown: CountDown?
) {
    Column(
        Modifier.fillMaxWidth()
    ) {
        Text(text = "Step: $stepSeconds")
        LinearProgressIndicator(progress = stepProgress)
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Total: $totalSeconds")
        LinearProgressIndicator(totalProgress)
        Spacer(modifier = Modifier.height(32.dp))
        if (countDown != null) {
            CountDownComponent(
                valueSeconds = countDown.secondsDisplay,
                progress = countDown.progress
            )
        }
    }
}


@Composable
fun CountDownComponent(
    valueSeconds: String,
    progress: Float
) {
    Column(Modifier.fillMaxWidth()) {
        Text(text = "Countdown: $valueSeconds")
        LinearProgressIndicator(progress = progress)
    }
}
