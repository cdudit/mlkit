package fr.cdudit.mlkit.features.main

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import fr.cdudit.mlkit.R

class ListAdapter(private val dataSet: ArrayList<String>) :
    RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val checkBox: CheckBox by lazy { view.findViewById(R.id.checkBox) }

        init {
            checkBox.setOnCheckedChangeListener { checkbox, isChecked ->
                checkbox.paintFlags =
                    if (isChecked) checkbox.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    else checkbox.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        fun bind(text: String) {
            checkBox.text = text
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.text_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size
}