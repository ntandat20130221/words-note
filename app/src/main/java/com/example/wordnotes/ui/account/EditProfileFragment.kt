package com.example.wordnotes.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.wordnotes.R
import com.example.wordnotes.WordViewModelFactory
import com.example.wordnotes.databinding.FragmentEditProfileBinding
import com.example.wordnotes.ui.BottomNavHideable
import com.example.wordnotes.utils.setUpToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditProfileFragment : Fragment(), BottomNavHideable {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val editProfileViewModel: EditProfileViewModel by viewModels { WordViewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setListeners()
        observeUiState()
    }

    private fun setUpToolbar() {
        binding.toolbar.toolbar.apply {
            title = getString(R.string.edit_profile)
            inflateMenu(R.menu.edit_profile)
            findNavController().setUpToolbar(this)
        }
    }

    private fun setListeners() {
        binding.inputUsername.doOnTextChanged { text, _, _, _ ->
            editProfileViewModel.updateProfile { currentUser -> currentUser.copy(username = text.toString()) }
        }

        binding.inputEmail.doOnTextChanged { text, _, _, _ ->
            editProfileViewModel.updateProfile { currentUser -> currentUser.copy(email = text.toString()) }
        }

        binding.inputPhone.doOnTextChanged { text, _, _, _ ->
            editProfileViewModel.updateProfile { currentUser -> currentUser.copy(phone = text.toString()) }
        }

        binding.inputGender.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.choose_gender))
                .setSingleChoiceItems(R.array.gender, editProfileViewModel.genderIndex) { _, which ->
                    editProfileViewModel.genderIndex = which
                }
                .setPositiveButton(R.string.ok) { _, _ ->
                    editProfileViewModel.updateProfile { currentUser ->
                        currentUser.copy(gender = requireContext().resources.getStringArray(R.array.gender)[editProfileViewModel.genderIndex])
                    }
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.inputDob.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().build().apply {
                addOnPositiveButtonClickListener {
                    val dateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                    editProfileViewModel.updateProfile { currentUser -> currentUser.copy(dob = dateString) }
                }
            }

            datePicker.show(parentFragmentManager, null)
        }

        binding.toolbar.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_done -> {
                    editProfileViewModel.commitChange()
                    true
                }

                else -> false
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                editProfileViewModel.uiState.collect { uiState ->
                    binding.apply {
                        if (inputUsername.text.toString() != uiState.user.username) inputUsername.setText(uiState.user.username)
                        if (inputEmail.text.toString() != uiState.user.email) inputEmail.setText(uiState.user.email)
                        if (inputPhone.text.toString() != uiState.user.phone) inputPhone.setText(uiState.user.phone)
                        if (inputGender.text.toString() != uiState.user.gender) inputPhone.setText(uiState.user.gender)
                        if (inputDob.text.toString() != uiState.user.dob) inputPhone.setText(uiState.user.dob)
                    }
                }
            }
        }
    }
}