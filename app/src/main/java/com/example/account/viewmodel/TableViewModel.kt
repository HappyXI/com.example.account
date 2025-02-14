package com.example.account.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.account.data.Table
import com.example.account.util.TableFileHelper

// ViewModel을 상속받아, UI와 관련된 데이터를 관리
// ViewModel은 수명 주기를 관리하여 화면 회전 등의 상황에서도 데이터가 유지됨
class TableViewModel(application: Application) : AndroidViewModel(application) {
    // 안전한 Application Context 사용
    private val appContext = getApplication<Application>().applicationContext

    // LiveData 변수 선언
    private val _transactions = MutableLiveData<List<Table>>(emptyList()) // 전체 데이터 리스트
    val transactions: LiveData<List<Table>> get() = _transactions

    private val _filteredTransactions = MutableLiveData<List<Table>>(emptyList()) // 필터된 데이터 리스트
    val filteredTransactions: LiveData<List<Table>> get() = _filteredTransactions

    init {
        loadTransactionsFromCsv() // 앱 실행 시 csv에서 데이터 불러오기
    }

    private fun loadTransactionsFromCsv() {
        val data = TableFileHelper.loadTables(appContext)
        _transactions.postValue(data)
        _filteredTransactions.postValue(data)

        Log.d("TABLE_FRAGMENT", "📌 CSV 데이터 로드 완료: ${data.size}개")
    }

    //  거래 내역 추가
    fun addTransaction(newTransaction: Table) {
        val currentData = _transactions.value?.toMutableList() ?: mutableListOf()
        currentData.add(newTransaction)

        _transactions.value = currentData
        _filteredTransactions.value = currentData

        TableFileHelper.saveTables(appContext, currentData) // CSV에 저장

        //loadTransactionsFromCsv()
    }

    //  거래 내역 수정
    fun updateTransaction(updatedTransaction: Table) {
        val currentData = _transactions.value?.toMutableList() ?: mutableListOf()
        val index = currentData.indexOfFirst { it.no == updatedTransaction.no }

        if (index != -1) {
            currentData[index] = updatedTransaction
            _transactions.value = ArrayList(currentData) // ✅ 새로운 리스트로 강제 변경
            _filteredTransactions.value = ArrayList(currentData)

            TableFileHelper.saveTables(appContext, currentData) // ✅ CSV 파일 저장
            loadTransactionsFromCsv() // ✅ CSV 다시 불러오기 (RecyclerView 즉시 반영)
        }
    }

    // 특정 거래 삭제
    fun removeTransaction(no: Int) {
        val updatedData = _transactions.value?.filter { it.no != no } ?: emptyList()

        _transactions.value = ArrayList(updatedData)
        _filteredTransactions.value = ArrayList(updatedData)

        TableFileHelper.saveTables(appContext, updatedData) // CSV 파일 저장
        loadTransactionsFromCsv() // CSV 다시 불러오기 (RecyclerView 즉시 반영)
    }

    // 전체 데이터 삭제
    fun clearAllTransactions() {
        _transactions.value = emptyList()
        _filteredTransactions.value = emptyList()

        TableFileHelper.saveTables(appContext, emptyList()) // CSV 데이터 초기화
    }

    // 특정 월의 데이터만 필터링하는 함수 추가
    fun filterTransactionsByMonth(year: Int, month: Int) {
        val filteredData = _transactions.value?.filter { transaction ->
            val dateParts = transaction.date.split("-") // 날짜가 "YYYY-MM-DD" 형식이라고 가정
            if (dateParts.size == 3) {
                val transactionYear = dateParts[0].toInt()
                val transactionMonth = dateParts[1].toInt()
                transactionYear == year && transactionMonth == month
            } else {
                false
            }
        } ?: emptyList()

        _filteredTransactions.value = filteredData
    }

    // 내용 검색 함수 추가
    fun filterTransactionsByDescription(query: String) {
        val filteredData = _transactions.value?.filter { transaction ->
            transaction.description.contains(query, ignoreCase = true) // 검색어와 일치하는지 확인
        } ?: emptyList()

        _filteredTransactions.value = filteredData // LiveData 업데이트
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