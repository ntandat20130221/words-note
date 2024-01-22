package com.example.wordnotes.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wordnotes.R
import com.example.wordnotes.data.FirebaseAuthWrapper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RoutingFragment : Fragment() {

    @Inject
    lateinit var firebaseAuthWrapper: FirebaseAuthWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!firebaseAuthWrapper.isLoggedIn()) {
            findNavController().apply {
                graph.setStartDestination(R.id.auth_flow)
                navigate(RoutingFragmentDirections.actionRoutingFragmentToAuthFlow())
            }
        } else {
            findNavController().apply {
                graph.setStartDestination(R.id.home_fragment)
                navigate(RoutingFragmentDirections.actionRoutingFragmentToHomeFragment())
            }
        }
    }
}