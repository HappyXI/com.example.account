package com.example.account.model

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.account.R
import com.example.account.data.Table
import com.example.account.databinding.FragmentChartBinding
import com.example.account.viewmodel.TableViewModel
import com.example.account.util.DatePickerUtils
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChartFragment : Fragment(R.layout.fragment_chart) {

    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!
    private lateinit var tableViewModel: TableViewModel
    private var currentCalendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy년 M월", Locale.KOREAN)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tableViewModel = ViewModelProvider(this)[TableViewModel::class.java]

        // 초기 UI 업데이트 (현재 월 정보 표시)
        updateMonthDisplay()

        // 월 변경 버튼 클릭 이벤트 추가
        binding.tvTargetMonth.setOnClickListener() { showMonthPickerDialog() }
        binding.btnPrevMonth.setOnClickListener() { changeMonth(-1) }
        binding.btnNextMonth.setOnClickListener() { changeMonth(1) }

        // 데이터 변경 감지 시작
        observeTransactions()
    }

    // 월 변경 함수
    private fun changeMonth(offset: Int) {
        currentCalendar.add(Calendar.MONTH, offset)
        updateMonthDisplay()
    }

    private fun updateMonthDisplay() {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentMonthKey = getCurrentMonthKey()

        // 목표 지출액 불러오기
        val savedTargetExpense = sharedPreferences.getInt("target_expense_$currentMonthKey", 0)
        Log.d("ChartFragment_TargetExpense","currentMonthKey $currentMonthKey,savedTargetExpense = $savedTargetExpense")
        binding.tvTargetExpense.text = "$savedTargetExpense 원"

        // 현재 월 표시
        binding.tvCurrentMonth.text = dateFormat.format(currentCalendar.time)
        binding.tvTargetMonth.text = (dateFormat.format(currentCalendar.time) + " 목표 지출액")

        // 데이터 다시 불러오기
        observeTransactions()
    }

    private fun observeTransactions() {
        // 파이 차트 업데이트
        tableViewModel.filteredTransactions.observe(viewLifecycleOwner) { filteredList ->
            updatePieChart(filteredList)
        }

        tableViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val selectedYear = currentCalendar.get(Calendar.YEAR)       // 선택된 연도 가져옴
            val selectedMonth = currentCalendar.get(Calendar.MONTH) + 1 // 선택된 월 가져옴

            // 해당 월의 지출 데이터만 필터링
            val monthlyTransactions = transactions.filter { transaction ->
                val dateParts = transaction.date.split("-") // YYYY-MM-DD 형식의 날짜를 분리
                if (dateParts.size == 3) {
                    val transactionYear = dateParts[0].toInt()      // 연도 추출
                    val transactionMonth = dateParts[1].toInt()     // 월 추출
                    transactionYear == selectedYear && transactionMonth == selectedMonth
                } else {
                    false
                }
            }

            val totalIncome = monthlyTransactions.filter { it.kind == "수익" }.sumOf { it.amount }
            val totalExpense = monthlyTransactions.filter { it.kind == "지출" }.sumOf { it.amount }  // 해당 월의 지출 총액 가져오기

            // SharedPreferences에서 목표 지출액 가져오기
            val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val targetExpense = sharedPreferences.getInt("target_expense_${getCurrentMonthKey()}", 0)

            // 목표 지출액 대비 퍼센트 계산
            val expensePercentage = if (targetExpense > 0) {
                (totalExpense.toFloat() / targetExpense.toFloat() * 100).toInt()
            } else {
                0
            }

            binding.progressBar.progress = expensePercentage
            binding.tvExpenseMessage.text = "목표 지출액의 $expensePercentage%를 사용하셨어요!"
        }
    }

    private fun updatePieChart(transactions: List<Table>) {
        val selectedYear = currentCalendar.get(Calendar.YEAR)   // 선택된 연도 가져옴
        val selectedMonth = currentCalendar.get(Calendar.MONTH) + 1 // 선택된 월 가져옴
        val categoryPercentageMap = tableViewModel.getCategoryWisePercentage(true, selectedYear, selectedMonth)  // 해당 월 지출 기준
        val pieEntries = categoryPercentageMap.map { PieEntry(it.value, it.key) } // PieEntry 변환

        val dataSet = PieDataSet(pieEntries, "")
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.pastel_rainbow1),
            ContextCompat.getColor(requireContext(), R.color.pastel_rainbow2),
            ContextCompat.getColor(requireContext(), R.color.pastel_rainbow3),
            ContextCompat.getColor(requireContext(), R.color.pastel_rainbow4),
            ContextCompat.getColor(requireContext(), R.color.pastel_rainbow5),
            ContextCompat.getColor(requireContext(), R.color.pastel_rainbow6),
            ContextCompat.getColor(requireContext(), R.color.pastel_rainbow7)
        ) // ✅ 각 카테고리별 색상 설정

        dataSet.valueTextSize = 16F
        dataSet.setDrawValues(true)  // 백분율 표시

        val pieData = PieData(dataSet)

        binding.pieChart.apply {
            data = pieData
            setUsePercentValues(true)                       // 백분율 표시
            description.isEnabled = false                   // 차트 설명 비활성화
            legend.isEnabled = true                         // 범례 활성화
            isRotationEnabled = false                       // 차트 회전 활성화
            setEntryLabelColor(Color.BLACK)                 // 라벨 색상
            animateY(1400, Easing.EaseInOutQuad) // 애니메이션 설정
        }
    }

    // 월 선택 다이얼로그 표시
    private fun showMonthPickerDialog() {
        DatePickerUtils.showMonthPicker(requireContext(), currentCalendar) { newCalendar ->
            currentCalendar = newCalendar // ✅ 현재 선택된 날짜 업데이트
            updateMonthDisplay()
        }
    }

    // 현재 월을  "YYYYMM" 형식으로 반환
    private fun getCurrentMonthKey(): String {
        return SimpleDateFormat("yyyyMM", Locale.getDefault()).format(currentCalendar.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

