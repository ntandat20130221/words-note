package com.example.wordnotes.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wordnotes.R
import com.example.wordnotes.WordNotesApplication
import com.example.wordnotes.data.KEY_IS_SIGNED_IN
import kotlinx.coroutines.runBlocking

class RoutingFragment : Fragment(), BottomNavHideable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            when ((requireContext().applicationContext as WordNotesApplication).appContainer.dataStoreRepository.getBoolean(KEY_IS_SIGNED_IN)) {
                true -> {
                    findNavController().apply {
                        graph.setStartDestination(R.id.words_fragment)
                        navigate(RoutingFragmentDirections.actionRoutingFragmentToWordsFragment())
                    }
                }

                else -> findNavController().navigate(RoutingFragmentDirections.actionRoutingFragmentToAuthFlow())
            }
        }
    }
}