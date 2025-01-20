package com.umang.MindzSpark.general

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.umang.MindzSpark.R
import com.umang.MindzSpark.databinding.ActivityUploadFilesBinding
import com.umang.MindzSpark.modals.FileUploadData
import com.umang.MindzSpark.utils.AppPreferences
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class UploadFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadFilesBinding
    private lateinit var database: DatabaseReference
    private lateinit var mStorage: StorageReference

    private var fileSelected: String = "0"
    private var unitNumber: String? = null
    private var fileType: String? = null
    private lateinit var uri: Uri

    private val REQUEST_CODE_DOC = 0
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=" + AppPreferences.AUTH_KEY_FCM
    private val contentType = "application/json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppPreferences.init(this)

        database = FirebaseDatabase.getInstance().reference
        mStorage = FirebaseStorage.getInstance().reference.child(AppPreferences.studentID.toString())

        setupUI()
    }

    private fun setupUI() {
        binding.uploadFileButton.setOnClickListener {
            selectFile()
        }

        binding.oneUnit.setOnClickListener {
            unitNumber = "1"
            resetUnitButtons()
            binding.oneUnit.setBackgroundResource(R.drawable.blue_rounded_button)
        }

        binding.publishFile.setOnClickListener {
            validateAndUpload()
        }

        binding.deleteIcon.setOnClickListener {
            resetFileSelection()
        }
    }

    private fun selectFile() {
        val mimeTypes = arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "image/*"
        )

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(Intent.createChooser(intent, "Choose File"), REQUEST_CODE_DOC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            uri = data.data!!
            fileSelected = "1"
            binding.statusIcon.visibility = View.GONE
            binding.greenStatusIcon.visibility = View.VISIBLE
            binding.uploadFileButton.text = "File Selected!"
            binding.uploadFileButton.isEnabled = false
            binding.deleteIcon.visibility = View.VISIBLE
        }
    }

    private fun validateAndUpload() {
        val fileName = binding.editFileTitle.text.toString()
        val subjectName = binding.subjectNameSpinner.text.toString()

        when {
            fileSelected == "0" -> showToast("Please select a file.")
            fileName.isBlank() -> binding.edtFileTitle.error = "Please enter a title."
            subjectName.isBlank() -> binding.edtSubjectName.error = "Please select a subject."
            unitNumber.isNullOrEmpty() -> showToast("Please choose a unit number.")
            else -> {
                binding.edtFileTitle.error = null
                binding.edtSubjectName.error = null
                uploadFile(fileName, subjectName)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun uploadFile(fileName: String, subjectName: String) {
        val fileReference = mStorage.child(fileName)

        showToast("Upload in Progress. Please don't close this window.")
        fileReference.putFile(uri).addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            binding.customProgressBar.progress = progress.toInt()
            binding.txtProgress.text = "${progress.toInt()}%"
        }.continueWithTask { task ->
            if (!task.isSuccessful) throw task.exception!!
            fileReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveFileData(fileName, subjectName, task.result.toString())
            } else {
                showToast("Error occurred during upload.")
            }
        }
    }

    private fun saveFileData(fileName: String, subjectName: String, downloadUri: String) {
        val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val fileData = FileUploadData(fileName, subjectName, unitNumber, fileType, downloadUri, AppPreferences.studentName, currentDate)

        database.child(AppPreferences.studentID).child("files_data").push().setValue(fileData)
        showToast("File uploaded successfully!")
    }

    private fun resetFileSelection() {
        fileSelected = "0"
        binding.uploadFileButton.text = "Upload File"
        binding.statusIcon.visibility = View.VISIBLE
        binding.deleteIcon.visibility = View.GONE
        binding.greenStatusIcon.visibility = View.GONE
    }

    private fun resetUnitButtons() {
        binding.oneUnit.setBackgroundResource(R.drawable.rounded_button)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
