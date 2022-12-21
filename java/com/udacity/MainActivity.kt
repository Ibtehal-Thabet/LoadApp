package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private val NOTIFICATION_ID = 0
    private var downloadStatus = "Failed"
    private lateinit var selectedProject: URL

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        createChannel(getString(R.string.app_name))

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        custom_button.setOnClickListener {
            choosingProjectRB()
        }

        radioGroup.setOnCheckedChangeListener { radioGroup, selected_project ->
            selectedProject = when (selected_project) {
                R.id.GlideRadioButton -> URL.GLIDE
                R.id.LoadAppRadioButton -> URL.LOAD_APP
                else -> URL.RETROFIT
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadID) {
                notificationManager.sendNotification(
                    selectedProject.text + " is downloaded",
                    applicationContext
                )
            }
        }
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.

        val cursor: Cursor =
            downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
        if (cursor.moveToFirst()) {
            val status = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            when (cursor.getInt(status)) {
                DownloadManager.STATUS_FAILED -> {
                    downloadStatus = "Failed"
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    downloadStatus = "Success"
                }
            }
        }
    }

    //Download The choosing Project
    private fun choosingProjectRB() {
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val selectedButton: Int = radioGroup!!.checkedRadioButtonId
        val btn = findViewById<RadioButton>(selectedButton)
        return if (btn == null)
            Toast.makeText(this, "Please select the file to download", Toast.LENGTH_SHORT).show()
        else {
            download(selectedProject.url)
        }
    }

    //Create Notification Channel
    private fun createChannel(channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.app_description)

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


    //Create Notification
    private fun NotificationManager.sendNotification(
        messageBody: String,
        applicationContext: Context
    ) {

        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val detailIntent = Intent(applicationContext, DetailActivity::class.java)
        detailIntent.putExtra("File_Name", selectedProject.text)
        detailIntent.putExtra("Status", downloadStatus)
        val detailPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(
                applicationContext
                    .getString(R.string.notification_title)
            )
            .setContentText(messageBody)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_assistant_black_24dp,
                getString(R.string.notification_button),
                detailPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notify(NOTIFICATION_ID, builder.build())

    }

    companion object {
        private enum class URL(val url: String, val text: String) {
            GLIDE(
                "https://github.com/bumptech/glide/archive/refs/heads/master.zip",
                "Glide-Image Loading Library by BumpTech"
            ),
            LOAD_APP(
                "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip",
                "LoadApp-Current repository by Udacity"
            ),
            RETROFIT(
                "https://github.com/square/retrofit/archive/refs/heads/master.zip",
                "Retrofit-Type-safe HTTP client for Android and Java by Square, Inc"
            )
        }

        private const val CHANNEL_ID = "channelId"
    }

}
