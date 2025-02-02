package com.example.account.data

object MemberManager {
    private val members = mutableListOf<Member>()

    // 모든 회원 목록 가져오기
    fun getMembers(): List<Member> {
        return members
    }

    // 회원 추가
    fun addMember(member: Member) {
        // 중복된 ID 확인 후 추가 (ID 중복 방지)
        if (members.none { it.id == member.id }) {
            members.add(member)
        }
    }

    // 회원 삭제
    fun removeMember(id: Int) {
        members.removeAll { it.id == id }
    }

    // 회원 목록 초기화
    fun clearMembers() {
        members.clear()
    }
}