package com.umang.MindzSpark.general

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.umang.MindzSpark.R
import com.umang.MindzSpark.databinding.ActivityFilterSubjectsBinding

class FilterSubjectsActivity : AppCompatActivity() {

    // ViewBinding variable
    private lateinit var binding: ActivityFilterSubjectsBinding

    private lateinit var checkedSubjectsList: ArrayList<String>
    private lateinit var checkedUnitsList: ArrayList<String>
    private lateinit var checkedFileTypeList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the ViewBinding
        binding = ActivityFilterSubjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkedSubjectsList = ArrayList<String>()
        checkedUnitsList = ArrayList<String>()
        checkedFileTypeList = ArrayList<String>()

        // Subjects Check List
        binding.PandS.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedSubjectsList.add("Probability and Statistics")
        }
        binding.ComputerNetworks.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedSubjectsList.add("Computer Networks")
        }
        binding.ComputerOrganization.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedSubjectsList.add("Computer Organization")
        }
        binding.OperatingSystems.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedSubjectsList.add("Operating Systems")
        }
        binding.WebTechnologies.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedSubjectsList.add("Web Technologies")
        }
        binding.SoftwareEngineering.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedSubjectsList.add("Software Engineering")
        }

        // Units Check List
        binding.unitOne.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedUnitsList.add("Unit-1")
        }
        binding.unitTwo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedUnitsList.add("Unit-2")
        }
        binding.unitThree.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedUnitsList.add("Unit-3")
        }
        binding.unitFour.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedUnitsList.add("Unit-4")
        }
        binding.QuestionPapers.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedUnitsList.add("Question Papers")
        }
        binding.Others.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedUnitsList.add("Others")
        }

        // File Type Check List
        binding.PDfFormat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedFileTypeList.add("PDF")
        }
        binding.PPTformat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedFileTypeList.add("PPT")
        }
        binding.otherFiles.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkedFileTypeList.add("Other Files")
        }

        // Button Click
        binding.btnApplyChanges.setOnClickListener {
            val intent = Intent(this, ClassNotesActivity::class.java)
            intent.putStringArrayListExtra("subjectsList", checkedSubjectsList)
            intent.putStringArrayListExtra("unitsList", checkedUnitsList)
            intent.putStringArrayListExtra("fileTypeList", checkedFileTypeList)
            startActivity(intent)
        }
    }
}
