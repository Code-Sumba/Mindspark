package com.umang.MindzSpark.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umang.MindzSpark.databinding.ClassmateViewCardBinding
import com.umang.MindzSpark.modals.StudentData


class ClassMatesAdapter(
    private val context: Context,
    private val classMatesList: ArrayList<StudentData>
) : RecyclerView.Adapter<ClassMatesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ClassmateViewCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(classMatesList[position])
    }

    override fun getItemCount(): Int {
        return classMatesList.size
    }

    class ViewHolder(private val binding: ClassmateViewCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(classMates: StudentData) {
            // Set student name and email
            binding.studentName.text = classMates.studentName
            binding.mailID.text = classMates.emailID

            // Generate profile text based on the student name
            val studentName = classMates.studentName
            val nameParts = studentName?.split(" ") ?: listOf()

            binding.profileTextLayout.text = when (nameParts.size) {
                1 -> studentName?.firstOrNull()?.toString()
                else -> {
                    val firstNameInitial = nameParts.firstOrNull()?.firstOrNull()?.toString()
                    val lastNameInitial = nameParts.lastOrNull()?.firstOrNull()?.toString()
                    "$firstNameInitial$lastNameInitial"
                }
            }
        }
    }
}
