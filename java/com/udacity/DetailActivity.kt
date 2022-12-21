package com.udacity

import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        fileName.text = intent.getStringExtra("File_Name")
        status.text = intent.getStringExtra("Status")

        if (status.text == "Failed")
            status.setTextColor(Color.RED)

        notificationManager= ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        )as NotificationManager

        notificationManager.cancelNotifications()

        button_ok.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    //Cancel Notifications
    fun NotificationManager.cancelNotifications() {
        cancelAll()
    }


}
