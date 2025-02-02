package com.example.account.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.account.R
import com.example.account.adapter.MemberAdapter
import com.example.account.data.MemberFileHelper
import com.example.account.data.Member

class MemberFragment : Fragment() {
    private lateinit var adapter: MemberAdapter
    private var members = mutableListOf<Member>()
    private var idCounter = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_member, container, false)

        val nameInput = view.findViewById<EditText>(R.id.etName)
        val emailInput = view.findViewById<EditText>(R.id.etEmail)
        val registerButton = view.findViewById<Button>(R.id.btnRegister)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        // 저장된 CSV 파일에서 회원 목록 불러오기
        members = MemberFileHelper.loadMembers(requireContext())

        // RecyclerView 설정
        adapter = MemberAdapter(members) { memberId ->
            members.removeAll { it.id == memberId }
            MemberFileHelper.saveMembers(requireContext(), members)
            updateMemberList()
        }

        members = MemberFileHelper.loadMembers(requireContext())
        if (members.isNotEmpty()) {
            idCounter = members.maxOf { it.id } + 1
        }

        adapter = MemberAdapter(members) { memberId ->
            members.removeAll { it.id == memberId }
            MemberFileHelper.saveMembers(requireContext(), members)
            updateMemberList()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        registerButton.setOnClickListener {
            val name = nameInput.text.toString()
            val email = emailInput.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty()) {
                val member = Member(idCounter++, name, email)
                members.add(member)
                MemberFileHelper.saveMembers(requireContext(), members)
                updateMemberList()
                nameInput.text.clear()
                emailInput.text.clear()
                Toast.makeText(requireContext(), "회원 등록 완료!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun updateMemberList() {
        adapter.updateMembers(members)
    }
}