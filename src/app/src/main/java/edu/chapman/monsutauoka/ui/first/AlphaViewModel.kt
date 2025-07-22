package edu.chapman.monsutauoka.ui.first

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import edu.chapman.monsutauoka.services.StepCounterService

class AlphaViewModel : ViewModel() {
    private var initialized = false

    private lateinit var _steps: LiveData<Float>
    val steps: LiveData<Float> get() = _steps

    private lateinit var _treats: LiveData<Float>
    val treats: LiveData<Float> get() = _treats

    private lateinit var _mood: LiveData<Float>
    val mood: LiveData<Float> get() = _mood

    private lateinit var stepService: StepCounterService

    fun initialize(service: StepCounterService) {
        if (initialized) {
            throw IllegalStateException("StepViewModel is already initialized")
        }

        stepService = service

        _steps = service.steps.asLiveData()
        _treats = service.treats.asLiveData()
        _mood = service.mood.asLiveData()

        initialized = true
    }

    fun consumeTreat() {
        stepService.consumeTreat()
    }
}