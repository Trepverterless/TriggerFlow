package com.example.macrodroid.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.macrodroid.R
import com.example.macrodroid.data.TriggerLog
import com.example.macrodroid.data.TriggerLogRepository

class TriggerLogAdapter(
    private var logs: List<TriggerLog>,
    private val repository: TriggerLogRepository
) : RecyclerView.Adapter<TriggerLogAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMacroName: TextView = itemView.findViewById(R.id.textMacroName)
        val textEvent: TextView = itemView.findViewById(R.id.textEvent)
        val textActions: TextView = itemView.findViewById(R.id.textActions)
        val textTime: TextView = itemView.findViewById(R.id.textTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trigger_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = logs[position]
        holder.textMacroName.text = log.macroName
        holder.textEvent.text = log.event
        
        // 显示动作列表
        if (log.actions.isNotEmpty()) {
            holder.textActions.visibility = View.VISIBLE
            val actionsText = holder.itemView.context.getString(
                R.string.actions_label, 
                log.actions.joinToString(", ")
            )
            holder.textActions.text = actionsText
        } else {
            holder.textActions.visibility = View.GONE
        }

        holder.textTime.text = repository.formatTime(log.timestamp)
    }

    override fun getItemCount(): Int = logs.size

    fun updateLogs(newLogs: List<TriggerLog>) {
        logs = newLogs
        notifyDataSetChanged()
    }
}