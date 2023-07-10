package com.example.wordnotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wordnotes.databinding.FragmentBottomSheetTemplateBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetTemplateFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentBottomSheetTemplateBinding? = null
    private val binding get() = _binding!!

    override fun getTheme() = R.style.BottomSheetDialogStyle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBottomSheetTemplateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}