package com.isolaatti.reports.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.isolaatti.R
import com.isolaatti.databinding.BottomSheetNewReportBinding
import com.isolaatti.reports.data.ContentType
import com.isolaatti.reports.presentation.CreateReportViewModel
import com.isolaatti.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewReportBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetNewReportBinding
    private val viewModel: CreateReportViewModel by viewModels()

    private lateinit var contentType: ContentType
    private lateinit var contentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentType = arguments?.getSerializable(ARG_CONTENT_TYPE) as ContentType
        contentId = arguments?.getString(ARG_CONTENT_ID)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetNewReportBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ArrayAdapter.createFromResource(requireContext(), R.array.report_reasons, android.R.layout.simple_spinner_item).also { spinnerAdapter ->
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.reportReasons.adapter = spinnerAdapter
        }

        binding.reportUserComment.editText?.doOnTextChanged { text, start, before, count ->
            binding.submitReportButton.isEnabled = count > 0
        }

        binding.submitReportButton.setOnClickListener {
            viewModel.createReport(binding.reportReasons.selectedItemPosition, binding.reportUserComment.editText?.text.toString(), contentType, contentId)
        }

        viewModel.createReportResponse.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Error -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    Toast.makeText(requireContext(), R.string.report_submitted_successfully, Toast.LENGTH_SHORT).show()
                    dialog?.dismiss()
                }
            }
        }
    }

    companion object {
        const val LOG_TAG = "NewReportBottomSheetDialogFragment"
        const val ARG_CONTENT_TYPE = "contentType"
        const val ARG_CONTENT_ID = "contentId"
        fun newInstance(contentType: ContentType, contentId: String): NewReportBottomSheetDialogFragment {
            return NewReportBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CONTENT_TYPE, contentType)
                    putString(ARG_CONTENT_ID, contentId)
                }
            }
        }
    }
}