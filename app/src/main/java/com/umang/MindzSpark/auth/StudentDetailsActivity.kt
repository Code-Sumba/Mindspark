package com.umang.MindzSpark.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.umang.MindzSpark.R
import com.umang.MindzSpark.databinding.ActivityStudentDetailsBinding
import com.umang.MindzSpark.general.HomeActivity
import com.umang.MindzSpark.modals.StudentData
import com.umang.MindzSpark.utils.AppPreferences
import java.util.regex.Pattern

class StudentDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentDetailsBinding
    private lateinit var database: DatabaseReference

    private val COLLEGE_NAME_GMRIT = Pair("GMR Institute of Technology", "GMRIT")
    private val DEPARTMENTS = listOf(
        Pair("Computer Science Engineering", "CSE"),
        Pair("Information Technology", "IT"),
        Pair("Electronics and Communication Engineering", "ECE"),
        Pair("Electrical and Electronics Engineering", "EEE"),
        Pair("Civil Engineering", "CIV"),
        Pair("Mechanical Engineering", "MECH"),
        Pair("Chemical Engineering", "CHEM")
    )
    private val YEARS = listOf(
        Pair("1st Year", "1"),
        Pair("2nd Year", "2"),
        Pair("3rd Year", "3"),
        Pair("4th Year", "4")
    )
    private val SECTIONS = listOf(
        Pair("A Section", "A"),
        Pair("B Section", "B"),
        Pair("C Section", "C")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpCollegeList()
        setUpYearList()
        setUpDepartmentList()
        setUpSectionList()

        database = Firebase.database.reference
        AppPreferences.init(this)

        binding.btnSubmit.setOnClickListener {
            val studentName = binding.editName.text.toString().trim()
            val studentPhoneNumber = binding.editPhone.text.toString().trim()
            val collegeName = binding.collegeList.text.toString().trim()
            val graduationYear = binding.yearsList.text.toString().trim()
            val studentDept = binding.deptSpinner.text.toString().trim()
            val studentSection = binding.sectionSpinner.text.toString().trim()

            when {
                studentName.isEmpty() -> {
                    binding.editName.error = "Please enter Name"
                }
                !isValidName(studentName) -> {
                    binding.editName.error = "Name should contain only alphabets"
                }
                studentPhoneNumber.isEmpty() -> {
                    binding.editPhone.error = "Please enter Phone Number"
                }
                studentPhoneNumber.length != 10 -> {
                    binding.editPhone.error = "Please enter a valid Phone Number"
                }
                collegeName.isEmpty() -> {
                    binding.collegeList.error = "Please choose College Name"
                }
                graduationYear.isEmpty() -> {
                    binding.yearsList.error = "Please choose current Studying Year"
                }
                studentDept.isEmpty() -> {
                    binding.deptSpinner.error = "Please choose Department"
                }
                studentSection.isEmpty() -> {
                    binding.sectionSpinner.error = "Please choose Section"
                }
                else -> {
                    binding.btnSubmit.isEnabled = false
                    val email = intent.getStringExtra("Email") ?: ""
                    val provider = intent.getStringExtra("provider") ?: ""

                    val deptID = DEPARTMENTS.firstOrNull { it.first == studentDept }?.second ?: ""
                    val yearID = YEARS.firstOrNull { it.first == graduationYear }?.second ?: ""
                    val sectionID = SECTIONS.firstOrNull { it.first == studentSection }?.second ?: ""
                    val userID = "${COLLEGE_NAME_GMRIT.second}_$deptID$yearID$sectionID"

                    writeNewUser(
                        userID,
                        studentName,
                        email,
                        studentPhoneNumber,
                        collegeName,
                        graduationYear,
                        studentDept,
                        studentSection,
                        provider
                    )
                }
            }
        }
    }

    private fun writeNewUser(
        userId: String,
        name: String,
        email: String,
        phone: String,
        collegeName: String,
        graduationYear: String,
        studentDept: String,
        studentSection: String,
        provider: String
    ) {
        val user = StudentData(
            userId, name, email, phone, collegeName, graduationYear, studentDept, studentSection, provider
        )
        database.child("students_data").push().setValue(user)

        AppPreferences.isLogin = true
        AppPreferences.studentName = name
        AppPreferences.studentID = userId
        AppPreferences.studentEmailID = email

        FirebaseMessaging.getInstance().subscribeToTopic("/topics/${AppPreferences.studentID}")

        Toast.makeText(this, "Details Submitted Successfully!", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun setUpCollegeList() {
        val collegeNames = listOf(COLLEGE_NAME_GMRIT.first)
        val adapter = ArrayAdapter(this, R.layout.list_item, collegeNames)
        (binding.collegeList as AutoCompleteTextView).setAdapter(adapter)
    }

    private fun setUpYearList() {
        val yearNames = YEARS.map { it.first }
        val adapter = ArrayAdapter(this, R.layout.list_item, yearNames)
        (binding.yearsList as AutoCompleteTextView).setAdapter(adapter)
    }

    private fun setUpDepartmentList() {
        val deptNames = DEPARTMENTS.map { it.first }
        val adapter = ArrayAdapter(this, R.layout.list_item, deptNames)
        (binding.deptSpinner as AutoCompleteTextView).setAdapter(adapter)
    }

    private fun setUpSectionList() {
        val sectionNames = SECTIONS.map { it.first }
        val adapter = ArrayAdapter(this, R.layout.list_item, sectionNames)
        (binding.sectionSpinner as AutoCompleteTextView).setAdapter(adapter)
    }

    private fun isValidName(name: String): Boolean {
        val pattern = Pattern.compile("^[A-Za-z ]+$")
        return pattern.matcher(name).matches()
    }
}
