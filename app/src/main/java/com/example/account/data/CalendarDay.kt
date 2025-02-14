package com.example.account.data

data class CalendarDay(
    val date: String,  // 날짜
    val income: Int,   // 수익
    val expense: Int,   // 지출
    val transactions: List<Table> = emptyList() // 거래 내역 리스트 추가
)

