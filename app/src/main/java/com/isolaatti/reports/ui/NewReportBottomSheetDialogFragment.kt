package com.isolaatti.reports.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.isolaatti.databinding.BottomSheetNewReportBinding

class NewReportBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetNewReportBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetNewReportBinding.inflate(inflater, container, false)

        return binding.root
    }

    companion object {
        const val LOG_TAG = "NewReportBottomSheetDialogFragment"
        fun newInstance(): NewReportBottomSheetDialogFragment {
            return NewReportBottomSheetDialogFragment()
        }
    }
}