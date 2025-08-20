package edu.chapman.monsutauoka.services

import edu.chapman.monsutauoka.services.data.DataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StepCounterService(
    private val dataStore: DataStore,
    private val now: () -> Long = { System.currentTimeMillis() }
) {
    private val STEPS_PER_TREAT = 100f
    private val DEFAULT_MOOD = 50f
    private val MOOD_MIN = 0f
    private val MOOD_MAX = 100f
    private val DECAY_PER_MINUTE = 1f

    private val stepsKey = "StepCounterService.steps"
    private val treatsKey = "StepCounterService.treats"
    private val moodKey = "StepCounterService.mood"
    private val lastDecayKey = "StepCounterService.lastDecay"

    // Last absolute sensor reading (session only)
    private var previousCount: Float? = null

    private val _steps: MutableStateFlow<Float>
    private val _treats: MutableStateFlow<Float>
    private val _mood: MutableStateFlow<Float>

    val steps: StateFlow<Float> get() = _steps       // remainder (0..99)
    val treats: StateFlow<Float> get() = _treats
    val mood: StateFlow<Float> get() = _mood         // 0..100

    init {
        val stepsValue = dataStore.load(stepsKey)?.toFloatOrNull()
        val treatsValue = dataStore.load(treatsKey)?.toFloatOrNull()
        val moodValue = dataStore.load(moodKey)?.toFloatOrNull()
        val lastDecay = dataStore.load(lastDecayKey)?.toLongOrNull()

        _steps = MutableStateFlow(stepsValue ?: 0f)
        _treats = MutableStateFlow(treatsValue ?: 0f)
        _mood = MutableStateFlow((moodValue ?: DEFAULT_MOOD).coerceIn(MOOD_MIN, MOOD_MAX))

        if (lastDecay != null) applyDecaySince(lastDecay, now())
        setDecayCheckpoint()
        persistAll()
    }

    // ---- Public API ----

    fun addTreats(count: Int) {
        if (count <= 0) return
        _treats.value += count
        dataStore.save(treatsKey, _treats.value.toString())
    }

    fun consumeTreat() = consumeTreats(1)

    fun consumeTreats(count: Int) {
        if (count <= 0) return
        val available = _treats.value.toInt()
        if (available <= 0) return
        val toConsume = count.coerceAtMost(available)
        _treats.value -= toConsume
        dataStore.save(treatsKey, _treats.value.toString())
        bumpMood(toConsume * 10f)
    }

    /**
     * Feed absolute step counter readings. Converts delta->remainder->treats.
     */
    fun updateSteps(newCount: Float) {
        if (previousCount == null) {
            previousCount = newCount
            return
        }
        val delta = newCount - (previousCount ?: newCount)
        previousCount = newCount
        if (delta <= 0f) return

        _steps.value += delta
        while (_steps.value >= STEPS_PER_TREAT) {
            _steps.value -= STEPS_PER_TREAT
            _treats.value += 1f
        }

        dataStore.save(stepsKey, _steps.value.toString())
        dataStore.save(treatsKey, _treats.value.toString())
    }

    fun decayOnce(amount: Float = DECAY_PER_MINUTE) {
        if (amount <= 0f) return
        bumpMood(-amount)
        setDecayCheckpoint()
    }

    fun setDecayCheckpoint() {
        dataStore.save(lastDecayKey, now().toString())
    }

    // ---- Internals ----

    private fun bumpMood(delta: Float) {
        val newValue = (_mood.value + delta).coerceIn(MOOD_MIN, MOOD_MAX)
        if (newValue != _mood.value) {
            _mood.value = newValue
            dataStore.save(moodKey, _mood.value.toString())
        }
    }

    private fun applyDecaySince(last: Long, current: Long) {
        if (current <= last) return
        val minutes = ((current - last) / 60_000L).toInt()
        if (minutes <= 0) return
        bumpMood(-(minutes * DECAY_PER_MINUTE))
    }

    private fun persistAll() {
        dataStore.save(stepsKey, _steps.value.toString())
        dataStore.save(treatsKey, _treats.value.toString())
        dataStore.save(moodKey, _mood.value.toString())
    }
}
