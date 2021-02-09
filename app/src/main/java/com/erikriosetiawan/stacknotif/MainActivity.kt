package com.erikriosetiawan.stacknotif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.erikriosetiawan.stacknotif.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var idNotification = 0
    private val stackNotif = ArrayList<NotificationItem>()

    companion object {
        private const val CHANNEL_NAME = "dicoding channel"
        private const val GROUP_KEY_EMAILS = "group_key_emails"
        private const val NOTIFICATION_REQUEST_CODE = 200
        private const val MAX_NOTIFICATION = 2
        private const val CHANNEL_ID = "channel_01"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSender.setOnClickListener {
            val sender = binding.etSender.text.toString()
            val message = binding.etMessage.text.toString()
            if (sender.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Data harus diisi", Toast.LENGTH_SHORT).show()
            } else {
                stackNotif.add(NotificationItem(idNotification, sender, message))
                sendNotif()
                idNotification++
                binding.etSender.setText("")
                binding.etMessage.setText("")

                val methodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                methodManager.hideSoftInputFromWindow(binding.etMessage.windowToken, 0)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        stackNotif.clear()
        idNotification = 0
    }

    private fun sendNotif() {
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val largeIcon =
            BitmapFactory.decodeResource(resources, R.drawable.ic_baseline_notifications_24)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        val mBuilder: NotificationCompat.Builder

        if (idNotification < MAX_NOTIFICATION) {
            mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("New Email from ${stackNotif[idNotification].sender}")
                .setContentText(stackNotif[idNotification].message)
                .setSmallIcon(R.drawable.ic_baseline_mail_24)
                .setLargeIcon(largeIcon)
                .setGroup(GROUP_KEY_EMAILS)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        } else {
            val inboxStyle = NotificationCompat.InboxStyle()
                .addLine("New Email from ${stackNotif[idNotification].sender}")
                .addLine("New Email from ${stackNotif[idNotification - 1].sender}")
                .setBigContentTitle("$idNotification new emails")
                .setSummaryText("mail@dicoding")
            mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("$idNotification new emails")
                .setContentText("mail@dicoding.com")
                .setSmallIcon(R.drawable.ic_baseline_mail_24)
                .setGroup(GROUP_KEY_EMAILS)
                .setGroupSummary(true)
                .setContentIntent(pendingIntent)
                .setStyle(inboxStyle)
                .setAutoCancel(true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mBuilder.setChannelId(CHANNEL_ID)
            mNotificationManager.createNotificationChannel(channel)
        }

        val notification = mBuilder.build()
        mNotificationManager.notify(idNotification, notification)
    }
}