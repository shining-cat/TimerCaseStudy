package fr.shining_cat.timer_case_study.ui.timer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TimerScreen(
    viewModel: TimerSateViewModel = hiltViewModel()
) {
    val screenViewState = viewModel.screenViewState.collectAsState().value
    //
    Column(
        Modifier
            .padding(64.dp)
            .fillMaxSize()
    ) {
        Button(onClick = { viewModel.startUseCaseTimerV1() }) {
            Text(text = "launch V1")
        }
        Button(onClick = { viewModel.startUseCaseTimerV2() }) {
            Text(text = "launch V2")
        }
        Button(onClick = { viewModel.startUseCaseTimerV3() }) {
            Text(text = "launch V3")
        }
        Divider(Modifier.height(1.dp))
        Button(onClick = { viewModel.startUseCaseTimerV1WholeSessionShort() }) {
            Text(text = "launch V1 whole session SHORT")
        }
        Button(onClick = { viewModel.startUseCaseTimerV1WholeSessionLong() }) {
            Text(text = "launch V1 whole session LONG")
        }
        Divider(Modifier.height(1.dp))
        Button(onClick = { viewModel.startUseCaseTimerV2WholeSessionShort() }) {
            Text(text = "launch V2 whole session SHORT")
        }
        Button(onClick = { viewModel.startUseCaseTimerV2WholeSessionLong() }) {
            Text(text = "launch V2 whole session LONG")
        }
        Divider(Modifier.height(1.dp))
        Button(onClick = { viewModel.startUseCaseTimerV3WholeSessionShort() }) {
            Text(text = "launch V3 whole session SHORT")
        }
        Button(onClick = { viewModel.startUseCaseTimerV3WholeSessionLong() }) {
            Text(text = "launch V3 whole session LONG")
        }
        Divider(Modifier.height(1.dp))
        Button(onClick = { viewModel.stop() }) {
            Text(text = "STOP")
        }

        Spacer(modifier = Modifier.height(64.dp))
        when (screenViewState) {
            TimerViewState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is TimerViewState.Error -> Text("whoups error ${screenViewState.errorCode}")
            is TimerViewState.Finished -> Text("Finished! total = ${screenViewState.expectedDuration} - real duration = ${screenViewState.realDuration} - drift = ${screenViewState.drift}")
            is TimerViewState.InitialCountDown -> CountDownComponent(
                screenViewState.countDown.secondsDisplay,
                screenViewState.countDown.progress
            )
            is TimerViewState.RestNominal -> StepComponent(
                "REST",
                screenViewState.stepRemainingTime,
                screenViewState.stepProgress,
                screenViewState.totalRemainingTime,
                screenViewState.totalProgress,
                screenViewState.countDown
            )
            is TimerViewState.WorkNominal -> StepComponent(
                "WORK",
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
    type: String,
    stepSeconds: String,
    stepProgress: Float,
    totalSeconds: String,
    totalProgress: Float,
    countDown: CountDown?
) {
    Column(
        Modifier.fillMaxWidth()
    ) {
        Text(text = "$type : $stepSeconds")
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
