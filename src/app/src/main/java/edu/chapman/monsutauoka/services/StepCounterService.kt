package edu.chapman.monsutauoka.services

import edu.chapman.monsutauoka.services.data.DataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StepCounterService(val dataStore: DataStore) {
    private val key: String
    private var previousCount: Float? = null

    private val _steps: MutableStateFlow<Float>
    private val _treats: MutableStateFlow<Float>
    private val _mood: MutableStateFlow<Float>
    val steps: StateFlow<Float> get() = _steps
    val treats: StateFlow<Float> get() = _treats
    val mood: StateFlow<Float> get() = _mood

    init {
        // "StepCounterService.steps"
        key = "${this::class.simpleName}.${::steps.name}"

        var stepsValue = dataStore.load(key)?.toFloatOrNull()
        var treatsValue = dataStore.load(key)?.toFloatOrNull()
        var moodValue = dataStore.load(key)?.toFloatOrNull()

        _steps = MutableStateFlow(stepsValue ?: 0f)
        _treats = MutableStateFlow(treatsValue ?: 0f)
        _mood = MutableStateFlow(moodValue ?: 0f)

    }

    fun consumeTreat() {
        if (_treats.value >= 1) {
            _treats.value -= 1
            _mood.value += 10
        }
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