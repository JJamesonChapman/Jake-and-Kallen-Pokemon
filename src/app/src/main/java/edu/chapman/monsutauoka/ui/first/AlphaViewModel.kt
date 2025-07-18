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

    fun initialize(service: StepCounterService) {
        if (initialized) {
            throw IllegalStateException("StepViewModel is already initialized")
        }

        _steps = service.steps.asLiveData()
        _treats = service.treats.asLiveData()

        initialized = true
    }
}