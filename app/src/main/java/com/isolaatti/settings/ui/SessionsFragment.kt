package com.isolaatti.settings.ui

import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.isolaatti.R
import com.isolaatti.common.generic_items_list.GenericItemsListRecyclerViewAdapter
import com.isolaatti.common.generic_items_list.GenericListItem
import com.isolaatti.databinding.FragmentSettingsSessionsBinding
import com.isolaatti.settings.presentation.SessionsViewModel
import com.isolaatti.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class SessionsFragment : Fragment() {
    lateinit var viewBinding: FragmentSettingsSessionsBinding
    private val viewModel: SessionsViewModel by viewModels()
    private var adapter: GenericItemsListRecyclerViewAdapter<String>? = null

    private val contextBarCallback: ActionMode.Callback = object: ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            requireActivity().menuInflater.inflate(R.menu.images_context_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return true
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when(item?.itemId) {
                R.id.delete_item -> {
                    showDeleteDialog()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter?.deleteMode = false
        }

    }

    private var actionMode: ActionMode? = null

    private fun showDeleteDialog() {
        val sessionsToDelete = adapter?.getSelectedItems()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_sessions)
            .setMessage(R.string.delete_sessions_conf_dialog_message)
            .setPositiveButton(R.string.yes_continue) { _, _ ->
                val ids = sessionsToDelete?.map { it.id }
                if(ids != null) {
                    viewModel.deleteSessions(ids)
                }
                actionMode?.finish()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentSettingsSessionsBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { _, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            viewBinding.toolbar.updatePadding(top = systemBarsInsets.top, left = systemBarsInsets.left, right = systemBarsInsets.right)
            viewBinding.root.updatePadding(bottom = systemBarsInsets.bottom, left = systemBarsInsets.left, right = systemBarsInsets.right)
            insets
        }

        adapter = GenericItemsListRecyclerViewAdapter(
            onClick = {},
            onItemsSelectedCountUpdate = {
                actionMode?.title = getString(R.string.sessions_selected_count, it)
                actionMode?.menu?.findItem(R.id.delete_item)?.isEnabled = it > 0
            },
            onDeleteMode = {
                adapter?.deleteMode = it
                actionMode = requireActivity().startActionMode(contextBarCallback)
            }
        )
        viewBinding.recycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewBinding.recycler.adapter = adapter
        setupObservers()
        setupListeners()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getSessions()
    }

    private fun setupObservers() {
        viewModel.sessions.observe(viewLifecycleOwner) { resource ->
            viewBinding.loading.visibility = View.GONE
            viewBinding.swipeToRefresh.isRefreshing = false
            adapter?.submitList(resource?.map { sessionDto ->
                val item = GenericListItem(
                    sessionDto.id,
                    title = "${if(sessionDto.current) getString(R.string.current) else ""} ${sessionDto.userAgent}",
                    subtitle = "${sessionDto.ip} - ${sessionDto.date.format(
                    DateTimeFormatter.ISO_DATE_TIME)}")
                item.disabled = sessionDto.current
                item
            })
        }

        viewModel.loading.observe(viewLifecycleOwner) {loading ->
            if(!viewBinding.swipeToRefresh.isRefreshing) {
                viewBinding.loading.visibility = if(loading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupListeners() {
        viewBinding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewBinding.swipeToRefresh.setOnRefreshListener {
            viewModel.getSessions()
        }
    }
}