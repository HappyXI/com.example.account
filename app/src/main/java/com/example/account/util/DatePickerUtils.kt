package com.example.account.util

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.widget.Button
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DatePickerUtils {
    // 날짜 선택기 표시
    fun showDatePicker(context: Context, button: Button, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(context, { _, year, month, day ->
            val localDate = LocalDate.of(year, month + 1, day) // LocalDate 객체 생성
            val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) // LocalDate → Date 변환
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) // 날짜 포맷 변환

            button.text = formattedDate   // 버튼에 선택된 날짜 표시
            onDateSelected(formattedDate) // 콜백을 통해 선택된 날짜 전달
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    // 월 선택기 표시
    fun showMonthPicker(context: Context, currentCalendar: Calendar, onMonthSelected: (Calendar) -> Unit) {
        val months = (1..12).map { "${currentCalendar.get(Calendar.YEAR)}년 ${it}월" }.toTypedArray()
        val selectedYear = currentCalendar.get(Calendar.YEAR)

        AlertDialog.Builder(context)
            .setTitle("월 선택")
            .setItems(months) { _, which ->
                currentCalendar.set(Calendar.MONTH, which) // 선택된 월로 변경
                currentCalendar.set(Calendar.YEAR, selectedYear) // 연도 유지
                onMonthSelected(currentCalendar) // 선택된 날짜 콜백 반환
            }
            .setNegativeButton("취소", null)
            .show()
    }

}