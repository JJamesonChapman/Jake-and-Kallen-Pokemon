package edu.chapman.monsutauoka.ui.second

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import edu.chapman.monsutauoka.MainActivity
import edu.chapman.monsutauoka.databinding.FragmentBetaBinding
import edu.chapman.monsutauoka.ui.first.AlphaViewModel

class BetaFragment : Fragment() {

    private var _binding: FragmentBetaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlphaViewModel by activityViewModels()

    private var treatAmount = 1
    private var currentTreats: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBetaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Ensure VM is initialized (safe to call multiple times)
        val main = requireActivity() as MainActivity
        val service = main.getStepCounterService()
        val prefs = requireContext().getSharedPreferences("PokemonPrefs", Context.MODE_PRIVATE)
        viewModel.initialize(service, prefs)

        // Observe treats/mood via VM
        viewModel.treats.observe(viewLifecycleOwner) { t ->
            currentTreats = t
            binding.textTreatsAvailable.text = "Treats: $t"
            updateFeedButtonState()
        }
        /*viewModel.mood.observe(viewLifecycleOwner) { m ->
            binding.textMood.text = "Mood: ${m.toInt()}"
        }*/

        // Amount UI
        binding.textTreatAmount.text = treatAmount.toString()
        binding.buttonDecreaseTreats.setOnClickListener {
            if (treatAmount > 1) {
                treatAmount--
                binding.textTreatAmount.text = treatAmount.toString()
                updateFeedButtonState()
            }
        }
        binding.buttonIncreaseTreats.setOnClickListener {
            if (treatAmount < 10) {
                treatAmount++
                binding.textTreatAmount.text = treatAmount.toString()
                updateFeedButtonState()
            }
        }

        // Feed N treats at once
        binding.buttonTreat.setOnClickListener {
            viewModel.consumeManyTreats(treatAmount)
        }

        updateFeedButtonState()
    }

    private fun updateFeedButtonState() {
        // enable only if you have at least that many treats
        binding.buttonTreat.isEnabled = currentTreats >= treatAmount
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

