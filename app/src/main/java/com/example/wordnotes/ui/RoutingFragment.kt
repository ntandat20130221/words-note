package com.example.wordnotes.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.wordnotes.R

class RoutingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("TAG", "onCreate: RoutingFragment")
        super.onCreate(savedInstanceState)
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (pref.getBoolean("is_sign_in", false)) {
            findNavController().graph.setStartDestination(R.id.words_fragment)
            findNavController().navigate(RoutingFragmentDirections.actionRoutingFragmentToWordsFragment())
        } else {
            findNavController().navigate(RoutingFragmentDirections.actionRoutingFragmentToAuthFlow())
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("TAG", "onStart: RoutingFragment")
    }
}