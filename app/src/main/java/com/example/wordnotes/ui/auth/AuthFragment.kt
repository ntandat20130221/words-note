package com.example.wordnotes.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.wordnotes.R
import com.example.wordnotes.databinding.FragmentAuthBinding
import com.google.android.material.tabs.TabLayoutMediator

class AuthFragment : Fragment() {
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager2.adapter = AuthPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Sign in"
                }

                1 -> {
                    tab.text = "Sign up"
                }
            }
        }.attach()

        val startDestination = findNavController().graph.startDestinationId
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.auth_fragment, true)
            .build()
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        pref.edit { putBoolean("is_sign_in", true) }
        findNavController().navigate(startDestination, null, navOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}