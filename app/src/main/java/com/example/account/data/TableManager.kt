package com.example.account.data

object TableManager {
    private val tables = mutableListOf<Table>()

    // 수입 / 지출 목록 필터링해서 가져오기
    fun filterIncomeAndExpense(): List<Table> {
        val incomeList = tables.filter { it.kind == "수익" }
        val expenseList = tables.filter { it.kind == "지출" }

        println("[수익 목록]")
        incomeList.forEach {
            println("${it.no}. ${it.category} | ${it.description} | ${it.amount}원 | ${it.date}")
        }

        println("[지출 목록]")
        expenseList.forEach {
            println("${it.no}. ${it.category} | ${it.description} | ${it.amount}원 | ${it.date}")
        }

        return incomeList + expenseList
    }

    // 수입 / 지출 내역 추가
    fun addTable(table: Table) {
        tables.add(table)
    }

    // 수입 / 지출 내역 삭제
    fun removeTable(no: Int) {
        tables.removeAll { it.no == no }
    }

    // 수입 / 지출 목록 초기화
    fun clearTables() {
        tables.clear()
    }
}