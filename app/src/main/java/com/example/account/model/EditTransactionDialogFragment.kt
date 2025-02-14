package com.example.account.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.account.R
import com.example.account.data.Table
import com.example.account.databinding.DialogEditTransactionBinding
import com.example.account.util.SpinnerUtils
import com.example.account.viewmodel.TableViewModel

class EditTransactionDialogFragment(private val transaction: Table) : DialogFragment() {

    private var _binding: DialogEditTransactionBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TableViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TableViewModel::class.java]

        // 기존 거래의 수익/지출 값을 기반으로 Spinner 목록 설정
        var selectedKind = transaction.kind
        SpinnerUtils.updateCategorySpinner(requireContext(), binding.spinnerCategory, selectedKind)

        // 기존 거래의 수익/지출 설정 (라디오버튼)
        if (transaction.kind == "수익") {
            binding.rbIncome.isChecked = true
        } else {
            binding.rbExpense.isChecked = true
        }

        // ✅ 라디오버튼 선택 시 카테고리 리스트 변경
        binding.rgKind.setOnCheckedChangeListener { _, checkedId ->
            selectedKind = if (checkedId == R.id.rb_income) "수익" else "지출"
            SpinnerUtils.updateCategorySpinner(requireContext(), binding.spinnerCategory, selectedKind)
        }

        // 기존 데이터 표시
        binding.etDescription.setText(transaction.description)
        binding.etAmount.setText(transaction.amount.toString())
        binding.etDate.setText(transaction.date)
        // 기존 지출 구분 값에 따라 라디오버튼 체크 설정
        if (transaction.kind == "수익") {
            binding.rbIncome.isChecked = true // 수익 버튼 체크
        } else {
            binding.rbExpense.isChecked = true // 지출 버튼 체크
        }


        // 수정 완료 버튼 클릭 시
        binding.btnSave.setOnClickListener {
            val updatedTransaction = transaction.copy(
                category = binding.spinnerCategory.selectedItem.toString(),
                description = binding.etDescription.text.toString(),
                amount = binding.etAmount.text.toString().toIntOrNull() ?: 0,
                date = binding.etDate.text.toString(),
                // 지출 / 구분 라디오버튼 체크에 따라 수익 / 지출 값 저장
                kind = if (binding.rbIncome.isChecked) "수익" else "지출"
            )

            viewModel.updateTransaction(updatedTransaction) // 수정된 데이터 저장
            (requireActivity().supportFragmentManager.findFragmentByTag("TableFragment") as? TableFragment)?.updateRecyclerViewData() // TableFragment를 찾아서 새로고침 호출
            dismiss() // 다이얼로그 닫기
        }

        // toolbar를 ActionBar로 설정
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        // 메뉴 추가 (MenuProvider 사용)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear() // 기존 메뉴 삭제
                menuInflater.inflate(R.menu.menu_edit, menu) // 메뉴 적용
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_cancel -> {
                        dismiss() // 취소 시 다이얼로그 닫기
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED) // 💡 수명 주기 상태 추가
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
