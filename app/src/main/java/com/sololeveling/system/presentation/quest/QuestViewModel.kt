package com.sololeveling.system.presentation.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.domain.repository.QuestRepository
import com.sololeveling.system.domain.usecase.QuestSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestViewModel @Inject constructor(
    private val questRepository: QuestRepository,
    private val questSyncUseCase: QuestSyncUseCase
) : ViewModel() {

    private val _activeQuests = MutableStateFlow<List<Quest>>(emptyList())
    val activeQuests: StateFlow<List<Quest>> = _activeQuests.asStateFlow()

    private val _completedQuests = MutableStateFlow<List<Quest>>(emptyList())
    val completedQuests: StateFlow<List<Quest>> = _completedQuests.asStateFlow()

    init {
        viewModelScope.launch {
            questRepository.getActiveQuests().collectLatest { quests ->
                _activeQuests.value = quests
            }
        }

        viewModelScope.launch {
            questRepository.getCompletedQuests().collectLatest { quests ->
                _completedQuests.value = quests
            }
        }
    }

    fun addProgress(questId: String, amount: Double) {
        viewModelScope.launch {
            questSyncUseCase.manualSyncQuest(questId, amount)
        }
    }
}
