package com.example.account.model

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.account.R
import com.example.account.data.UserManager  // 사용자 정보를 관리하는 가정
import com.example.account.model.LoginActivity  // 로그아웃 후 로그인 화면으로 이동

class AccountDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_account, null)

        // ✅ 사용자 정보 가져오기 (임시 데이터 또는 SharedPreferences 사용)
        val userName = UserManager.getUserName(requireContext())  // 사용자의 이름
        val userId = UserManager.getUserId(requireContext())      // 사용자의 아이디
        val userJoinDate = UserManager.getJoinDate(requireContext())  // 가입일

        // ✅ UI 요소 연결
        val tvName = view.findViewById<TextView>(R.id.tvUserName)
        val tvId = view.findViewById<TextView>(R.id.tvUserId)
        val tvJoinDate = view.findViewById<TextView>(R.id.tvUserJoinDate)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        tvName.text = userName
        tvId.text = userId
        tvJoinDate.text = userJoinDate

        // ✅ 로그아웃 버튼 클릭 시 처리
        btnLogout.setOnClickListener {
            logoutUser()
        }

        builder.setView(view)
        return builder.create()
    }

    // ✅ 로그아웃 기능
    private fun logoutUser() {
        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()  // 저장된 사용자 데이터 삭제

        // 로그인 화면으로 이동
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()  // 현재 액티비티 종료 (로그아웃 효과)
    }
}
