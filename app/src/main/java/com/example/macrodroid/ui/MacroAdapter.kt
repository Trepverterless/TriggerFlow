package com.example.macrodroid.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.macrodroid.R
import com.example.macrodroid.data.Macro

class MacroAdapter(
    private var macros: List<Macro>,
    private val onItemClick: (Macro) -> Unit,
    private val onToggle: (Macro) -> Unit
) : RecyclerView.Adapter<MacroAdapter.MacroViewHolder>() {

    inner class MacroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvTriggerCount: TextView = itemView.findViewById(R.id.tvTriggerCount)
        val tvActionCount: TextView = itemView.findViewById(R.id.tvActionCount)
        val swEnabled: Switch = itemView.findViewById(R.id.swEnabled)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(macros[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MacroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_macro, parent, false)
        val holder = MacroViewHolder(view)
        
        holder.swEnabled.setOnCheckedChangeListener { _, isChecked ->
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onToggle(macros[position].copy(isEnabled = isChecked))
            }
        }
        
        return holder
    }

    override fun onBindViewHolder(holder: MacroViewHolder, position: Int) {
        val macro = macros[position]
        holder.tvName.text = macro.name
        holder.tvDescription.text = macro.description
        holder.tvTriggerCount.text = "${macro.triggers.size} ${holder.itemView.context.getString(R.string.trigger_count)}"
        holder.tvActionCount.text = "${macro.actions.size} ${holder.itemView.context.getString(R.string.action_count)}"
        holder.swEnabled.setOnCheckedChangeListener(null)
        holder.swEnabled.isChecked = macro.isEnabled
        holder.swEnabled.setOnCheckedChangeListener { _, isChecked ->
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onToggle(macros[pos].copy(isEnabled = isChecked))
            }
        }
    }

    override fun getItemCount(): Int = macros.size

    fun updateMacros(newMacros: List<Macro>) {
        macros = newMacros
        notifyDataSetChanged()
    }
}