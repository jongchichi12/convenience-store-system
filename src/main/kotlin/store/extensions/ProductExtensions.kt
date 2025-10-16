package store.extensions

import store.model.Product
import java.time.LocalDate
import java.time.temporal.ChronoUnit
// product의 클래스를 변경하지않고 함수 추가
/** 남은 유통기한 일수 (null이면 null, 이미 지난 경우 음수 가능) */
fun Product.daysLeft(today: LocalDate): Int? =
    expiryDate?.let { ChronoUnit.DAYS.between(today, it).toInt() }

/** 유통기한 임박 여부 (남은 일수가 0..warningDays 범위) */
fun Product.isExpiringSoon(today: LocalDate, warningDays: Int): Boolean {
    val d = daysLeft(today) ?: return false
    return d in 0..warningDays
}

/**
 * 할인율 계산.
 *  - 정책키: 남은 일수
 *  - 예: {3->0.0, 2->0.3, 1->0.5, 0->0.7}
 *  - 정책에 키가 없으면 0.0
 */
fun Product.discountRate(today: LocalDate, policy: Map<Int, Double>): Double {
    val d = daysLeft(today) ?: return 0.0
    return policy[d] ?: if (d >= (policy.keys.maxOrNull() ?: 0)) 0.0 else 0.0
}

/** 할인가(원) = ceil(price * (1 - rate)) */
fun Product.discountedPrice(today: LocalDate, policy: Map<Int, Double>): Int {
    val rate = discountRate(today, policy)
    return kotlin.math.ceil(price * (1 - rate)).toInt()
}

/** 재고율 = (현재재고 / 적정재고) */
fun Product.stockRate(): Double =
    if (targetStock <= 0) 0.0 else stock.toDouble() / targetStock.toDouble()