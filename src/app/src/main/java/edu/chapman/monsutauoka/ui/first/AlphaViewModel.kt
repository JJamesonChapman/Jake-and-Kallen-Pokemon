package edu.chapman.monsutauoka.ui.first

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import edu.chapman.monsutauoka.services.StepCounterService
import java.time.LocalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Meal { NONE, BREAKFAST, LUNCH, DINNER }

data class AlphaUiState(
    val isAsleep: Boolean = false,
    val meal: Meal = Meal.NONE,
    val isBedtimeRoutine: Boolean = false
)

/**
 * AlphaViewModel
 *
 * - Exposes steps/treats/mood from StepCounterService as LiveData.
 * - Computes time-of-day UI state (sleep, meals, bedtime routine).
 * - Triggers mood decay once per minute when awake (service persists & clamps mood).
 *
 * NOTE: We keep initialize(service, preferences) to match your existing call sites.
 *       SharedPreferences is no longer used for mood (moved to StepCounterService).
 */
class AlphaViewModel : ViewModel() {
    private var initialized = false

    private lateinit var stepService: StepCounterService
    @Suppress("UNUSED_PARAMETER")
    private lateinit var prefs: SharedPreferences // kept only to preserve the API

    // Flows from service exposed as LiveData
    private lateinit var _steps: LiveData<Float>
    val steps: LiveData<Float> get() = _steps

    private lateinit var _treats: LiveData<Float>
    val treats: LiveData<Float> get() = _treats

    private lateinit var _mood: LiveData<Float>
    val mood: LiveData<Float> get() = _mood

    // Derived UI state (sleep, meals, bedtime)
    private val _ui = MutableLiveData(AlphaUiState())
    val ui: LiveData<AlphaUiState> get() = _ui

    fun initialize(service: StepCounterService, preferences: SharedPreferences) {
        if (initialized) return

        stepService = service
        prefs = preferences // no longer used for mood

        _steps = stepService.steps.asLiveData()
        _treats = stepService.treats.asLiveData()
        _mood = stepService.mood.asLiveData()

        // Kick off periodic tasks
        updateTimeOfDayState()
        startTimeOfDayTicker()
        startMoodDecayTicker()

        initialized = true
    }

    // --- Public actions for UI ---

    fun consumeTreat() = stepService.consumeTreat()

    fun consumeManyTreats(n: Int) = stepService.consumeTreats(n)

    // --- Internals ---

    // Recompute time-of-day state every 30s
    private fun startTimeOfDayTicker() {
        viewModelScope.launch {
            while (true) {
                delay(30_000L)
                updateTimeOfDayState()
            }
        }
    }

    private fun updateTimeOfDayState() {
        val now = LocalTime.now()
        val asleep = now >= LocalTime.of(21, 0) || now < LocalTime.of(6, 0)     // 9pm–6am
        val bedtimeRoutine = now >= LocalTime.of(20, 30) && now < LocalTime.of(21, 0) // 8:30–9pm
        val meal = when {
            now >= LocalTime.of(7, 0) && now < LocalTime.of(7, 30) -> Meal.BREAKFAST
            now >= LocalTime.of(12, 0) && now < LocalTime.of(12, 30) -> Meal.LUNCH
            now >= LocalTime.of(18, 0) && now < LocalTime.of(18, 30) -> Meal.DINNER
            else -> Meal.NONE
        }
        _ui.value = AlphaUiState(isAsleep = asleep, meal = meal, isBedtimeRoutine = bedtimeRoutine)
    }

    // Decay mood once per minute when awake. When asleep, we reset the decay checkpoint so
    // you don't get penalized overnight; the service will persist all of this.
    private fun startMoodDecayTicker() {
        viewModelScope.launch {
            while (true) {
                delay(60_000L)
                val asleep = _ui.value?.isAsleep == true
                if (asleep) {
                    stepService.setDecayCheckpoint()
                } else {
                    stepService.decayOnce()
                }
            }
        }
    }

    fun awardTreats(n: Int) {
        stepService.addTreats(n)
    }
}
