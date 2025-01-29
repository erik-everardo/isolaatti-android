package com.isolaatti.push_notifications

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import coil3.request.ImageRequest
import coil3.request.fallback
import coil3.toBitmap
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.isolaatti.MyApplication
import com.isolaatti.R
import com.isolaatti.common.CoilImageLoader
import com.isolaatti.notifications.domain.FollowNotification
import com.isolaatti.notifications.domain.LikeNotification
import com.isolaatti.posting.posts.viewer.ui.PostViewerActivity
import com.isolaatti.profile.ui.ProfileActivity
import com.isolaatti.utils.UrlGen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.awaitResponse
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {
    companion object {
        const val LOG_TAG = "FcmService"

        object NotificationsConstants {

            const val NOTIFICATION_ID       = "id"
            const val TIMESTAMP             = "timeStamp"
            const val USER_ID               = "userId"
            const val READ                  = "read"
            const val RELATED_NOTIFICATIONS = "relatedNotifications"

            object LikeNotificationConstants {
                const val LIKE_ID     = "likeId"
                const val POST_ID     = "postId"
                const val AUTHOR_ID   = "authorId"
                const val AUTHOR_NAME = "authorName"
            }

            object NewFollowerNotificationConstants {
                const val FOLLOWER_USER_ID = "followerUserId"
                const val FOLLOWER_NAME = "followerName"
            }
        }

    }

    @Inject
    lateinit var pushNotificationsApi: PushNotificationsApi

    override fun onNewToken(token: String) {

        CoroutineScope(Dispatchers.IO).launch {
            val response = pushNotificationsApi.registerDevice(token.toRequestBody()).awaitResponse()
        }

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(LOG_TAG, "onMessageReceived")
        val type = message.data["type"]

        when(type) {
            LikeNotification.TYPE -> showLikeNotification(message.data)
            FollowNotification.TYPE -> showNewFollowerNotification(message.data)

            else -> {
                Log.i(LOG_TAG, "Not showing notification of unknown type: ${message.data}")
            }
        }
    }

    private fun showLikeNotification(data: Map<String, String>) {
        val notificationId = data[NotificationsConstants.NOTIFICATION_ID]?.toIntOrNull()
        val relatedNotifications = data[NotificationsConstants.RELATED_NOTIFICATIONS]?.trimStart('[')?.trimEnd(']')?.split(",")
        val likeId = data[NotificationsConstants.LikeNotificationConstants.LIKE_ID]
        val postId = data[NotificationsConstants.LikeNotificationConstants.POST_ID]?.toLongOrNull()
        val authorId = data[NotificationsConstants.LikeNotificationConstants.AUTHOR_ID]?.toIntOrNull()
        val authorName = data[NotificationsConstants.LikeNotificationConstants.AUTHOR_NAME]


        // notificationId should never be null
        if(notificationId == null) {
            Log.e(LOG_TAG, "notification id is null")
            return
        }
        Log.v(LOG_TAG, "Notification id: $notificationId")

        val imageUrl = authorId?.let { UrlGen.userProfileImage(it, true) }

        Log.d(LOG_TAG, data.toString())

        val imageRequest = ImageRequest
            .Builder(this)
            .data(imageUrl)
            .fallback(R.drawable.baseline_person_24)
            .target { drawable ->
                val notificationBuilder = NotificationCompat.Builder(this, MyApplication.LIKES_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.like_notification_title, authorName ?: ""))
                    .setContentText(getString(R.string.like_notification_text))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setLargeIcon(drawable.toBitmap())
                    .setAutoCancel(true)

                Log.v(LOG_TAG, "Post liked: $postId")

                if(postId != null) {
                    notificationBuilder.setContentIntent(PostViewerActivity.getPendingIntent(this, postId))
                }

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return@target
                }

                NotificationManagerCompat.from(this).run {
                    relatedNotifications?.forEach {
                        it.toIntOrNull()?.let { relatedNotificationId -> cancel(relatedNotificationId) }
                    }


                    notify(notificationId, notificationBuilder.build())
                }
            }.build()


        CoilImageLoader.getImageLoader(this).enqueue(imageRequest)
    }

    private fun showNewFollowerNotification(data: Map<String, String>) {
        val notificationId = data[NotificationsConstants.NOTIFICATION_ID]?.toIntOrNull()
        val relatedNotifications = data[NotificationsConstants.RELATED_NOTIFICATIONS]?.trimStart('[')?.trimEnd(']')?.split(",")
        val followerUserId = data[NotificationsConstants.NewFollowerNotificationConstants.FOLLOWER_USER_ID]?.toIntOrNull()
        val followerName = data[NotificationsConstants.NewFollowerNotificationConstants.FOLLOWER_NAME]

        // notificationId should never be null
        if(notificationId == null) {
            Log.e(LOG_TAG, "notification id is null")
            return
        }

        if(followerUserId == null) {
            Log.e(LOG_TAG, "followerUserId is not present or is not valid")
        }

        val imageUrl = followerUserId?.let { UrlGen.userProfileImage(it, true) }

        val imageRequest = ImageRequest
            .Builder(this)
            .data(imageUrl)
            .fallback(R.drawable.baseline_person_24)
            .target { drawable ->
                val notificationBuilder = NotificationCompat.Builder(this, MyApplication.LIKES_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.new_follower_notification_title, followerName ?: ""))
                    .setContentText(getString(R.string.new_follower_notification_text))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setLargeIcon(drawable.toBitmap())
                    .setAutoCancel(true)

                    if(followerUserId != null) {
                        notificationBuilder.setContentIntent(ProfileActivity.getPendingIntent(this, followerUserId))
                    }


                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return@target
                }



                NotificationManagerCompat.from(this).run {
                    relatedNotifications?.forEach {
                        it.toIntOrNull()?.let { relatedNotificationId -> cancel(relatedNotificationId) }
                    }


                    notify(notificationId, notificationBuilder.build())
                }
            }.build()


        CoilImageLoader.getImageLoader(this).enqueue(imageRequest)

    }
}