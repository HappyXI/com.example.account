package com.example.account.model

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.account.R
import com.example.account.databinding.FragmentChartBinding
import com.example.account.viewmodel.TableViewModel

class ChartFragment : Fragment(R.layout.fragment_chart) {

    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!
    private lateinit var tableViewModel: TableViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tableViewModel = ViewModelProvider(this)[TableViewModel::class.java]

        observeTransactions()  // ✅ 데이터 변경 감지 시작
    }

    private fun observeTransactions() {
        tableViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val totalExpense = transactions.filter { it.kind == "지출" }.sumOf { it.amount }  // ✅ 해당 월의 지출 총액 가져오기

            // ✅ SharedPreferences에서 목표 지출액 가져오기
            val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val targetExpense = sharedPreferences.getInt("target_expense", 0)  // 기본값 0

            // ✅ 목표 지출액 대비 퍼센트 계산
            val expensePercentage = if (targetExpense > 0) {
                (totalExpense.toFloat() / targetExpense.toFloat() * 100).toInt()
            } else {
                0
            }

            // ✅ ProgressBar 업데이트
            binding.tvExpensePercentage.text = "$targetExpense 원"  // ✅ 퍼센트 숫자 표시
            binding.progressBar.progress = expensePercentage
            binding.tvExpenseMessage.text = "목표 지출액의 $expensePercentage%를 사용하셨어요!"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

