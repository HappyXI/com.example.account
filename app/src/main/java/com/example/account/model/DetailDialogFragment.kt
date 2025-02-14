package com.example.account.model

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.account.R
import com.example.account.data.Table
import com.example.account.databinding.DialogDetailBinding
import com.example.account.viewmodel.TableViewModel


class DetailDialogFragment(
    private val transaction: Table,
    private val ondismissListener: (() -> Unit)? = null // 다이얼로그 종료 후 실행할 콜백
    ) : DialogFragment() {

    private var _binding: DialogDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TableViewModel // ViewModel 선언

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        ondismissListener?.invoke() // 다이얼로그가 닫히면 새로고침 실행
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // viewModel 초기화
        viewModel = ViewModelProvider(requireActivity())[TableViewModel::class.java]

        // 다이얼로그 크기 설정
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // 거래 내역 데이터 설정
        binding.tvDate.text = "📅 날짜: ${transaction.date}"
        binding.tvAmount.text = "💰 금액: ${transaction.amount} 원"
        binding.tvCategory.text = "📂 카테고리: ${transaction.category}"
        binding.tvLocation.text = "📍 장소: ${transaction.description}"
        binding.tvMemo.text = "📝 메모: 없음"

        // 수정 버튼 클릭 시 내역 수정
        binding.btnEdit.setOnClickListener {
            dismiss() // 현재 상세 다이얼로그 닫기
            val editDialog = EditTransactionDialogFragment(transaction) // 수정 다이얼로그 띄우기
            editDialog.show(parentFragmentManager, "EditTransactionDialogFragment")
        }

        // 닫기 버튼 클릭 시 다이얼로그 닫기
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // 삭제 버튼 클릭 시 내역 삭제
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("삭제 확인")
                .setMessage("이 내역을 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    viewModel.removeTransaction(transaction.no) // 삭제 기능 추가
                    (requireActivity().supportFragmentManager.findFragmentByTag("TableFragment") as? TableFragment)?.updateRecyclerViewData() // TableFragment를 찾아서 새로고침 호출
                    dismiss() // 다이얼로그 닫기
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}