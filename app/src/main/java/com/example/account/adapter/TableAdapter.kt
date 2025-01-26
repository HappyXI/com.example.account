package com.example.account.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.account.R
import com.example.account.model.TableItem

class TableAdapter(private val tableItems: List<TableItem>) :
    RecyclerView.Adapter<TableAdapter.TableViewHolder>() {

    // ViewHolder 클래스 정의
    class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noTextView: TextView = itemView.findViewById(R.id.tv_no)
        val categoryTextView: TextView = itemView.findViewById(R.id.tv_category)
        val descriptionTextView: TextView = itemView.findViewById(R.id.tv_description)
        val amountTextView: TextView = itemView.findViewById(R.id.tv_amount)
        val dateTextView: TextView = itemView.findViewById(R.id.tv_date)
    }

    // ViewHolder를 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_table_row, parent, false)
        return TableViewHolder(view)
    }

    // ViewHolder에 데이터 바인딩
    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        val item = tableItems[position]
        holder.noTextView.text = item.no.toString()
        holder.categoryTextView.text = item.category
        holder.descriptionTextView.text = item.description
        holder.amountTextView.text = item.amount.toString()
        holder.dateTextView.text = item.date
    }

    // 아이템 수 반환
    override fun getItemCount(): Int = tableItems.size
}