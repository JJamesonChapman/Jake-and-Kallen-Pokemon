package edu.chapman.monsutauoka.services

import edu.chapman.monsutauoka.services.data.DataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StepCounterService(val dataStore: DataStore) {
    private val key: String
    private var previousCount: Float? = null

    private val _steps: MutableStateFlow<Float>
    private val _treats: MutableStateFlow<Float>
    val steps: StateFlow<Float> get() = _steps
    val treats: StateFlow<Float> get() = _treats

    init {
        // "StepCounterService.steps"
        key = "${this::class.simpleName}.${::steps.name}"

        var stepsValue = dataStore.load(key)?.toFloatOrNull()
        var treatsValue = dataStore.load(key)?.toFloatOrNull()
        _steps = MutableStateFlow(stepsValue ?: 0f)
        _treats = MutableStateFlow(treatsValue ?: 0f)
    }

    fun updateSteps(newCount: Float) {
        if (previousCount == null) {
            previousCount = newCount
            return
        }

        _steps.value += newCount - previousCount!!
        previousCount = newCount

        if (_steps.value > 100){
            _steps.value -= 100
            _treats.value += 1
        }

        dataStore.save(key, _steps.value.toString())
    }
}