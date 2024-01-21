package com.example.wordnotes.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.wordnotes.R
import com.example.wordnotes.databinding.FragmentForgotPasswordBinding
import com.example.wordnotes.ui.BottomNavHideable
import com.example.wordnotes.utils.setUpToolbar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment(), BottomNavHideable {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setListeners()
        observeUiState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpToolbar() {
        binding.toolbar.toolbar.apply {
            findNavController().setUpToolbar(this)
        }
    }

    private fun setListeners() {
        binding.inputEmail.doOnTextChanged { text, _, _, _ ->
            forgotPasswordViewModel.updateEmail(text.toString())
        }

        binding.buttonSend.setOnClickListener {
            forgotPasswordViewModel.send()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                forgotPasswordViewModel.uiState.collect { uiState ->
                    uiState.message?.let { message ->
                        showSnackBar(message)
                    }
                    toggleCircularLoading(isLoading = uiState.isSending)
                    if (uiState.resetPasswordSuccessful) {
                        binding.buttonSend.apply {
                            setText(R.string.sent)
                            isEnabled = false
                        }
                    }
                    if (binding.inputEmail.text.toString() != uiState.email) {
                        binding.inputEmail.setText(uiState.email)
                    }
                }
            }
        }
    }

    private fun toggleCircularLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonSend.visibility = View.INVISIBLE
        } else {
            binding.buttonSend.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun showSnackBar(@StringRes messageResId: Int) {
        Snackbar.make(binding.root, messageResId, Snackbar.LENGTH_SHORT).show()
        forgotPasswordViewModel.snackBarShown()
    }
}