package com.example.account.model

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.account.data.Table
import com.example.account.databinding.DialogDetailBinding


class DetailDialogFragment(private val transaction: Table) : DialogFragment() {

    private var _binding: DialogDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 다이얼로그 크기 설정
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 거래 내역 데이터 설정
        binding.tvDate.text = "📅 날짜: ${transaction.date}"
        binding.tvAmount.text = "💰 금액: ${transaction.amount} 원"
        binding.tvCategory.text = "📂 카테고리: ${transaction.category}"
        binding.tvLocation.text = "📍 장소: ${transaction.description}"
        binding.tvMemo.text = "📝 메모: 없음"

        // 닫기 버튼 클릭 시 다이얼로그 닫기
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}