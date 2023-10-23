package com.example.wordnotes.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.wordnotes.R
import com.example.wordnotes.WordViewModelFactory
import com.example.wordnotes.databinding.FragmentSignInBinding
import com.example.wordnotes.ui.BottomNavHideable
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SignInFragment : Fragment(), BottomNavHideable {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private val signInViewModel: SignInViewModel by viewModels { WordViewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        observeUiState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListeners() {
        binding.buttonSignIn.setOnClickListener {
            signInViewModel.signIn(
                email = binding.inputEmail.text.toString(),
                password = binding.inputPassword.text.toString()
            )
        }

        binding.textSignUp.setOnClickListener {
            navigateToSignUpFragment()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                signInViewModel.uiState.collect { uiState ->
                    if (uiState.isSignInSuccess) {
                        navigateToStartDestination()
                    }
                    uiState.message?.let { message ->
                        showSnackBar(message)
                    }
                    if (uiState.isLoading) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.buttonSignIn.visibility = View.INVISIBLE
                    } else {
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.buttonSignIn.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun navigateToStartDestination() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.sign_in_fragment, true)
            .build()
        findNavController().navigate(findNavController().graph.startDestinationId, null, navOptions)
    }

    private fun navigateToSignUpFragment() {
        findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToSignUpFragment())
    }

    private fun showSnackBar(@StringRes messageResId: Int) {
        Snackbar.make(requireView(), messageResId, Snackbar.LENGTH_SHORT).show()
        signInViewModel.snakeBarShown()
    }
}