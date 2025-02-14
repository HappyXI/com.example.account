package com.example.account.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.account.R
import com.example.account.data.CalendarDay

class CalendarAdapter(private var days: List<CalendarDay>, private val onItemClick: (CalendarDay) -> Unit) :
    RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar, parent, false)

        // View 크기 조정 (GridLayoutManager에서 균등 배치)
        val layoutParams = view.layoutParams
        layoutParams.width = parent.width / 7  // 7개의 날짜를 균등 배치
        view.layoutParams = layoutParams

        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    // 새로운 데이터가 들어오면 RecyclerView를 업데이트하는 메서드 추가
    fun updateData(newDays: List<CalendarDay>) {
        this.days = newDays
        notifyDataSetChanged() // UI 갱신
    }

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayNumber: TextView = itemView.findViewById(R.id.tvDayNumber)
        private val tvIncome: TextView = itemView.findViewById(R.id.tvIncome)
        private val tvExpense: TextView = itemView.findViewById(R.id.tvExpense)

        fun bind(day: CalendarDay) {
            if(day.date.isNotEmpty() && day.date.length >= 10) {
                tvDayNumber.text = day.date.substring(8).toInt().toString()
            } else {
                tvDayNumber.text = ""
            }
            tvIncome.text = if (day.income > 0) "+${String.format("%,d", day.income)}" else ""
            tvExpense.text = if (day.expense > 0) "-${String.format("%,d", day.expense)}" else ""

            itemView.setOnClickListener {
                onItemClick(day)
            }
        }
    }
}
