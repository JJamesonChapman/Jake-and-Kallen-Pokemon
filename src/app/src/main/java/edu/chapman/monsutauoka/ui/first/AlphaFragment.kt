package edu.chapman.monsutauoka.ui.first

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import edu.chapman.monsutauoka.MainActivity
import edu.chapman.monsutauoka.R
import edu.chapman.monsutauoka.databinding.FragmentAlphaBinding
import edu.chapman.monsutauoka.extensions.TAG
import edu.chapman.monsutauoka.extensions.applySystemBarPadding

class AlphaFragment : Fragment() {

    private var _binding: FragmentAlphaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlphaViewModel by activityViewModels()

    private var latestMood: Float = 50f
    private var latestUi: AlphaUiState = AlphaUiState()

    private var treatAmount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val main = requireActivity() as MainActivity
        val service = main.getStepCounterService()

        val prefs = requireContext()
            .getSharedPreferences("PokemonPrefs", Context.MODE_PRIVATE)

        viewModel.initialize(service, prefs)
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

        viewModel.steps.observe(viewLifecycleOwner) { stepRemainder ->
            binding.textSteps.text = "Steps / 100: $stepRemainder"
        }

        viewModel.treats.observe(viewLifecycleOwner) { treatCount ->
            binding.textTreats.text = "Treats: $treatCount"
        }

        viewModel.mood.observe(viewLifecycleOwner) { moodValue ->
            latestMood = moodValue
            binding.textMood.text = "Mood: ${moodValue.toInt()}"
            render()
        }

        viewModel.ui.observe(viewLifecycleOwner) { ui ->
            latestUi = ui
            // keep some flavor text without changing images
            binding.textSleepState.text = when {
                ui.isAsleep -> "Your Pokémon is sleeping..."
                ui.isBedtimeRoutine -> "Getting ready for bed..."
                ui.meal != Meal.NONE -> when (ui.meal) {
                    Meal.BREAKFAST -> "Breakfast time!"
                    Meal.LUNCH -> "Lunch time!"
                    Meal.DINNER -> "Dinner time!"
                    else -> "Mealtime!"
                }
                else -> "Your Pokémon is awake!"
            }
            render()
        }
    }

    private fun render() {
        val resId = if (latestUi.isAsleep) {
            R.drawable.pokemon_sleeping
        } else {
            when (latestMood.toInt()) {
                in 81..100 -> R.drawable.pokemon_happy
                in 41..80 -> R.drawable.pokemon_awake   // neutral/default
                in 21..40 -> R.drawable.pokemon_angry
                else -> R.drawable.pokemon_sad
            }
        }
        binding.imagePokemon.setImageResource(resId)
    }

    override fun onDestroyView() {
        Log.d(TAG, ::onDestroyView.name)
        super.onDestroyView()
        _binding = null
    }
}
