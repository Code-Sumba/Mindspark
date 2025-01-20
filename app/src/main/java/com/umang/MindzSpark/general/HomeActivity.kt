package com.umang.MindzSpark.general

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.umang.MindzSpark.adapters.DashboardIconsAdapter
import com.umang.MindzSpark.adapters.NewsAdapter
import com.umang.MindzSpark.auth.AuthenticationActivity
import com.umang.MindzSpark.modals.DashboardIconData
import com.umang.MindzSpark.modals.NewsData
import com.umang.MindzSpark.utils.AppPreferences
import com.umang.MindzSpark.R
import com.umang.MindzSpark.databinding.ActivityHomeBinding
import android.view.View


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var newsLayoutManager: LinearLayoutManager
    lateinit var bottomnav: BottomAppBar

    lateinit var mGoogleSignInClient: GoogleSignInClient

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomnav = binding.bottomnav
        AppPreferences.init(this)

        // Google AdMob
//        MobileAds.initialize(this) {}
//        val adRequest = AdRequest.Builder().build()
//       // binding.adView.loadAd(adRequest)

        if (AppPreferences.isLogin) {
            binding.studentName.text = "Hi " + AppPreferences.studentName
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.uploadFilesButton.setOnClickListener {
            val intent = Intent(this, UploadFilesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

//        binding.notificationLayout.setOnClickListener {
//            val intent = Intent(this, ViewNotificationsActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            startActivity(intent)
//        }

        // DashboardIconsAdapter
        linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        linearLayoutManager.reverseLayout = false

        val dashboardIconsList = ArrayList<DashboardIconData>()
        dashboardIconsList.add(DashboardIconData("Class Notes", R.drawable.ic_baseline_menu_book_24))
        dashboardIconsList.add(DashboardIconData("Class Mates", R.drawable.ic_baseline_supervisor_account_24))
        dashboardIconsList.add(DashboardIconData("Reminders", R.drawable.ic_baseline_notifications_active_24))
        dashboardIconsList.add(DashboardIconData("Profile", R.drawable.ic_baseline_person_24))

        val dashboardIconAdapter = DashboardIconsAdapter(dashboardIconsList)
        binding.dashboardRecycler.layoutManager = linearLayoutManager
        binding.dashboardRecycler.adapter = dashboardIconAdapter

        binding.searchEditText.setOnClickListener {
            val intent = Intent(this, ClassNotesActivity::class.java)
            intent.putExtra("navigatedFrom", "HomeActivity")
            startActivity(intent)
        }

        // News Adapter
        newsLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val newsList = ArrayList<NewsData>()
        newsList.add(NewsData(R.drawable.student, "New Version is Live!", "New UI and added facility to send Reminders for Assignments, Exams, etc."))
        newsList.add(NewsData(R.drawable.stumate, "Welcome to Mindzspark", "Hope you are enjoying the App and All the Best for your Exams"))

        val newsAdapter = NewsAdapter(newsList)
        binding.newsRecycler.layoutManager = newsLayoutManager
        binding.newsRecycler.adapter = newsAdapter

        bottomnav.setOnClickListener {
            val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
            val view = layoutInflater.inflate(R.layout.bottom_items, null)

            view.findViewById<TextView>(R.id.classNotes).setOnClickListener {
                view.findViewById<TextView>(R.id.classNotes).setBackgroundResource(R.drawable.bottom_sheet_dialog_button)
                view.findViewById<TextView>(R.id.classNotes).setTextColor(resources.getColor(R.color.colorPrimary))

                val intent = Intent(this, ClassNotesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)

                // Reset other background styles
                resetBottomSheetBackgrounds(view)
            }

            view.findViewById<TextView>(R.id.remainders).setOnClickListener {
                view.findViewById<TextView>(R.id.remainders).setBackgroundResource(R.drawable.bottom_sheet_dialog_button)
                view.findViewById<TextView>(R.id.remainders).setTextColor(resources.getColor(R.color.colorPrimary))

                val intent = Intent(this, ReminderActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)

                resetBottomSheetBackgrounds(view)
            }

            view.findViewById<TextView>(R.id.profile).setOnClickListener {
                view.findViewById<TextView>(R.id.profile).setBackgroundResource(R.drawable.bottom_sheet_dialog_button)
                view.findViewById<TextView>(R.id.profile).setTextColor(resources.getColor(R.color.colorPrimary))
                val intent = Intent(this, StudentProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)

                resetBottomSheetBackgrounds(view)
            }

            view.findViewById<TextView>(R.id.rateUs).setOnClickListener {
                view.findViewById<TextView>(R.id.rateUs).setBackgroundResource(R.drawable.bottom_sheet_dialog_button)
                view.findViewById<TextView>(R.id.rateUs).setTextColor(resources.getColor(R.color.colorPrimary))

                val intent = Intent(this, AboutActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)

                resetBottomSheetBackgrounds(view)
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
                        // Google sign out
                        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
                            AppPreferences.isLogin = false
                            AppPreferences.studentID = ""
                            AppPreferences.studentName = ""
                            navigateToAuthenticationActivity()
                        }
                    } else {
                        // No account found, logout directly
                        AppPreferences.isLogin = false
                        AppPreferences.studentID = ""
                        AppPreferences.studentName = ""
                        navigateToAuthenticationActivity()
                    }
                }

                logoutDialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                    logoutDialog.dismiss()
                }

                logoutDialog.show()

                resetBottomSheetBackgrounds(view)
            }

            view.findViewById<TextView>(R.id.collegeMates).setOnClickListener {
                view.findViewById<TextView>(R.id.collegeMates).setBackgroundResource(R.drawable.bottom_sheet_dialog_button)
                view.findViewById<TextView>(R.id.collegeMates).setTextColor(resources.getColor(R.color.colorPrimary))

                val intent = Intent(this, ClassMatesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)

                resetBottomSheetBackgrounds(view)
            }

            dialog.setContentView(view)
            dialog.show()
        }
    }

    private fun resetBottomSheetBackgrounds(view: View) {
        view.findViewById<TextView>(R.id.classNotes).setBackgroundResource(0)
        view.findViewById<TextView>(R.id.remainders).setBackgroundResource(0)
        view.findViewById<TextView>(R.id.profile).setBackgroundResource(0)
        view.findViewById<TextView>(R.id.rateUs).setBackgroundResource(0)
        view.findViewById<TextView>(R.id.logOut).setBackgroundResource(0)
        view.findViewById<TextView>(R.id.collegeMates).setBackgroundResource(0)
    }

    private fun navigateToAuthenticationActivity() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
