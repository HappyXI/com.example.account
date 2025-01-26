package com.example.account.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.account.adapter.TableAdapter
import com.example.account.repository.TableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.account.R

class TableFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Layout을 Fragment에 연결
        val view = inflater.inflate(R.layout.fragment_table, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_table)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 데이터베이스에서 데이터 로드
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                TableRepository.fetchTableItems()
            }
            val adapter = TableAdapter(items)
            recyclerView.adapter = adapter
        }

        return view
    }
}