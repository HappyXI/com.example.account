package com.example.account.model

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.account.R
import com.example.account.data.Table
import com.example.account.viewmodel.TableViewModel
import com.example.account.adapter.CalendarAdapter
import com.example.account.data.CalendarDay
import com.example.account.util.DatePickerUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private val tableViewModel: TableViewModel by lazy {
        ViewModelProvider(this)[TableViewModel::class.java]
    }
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var calendarRecyclerView: RecyclerView
    private var currentCalendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy년 M월", Locale.KOREAN)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView) // RecyclerView 찾기

        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)
        setupRecyclerView()
        observeTransactions()
        updateMonthDisplay()

        // 월 변경 버튼 클릭 이벤트
        view.findViewById<View>(R.id.btn_prev_month).setOnClickListener { changeMonth(-1) }
        view.findViewById<View>(R.id.btn_next_month).setOnClickListener { changeMonth(1) }
        view.findViewById<View>(R.id.tv_current_month).setOnClickListener { showMonthPickerDialog() }
    }

    private fun setupRecyclerView() {
        calendarAdapter = CalendarAdapter(emptyList()) { day ->
            // 날짜 클릭 시 상세 내역 표시 (예: 다이얼로그)
            showTransactionDetails(day)
        }

        calendarRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 7) // 7일(요일) 기준 그리드
            adapter = calendarAdapter
        }
    }

    private fun observeTransactions() {
        tableViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val calendarData = generateCalendarData(transactions)
            calendarAdapter.updateData(calendarData)
        }
    }

    private fun generateCalendarData(transactions: List<Table>): List<CalendarDay> {
        val groupedData = transactions.groupBy { it.date }
        val calendarList = mutableListOf<CalendarDay>()

        val firstDayOfMonth = Calendar.getInstance().apply {
            set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH),1)  // ✅ 2025년 1월 1일 기준
        }.get(Calendar.DAY_OF_WEEK) - 1 // 일요일(1) ~ 토요일(7) → 0부터 시작하도록 조정

        // ✅ 빈 칸 추가 (달력의 첫 요일 맞추기)
        for (i in 0 until firstDayOfMonth) {
            calendarList.add(CalendarDay("", 0, 0)) // 빈 날짜
        }

        for (day in 1..31) {
            val date = "${currentCalendar.get(Calendar.YEAR)}-${String.format("%02d", currentCalendar.get(Calendar.MONTH) + 1)}-${String.format("%02d", day)}"
            val transactionsForDay = groupedData[date] ?: emptyList()

            val income = transactionsForDay.filter { it.kind == "수익" }.sumOf { it.amount }
            val expense = transactionsForDay.filter { it.kind == "지출" }.sumOf { it.amount }

            calendarList.add(CalendarDay(date, income, expense, transactionsForDay))
        }

        return calendarList
    }

    private fun changeMonth(offset:Int) {
        currentCalendar.add(Calendar.MONTH, offset)
        updateMonthDisplay()
    }

    private fun updateMonthDisplay() {
        view?.findViewById<View>(R.id.tv_current_month)?.let {
            (it as? android.widget.TextView)?.text = dateFormat.format(currentCalendar.time)
        }
        observeTransactions()
    }

    private fun showMonthPickerDialog() {
        DatePickerUtils.showMonthPicker(requireContext(), currentCalendar) { newCalendar ->
            currentCalendar = newCalendar
            updateMonthDisplay()
        }
    }

    private fun showTransactionDetails(day: CalendarDay) {
        // 특정 날짜 클릭 시 다이얼로그나 새로운 화면으로 상세 내역 표시 가능
        if(day.income != 0 || day.expense != 0) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("${day.date} 내역")
            val TAG = "Calendar_DATA_TEST!!!!"
            val detailsList = mutableListOf<String>()

            if (day.income > 0) {
                detailsList.add("수익")
                for (transaction in day.transactions.filter { it.kind == "수익" }) {
                    Log.d(TAG, "LIST AMOUNT = ${String.format("%,d", transaction.amount)}원")
                    detailsList.add(
                        "  - ${String.format("%,d", transaction.amount)}원 ${transaction.description}"
                    )
                }
            }

            if (day.expense > 0) {
                detailsList.add("지출")
                for (transaction in day.transactions.filter { it.kind == "지출" }) {
                    detailsList.add(
                        "  - ${String.format("%,d", transaction.amount)}원 ${transaction.description}"
                    )
                }
            }
            val details = detailsList.joinToString("\n")  // ✅ 줄바꿈 적용

            builder.setMessage(details)
            builder.setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
            builder.show()
        }
    }
}