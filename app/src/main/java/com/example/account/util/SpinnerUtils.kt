package com.example.account.util

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.account.R

object SpinnerUtils {
    // 수익/지출 선택에 따라 Spinner 카테고리 변경
    fun updateCategorySpinner(context: Context, spinner: Spinner, kind: String) {
        val categoryArray = if (kind == "수익") {
            context.resources.getStringArray(R.array.income_categories)
        } else {
            context.resources.getStringArray(R.array.expense_categories)
        }

        // ArrayAdapter 생성
        val categoryAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, categoryArray)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Spinner에 어댑터 적용
        spinner.adapter = categoryAdapter
    }
}