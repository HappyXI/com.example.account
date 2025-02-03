package com.example.account.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.account.data.Table
import com.example.account.data.TableFileHelper

// ViewModel을 상속받아, UI와 관련된 데이터를 관리
// ViewModel은 수명 주기를 관리하여 화면 회전 등의 상황에서도 데이터가 유지됨
class TableViewModel(application: Application) : AndroidViewModel(application) {
    // 안전한 Application Context 사용
    private val appContext = getApplication<Application>().applicationContext

    // LiveData 변수 선언
    private val _transactions = MutableLiveData<List<Table>>() // 전체 데이터 리스트
    val transactions: LiveData<List<Table>> get() = _transactions

    private val _filteredTransactions = MutableLiveData<List<Table>>() // 필터된 데이터 리스트
    val filteredTransactions: LiveData<List<Table>> get() = _filteredTransactions

    init {
        loadTransactionsFromCsv() // 앱 실행 시 csv에서 데이터 불러오기
    }

    private fun loadTransactionsFromCsv() {
        val data = TableFileHelper.loadTables(getApplication<Application>().applicationContext) // CSV에서 데이터 가져오기

        _transactions.postValue(data)
        _filteredTransactions.postValue(data)
    }

    fun addTransaction(newTransaction: Table) {
        val currentData = _transactions.value?.toMutableList() ?: mutableListOf()
        currentData.add(newTransaction)

        _transactions.value = currentData
        _filteredTransactions.value = currentData

        TableFileHelper.saveTables(appContext, currentData) // CSV에 저장

        loadTransactionsFromCsv()
    }

    // 특정 거래 삭제 (TableManager.removeTable() 대체)
    fun removeTransaction(no: Int) {
        val updatedData = _transactions.value?.filter { it.no != no } ?: emptyList()

        _transactions.value = updatedData
        _filteredTransactions.value = updatedData

        TableFileHelper.saveTables(appContext, updatedData) // CSV에 저장
    }

    // 전체 데이터 삭제 (TableManager.clearTables() 대체)
    fun clearAllTransactions() {
        _transactions.value = emptyList()
        _filteredTransactions.value = emptyList()

        TableFileHelper.saveTables(appContext, emptyList()) // CSV 데이터 초기화
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