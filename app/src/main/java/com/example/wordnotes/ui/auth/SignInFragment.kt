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
import androidx.navigation.fragment.findNavController
import com.example.wordnotes.R
import com.example.wordnotes.databinding.FragmentSignInBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private val signInViewModel: SignInViewModel by viewModels()

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

        binding.textForgotPassword.setOnClickListener {
            navigateToForgotPasswordFragment()
        }

        binding.textSignUp.setOnClickListener {
            navigateToSignUpFragment()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                signInViewModel.uiState.collect { uiState ->
                    if (uiState.isSignInSuccess) {
                        navigateToRoutingFragment()
                    }
                    uiState.message?.let { message ->
                        showSnackBar(message)
                    }
                    toggleCircularLoading(uiState.isRequesting)
                }
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.apply {
            getLiveData(IS_CLEAR, false).observe(viewLifecycleOwner) { isClear ->
                if (isClear) {
                    binding.inputEmail.text = null
                    binding.inputPassword.text = null
                    set(IS_CLEAR, false)
                }
            }
        }
    }

    /**
     * Navigate to RoutingFragment by reinflating the navigation graph whose start destination is RoutingFragment.
     */
    private fun navigateToRoutingFragment() {
        findNavController().apply {
            graph = navInflater.inflate(R.navigation.nav_graph)
        }
    }

    private fun navigateToForgotPasswordFragment() {
        findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToForgotPasswordFragment())
    }

    private fun navigateToSignUpFragment() {
        findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToSignUpFragment())
    }

    private fun toggleCircularLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonSignIn.visibility = View.INVISIBLE
        } else {
            binding.buttonSignIn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun showSnackBar(@StringRes messageResId: Int) {
        Snackbar.make(requireView(), messageResId, Snackbar.LENGTH_SHORT).show()
        signInViewModel.snakeBarShown()
    }

    companion object {
        const val IS_CLEAR = "is_clear"
    }
}