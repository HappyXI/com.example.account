package com.example.account.model

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.account.R
import com.example.account.util.DatePickerUtils
import com.example.account.databinding.FragmentSettingBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.account.viewmodel.TableViewModel

class SettingsFragment : Fragment(R.layout.fragment_setting) {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private var currentCalendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy년 M월", Locale.KOREAN)
    private lateinit var tableViewModel: TableViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tableViewModel = ViewModelProvider(this)[TableViewModel::class.java]
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedTargetExpense = sharedPreferences.getInt("target_expense", 0)

        // 월 변경 버튼 클릭 이벤트
        binding.tvCurrentMonth.setOnClickListener {
            showMonthPickerDialog()
        }

        binding.btnPrevMonth.setOnClickListener {
            changeMonth(-1)
        }

        binding.btnNextMonth.setOnClickListener {
            changeMonth(1)
        }

        // 기존 목표 지출액 표시
        binding.etTargetExpense.setText(savedTargetExpense.toString())

        // 목표 지출액 저장 버튼 클릭 시
        binding.btnSaveTarget.setOnClickListener {
            val targetExpense = binding.etTargetExpense.text.toString().toIntOrNull() ?: 0

            sharedPreferences.edit().putInt("target_expense", targetExpense).apply()

            // 저장 후 사용자에게 알림
            Toast.makeText(requireContext(), "목표 지출액이 설정되었습니다!", Toast.LENGTH_SHORT).show()
        }

        // ✅ "내 계정 확인" 버튼 클릭 시 팝업 띄우기
        view.findViewById<View>(R.id.btn_check_account).setOnClickListener {
            val dialog = AccountDialogFragment()
            dialog.show(childFragmentManager, "AccountDialog")
        }

        observeTransactions()
    }

    private fun changeMonth(offset:Int) {
        currentCalendar.add(Calendar.MONTH, offset)
        updateMonthDisplay()
    }

    private fun updateMonthDisplay() {
        view?.findViewById<View>(R.id.tv_current_month)?.let {
            (it as? android.widget.TextView)?.text = dateFormat.format(currentCalendar.time)
        }
        observeTransactions()
    }

    private fun observeTransactions() {
        tableViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            // ✅ 데이터를 활용하여 UI 업데이트 (예: 목표 지출액 대비 실시간 비교)
            val totalExpense = transactions.filter { it.kind == "지출" }.sumOf { it.amount }
            binding.tvCurrentMonth.text = "현재 지출: $totalExpense 원"
        }
    }

    private fun showMonthPickerDialog() {
        DatePickerUtils.showMonthPicker(requireContext(), currentCalendar) { newCalendar ->
            currentCalendar = newCalendar
            updateMonthDisplay()
        }
    }
}
