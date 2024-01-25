package com.example.wordnotes.ui.account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import coil.load
import coil.transform.CircleCropTransformation
import com.example.wordnotes.R
import com.example.wordnotes.databinding.FragmentAccountBinding
import com.example.wordnotes.ui.MainActivity
import com.example.wordnotes.utils.isNetworkAvailable
import com.example.wordnotes.utils.setUpToolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val accountViewModel: AccountViewModel by viewModels()

    private object OnTouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            return when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.scaleX = 0.98f
                    view.scaleY = 0.98f
                    false
                }

                MotionEvent.ACTION_UP -> {
                    view.scaleX = 1f
                    view.scaleY = 1f
                    false
                }

                else -> false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setListeners()
        observeUiState()
    }

    /**
     * Load user after returned from EditProfileFragment
     */
    override fun onStart() {
        super.onStart()
        accountViewModel.loadUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpToolbar() {
        binding.toolbar.toolbar.apply {
            findNavController().setUpToolbar(
                this,
                AppBarConfiguration(setOf(R.id.home_fragment, R.id.reminder_fragment, R.id.account_fragment))
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        binding.layoutEdit.setOnTouchListener(OnTouchListener)
        binding.layoutReminder.setOnTouchListener(OnTouchListener)
        binding.layoutLogout.setOnTouchListener(OnTouchListener)

        binding.layoutEdit.setOnClickListener {
            if (requireContext().isNetworkAvailable()) {
                navigateToEditProfileFragment()
            } else {
                (requireActivity() as MainActivity).showNoInternetMessage()
            }
        }

        binding.layoutReminder.setOnClickListener {
            findNavController().navigate(AccountFragmentDirections.actionAccountFragmentToReminderFragment())
        }

        binding.layoutLogout.setOnClickListener {
            if (requireContext().isNetworkAvailable()) {
                accountViewModel.logOut()
            } else {
                (requireActivity() as MainActivity).showNoInternetMessage()
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountViewModel.uiState.collect { uiState ->
                    binding.apply {
                        if (uiState.user.imageUrl.isNotBlank()) {
                            imageProfile.load(uiState.user.imageUrl) {
                                crossfade(true)
                                placeholder(R.drawable.profile)
                                transformations(CircleCropTransformation())
                            }
                        }
                        textName.text = uiState.user.username
                        textEmail.text = uiState.user.email
                    }

                    if (uiState.isLogOut) {
                        navigateToRoutingFragment()
                    }
                }
            }
        }
    }

    private fun navigateToEditProfileFragment() {
        findNavController().navigate(AccountFragmentDirections.actionAccountFragmentToEditProfileFragment())
    }

    /**
     * Navigate to RoutingFragment by reinflating the navigation graph whose start destination is RoutingFragment.
     */
    private fun navigateToRoutingFragment() {
        findNavController().apply {
            graph = navInflater.inflate(R.navigation.nav_graph)
        }
    }
}