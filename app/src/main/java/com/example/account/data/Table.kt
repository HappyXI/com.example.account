package com.example.account.data

data class Table(
    val no: Int,                        // 번호
    val category: String,               // 카테고리
    val description: String,            // 내용
    val amount: Int,                    // 금액
    val date: String,                   // 날짜
    val kind: String,                    // 수익 / 지출 구분
    val receiptImageUri: String? = null // 영수증 이미지 경로 추가 (기본값: 없음)
)