package edu.chapman.monsutauoka.ui.first

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import edu.chapman.monsutauoka.services.StepCounterService

import androidx.lifecycle.MutableLiveData
import java.time.LocalTime

class AlphaViewModel : ViewModel() {
    private var initialized = false

    private lateinit var _steps: LiveData<Float>
    val steps: LiveData<Float> get() = _steps

    private lateinit var _treats: LiveData<Float>
    val treats: LiveData<Float> get() = _treats

    //private lateinit var _mood: LiveData<Float>
    private val _mood = MutableLiveData<Float>(50f) // Or starting mood value
    val mood: LiveData<Float> get() = _mood

    private lateinit var stepService: StepCounterService

    private val _isAsleep = MutableLiveData<Boolean>()
    val isAsleep: LiveData<Boolean> get() = _isAsleep

    fun initialize(service: StepCounterService) {
        if (initialized) {
            throw IllegalStateException("StepViewModel is already initialized")
        }

        stepService = service

        _steps = service.steps.asLiveData()
        _treats = service.treats.asLiveData()
        //_mood = service.mood.asLiveData()

        checkSleepState() // Initialize sleep state

        initialized = true
    }

    fun consumeTreat() {
        stepService.consumeTreat()
        updateMood(1)
    }

    private fun checkSleepState() {
        /*val now = LocalTime.now()
        val nightStart = LocalTime.of(21, 0) // 9 PM
        val nightEnd = LocalTime.of(6, 0)    // 6 AM
        val asleep = now.isAfter(nightStart) || now.isBefore(nightEnd)
        _isAsleep.value = asleep*/
        val now = LocalTime.now()
        val nightStart = LocalTime.of(21, 0) // 9 PM
        val nightEnd = LocalTime.of(6, 0)    // 6 AM
        _isAsleep.value = now.isAfter(nightStart) || now.isBefore(nightEnd)
    }

    private fun updateMood(delta: Int) {
        val currentMood = _mood.value ?: 0f
        val newMood = (currentMood + delta).coerceIn(0f, 100f)
        _mood.value = newMood
    }
}