package com.example.account.model

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.account.R
import com.example.account.adapter.TableAdapter
import com.example.account.data.Table
import com.example.account.databinding.FragmentTableBinding
import java.lang.IllegalArgumentException
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

                // ViewModel를 통해 데이터 추가 ( 자동으로 CSV 저장 & UI 갱신 등)
                viewModel.addTransaction(newTransaction)

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
        tableAdapter = TableAdapter { transaction ->
            showDetailDialog(transaction) // 클릭하면 다이얼로그 띄우기
        }

        binding.recyclerViewTable.apply {
            layoutManager = LinearLayoutManager(requireContext()) // LayoutManager 추가
            adapter = tableAdapter
            setHasFixedSize(true) // 성능 최적화
        }

        // 데이터 변경 시 RecyclerView 업데이트
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TableViewModel::class.java)) {
                    return TableViewModel(requireActivity().application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        })[TableViewModel::class.java]

        // LiveData 관찰하여 RecyclerView 업데이트
        viewModel.filteredTransactions.observe(viewLifecycleOwner) { data ->
            Log.d("TABLE_FRAGMENT", "🔄 RecyclerView 업데이트 시도, 데이터 개수: ${data.size}")

            if (data.isNotEmpty()) {
                Log.d("TABLE_FRAGMENT", "✅ RecyclerView에 데이터 반영됨: ${data.size}개")
            } else {
                Log.e("TABLE_FRAGMENT", "⚠️ RecyclerView에 표시할 데이터 없음")
            }

            tableAdapter.submitList(data)
        }



        // 필터 버튼 클릭 시 데이터 변경
        binding.btnFilterIncome.setOnClickListener { viewModel.filterTransactions("수익") }
        binding.btnFilterExpense.setOnClickListener { viewModel.filterTransactions("지출") }
        binding.btnFilterNet.setOnClickListener { viewModel.filterTransactions("순수입") }
    }

    // 상세 내역 확인
    private fun showDetailDialog(transaction: Table) {
        val dialog = DetailDialogFragment(transaction)
        dialog.show(childFragmentManager, "DetailDialogFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}