package com.sololeveling.system.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sololeveling.system.domain.model.Quest
import com.sololeveling.system.domain.repository.QuestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailySummary(
    val date: String,
    val total: Int,
    val completed: Int,
    val xpEarned: Long,
    val quests: List<Quest>
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val questRepository: QuestRepository
) : ViewModel() {

    private val _summaries = MutableStateFlow<List<DailySummary>>(emptyList())
    val summaries: StateFlow<List<DailySummary>> = _summaries.asStateFlow()

    init {
        viewModelScope.launch {
            questRepository.getAllQuests().collectLatest { all ->
                _summaries.value = all
                    .groupBy { it.date.ifBlank { "" } }
                    .map { (date, quests) ->
                        DailySummary(
                            date = date,
                            total = quests.size,
                            completed = quests.count { it.isCompleted },
                            xpEarned = quests.filter { it.isCompleted }.sumOf { it.xpReward },
                            quests = quests.sortedBy { it.title }
                        )
                    }
                    .sortedByDescending { it.date }
            }
        }
    }
}
