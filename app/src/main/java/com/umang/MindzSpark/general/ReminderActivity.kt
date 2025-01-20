package com.umang.MindzSpark.general

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.FirebaseDatabase
import com.umang.MindzSpark.R
import com.umang.MindzSpark.auth.AuthenticationActivity
import com.umang.MindzSpark.databinding.ActivityReminderBinding
import com.umang.MindzSpark.modals.NotificationData
import com.umang.MindzSpark.utils.AppPreferences
import org.json.JSONObject
import java.util.*

class ReminderActivity : AppCompatActivity() {

    private val TAG = "TOKENS_DATA"
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=" + AppPreferences.AUTH_KEY_FCM
    private val contentType = "application/json"
    private var TOPIC: String? = null
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivityReminderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpTypeList()
        AppPreferences.init(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.closeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        binding.remainderFAB.setOnClickListener {
            val intent = Intent(this, UploadFilesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        binding.remainderBottomNav.setOnClickListener {
            showBottomSheetDialog()
        }

        binding.btnName.setOnClickListener {
            sendNotification()
        }
    }

    private fun setUpTypeList() {
        val typeNames = listOf("Assignment", "Lab Record", "Exam", "Others")
        val adapter = ArrayAdapter(
            this,
            R.layout.list_item,
            typeNames
        )
        binding.chooseType.setAdapter(adapter)
    }

    private fun sendNotification() {
        val title = binding.editTitleMessage.text.toString()
        val type = binding.chooseType.text.toString()
        val description = binding.editDescription.text.toString()

        if (title.isEmpty()) {
            binding.editTitleMessage.error = "Please enter Title of Notification"
        } else if (type.isEmpty()) {
            binding.chooseType.error = "Please choose Relevant Type"
            binding.editTitleMessage.error = null
        } else if (description.isEmpty()) {
            binding.editDescription.error = "Please enter Description"
            binding.chooseType.error = null
        } else {
            binding.btnName.isEnabled = false
            binding.editDescription.error = null

            try {
                val queue = Volley.newRequestQueue(this)
                TOPIC = "/topics/${AppPreferences.studentID}"

                val data = JSONObject().apply {
                    put("title", "$title from ${AppPreferences.studentName}")
                    put("message", "$description Related to $type")
                }

                val notificationData = JSONObject().apply {
                    put("data", data)
                    put("to", TOPIC)
                }

                val request = object : JsonObjectRequest(
                    FCM_API, notificationData,
                    Response.Listener<JSONObject?> {
                        saveNotificationToDatabase(title, description)
                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                        binding.btnName.isEnabled = true
                    }
                ) {
                    override fun getHeaders(): Map<String, String> {
                        return hashMapOf(
                            "Authorization" to serverKey,
                            "Content-Type" to contentType
                        )
                    }
                }

                queue.add(request)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.btnName.isEnabled = true
            }
        }
    }

    private fun saveNotificationToDatabase(title: String, description: String) {
        val myRef = FirebaseDatabase.getInstance()
            .getReference(AppPreferences.studentID)
            .child("notifications_data")
        myRef.push().setValue(NotificationData(title, description, AppPreferences.studentName))

        val successDialog = Dialog(this).apply {
            setContentView(R.layout.upload_success_layout)
            window!!.setBackgroundDrawableResource(android.R.color.transparent)
            findViewById<TextView>(R.id.dialogText).text =
                "Notification Sent Successfully to all the Class Mates!"
            findViewById<Button>(R.id.back_to_home).setOnClickListener {
                val intent = Intent(this@ReminderActivity, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                dismiss()
            }
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            show()
        }
    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_home, null)
        dialog.setContentView(view)
        dialog.show()
    }
}
