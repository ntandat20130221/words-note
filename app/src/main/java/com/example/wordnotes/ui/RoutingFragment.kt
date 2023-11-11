package com.example.wordnotes.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wordnotes.R
import com.example.wordnotes.WordNotesApplication
import com.example.wordnotes.data.repositories.DefaultUserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RoutingFragment : Fragment(), BottomNavHideable {
    private val isSignedIn
        get() = ((requireContext().applicationContext as WordNotesApplication).appContainer.userRepository as DefaultUserRepository).isSignedIn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Firebase.auth.currentUser != null || isSignedIn) {
            findNavController().apply {
                graph.setStartDestination(R.id.words_fragment)
                navigate(RoutingFragmentDirections.actionRoutingFragmentToWordsFragment())
            }
        } else {
            findNavController().navigate(RoutingFragmentDirections.actionRoutingFragmentToAuthFlow())
        }
    }
}