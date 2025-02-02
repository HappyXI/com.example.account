package com.example.account.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.account.data.Table

// ViewModel을 상속받아, UI와 관련된 데이터를 관리
// ViewModel은 수명 주기를 관리하여 화면 회전 등의 상황에서도 데이터가 유지됨
class TableViewModel : ViewModel() {
    // LiveData 변수 선언
    private val _transactions = MutableLiveData<List<Table>>() // 전체 데이터 리스트
    val transactions: LiveData<List<Table>> get() = _transactions

    private val _filteredTransactions = MutableLiveData<List<Table>>() // 필터된 데이터 리스트
    val filteredTransactions: LiveData<List<Table>> get() = _filteredTransactions

    // 앱이 실행될 때 전체 데이터 설정
    // 필터링하기 전 기본적으로 모든 거래 내역을 _filteredTransactions 저장
    fun setTransactions(data: List<Table>) {
        _transactions.value = data
        _filteredTransactions.value = data // 초기값은 전체 데이터
    }

    // 데이터 필터링
    fun filterTransactions(type: String) {
        _filteredTransactions.value = when (type) {
            "수익" -> _transactions.value?.filter { it.kind == "수익" }
            "지출" -> _transactions.value?.filter { it.kind == "지출" }
            "순수입" -> listOf(
                Table(0, "순수입", "총 수익 - 총 지출", getNetIncome(), "", "결과")
            )
            else -> _transactions.value // 전체 보기
        }
    }

    // 순수입 계산
    private fun getNetIncome(): Int {
        val income = _transactions.value?.filter { it.kind == "수익" }?.sumOf { it.amount } ?: 0
        val expense = _transactions.value?.filter { it.kind == "지출" }?.sumOf { it.amount } ?: 0
        return income - expense
    }
}