package com.example.account.util

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.widget.Button
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Calendar
import java.util.Locale

object DatePickerUtils {
    // 1. 날짜 선택기 표시
    fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(context, { _, year, month, day ->
            val localDate = LocalDate.of(year, month + 1, day) // LocalDate 객체 생성
            val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) // LocalDate → Date 변환
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) // 날짜 포맷 변환

            onDateSelected(formattedDate) // 콜백을 통해 선택된 날짜 전달
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    // 2. 월 선택기 표시
    fun showMonthPicker(context: Context, currentCalendar: Calendar, onMonthSelected: (Calendar) -> Unit) {
        val months = (1..12).map { "${currentCalendar.get(Calendar.YEAR)}년 ${it}월" }.toTypedArray()
        val selectedYear = currentCalendar.get(Calendar.YEAR)

        AlertDialog.Builder(context)
            .setTitle("월 선택")
            .setItems(months) { _, which ->
                val newCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, which)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                onMonthSelected(newCalendar) // 선택된 날짜 콜백 반환
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 3. 현재 날짜 반환 (YYYY-MM-DD 형식)
    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return formatDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
    }

    // 4. 현재 월 반환(YYYY년 MM월 형식)
    fun getCurrentMonth(calendar: Calendar): String {
        return formatMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }

    // 5. 특정 연/월의 시작일과 마지막일 가져오기
    fun getMonthStartEnd(year: Int, month: Int): Pair<String, String> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1) // 해당 월의 1일 설정

        val startDate = formatDate(year, month, 1) // 해당 월의 1일 설정
        val endDate = formatDate(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) // 마지막일

        return Pair(startDate, endDate)
    }

    // 6. 날짜 포맷 변환 ( YYYY-MM-DD 형식)
    private fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day)
    }

    // 7. 날짜 포맷 변환 (YYYY년  MM월 형식)
    private fun formatMonth(year: Int, month: Int): String {
        return String.format(Locale.getDefault(), "%04d년 %02d월", year, month)
    }
}