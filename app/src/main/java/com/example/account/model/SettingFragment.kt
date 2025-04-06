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
import android.util.Log
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

        // 초기 UI 업데이트 ( 현재 월 표시 및 목표 지출 불러오기)
        updateMonthDisplay()

        // 월 변경 버튼 클릭 이벤트
        binding.tvCurrentMonth.setOnClickListener { showMonthPickerDialog() }
        binding.btnPrevMonth.setOnClickListener { changeMonth(-1)}
        binding.btnNextMonth.setOnClickListener { changeMonth(1) }

        // "내 계정 확인" 버튼 클릭 시 팝업 띄우기
        binding.btnCheckAccount.setOnClickListener {
            val dialog = AccountDialogFragment()
            dialog.show(childFragmentManager, "AccountDialog")
        }

        // 목표 지출액 저장 버튼 클릭 시
        binding.btnSaveTarget.setOnClickListener {
            saveTargetExpense()
        }
    }

    private fun changeMonth(offset:Int) {
        currentCalendar.add(Calendar.MONTH, offset)
        updateMonthDisplay()
    }

    // 해당 월 UI 업데이트 +  목표 지출액 불러오기
    private fun updateMonthDisplay() {
        // 현재 월 키값 다시 계산
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentMonthKey = getCurrentMonthKey()

        // 해당 월 목표 지출액 불러오기
        val savedTargetExpense = sharedPreferences.getInt("target_expense_$currentMonthKey", 0)
        binding.etTargetExpense.setText(savedTargetExpense.toString())

        // 현재 월을 UI에 표시
        binding.tvCurrentMonth.text = dateFormat.format(currentCalendar.time)
    }

    // 목표 지출액 저장 함수
    private fun saveTargetExpense() {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentMonthKey = getCurrentMonthKey()
        val targetExpense = binding.etTargetExpense.text.toString().toIntOrNull() ?: 0

        Log.d("SettingFragment_targetExepense", "currentMonthKey = $currentMonthKey, targetExpense = $targetExpense")
        // 목표 지출액 저장
        sharedPreferences.edit().putInt("target_expense_$currentMonthKey", targetExpense).apply()

        Toast.makeText(requireContext(), "목표 지출액이 설정되었습니다!", Toast.LENGTH_SHORT).show()
    }

    // 월 선택기 다이얼로그 실행
    private fun showMonthPickerDialog() {
        DatePickerUtils.showMonthPicker(requireContext(), currentCalendar) { newCalendar ->
            currentCalendar = newCalendar
            updateMonthDisplay()
        }
    }

    // 현재 월을  "YYYYMM" 형식으로 반환
    private fun getCurrentMonthKey(): String {
        return SimpleDateFormat("yyyyMM", Locale.getDefault()).format(currentCalendar.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}
