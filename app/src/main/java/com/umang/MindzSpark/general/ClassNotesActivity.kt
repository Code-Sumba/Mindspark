package com.umang.MindzSpark.general

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.umang.MindzSpark.R
import com.umang.MindzSpark.adapters.ClassNotesAdapter
import com.umang.MindzSpark.auth.AuthenticationActivity
import com.umang.MindzSpark.modals.FileUploadData
import com.umang.MindzSpark.utils.AppPreferences
import com.umang.MindzSpark.databinding.ActivityClassNotesBinding

class ClassNotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClassNotesBinding
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var classNotesList: ArrayList<FileUploadData>
    private lateinit var classNotesAdapter: ClassNotesAdapter
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Google Sign-In configuration
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        AppPreferences.init(this)

        // Initialize Google AdMob
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        binding.closeButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }

        classNotesList = ArrayList()

        // Set up the RecyclerView LayoutManager
        linearLayoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
            reverseLayout = true
        }

        binding.sortbyDate.setOnClickListener {
            Toast.makeText(this, "Already Sorted by Date!", Toast.LENGTH_LONG).show()
        }

        intent.getStringExtra("navigatedFrom")?.let {
            binding.sortbyDate.visibility = View.GONE
            binding.searchFiles.visibility = View.GONE
            binding.divider.visibility = View.GONE
            binding.searchEditText.visibility = View.VISIBLE
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }
        })

        binding.animationView.visibility = View.VISIBLE
        binding.prefsLayout.visibility = View.GONE
        binding.recyclerLayout.visibility = View.GONE

        binding.searchFiles.setOnClickListener {
            binding.sortbyDate.visibility = View.GONE
            binding.searchFiles.visibility = View.GONE
            binding.divider.visibility = View.GONE
            binding.searchEditText.visibility = View.VISIBLE
        }

        // Retrieve data from Firebase
        retrieveClassNotesData()

        binding.classNotesFAB.setOnClickListener {
            startActivity(Intent(this, UploadFilesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }

        setupBottomNavigation()
    }

    private fun retrieveClassNotesData() {
        val myRef = FirebaseDatabase.getInstance().reference
            .child(AppPreferences.studentID).child("files_data")

        myRef.keepSynced(true)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                classNotesList.clear()

                if (dataSnapshot.exists()) {
                    binding.apply {
                        animationView.visibility = View.GONE
                        prefsLayout.visibility = View.VISIBLE
                        recyclerLayout.visibility = View.VISIBLE
                    }

                    for (ds in dataSnapshot.children) {
                        val classNotesData = ds.getValue(FileUploadData::class.java)
                        if (classNotesData != null) {
                            classNotesList.add(classNotesData)
                        }
                    }

                    if (!::classNotesAdapter.isInitialized) {
                        classNotesAdapter = ClassNotesAdapter(
                            this@ClassNotesActivity,
                            classNotesList,
                            AppPreferences.studentName ?: "",
                            AppPreferences.studentID ?: ""
                        )
                        binding.classNotesRecycler.apply {
                            setHasFixedSize(true)
                            layoutManager = linearLayoutManager
                            adapter = classNotesAdapter
                        }
                    } else {
                        classNotesAdapter.notifyDataSetChanged()
                    }
                } else {
                    binding.apply {
                        animationView.visibility = View.GONE
                        prefsLayout.visibility = View.GONE
                        recyclerLayout.visibility = View.GONE
                        noDataAnimation.visibility = View.VISIBLE
                    }
                    Toast.makeText(this@ClassNotesActivity, "No Data Found !!!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.apply {
                    animationView.visibility = View.GONE
                    prefsLayout.visibility = View.GONE
                    recyclerLayout.visibility = View.GONE
                    noDataAnimation.visibility = View.VISIBLE
                }
                Toast.makeText(this@ClassNotesActivity, error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun filter(text: String) {
        val filteredList = classNotesList.filter {
            it.fileName?.contains(text, ignoreCase = true) == true ||
                    it.subjectName?.contains(text, ignoreCase = true) == true ||
                    it.unitNumber?.contains(text, ignoreCase = true) == true ||
                    it.studentName?.contains(text, ignoreCase = true) == true ||
                    it.fileType?.contains(text, ignoreCase = true) == true
        }
        classNotesAdapter.filterList(ArrayList(filteredList))
    }

    private fun setupBottomNavigation() {
        binding.classNotesBottomNav.setOnClickListener {
            val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
            val view = layoutInflater.inflate(R.layout.bottom_home, null)

            view.findViewById<TextView>(R.id.homePage).setOnClickListener {
                startActivity(Intent(this, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
            }

            view.findViewById<TextView>(R.id.classNotes).setOnClickListener {
                startActivity(Intent(this, ClassNotesActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
            }

            view.findViewById<TextView>(R.id.remainders).setOnClickListener {
                startActivity(Intent(this, ReminderActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
            }

            view.findViewById<TextView>(R.id.profile).setOnClickListener {
                startActivity(Intent(this, StudentProfileActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
            }

            view.findViewById<TextView>(R.id.rateUs).setOnClickListener {
                startActivity(Intent(this, AboutActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
            }

            view.findViewById<TextView>(R.id.logOut).setOnClickListener {
                showLogoutDialog()
            }

            view.findViewById<TextView>(R.id.collegeMates).setOnClickListener {
                startActivity(Intent(this, ClassMatesActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
            }

            dialog.setContentView(view)
            dialog.show()
        }
    }

    private fun showLogoutDialog() {
        val logoutDialog = Dialog(this).apply {
            setContentView(R.layout.logout_dialog)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        logoutDialog.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (account != null) {
                mGoogleSignInClient.signOut().addOnCompleteListener(this) {
                    AppPreferences.isLogin = false
                    AppPreferences.studentID = ""
                    AppPreferences.studentName = ""
                    startActivity(Intent(this, AuthenticationActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    finish()
                }
            } else {
                AppPreferences.isLogin = false
                AppPreferences.studentID = ""
                AppPreferences.studentName = ""
                startActivity(Intent(this, AuthenticationActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
                finish()
            }
        }

        logoutDialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            logoutDialog.dismiss()
        }

        logoutDialog.show()
    }
}
