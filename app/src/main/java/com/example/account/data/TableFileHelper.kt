package com.example.account.data

import android.content.Context
import android.util.Log
import java.io.File

object TableFileHelper {
    private const val FILE_NAME = "tables.csv"

    // 수익 / 지출 정보를 CSV 파일에 저장
    fun saveTables(context: Context, members: List<Table>) {
        val TAG = "TableFileHelper_save_TEST"
        val file = File(context.filesDir, FILE_NAME)
        file.printWriter().use { out ->
            members.forEach {
                out.println("${it.no},${it.category},${it.description},${it.amount},${it.date},${it.kind}")
                if (members.isNotEmpty()) {
                    Log.d(TAG,"🚀 CSV 데이터 로드 완료: ${members.size}개")
                } else {
                    Log.e(TAG,"⚠️ CSV 데이터가 비어 있음")
                }
            }
        }

    }

    // CSV 파일에서 수익 / 지출 목록 불러오기
    fun loadTables(context: Context): MutableList<Table> {
        val tables = mutableListOf<Table>()
        val file = File(context.filesDir, FILE_NAME)

        if (!file.exists()) return tables // 파일이 없으면 빈 목록 반환

        file.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(",")
                if (parts.size == 6) {
                    val no = parts[0].toIntOrNull() ?: return@forEach
                    val category = parts[1]
                    val description = parts[2]
                    val amount = parts[3].trim().toIntOrNull() ?: 0
                    val date = parts[4]
                    val kind = parts[5]

                    val table = Table(no, category, description, amount, date, kind)
                    tables.add(table)
                }
            }
        }
        val TAG = "TableFileHepler_load_TEST"

        if (tables.isNotEmpty()) {
            Log.d(TAG,"🚀 CSV 데이터 로드 완료: ${tables.size}개")
        } else {
            Log.e(TAG,"⚠️ CSV 데이터가 비어 있음")
        }
        return tables
    }
}