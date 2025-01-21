package com.umang.MindzSpark.auth

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.umang.MindzSpark.R
import com.umang.MindzSpark.databinding.ActivityAuthenticationBinding
import com.umang.MindzSpark.general.HomeActivity
import com.umang.MindzSpark.modals.StudentData
import com.umang.MindzSpark.onboarding.GettingStartedActivity
import com.umang.MindzSpark.utils.AppPreferences

class AuthenticationActivity : AppCompatActivity() {


    // Firebase and Google Sign-In variables
    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: ActivityAuthenticationBinding
    private val RC_SIGN_IN = 123

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth and Database Reference
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize AppPreferences
        AppPreferences.init(this)

        // Handle Google Sign-In Button
        binding.googleSign.setOnClickListener { signInWithGoogle() }

        // Handle Back Button to GettingStartedActivity
        binding.welcomeBackButton.setOnClickListener {
            startActivity(Intent(this, GettingStartedActivity::class.java))
            finish()
        }

        // Switch between Sign-Up and Sign-In
        binding.newUserText.setOnClickListener { toggleSignInSignUp() }

        // Forgot Password logic
        binding.forgotPassword.setOnClickListener { showForgotPasswordDialog() }

        // Handle Sign-In or Sign-Up button click
        binding.btnName.setOnClickListener { handleSignInOrSignUp() }
    }

    /**
     * Handles Google Sign-In
     */
    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /**
     * Handles Activity Result for Google Sign-In
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Authenticate with Firebase using Google Token
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                // Safely handle the nullable user.email
                user?.email?.let { email ->
                    // Proceed with non-null email
                    retrieveStudentDetails(email)
                } ?: run {
                    // Handle the case where email is null
                    Toast.makeText(this, "Email is null. Cannot retrieve student details.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Handle authentication failure
                Toast.makeText(this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    /**
     * Handles Forgot Password logic
     */
    private fun showForgotPasswordDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.forgot_password_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val emailField = dialog.findViewById<android.widget.EditText>(R.id.editResEmail)
        val submitButton = dialog.findViewById<android.widget.Button>(R.id.btnSubmit)

        submitButton.setOnClickListener {
            val email = emailField.text.toString()

            if (email.isEmpty()) {
                emailField.error = "Please enter your email address"
            } else if (!isValidEmail(email)) {
                emailField.error = "Please enter a valid email address"
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        dialog.show()
    }

    /**
     * Handles Sign-In or Sign-Up button click
     */
    private fun handleSignInOrSignUp() {
        val email = binding.editEmailIn.text.toString()
        val password = binding.editPasswordIn.text.toString()

        // Validation
        if (email.isEmpty()) {
            binding.editEmailIn.error = "Please enter your email"
        } else if (!isValidEmail(email)) {
            binding.editEmailIn.error = "Invalid email format"
        } else if (password.isEmpty()) {
            binding.editPasswordIn.error = "Please enter your password"
        } else if (password.length < 8) {
            binding.editPasswordIn.error = "Password must be at least 8 characters long"
        } else {
            binding.loadingProgress.visibility = android.view.View.VISIBLE
            if (binding.btnName.text.toString().contains("Sign in", true)) {
                signInUser(email, password)
            } else {
                signUpUser(email, password)
            }
        }
    }

    /**
     * Signs in the user with Firebase Authentication
     */
    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            binding.loadingProgress.visibility = android.view.View.GONE
            if (task.isSuccessful) {
                retrieveStudentDetails(email)
            } else {
                Toast.makeText(this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Signs up the user with Firebase Authentication
     */
    private fun signUpUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            binding.loadingProgress.visibility = android.view.View.GONE
            if (task.isSuccessful) {
                val intent = Intent(this, StudentDetailsActivity::class.java)
                intent.putExtra("Email", email)
                intent.putExtra("provider", "FirebaseAuth")
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Retrieves student details from Firebase Database
     */
    private fun retrieveStudentDetails(emailID: String) {
        val query = databaseReference.child("students_data").orderByChild("emailID").equalTo(emailID)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val studentData = child.getValue(StudentData::class.java)
                    if (studentData != null) {
                        AppPreferences.isLogin = true
                        AppPreferences.studentName = studentData.studentName.toString()
                        AppPreferences.studentID = studentData.studentID.toString()
                        AppPreferences.studentEmailID = studentData.emailID.toString()

                        FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + AppPreferences.studentID)
                        navigateToHome()
                        return
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AuthenticationActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Navigates to the HomeActivity
     */
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    /**
     * Toggles between Sign-In and Sign-Up UI
     */
    private fun toggleSignInSignUp() {
        val isSignUp = binding.newUserText.text.toString().contains("Don't have an Account?", true)
        if (isSignUp) {
            binding.welcomeText.text = "Create a new\nAccount"
            binding.btnName.text = "Sign up"
            binding.newUserText.text = "Already have an Account?"
            binding.forgotPassword.visibility = android.view.View.GONE
        } else {
            binding.welcomeText.text = "Welcome back"
            binding.btnName.text = "Sign in"
            binding.newUserText.text = "Don't have an Account?"
            binding.forgotPassword.visibility = android.view.View.VISIBLE
        }
    }

    /**
     * Checks if the provided email is valid
     */
    private fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target!!).matches()
    }
}