package com.example.account.model

data class TableItem(
    val no: Int,             // 번호
    val category: String,    // 카테고리
    val description: String, // 내용
    val amount: Int,         // 금액
    val date: String         // 날짜
)