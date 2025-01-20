package com.umang.MindzSpark.general

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.umang.MindzSpark.R

import com.umang.MindzSpark.auth.AuthenticationActivity
import com.umang.MindzSpark.modals.StudentData
import com.umang.MindzSpark.utils.AppPreferences

class StudentProfileActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var loadingAnimationView: View
    private lateinit var profileText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile)

        AppPreferences.init(this)

        // Initialize Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Retrieve student details
        val studentEmail = AppPreferences.studentEmailID ?: ""
        retrieveStudentDetails(studentEmail)

        // Set up views and listeners
        loadingAnimationView = findViewById(R.id.loadingAnimationView)
        profileText = findViewById(R.id.profileText)

        findViewById<Button>(R.id.closeButton).setOnClickListener {
            navigateTo(HomeActivity::class.java)
        }

        findViewById<View>(R.id.profileFAB).setOnClickListener {
            navigateTo(UploadFilesActivity::class.java)
        }

        findViewById<View>(R.id.profileBottomNav).setOnClickListener {
            showBottomSheetDialog()
        }

        // Display personal email passed via intent
        val personalEmail = intent.getStringExtra("Email")
        findViewById<TextView>(R.id.txtpersonalEmail).text = personalEmail
    }

    private fun retrieveStudentDetails(emailID: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val query = databaseReference.child("students_data").orderByChild("emailID").equalTo(emailID)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    loadingAnimationView.isVisible = false
                    snapshot.children.forEach { data ->
                        val studentData = data.getValue(StudentData::class.java)
                        studentData?.let { showStudentDetails(it) }
                    }
                } else {
                    Toast.makeText(this@StudentProfileActivity, "No Data Found!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StudentProfileActivity, error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showStudentDetails(studentData: StudentData) {
        findViewById<View>(R.id.profileNameLayout).isVisible = true
        findViewById<View>(R.id.profileEmailLayout).isVisible = true
        findViewById<View>(R.id.profilePhoneLayout).isVisible = true
        findViewById<View>(R.id.profileCollegeLayout).isVisible = true
        findViewById<View>(R.id.profileDeptSectionLayout).isVisible = true

        findViewById<TextView>(R.id.profileName).text = studentData.studentName
        findViewById<TextView>(R.id.profileEmail).text = studentData.emailID
        findViewById<TextView>(R.id.profilePhone).text = "+91 ${studentData.studentPhoneNumber}"
        findViewById<TextView>(R.id.profileCollege).text = studentData.collegeName
        findViewById<TextView>(R.id.profileDeptSection).text =
            "${studentData.graduationYear} ${studentData.studentDept}"

        val studentName: String? = null  // Example nullable String

// Using safe call to avoid null issues
        val firstChar = studentName?.firstOrNull()
        println(firstChar)  // prints "null" if studentName is null

    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_home, null)

        view.findViewById<TextView>(R.id.homePage).setOnClickListener { navigateTo(HomeActivity::class.java) }
        view.findViewById<TextView>(R.id.classNotes).setOnClickListener { navigateTo(ClassNotesActivity::class.java) }
        view.findViewById<TextView>(R.id.remainders).setOnClickListener { navigateTo(ReminderActivity::class.java) }
        view.findViewById<TextView>(R.id.profile).setOnClickListener { navigateTo(StudentProfileActivity::class.java) }
        view.findViewById<TextView>(R.id.rateUs).setOnClickListener { navigateTo(AboutActivity::class.java) }
        view.findViewById<TextView>(R.id.logOut).setOnClickListener { showLogoutDialog() }
        view.findViewById<TextView>(R.id.collegeMates).setOnClickListener { navigateTo(ClassMatesActivity::class.java) }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

//    private fun showLogoutDialog() {
//        val dialog = Dialog(this)
//        dialog.setContentView(R.layout.dialog_logout)
//        dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
//        dialog.findViewById<Button>(R.id.btnLogout).setOnClickListener {
//            AppPreferences.clearPreferences()
//            mGoogleSignInClient.signOut().addOnCompleteListener {
//                startActivity(Intent(this, AuthenticationActivity::class.java))
//                finish()
//            }
//            dialog.dismiss()
//        }
//        dialog.show()
//    }
private fun showLogoutDialog() {
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.logout_dialog)

    dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
        dialog.dismiss()
    }
    dialog.findViewById<Button>(R.id.btnLogout).setOnClickListener {
        AppPreferences.clearPreferences()  // Ensure this works now
        mGoogleSignInClient.signOut().addOnCompleteListener {
            startActivity(Intent(this, AuthenticationActivity::class.java))
            finish()
        }
        dialog.dismiss()
    }
    dialog.show()
}


}
