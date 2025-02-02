package com.example.account.model

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.account.R
import com.example.account.adapter.TableAdapter
import com.example.account.data.Table
import com.example.account.data.TableManager
import com.example.account.data.TableFileHelper
import com.example.account.databinding.FragmentTableBinding
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TableFragment : Fragment() {
    private var _binding: FragmentTableBinding? = null // 뷰 바인딩 객체를 저장
    private val binding get() = _binding!!
    private lateinit var viewModel: TableViewModel  // TableViewModel을 사용하여 데이터 관리
    private lateinit var tableAdapter: TableAdapter // RecyclerView에서 데이터를 표시할 TableAdapter

    // 수입 / 지출 내역 추가
    private fun showAddTransactionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_transaction, null)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val etDescription = dialogView.findViewById<EditText>(R.id.et_description)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_amount)
        val btnSelectDate = dialogView.findViewById<Button>(R.id.btn_select_date)
        val rbIncome = dialogView.findViewById<RadioButton>(R.id.rb_income)
        val rbExpense = dialogView.findViewById<RadioButton>(R.id.rb_expense)

        // 날짜 선택 버튼 설정
        var selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) // 기본값 오늘 날짜
        btnSelectDate.text = selectedDate

        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                selectedDate = "$year-${month + 1}-$day"
                btnSelectDate.text = selectedDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("내역 추가")
            .setView(dialogView)
            .setPositiveButton("추가") { _, _ ->
                val category = spinnerCategory.selectedItem.toString()  // 선택된 카테고리 가져오기
                val description = etDescription.text.toString()
                val amount = etAmount.text.toString().toIntOrNull() ?: 0
                val kind = if (rbIncome.isChecked) "수익" else "지출"

                // 데이터 저장 및 CSV 저장
                val newTransaction = Table(
                    no = System.currentTimeMillis().toInt(),
                    category = category,
                    description = description,
                    amount = amount,
                    date = selectedDate,
                    kind = kind
                )

                TableManager.addTable(newTransaction)  // 데이터 저장
                TableFileHelper.saveTables(requireContext(), TableManager.filterIncomeAndExpense())  // CSV 저장

                Toast.makeText(requireContext(), "내역이 추가되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }

    // 프래그먼트 UI 생성
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTableBinding.inflate(inflater, container, false)
        return binding.root
    }

    // UI 설정 및 데이터 처리
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // toolbar를 ActionBar로 설정
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        // 메뉴 추가 (MenuProvider 사용)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_table, menu) // 메뉴 적용
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_search -> {
                        Toast.makeText(requireContext(), "검색 버튼 클릭됨", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_filter -> {
                        Toast.makeText(requireContext(), "필터 버튼 클릭됨", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_add -> {
                        Toast.makeText(requireContext(), "추가 버튼 클릭됨", Toast.LENGTH_SHORT).show()
                        showAddTransactionDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // RecyclerView 설정
        tableAdapter = TableAdapter()
        binding.recyclerViewTable.adapter = tableAdapter

        // 데이터 변경 시 RecyclerView 업데이트
        viewModel = ViewModelProvider(this)[TableViewModel::class.java]
        viewModel.filteredTransactions.observe(viewLifecycleOwner) {
            tableAdapter.submitList(it)
        }

        // 필터 버튼 클릭 시 데이터 변경
        binding.btnFilterIncome.setOnClickListener { viewModel.filterTransactions("수익") }
        binding.btnFilterExpense.setOnClickListener { viewModel.filterTransactions("지출") }
        binding.btnFilterNet.setOnClickListener { viewModel.filterTransactions("순수입") }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}