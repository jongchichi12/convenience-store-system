package store.util

import java.text.NumberFormat
import java.util.Locale

/** 통화 포맷 (₩) */
fun won(v: Int): String = NumberFormat.getCurrencyInstance(Locale.KOREA).format(v)

/** 퍼센트(소수점 1자리) */
fun pct(v: Double): String = "${"%.1f".format(v * 100)}%"