package com.example.account.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.account.R
import com.example.account.data.Table
import com.example.account.databinding.ItemTableRowBinding

// TableAdapter의 생성자
class TableAdapter : ListAdapter<Table, TableAdapter.TableViewHolder>(TableDiffCallback()) {

    // 새로운 ViewHolder 객체를 생성하여 반환
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTableRowBinding.inflate(inflater, parent, false)
        return TableViewHolder(binding)
    }

    // ViewHolder에 데이터를 바인딩
    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Recycler View의 각 아이템을 담당하는 ViewHolder
    // 개별 아이템의 UI를 관리하는 역할
    class TableViewHolder(private val binding: ItemTableRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Table 객체의 데이터를 UI에 반영
        fun bind(table: Table) {
            binding.tvCategory.text = table.category
            binding.tvDescription.text = table.description
            binding.tvAmount.text = binding.root.context.getString(R.string.amount_format, table.amount)
            binding.tvDate.text = table.date

            // 금액 색상 변경 (수익: 파란색, 지출: 빨간색)
            binding.tvAmount.setTextColor(
                if (table.kind == "수익") Color.BLUE else Color.RED
            )
        }
    }
}

// 리스트의 변경 사항을 감지하여 Recyclerview의 업데이트 성능을 향상시키는 DiffUtill.ItemCallback을 구현한 클래스
// 리스트의 변경을 최소한으로 감지하여 효율적인 업데이트 수행
class TableDiffCallback : DiffUtil.ItemCallback<Table>() {
    // 아이템 고유 ID(no)가 같은지 확인
    override fun areItemsTheSame(oldItem: Table, newItem: Table) = oldItem.no == newItem.no
    // 내용이 완전히 같은지 비교
    override fun areContentsTheSame(oldItem: Table, newItem: Table) = oldItem == newItem
}