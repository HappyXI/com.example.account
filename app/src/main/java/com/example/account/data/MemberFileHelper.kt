package com.example.account.data

import android.content.Context
import java.io.*

object MemberFileHelper {
    private const val FILE_NAME = "members.csv"

    // 회원 정보를 CSV 파일에 저장
    fun saveMembers(context: Context, members: List<Member>) {
        val file = File(context.filesDir, FILE_NAME)
        file.printWriter().use { out ->
            members.forEach {
                out.println("${it.id},${it.name},${it.email}")
            }
        }
    }

    // CSV 파일에서 회원 목록 불러오기
    fun loadMembers(context: Context): MutableList<Member> {
        val members = mutableListOf<Member>()
        val file = File(context.filesDir, FILE_NAME)

        if (!file.exists()) return members // 파일이 없으면 빈 목록 반환

        file.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(",")
                if (parts.size == 3) {
                    val id = parts[0].toIntOrNull() ?: return@forEach
                    val name = parts[1]
                    val email = parts[2]
                    members.add(Member(id, name, email))
                }
            }
        }
        return members
    }
}