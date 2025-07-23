package edu.chapman.monsutauoka.ui.first

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import edu.chapman.monsutauoka.MainActivity
import edu.chapman.monsutauoka.databinding.FragmentAlphaBinding
import edu.chapman.monsutauoka.extensions.TAG
import edu.chapman.monsutauoka.extensions.applySystemBarPadding
import edu.chapman.monsutauoka.services.StepCounterService


class AlphaFragment : Fragment() {

    private var _binding: FragmentAlphaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlphaViewModel by viewModels()

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
            binding.textSteps.text = "Steps / 100: " + stepCount.toString()
        }

        viewModel.treats.observe(viewLifecycleOwner) { treatCount ->
            binding.textTreats.text = "Treats: " + treatCount.toString()
        }

        viewModel.mood.observe(viewLifecycleOwner) { moodCount ->
            binding.textMood.text = "Mood: " + moodCount.toString()
        }

        binding.buttonTreat.setOnClickListener {
            viewModel.consumeTreat()
        }

        viewModel.isAsleep.observe(viewLifecycleOwner) { asleep ->
            if (asleep) {
                binding.textSleepState.text = "Your Pokémon is sleeping..."
                // Optionally update an image too:
                // binding.imagePokemon.setImageResource(R.drawable.pokemon_sleeping)
            } else {
                binding.textSleepState.text = "Your Pokémon is awake!"
                // binding.imagePokemon.setImageResource(R.drawable.pokemon_awake)
            }
        }

    }


    override fun onDestroyView() {
        Log.d(TAG, ::onDestroyView.name)

        super.onDestroyView()
        _binding = null
    }
}