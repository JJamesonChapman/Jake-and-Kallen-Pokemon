package edu.chapman.monsutauoka.ui.first

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import edu.chapman.monsutauoka.MainActivity
import edu.chapman.monsutauoka.R
import edu.chapman.monsutauoka.databinding.FragmentAlphaBinding
import edu.chapman.monsutauoka.extensions.TAG
import edu.chapman.monsutauoka.extensions.applySystemBarPadding
import edu.chapman.monsutauoka.services.StepCounterService

class AlphaFragment : Fragment() {

    private var _binding: FragmentAlphaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlphaViewModel by viewModels()

    private var currentMood: Float = 50f
    private var isCurrentlyAsleep: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val main = requireActivity() as MainActivity
        val service = main.getStepCounterService()
        viewModel.initialize(service)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, ::onCreateView.name)
        _binding = FragmentAlphaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, ::onViewCreated.name)
        binding.root.applySystemBarPadding()

        viewModel.steps.observe(viewLifecycleOwner) { stepCount ->
            binding.textSteps.text = "Steps / 100: $stepCount"
        }

        viewModel.treats.observe(viewLifecycleOwner) { treatCount ->
            binding.textTreats.text = "Treats: $treatCount"
        }

        viewModel.mood.observe(viewLifecycleOwner) { moodCount ->
            binding.textMood.text = "Mood: $moodCount"
            currentMood = moodCount
            updatePokemonImage()
        }

        viewModel.isAsleep.observe(viewLifecycleOwner) { asleep ->
            isCurrentlyAsleep = asleep
            binding.textSleepState.text = if (asleep) "Your Pokémon is sleeping..." else "Your Pokémon is awake!"
            updatePokemonImage()
        }

        binding.buttonTreat.setOnClickListener {
            viewModel.consumeTreat()
        }
    }

    private fun updatePokemonImage() {
        val imageRes = if (isCurrentlyAsleep) {
            R.drawable.pokemon_sleeping
        } else {
            when (currentMood.toInt()) {
                in 81..100 -> R.drawable.pokemon_happy
                in 41..80 -> R.drawable.pokemon_awake
                in 21..40 -> R.drawable.pokemon_angry
                else -> R.drawable.pokemon_sad
            }
        }

        binding.imagePokemon.setImageResource(imageRes)
    }

    override fun onDestroyView() {
        Log.d(TAG, ::onDestroyView.name)
        super.onDestroyView()
        _binding = null
    }
}
