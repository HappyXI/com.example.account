package com.example.account.util

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

object extractTextFromImageUtils {
    // ML Kit의 OCR 기능을 이용하여 영수증의 이미지를 통해 수익 / 지출 내역 작성
    fun extractTextFromImage(context: Context, imageUri: Uri, onResult: (String) -> Unit) {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(context, imageUri)
            val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())// TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extractedText = visionText.text.trim()

                    Log.d("OCR_RESULT", "원본 OCR 인식 테스트:\n$extractedText")
                    onResult(visionText.text) // 콜백을 통해 반환
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "텍스트 인식 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "이미지 처리 중 오류 발생", Toast.LENGTH_SHORT).show()
        }
    }

    // OCR로 추출한 텍스트에서 날짜, 금액, 카테고리 자동 감지
    fun parseReceiptText(extractedText: String): Triple<String, Int, String> {
        var date = DatePickerUtils.getCurrentDate() // 기본값: 오늘 날짜
        var amount = 0
        var category = "기타"

        val cleanedText = extractedText.trim().replace("\\s+".toRegex(), " ")

        // OCR 결과를 한 줄씩 나누기
        val lines = extractedText.split("\n").map { it.trim() }

        //1. 가맹점명(사업체명) 추출
        var businessName = lines.firstOrNull()?.takeIf { it.length in 4..20 } ?: "" // 첫 줄을 가맹점명으로 추정

        //1-1 가맹점명에서 지점명(~점) 제거
        businessName = businessName.replace(Regex("""\)?\s*[가-힣A-Za-z\d]+점$"""), "")

        // ✅ 괄호 제거
        businessName = businessName.replace(Regex("""\)"""), "")

        businessName = cleanBusinessName(businessName)

        Log.d("OCR_CATEGORY_TEST", "가맹점명 감지 (정제 후): $businessName")

        //2. 사업체명 기반 카테고리 매칭
        val categroyMap = mapOf(
            // 음식 관련 (식비)
            "스타벅스" to "식비", "투썸플레이스" to "식비", "이디야" to "식비", "빽다방" to "식비",
            "BBQ" to "식비", "맥도날드" to "식비", "버거킹" to "식비", "롯데리아" to "식비", "KFC" to "식비",

            // 쇼핑 관련 (패션/쇼핑)
            "올리브영" to "패션/쇼핑", "나이키" to "패션/쇼핑", "디스커버리" to "패션/쇼핑", "ABC마트" to "패션/쇼핑",
            "H&M" to "패션/쇼핑", "ZARA" to "패션/쇼핑", "무신사" to "패션/쇼핑", "유니클로" to "패션/쇼핑",

            // 마트 & 편의점 (생활비)
            "이마트" to "생활비", "롯데마트" to "생활비", "홈플러스" to "생활비", "GS25" to "생활비",
            "CU" to "생활비", "세븐일레븐" to "생활비",

            // 교통비
            "주유소" to "교통비", "하이패스" to "교통비", "코레일" to "교통비", "KTX" to "교통비",
            "택시" to "교통비", "카카오T" to "교통비",

            // 도서 & 교육
            "교보문고" to "도서/교육", "영풍문고" to "도서/교육", "알라딘" to "도서/교육",

            // 의료비
            "병원" to "의료비", "약국" to "의료비"
        )

        // 가맹점명이 카테고리 맵에 포함되어 있으면 해당 카테고리 지정
        categroyMap.forEach{ (key, value) ->
            if(businessName.contains(key)) {
                category = value
            }
        }

        Log.d("ETFIIU_CATEGROY_DATA_1","category = $category")

        // 카테고리 추출 (간단한 키워드 기반 매칭)
        if (category == "기타") {
            category = when {
                cleanedText.contains("식비") || cleanedText.contains("식당") || cleanedText.contains("레스토랑") || cleanedText.contains("카페") -> "식비"
                cleanedText.contains("생활비") || cleanedText.contains("마트") || cleanedText.contains("편의점") -> "생활비"
                cleanedText.contains("교통비") || cleanedText.contains("주유소") || cleanedText.contains("택시") -> "교통비"
                cleanedText.contains("의료비") || cleanedText.contains("병원") || cleanedText.contains("약국") -> "의료비"
                cleanedText.contains("패션/쇼핑") || cleanedText.contains("의류") || cleanedText.contains("쇼핑") -> "패션/쇼핑"
                cleanedText.contains("문화/여가") || cleanedText.contains("영화") || cleanedText.contains("공연") -> "문화/여가"
                cleanedText.contains("도서/교육") || cleanedText.contains("도서") || cleanedText.contains("문구") -> "도서/교육"
                else -> "기타"
            }
        }

        Log.d("ETFIIU_CATEGROY_DATA_2","category = $category")

        // 날짜 추출 (YYYY-MM-DD 또는 YYYY/MM/DD 형태)
        val dateRegex = """(\d{4}[-/]\d{1,2}[-/]\d{1,2})""".toRegex()
        dateRegex.find(extractedText)?.let {
            date = it.value.replace("/", "-") // YYYY/MM/DD → YYYY-MM-DD 형식 변환
        }

        // 금액 추출
        val numberRegex = """\b\d{1,3}(,\d{3})+\b""".toRegex()

        var totalProductAmount = 0 // 상품 가격 총합
        var detectedFinalAmount = 0 // 결제 금액 감지 값

        // "결제금액" 또는 "승인금액"이 포함된 줄을 찾기
        for (line in lines) {
            val numbers = numberRegex.findAll(line).map { it.value.replace(",", "").toIntOrNull() ?: 0}.toList()

            Log.d("OCR_AMOUNT_TEST", "numbers: $numbers")
            // 상품 가격 & 수량 패턴 감지 (일반적으로 ""가격 수량 총액" 형태)
            if (numbers.size == 4) {
                val (price, quantity, total) = numbers

                // 상품 가격 * 수량 == 총액이면 유효한 상품 데이터로 판단
                if (price * quantity == total) {
                    totalProductAmount += total
                }
            }
            if (line.contains("결제금액") || line.contains("승인금액")) {
                val numbers = numberRegex.findAll(line).map { it.value.replace(",", "").toIntOrNull() ?: 0 }.toList()
                if (numbers.isNotEmpty()) {
                    detectedFinalAmount = numbers.last()
                    break
                }
            }
        }

        // ✅ 최종 검증: 상품 가격 총합 == 결제금액 ?
        if (detectedFinalAmount == totalProductAmount && detectedFinalAmount > 0) {
            amount = detectedFinalAmount // **일치하면 결제금액 사용**
        } else if (totalProductAmount > 0) {
            amount = totalProductAmount // **일치하지 않으면 상품 총액 사용**
        } else {
            // ✅ 기존 방식: 마지막 등장하는 숫자를 amount로 설정
            val matchedAmounts = numberRegex.findAll(extractedText)
                .map { it.value.replace(",", "").toIntOrNull() ?: 0 }
                .toList()

            if (matchedAmounts.isNotEmpty()) {
                amount = matchedAmounts.last()
            }
        }

        Log.d("OCR_AMOUNT_TEST", "상품 총합: $totalProductAmount, 감지된 결제금액: $detectedFinalAmount, 최종 선택 금액: $amount")

        return Triple(date, amount, category)
    }

    // 이미지 선택 다이얼로그 (카메라 & 갤러리)
    fun showImagePickerDialog(
        context: Context,
        cameraLauncher: ActivityResultLauncher<Uri>,
        galleryLauncher: ActivityResultLauncher<String>,
        imageUri: Uri
    ) {
        val options = arrayOf("카메라로 촬영", "갤러리에서 선택", "취소")

        AlertDialog.Builder(context)
            .setTitle("영수증 추가")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> cameraLauncher.launch(imageUri)   // 카메라 열기
                    1 -> galleryLauncher.launch("image/*") // 갤러리 열기
                }
            }
            .show()
    }

    fun cleanBusinessName(rawName: String): String {
        var name = rawName.trim()

        // ✅ 1. `~점`(지점명) 제거
        name = name.replace(Regex("""\)?\s*[가-힣A-Za-z\d]+점$"""), "")

        // ✅ 2. 괄호 제거
        name = name.replace(Regex("""[()]"""), "")

        // ✅ 3. 사전 등록된 브랜드명과 비교하여 가장 유사한 이름 선택
        val knownBrands = listOf(
            "올리브영", "스타벅스", "나이키", "디스커버리", "ABC마트", "H&M", "ZARA",
            "이마트", "롯데마트", "홈플러스", "GS25", "CU", "세븐일레븐",
            "교보문고", "영풍문고", "알라딘", "카카오T", "KTX"
        )

        // ✅ OCR 인식 결과와 가장 유사한 브랜드명 찾기
        name = findMostSimilarBrand(name, knownBrands) ?: name

        Log.d("OCR_CATEGORY_TEST", "최종 정제된 가맹점명: $name")
        return name
    }

    // ✅ 가장 유사한 브랜드명을 찾는 함수 (Jaccard + Levenshtein Distance)
    fun findMostSimilarBrand(input: String, brands: List<String>): String? {
        return brands.maxByOrNull {
            (jaccardSimilarity(it, input) + levenshteinSimilarity(it, input)) / 2
        }
    }

    // ✅ Jaccard Similarity (자카드 유사도) 계산
    fun jaccardSimilarity(str1: String, str2: String): Double {
        val set1 = str1.toSet()
        val set2 = str2.toSet()
        val intersection = set1.intersect(set2).size.toDouble()
        val union = set1.union(set2).size.toDouble()
        return if (union == 0.0) 0.0 else intersection / union
    }

    // ✅ Levenshtein Distance (편집 거리) 기반 유사도 계산
    fun levenshteinSimilarity(str1: String, str2: String): Double {
        val distance = levenshteinDistance(str1, str2)
        return 1.0 - (distance.toDouble() / maxOf(str1.length, str2.length))
    }

    // ✅ Levenshtein Distance 계산 (문자열 편집 거리)
    fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in s1.indices) dp[i][0] = i
        for (j in s2.indices) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    minOf(dp[i - 1][j - 1], dp[i][j - 1], dp[i - 1][j]) + 1
                }
            }
        }
        return dp[s1.length][s2.length]
    }
}