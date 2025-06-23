package com.isolaatti.notifications.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.isolaatti.R
import com.isolaatti.databinding.FragmentNotificationsBinding
import com.isolaatti.notifications.domain.FollowNotification
import com.isolaatti.notifications.domain.LikeNotification
import com.isolaatti.notifications.domain.Notification
import com.isolaatti.notifications.presentation.NotificationsAdapter
import com.isolaatti.notifications.presentation.NotificationsViewModel
import com.isolaatti.posting.viewer.ui.PostViewerActivity
import com.isolaatti.profile.ui.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    companion object {
        fun newInstance() = NotificationsFragment()
    }

    private lateinit var binding: FragmentNotificationsBinding
    private val viewModel: NotificationsViewModel by viewModels()
    private var adapter: NotificationsAdapter? = null

    private fun showDeleteNotificationDialog(notification: Notification) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_notification)
            .setMessage(R.string.delete_notification_dialog_message)
            .setPositiveButton(R.string.accept) { _, _ ->
                viewModel.deleteNotification(notification)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private val onItemOptionsClick: (button: View, notification: Notification) -> Unit = { button, notification ->
        val popupMenu = PopupMenu(requireContext(), button)

        popupMenu.inflate(R.menu.notification_menu)

        popupMenu.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.delete_notification -> {
                    showDeleteNotificationDialog(notification)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private val onNotificationClick: (notification: Notification) -> Unit = { notification ->
        when(notification) {
            is LikeNotification -> {
                notification.postId?.also { postId ->
                    PostViewerActivity.startActivity(requireContext(), postId)
                }
            }
            is FollowNotification -> {
                notification.followerUserId?.also {  followerUserId ->
                    ProfileActivity.startActivity(requireContext(), followerUserId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        adapter = NotificationsAdapter(onNotificationClick, onItemOptionsClick)
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        viewModel.getData()

        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.swipeToRefresh.setOnRefreshListener {
            viewModel.getData()
        }
    }

    private fun setupObservers() {
        viewModel.notifications.observe(viewLifecycleOwner) {
            adapter?.submitList(it)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.swipeToRefresh.isRefreshing = it
        }

        viewModel.error.observe(viewLifecycleOwner) {
            if(it){
                Toast.makeText(requireContext(), R.string.error_making_request, Toast.LENGTH_SHORT).show()
                viewModel.error.value = false
            }
        }
    }
}