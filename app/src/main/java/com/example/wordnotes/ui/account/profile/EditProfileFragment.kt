package com.example.wordnotes.ui.account.profile

import android.annotation.SuppressLint
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.example.wordnotes.R
import com.example.wordnotes.data.model.getFormattedDob
import com.example.wordnotes.databinding.FragmentEditProfileBinding
import com.example.wordnotes.utils.hideSoftKeyboard
import com.example.wordnotes.utils.setUpToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val editProfileViewModel: EditProfileViewModel by viewModels()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { editProfileViewModel.updateProfileImage(it) }
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpToolbar() {
        binding.toolbar.toolbar.apply {
            inflateMenu(R.menu.edit_profile)
            findNavController().setUpToolbar(this)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        binding.viewImage.setOnClickListener {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.viewAvatarOutline.setOnClickListener {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.inputUsername.doOnTextChanged { text, _, _, _ ->
            editProfileViewModel.updateProfile { currentUser -> currentUser.copy(username = text.toString()) }
        }

        binding.inputEmail.setOnClickListener {
            Snackbar.make(binding.root, R.string.cant_change_email_address, Snackbar.LENGTH_SHORT).show()
        }

        binding.inputPhone.doOnTextChanged { text, _, _, _ ->
            editProfileViewModel.updateProfile { currentUser -> currentUser.copy(phone = text.toString()) }
        }

        binding.inputGender.setOnClickListener {
            var selectedItem = 0
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.choose_gender))
                .setSingleChoiceItems(R.array.gender, editProfileViewModel.getUserGender()) { _, which -> selectedItem = which }
                .setPositiveButton(R.string.ok) { _, _ ->
                    editProfileViewModel.updateProfile { currentUser -> currentUser.copy(gender = selectedItem) }
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.inputDob.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().build().apply {
                addOnPositiveButtonClickListener {
                    editProfileViewModel.updateProfile { currentUser -> currentUser.copy(dob = it) }
                }
            }
            datePicker.show(childFragmentManager, null)
        }

        binding.toolbar.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_done -> {
                    editProfileViewModel.commitChanges()
                    true
                }

                else -> false
            }
        }

        /**
         * Clear EditText focus when clicking other views.
         */
        binding.touchInterceptor.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val view = requireActivity().currentFocus
                if (view is EditText) {
                    val outRect = Rect()
                    view.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        view.clearFocus()
                        requireContext().hideSoftKeyboard(view, 0)
                    }
                }
            }
            false
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                editProfileViewModel.uiState.collect { uiState ->
                    if (uiState.isCommitSuccessful) {
                        findNavController().popBackStack()
                    }

                    binding.apply {
                        if (uiState.isCommitting) {
                            toolbar.toolbar.menu.getItem(0).setActionView(R.layout.layout_commit)
                        } else {
                            toolbar.toolbar.menu.getItem(0).actionView = null
                        }

                        if (inputUsername.text.toString() != uiState.user.username) inputUsername.setText(uiState.user.username)
                        if (inputEmail.text.toString() != uiState.user.email) inputEmail.setText(uiState.user.email)
                        if (inputPhone.text.toString() != uiState.user.phone) inputPhone.setText(uiState.user.phone)

                        if (uiState.user.gender != -1) {
                            inputGender.setText(resources.getStringArray(R.array.gender)[uiState.user.gender])
                        }

                        if (uiState.user.dob > 0) {
                            inputDob.setText(uiState.user.getFormattedDob())
                        }

                        if (uiState.imageUri != Uri.EMPTY) {
                            imageProfile.load(uiState.imageUri)
                        } else if (uiState.user.imageUrl.isNotBlank()) {
                            imageProfile.load(uiState.user.imageUrl) {
                                crossfade(true)
                                placeholder(R.drawable.profile)
                                transformations(CircleCropTransformation())
                            }
                        }
                    }
                }
            }
        }
    }
}