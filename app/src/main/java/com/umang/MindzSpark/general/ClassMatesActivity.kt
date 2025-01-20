package com.umang.MindzSpark.general

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.umang.MindzSpark.R
import com.umang.MindzSpark.adapters.ClassMatesAdapter
import com.umang.MindzSpark.auth.AuthenticationActivity
import com.umang.MindzSpark.modals.StudentData
import com.umang.MindzSpark.utils.AppPreferences
import com.umang.MindzSpark.databinding.ActivityClassMatesBinding

class ClassMatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClassMatesBinding
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var classMatesList: ArrayList<StudentData>
    lateinit var bottomnav: BottomAppBar
    lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassMatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomnav = binding.bottomNavigation

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        AppPreferences.init(this)

        binding.closeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        classMatesList = ArrayList<StudentData>()

        linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        linearLayoutManager.reverseLayout = false

        binding.uploadFilesButtonfab.setOnClickListener {
            val intent = Intent(this, UploadFilesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        bottomnav.setOnClickListener {
            val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
            val view = layoutInflater.inflate(R.layout.bottom_home, null)

            view.findViewById<TextView>(R.id.homePage).setOnClickListener {
                view.findViewById<TextView>(R.id.homePage).setBackgroundResource(R.drawable.bottom_sheet_dialog_button)
                view.findViewById<TextView>(R.id.homePage).setTextColor(resources.getColor(R.color.colorPrimary))

                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
                clearBackgrounds(view)
            }

            view.findViewById<TextView>(R.id.classNotes).setOnClickListener {
                view.findViewById<TextView>(R.id.classNotes).setBackgroundResource(R.drawable.bottom_sheet_dialog_button)
                view.findViewById<TextView>(R.id.classNotes).setTextColor(resources.getColor(R.color.colorPrimary))

                val intent = Intent(this, ClassNotesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
                clearBackgrounds(view)
            }

            view.findViewById<TextView>(R.id.remainders).setOnClickListener {
                view.findViewById<TextView>(R.id.remainders).setBackgroundResource(R.drawable.bottom_sheet_dialog_button)
                view.findViewById<TextView>(R.id.remainders).setTextColor(resources.getColor(R.color.colorPrimary))

                val intent = Intent(this, ReminderActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
                clearBackgrounds(view)
            }

            view.findViewById<TextView>(R.id.profile).setOnClickListener {
                view.findViewById<TextView>(R.id.profile).setBackgroundResource(R.drawable.bottom_sheet_dialog_button)
                view.findViewById<TextView>(R.id.profile).setTextColor(resources.getColor(R.color.colorPrimary))

                val intent = Intent(this, StudentProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
                clearBackgrounds(view)
            }

            view.findViewById<TextView>(R.id.rateUs).setOnClickListener {
                view.findViewById<TextView>(R.id.rateUs).setBackgroundResource(R.drawable.bottom_sheet_dialog_button)
                view.findViewById<TextView>(R.id.rateUs).setTextColor(resources.getColor(R.color.colorPrimary))

                val intent = Intent(this, AboutActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
                clearBackgrounds(view)
            }

            view.findViewById<TextView>(R.id.logOut).setOnClickListener {
                view.findViewById<TextView>(R.id.logOut).setBackgroundResource(R.drawable.bottom_sheet_dialog_button)
                view.findViewById<TextView>(R.id.logOut).setTextColor(resources.getColor(R.color.colorPrimary))
                val logoutDialog = Dialog(this)
                logoutDialog.setContentView(R.layout.logout_dialog)
                logoutDialog.setCancelable(false)
                logoutDialog.setCanceledOnTouchOutside(false)
                logoutDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

                logoutDialog.findViewById<Button>(R.id.btnLogout).setOnClickListener {
                    val account = GoogleSignIn.getLastSignedInAccount(this)
                    if (account != null) {
                        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
                            AppPreferences.isLogin = false
                            AppPreferences.studentID = ""
                            AppPreferences.studentName = ""
                            val intent = Intent(this, AuthenticationActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        AppPreferences.isLogin = false
                        AppPreferences.studentID = ""
                        AppPreferences.studentName = ""
                        val intent = Intent(this, AuthenticationActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    }
                }

                logoutDialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                    logoutDialog.dismiss()
                }

                logoutDialog.show()

                clearBackgrounds(view)
            }

            view.findViewById<TextView>(R.id.collegeMates).setOnClickListener {
                view.findViewById<TextView>(R.id.collegeMates).setBackgroundResource(R.drawable.bottom_sheet_dialog_button)
                view.findViewById<TextView>(R.id.collegeMates).setTextColor(resources.getColor(R.color.colorPrimary))
                val intent = Intent(this, ClassMatesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
                clearBackgrounds(view)
            }

            dialog.setContentView(view)
            dialog.show()
        }

        retrieveClassNotesData()
    }

    private fun clearBackgrounds(view: View) {
        view.findViewById<TextView>(R.id.homePage).setBackgroundResource(0)
        view.findViewById<TextView>(R.id.classNotes).setBackgroundResource(0)
        view.findViewById<TextView>(R.id.remainders).setBackgroundResource(0)
        view.findViewById<TextView>(R.id.profile).setBackgroundResource(0)
        view.findViewById<TextView>(R.id.rateUs).setBackgroundResource(0)
        view.findViewById<TextView>(R.id.logOut).setBackgroundResource(0)
    }

    private fun retrieveClassNotesData() {
        binding.animationView.visibility = View.VISIBLE

        val myRef = FirebaseDatabase.getInstance().reference.child("students_data").orderByChild("studentID").equalTo(AppPreferences.studentID)

        val classMatesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    binding.animationView.visibility = View.GONE
                    for (ds in dataSnapshot.children) {
                        val classMatesData = ds.getValue(StudentData::class.java)
                        if (classMatesData != null) {
                            classMatesList.add(classMatesData)
                        }
                    }
                    val classMatesAdapter = ClassMatesAdapter(baseContext, classMatesList)

                    linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                    linearLayoutManager.reverseLayout = true
                    linearLayoutManager.stackFromEnd = true
                    binding.classMatesRecycler.layoutManager = linearLayoutManager

                    binding.classMatesRecycler.setHasFixedSize(true)
                    binding.classMatesRecycler.adapter = classMatesAdapter
                    classMatesAdapter.notifyDataSetChanged()

                } else {
                    binding.animationView.visibility = View.GONE
                    binding.noDataAnimation.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.animationView.visibility = View.GONE
                binding.noDataAnimation.visibility = View.VISIBLE
                Toast.makeText(baseContext, error.message, Toast.LENGTH_LONG).show()
            }
        }
        myRef.addValueEventListener(classMatesListener)
    }
}
