package com.example.aivideonote.screen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aivideonote.data.MCQ
import com.example.aivideonote.data.Resource
import com.example.aivideonote.domain.RepositoryApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeminiViewModel @Inject constructor(
    private val repo: RepositoryApi,
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<MCQ>>>(Resource.Success(emptyList()))
    val state = _state.asStateFlow()

    var mcqList by mutableStateOf<List<MCQ>>(emptyList())
        private set
    var difficulty by mutableStateOf("easy")
    var buttonClicked by mutableIntStateOf(0)
    var prompt by mutableStateOf("")
    var camera by mutableStateOf(false)

    fun askGemini(text: String = " ") {

        viewModelScope.launch {
            _state.value = Resource.Loading()
            val res = repo.getResponse(prompt + text)
            Log.d("MCQLIST", "askGemini: $res")
            res.onSuccess {
                try {
                    val json = extractJsonArray(it)
                    val list = parseResponse(json)
                    mcqList = list
                    _state.value = Resource.Success(list)
                    Log.d("ThisIsText", "askGemini: ${state.value}")
                } catch (e: Exception) {
                    _state.value = Resource.Error(e.message ?: "Something went wrong")
                }
            }.onFailure {
                _state.value = Resource.Error(it.message ?: "Something went wrong")
            }
        }
    }

    fun selectOption(mcqIndex: Int, selected: String) {
        mcqList = mcqList.toMutableList().also {
            if (mcqIndex in it.indices) {
                it[mcqIndex] = it[mcqIndex].copy(selectedOption = selected)
            }
        }
    }

    fun resetSelectedOption(mcqIndex: Int) {
        mcqList = mcqList.toMutableList().also {
            if (mcqIndex in it.indices) {
                it[mcqIndex] = it[mcqIndex].copy(selectedOption = null)
            }
        }
    }

    private fun parseResponse(response: String): List<MCQ> {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val type = Types.newParameterizedType(List::class.java, MCQ::class.java)
        val adapter = moshi.adapter<List<MCQ>>(type).lenient()

        return adapter.fromJson(response) ?: emptyList()
    }

    // Extracts the first valid JSON array found in the response
    private fun extractJsonArray(raw: String): String {
        val start = raw.indexOf("[")
        val end = raw.lastIndexOf("]") + 1
        return if (start != -1 && end != -1 && end > start) raw.substring(start, end) else raw
    }

    fun feedback(score: Int, mcqQuant: Int): String {
        val text: Float = (score.toFloat() / mcqQuant.toFloat()) * 100
        return when (text) {
            in 0f..40f -> "Do You Know?\uD83E\uDD28"
            in 41f..75f -> "You Know!!\uD83D\uDE0A"
            in 76f..99f -> "Excellent\uD83D\uDE4C"
            else -> "Master!!\uD83D\uDE47\u200D♂\uFE0F"
        }
    }


}