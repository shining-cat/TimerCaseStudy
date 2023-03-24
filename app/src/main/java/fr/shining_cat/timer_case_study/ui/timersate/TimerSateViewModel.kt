/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.shining_cat.timer_case_study.ui.timersate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import fr.shining_cat.timer_case_study.data.TimerSateRepository
import fr.shining_cat.timer_case_study.ui.timersate.TimerSateUiState.Error
import fr.shining_cat.timer_case_study.ui.timersate.TimerSateUiState.Loading
import fr.shining_cat.timer_case_study.ui.timersate.TimerSateUiState.Success
import javax.inject.Inject

@HiltViewModel
class TimerSateViewModel @Inject constructor(
    private val timerSateRepository: TimerSateRepository
) : ViewModel() {

    val uiState: StateFlow<TimerSateUiState> = timerSateRepository
        .timerSates.map(::Success)
        .catch { Error(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)

    fun addTimerSate(name: String) {
        viewModelScope.launch {
            timerSateRepository.add(name)
        }
    }
}

sealed interface TimerSateUiState {
    object Loading : TimerSateUiState
    data class Error(val throwable: Throwable) : TimerSateUiState
    data class Success(val data: List<String>) : TimerSateUiState
}
