package edu.chapman.monsutauoka.ui.third

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import edu.chapman.monsutauoka.databinding.FragmentGammaBinding
import edu.chapman.monsutauoka.ui.first.AlphaViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.math.ceil


class GammaFragment : Fragment() {

    private var _binding: FragmentGammaBinding? = null
    private val binding get() = _binding!!

    // Use the activity-scoped VM so rewards flow through the same service/state
    private val sharedVm: AlphaViewModel by activityViewModels()

    private var tapCount = 0
    private var gameJob: Job? = null
    private var running = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGammaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Round config
        var targetTaps = 100
        val roundMs = 5_000L


        binding.buttonGameStart.setOnClickListener {
            if (running) return@setOnClickListener
            targetTaps = Random.nextInt(1, 30)
            running = true
            tapCount = 0
            binding.textGameStatus.text = "Tap ${targetTaps} times in ${roundMs/1000}s!"
            binding.textGameTaps.text = "Taps: 0/$targetTaps"   // <— show starting count
            binding.buttonGameTap.isEnabled = true
            binding.buttonGameStart.isEnabled = false

            gameJob?.cancel()
            gameJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(roundMs)
                endRound(targetTaps)
            }
        }

        binding.buttonGameTap.setOnClickListener {
            if (!running) return@setOnClickListener
            tapCount++
            binding.textGameTaps.text = "Taps: $tapCount/$targetTaps" // <— live update
            if (tapCount >= targetTaps) {
                gameJob?.cancel()
                endRound(targetTaps)
            }
        }
    }

    private fun endRound(targetTaps: Int) {
        binding.buttonGameTap.isEnabled = false
        binding.buttonGameStart.isEnabled = true

        val won = tapCount >= targetTaps
        if (won) {
            val awarded = ceil(targetTaps.toDouble() / 8).toInt()
            sharedVm.awardTreats(awarded) // MVVM-friendly: award via VM/service
            binding.textGameStatus.text = "You win! +" + awarded.toString() + " Treats 🎉"
        } else {
            binding.textGameStatus.text = "Try again!"
        }
        running = false
    }

    override fun onDestroyView() {
        gameJob?.cancel()
        _binding = null
        super.onDestroyView()
    }
}
