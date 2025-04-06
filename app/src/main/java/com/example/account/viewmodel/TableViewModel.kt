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
        Log.d("TABLE_LOAD_TEST", "📌 CSV 데이터 로드 개수: ${data.size}개")

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
    }

    //  거래 내역 수정
    fun updateTransaction(updatedTransaction: Table) {
        val currentData = _transactions.value?.toMutableList() ?: mutableListOf()
        val index = currentData.indexOfFirst { it.no == updatedTransaction.no }

        if (index != -1) {
            currentData[index] = updatedTransaction
            _transactions.value = ArrayList(currentData) // 새로운 리스트로 강제 변경
            _filteredTransactions.value = ArrayList(currentData)

            TableFileHelper.saveTables(appContext, currentData) // CSV 파일 저장
        }
    }

    // 특정 거래 삭제
    fun removeTransaction(no: Int) {
        val currentList = _transactions.value.orEmpty()
        val updatedList = currentList.filter { it.no != no }
        Log.d("ViewModel_Test","Before Delete - Transactions Count: ${currentList.size}")
        Log.d("ViewModel_Test","after Delete - Transactions Count: ${updatedList.size}")

        _transactions.value = ArrayList(updatedList)
        _filteredTransactions.value = ArrayList(updatedList)

        TableFileHelper.saveTables(appContext, updatedList) // CSV 파일 저장

        // LiveData 변경을 강제 트리거
        _transactions.postValue(ArrayList(updatedList))
        _filteredTransactions.postValue(ArrayList(updatedList))
    }

    // 전체 데이터 삭제
    fun clearAllTransactions() {
        _transactions.value = emptyList()
        _filteredTransactions.value = emptyList()

        TableFileHelper.saveTables(appContext, emptyList()) // CSV 데이터 초기화
    }

    // 특정 월의 데이터만 필터링하는 함수 추가
    fun filterTransactionsByMonth(year: Int, month: Int, type: String? = null) {
        val allTransactions = _transactions.value
        if (allTransactions == null) {
            Log.e("FILTER_TEST", "❌ 필터링 실패: _transactions가 아직 초기화되지 않음!")
            return
        }
        Log.d("TABLE_FILTER_TEST", "전체 거래 내역 개수 (필터링 시작 전) : ${allTransactions.size}")

        val filteredByMonth = _transactions.value?.filter { transaction ->
            Log.d("FILTER_TEST","Checking Transaction: ${transaction.date}")
            val normalizedDate = transaction.date.replace("/", "-").trim()
            val dateParts = normalizedDate.split("-") // 날짜가 "YYYY-MM-DD" 형식이라고 가정
            if (dateParts.size == 3) {
                val transactionYear = dateParts[0].toInt()
                val transactionMonth = dateParts[1].toInt()

                Log.d("FILTER_TEST","Checking Transaction: ${transaction.date} -> Year: $transactionYear, Month: $transactionMonth")

                transactionYear == year && transactionMonth == month
            } else {
                false
            }
        } ?: emptyList()

        Log.d("FILTER_TEST", "Filtered Count: ${filteredByMonth.size}")

        _filteredTransactions.value = when (type) {
            "수익" -> filteredByMonth.filter { it.kind == "수익" }
            "지출" -> filteredByMonth.filter { it.kind == "지출" }
            "손익계산" -> listOf(
                Table(0, "손익계산", "총 수익 - 총 지출", getNetIncome(), "", "결과")
            )
            else -> filteredByMonth // 전체 보기
        }
    }

    // 내용 검색 함수 추가
    fun filterTransactionsByDescription(query: String) {
        val filteredData = _transactions.value?.filter { transaction ->
            transaction.description.contains(query, ignoreCase = true) // 검색어와 일치하는지 확인
        } ?: emptyList()

        _filteredTransactions.value = filteredData // LiveData 업데이트
    }

    //카테고리별 수익/지출 종합을 계산하여 백분율 변환
    fun getCategoryWisePercentage(isExpense: Boolean, year: Int, month: Int): Map<String, Float> {
        val filteredTransactions = transactions.value?.filter { transaction ->
            val dateParts = transaction.date.split("-")
            if (dateParts.size == 3) {
                val transactionYear = dateParts[0].toInt()
                val transactionMonth = dateParts[1].toInt()
                transactionYear == year && transactionMonth == month
            } else {
                false
            }
        }?.filter {it.kind == if (isExpense) "지출" else "수익" } ?: emptyList()

        // 카테고리별 합계 계산
        val categoryTotalMap = filteredTransactions.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount }.toFloat() }

        // 전체 금액 합산
        val totalAmount = categoryTotalMap.values.sum()

        // 백분율 변환
        return if (totalAmount > 0) {
            categoryTotalMap.mapValues { (_, amount) -> (amount / totalAmount) * 100 }
        } else {
            emptyMap()
        }
    }


    // 손익 계산
    private fun getNetIncome(): Int {
        val income = _transactions.value?.filter { it.kind == "수익" }?.sumOf { it.amount } ?: 0
        val expense = _transactions.value?.filter { it.kind == "지출" }?.sumOf { it.amount } ?: 0
        return income - expense
    }
}