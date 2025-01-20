package com.umang.MindzSpark.general

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.umang.MindzSpark.R
import com.umang.MindzSpark.adapters.MissedNotificationsAdapter
import com.umang.MindzSpark.databinding.ActivityViewNotificationsBinding
import com.umang.MindzSpark.modals.NotificationData
import com.umang.MindzSpark.utils.AppPreferences

class ViewNotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewNotificationsBinding
    private lateinit var notificationsList: ArrayList<NotificationData>
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var notificationAdapter: MissedNotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationsList = ArrayList()

        layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
            stackFromEnd = true
            reverseLayout = true
        }

        binding.animationView.visibility = View.VISIBLE
        binding.recyclerLayout.visibility = View.GONE

        binding.closeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        retrieveNotificationData()
    }

    private fun retrieveNotificationData() {
        val myRef = FirebaseDatabase.getInstance()
            .getReference(AppPreferences.studentID)
            .child("notifications_data")

        myRef.keepSynced(true)

        val databaseListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.animationView.visibility = View.GONE
                    binding.recyclerLayout.visibility = View.VISIBLE

                    notificationsList.clear()
                    for (ds in snapshot.children) {
                        val notificationData = ds.getValue(NotificationData::class.java)
                        notificationData?.let { notificationsList.add(it) }
                    }

                    notificationAdapter = MissedNotificationsAdapter(
                        this@ViewNotificationsActivity,
                        notificationsList
                    )
                    binding.notificationRecycler.apply {
                        layoutManager = this@ViewNotificationsActivity.layoutManager
                        setHasFixedSize(true)
                        adapter = notificationAdapter
                    }
                } else {
                    binding.animationView.visibility = View.GONE
                    binding.recyclerLayout.visibility = View.GONE
                    binding.noDataAnimation.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.animationView.visibility = View.GONE
                binding.recyclerLayout.visibility = View.VISIBLE
                Toast.makeText(
                    this@ViewNotificationsActivity,
                    error.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        myRef.addValueEventListener(databaseListener)
    }
}
