package store.util

import java.text.NumberFormat
import java.util.Locale

//  금액(정수)을 보기 좋게 '₩' 단위로 포맷하는 함수
// - NumberFormat.getCurrencyInstance(Locale.KOREA)를 사용하면
//   자동으로 "₩1,500" 형식으로 변환됨
// - 굳이 문자열 더하기로 처리하지 않고, 로케일에 맞춰 통화 단위를 표시하게 함
fun won(v: Int): String = NumberFormat.getCurrencyInstance(Locale.KOREA).format(v)

//  소수(Double)를 퍼센트 문자열로 변환하는 함수
// - 예: 0.25 → "25.0%"
// - "%.1f"를 사용해 소수점 1자리까지만 표시 (가독성 향상)
// - *100을 곱해서 실제 백분율로 변환 후 문자열 끝에 '%' 추가
fun pct(v: Double): String = "${"%.1f".format(v * 100)}%"
