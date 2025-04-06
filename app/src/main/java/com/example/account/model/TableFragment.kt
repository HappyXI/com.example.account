package com.example.account.model

import android.app.AlertDialog
import android.net.Uri
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
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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
import com.example.account.util.DatePickerUtils
import com.example.account.util.SpinnerUtils
import com.example.account.util.extractTextFromImageUtils
import com.example.account.util.extractTextFromImageUtils.showImagePickerDialog
import com.example.account.viewmodel.TableViewModel
import java.io.File
import java.lang.IllegalArgumentException
import java.util.Calendar

class TableFragment : Fragment() {

    private var _binding: FragmentTableBinding? = null // 뷰 바인딩 객체를 저장
    private val binding get() = _binding!!
    private lateinit var viewModel: TableViewModel  // TableViewModel을 사용하여 데이터 관리
    private lateinit var tableAdapter: TableAdapter // RecyclerView에서 데이터를 표시할 TableAdapter
    private var currentCalendar: Calendar = Calendar.getInstance()
    private var imageUri: Uri? = null
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> // 이미지 선택 (OCR) 관련 ActivityResultLauncher 추가
        uri?.let { showAddTransactionDialog(it) }
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success -> // 카메라 촬영 후 OCR 처리
        if(success && imageUri != null) {
            showAddTransactionDialog(imageUri!!) // Uri를 바로 전달
        } else {
            Toast.makeText(requireContext(), "사진 촬영 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // 수입 / 지출 내역 추가
    private fun showAddTransactionDialog(uri: Uri? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_transaction, null)
        val toolbar = dialogView.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val etDescription = dialogView.findViewById<EditText>(R.id.et_description)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_amount)
        val btnSelectDate = dialogView.findViewById<Button>(R.id.btn_select_date)
        val etDate = dialogView.findViewById<EditText>(R.id.et_date)
        val rbIncome = dialogView.findViewById<RadioButton>(R.id.rb_income)
        val rbExpense = dialogView.findViewById<RadioButton>(R.id.rb_expense)
        val imageViewReceipt = dialogView.findViewById<ImageView>(R.id.receiptImageView)
        val btnOcrInput = dialogView.findViewById<Button>(R.id.btn_ocr_input)

        // 날짜 선택 버튼 설정
        var selectedDate = DatePickerUtils.getCurrentDate()// SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) // 기본값 오늘 날짜
        etDate.setText(selectedDate)

        // 날짜 선택 버튼 클릭 리스너
        btnSelectDate.setOnClickListener {
            DatePickerUtils.showDatePicker(requireContext()) { date ->
                selectedDate = date
                etDate.setText(selectedDate)
            }
        }

        // 초기 카테고리 설정 (기본값: 수익)
        var selectedKind = "수익"
        rbIncome.isChecked = true
        SpinnerUtils.updateCategorySpinner(requireContext(), spinnerCategory, selectedKind)

        uri?.let {
            imageUri = it
            imageViewReceipt.setImageURI(it)
            imageViewReceipt.visibility = View.VISIBLE

            extractTextFromImageUtils.extractTextFromImage(requireContext(), uri) { extractedText ->
                val (date, amount, category) = extractTextFromImageUtils.parseReceiptText(extractedText)

                selectedDate = date

                Log.d("OCR DATA TEST", "date = " + date + ", amount = " + amount + ", category = " + category)

                // OCR을 이용해 영수증으로 추출한 데이터를 각 UI에 설정
                etDate.setText(selectedDate)
                etAmount.setText(amount.toString())

                // OCR을 이용해 영수증으로 입력 시 수익 / 지출 구분을 자동으로 "지출" 설정
                rbExpense.isChecked = true
                selectedKind = "지출"
                SpinnerUtils.updateCategorySpinner(requireContext(), spinnerCategory, selectedKind)
                //spinnerCategory.setSelection(getCategoryIndex(spinnerCategory, category))
                // Spinner의 선택된 항목을 OCR에서 감지된 category 값으로 실행
                val categoryIndex = getCategoryIndex(spinnerCategory, category)
                if (categoryIndex >= 0) {
                    spinnerCategory.setSelection(categoryIndex)
                }
            }
        }

        btnOcrInput.setOnClickListener {
            if (imageUri == null || imageUri?.toString().isNullOrEmpty()) {
                val file = File(requireContext().cacheDir, "captured_image.jpg")
                imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
            }

            val safeImageUri: Uri = imageUri!!

            showImagePickerDialog(
                requireContext(),
                cameraLauncher,
                imagePickerLauncher,
                safeImageUri
            )
        }

        // 라디오 버튼 선택 시 카테고리 리스트 변경
        // 라디오 버튼 수익 체크 시
        rbIncome.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedKind = "수익"
                SpinnerUtils.updateCategorySpinner(requireContext(), spinnerCategory, selectedKind)
            }
        }
        // 라디오 버튼 지출 체크 시
        rbExpense.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedKind = "지출"
                SpinnerUtils.updateCategorySpinner(requireContext(), spinnerCategory, selectedKind)
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("추가") { _, _ ->
                val category = spinnerCategory.selectedItem.toString()    // 선택된 카테고리
                val description = etDescription.text.toString()           // 내용
                val amount = etAmount.text.toString().toIntOrNull() ?: 0  // 금액
                val kind = if (rbIncome.isChecked) "수익" else "지출"      // 수익 / 지출 구분

                // 데이터 저장 및 CSV 저장
                val newTransaction = Table(
                    no = System.currentTimeMillis().toInt(),
                    category = category,
                    description = description,
                    amount = amount,
                    date = selectedDate,
                    kind = kind,
                    receiptImageUri = imageUri?.toString()

                )

                // ViewModel를 통해 데이터 추가 ( 자동으로 CSV 저장 & UI 갱신 등)
                viewModel.addTransaction(newTransaction)
                Toast.makeText(requireContext(), "내역이 추가되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .create()

        // 다이얼로그 'X'로 닫기
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_cancel -> {
                    dialog.dismiss()
                    true
                }
                else -> false
            }
        }
        dialog.show()
    }

    // 상세 내역 확인
    private fun showDetailDialog(transaction: Table) {
        val dialog = DetailDialogFragment(transaction) {
            updateRecyclerViewData() // 다이얼로그가 닫히면 RecyclerView 업데이트
        }
        dialog.show(childFragmentManager, "DetailDialogFragment")
    }

    // 1달 이동하는 함수
    private fun changeMonth(offset: Int) {
        Log.d("Date_test_change", "CurrentMonth: " + Calendar.MONTH + "offset : " + offset)
        currentCalendar.add(Calendar.MONTH, offset)
        Log.d("Date_test_change", "CurrentMonth: " + currentCalendar.get(Calendar.MONTH))
        updateMonthDisplay()
    }

    // 현재 월을 UI에 업데이트하는 함수
    private fun updateMonthDisplay() {
        Log.d("Date UI update","DatePickerUtils.getCurrentMonth(currentCalendar) = "+ DatePickerUtils.getCurrentMonth(currentCalendar))
        binding.tvCurrentMonth.text = DatePickerUtils.getCurrentMonth(currentCalendar) // 현재 월 표시
        updateRecyclerViewData()
    }

    // 특정 월을 선택하는 다이얼로그
    private fun showMonthPickerDialog() {
        DatePickerUtils.showMonthPicker(requireContext(), currentCalendar) { newCalendar ->
            currentCalendar = newCalendar
            updateMonthDisplay()
        }
    }

    // RecyclerView 데이터 업데이트 (월 기준 필터링)
    fun updateRecyclerViewData() {
        val selectedYear = currentCalendar.get(Calendar.YEAR)
        val selectedMonth = currentCalendar.get(Calendar.MONTH) + 1

        viewModel.filterTransactionsByMonth(selectedYear, selectedMonth) // 현재 월의 필터링

        // 필터 버튼 클릭 시 데이터 변경
        binding.btnFilterIncome.setOnClickListener { viewModel.filterTransactionsByMonth(selectedYear, selectedMonth,"수익") }
        binding.btnFilterExpense.setOnClickListener { viewModel.filterTransactionsByMonth(selectedYear, selectedMonth,"지출") }
        binding.btnFilterNet.setOnClickListener { viewModel.filterTransactionsByMonth(selectedYear, selectedMonth,"손익계산") }

        // RecyclerViewTable 즉시 새로고침
        binding.recyclerViewTable.adapter?.notifyDataSetChanged()
    }

    private fun getCategoryIndex(spinner: Spinner, category: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == category) {
                return i
            }
        }
        return 0 // 기본값
    }

    /************************************** Main **************************************/
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

        // ViewModel 초기화
        viewModel = ViewModelProvider(this)[TableViewModel::class.java]

        // LiveData가 변할 때만 필터링 실행
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isNotEmpty()) {
                Log.d("TABLE_NEW_FRAGMENT", "데이터 로드 완료: ${transactions.size}개")

                // 데이터가 로드되면 현재 월 기준으로 필터링 실행
                if (transactions.size > 1) {
                    val selectedYear = currentCalendar.get(Calendar.YEAR)
                    val selectedMonth = currentCalendar.get(Calendar.MONTH) + 1
                    viewModel.filterTransactionsByMonth(selectedYear,selectedMonth)
                }
            } else {
                Log.d("TABLE_NEW_FRAGMENT", "데이터 로드 실패")
            }
        }

        // 현재 월 필터링 적용(초기 데이터)
        // updateRecyclerViewData()

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
                        // 내용 검색
                        val editText = EditText(requireContext()).apply {
                            hint = "검색어를 입력하세요"
                        }

                        AlertDialog.Builder(requireContext())
                            .setTitle("검색")
                            .setView(editText)
                            .setPositiveButton("검색") { _, _ ->
                                val query = editText.text.toString().trim()
                                if (query.isNotEmpty()) {
                                    viewModel.filterTransactionsByDescription(query) // ✅ 검색 기능 실행
                                } else {
                                    updateMonthDisplay() // ✅ 검색어가 없으면 전체 목록 표시
                                }
                            }
                            .setNegativeButton("취소", null)
                            .show()
                        true
                    }
                    R.id.action_filter -> {
                        Toast.makeText(requireContext(), "필터 버튼 클릭됨", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_add -> {
                        showAddTransactionDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // updateRecyclerViewData()

        // 월 업데이트 UI 갱신
        updateMonthDisplay()

        // 이전/다음 달 이동 버튼
        binding.btnPrevMonth.setOnClickListener { changeMonth(-1) }     // 이전 달 버튼 클릭
        binding.btnNextMonth.setOnClickListener { changeMonth(1) }      // 다음 달 버튼 클릭
        binding.tvCurrentMonth.setOnClickListener { showMonthPickerDialog() } // 중앙 날짜 클릭 시 다이얼로그 띄우기

        // RecyclerView 설정
        tableAdapter = TableAdapter { transaction ->
            showDetailDialog(transaction) // 클릭하면 다이얼로그 띄우기
            // 다이얼로그가 닫힌 후 RecyclerView 새로고침
            updateRecyclerViewData()
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
            Log.d("Fragment_Test", "LiveData Changed - Transactions Count: ${data.size}")

            tableAdapter.submitList(null)            // 리스트 초기화
            tableAdapter.submitList(ArrayList(data))     // 필터링된 데이터 적용

            binding.recyclerViewTable.adapter?.notifyDataSetChanged()
            Log.d("TABLE_FRAGMENT", "RecyclerView 업데이트 시도, 데이터 개수: ${data.size}")

            if (data.isNotEmpty()) {
                Log.d("TABLE_FRAGMENT", "RecyclerView에 데이터 반영됨: ${data.size}개")
            } else {
                Log.e("TABLE_FRAGMENT", "RecyclerView에 표시할 데이터 없음")
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}