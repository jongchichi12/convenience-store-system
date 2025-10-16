package store.extensions

import store.model.Product
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// ✅ Product 클래스를 수정하지 않고, 기능만 확장하기 위해
//    Kotlin의 '확장 함수(Extension Function)' 문법을 활용.
//    Product와 관련된 계산, 판정, 할인 관련 로직들을 모듈화하였다.

/**
 * ▶ 남은 유통기한 일수를 계산하는 확장 함수
 *  - expiryDate가 null이면 null 반환 (유통기한 없는 상품)
 *  - 현재 날짜(today) 기준으로 남은 일수를 Int로 반환
 *  - 이미 지난 날짜면 음수값이 나올 수 있음 (예: -1일)
 */
fun Product.daysLeft(today: LocalDate): Int? =
    expiryDate?.let { ChronoUnit.DAYS.between(today, it).toInt() }

/**
 * ▶ 유통기한 임박 여부 판단 함수
 *  - 남은 일수가 0~warningDays 범위 안에 포함되면 true 반환
 *  - 예를 들어 warningDays=3이면, 3일 이하 남은 상품은 모두 “임박”으로 분류
 */
fun Product.isExpiringSoon(today: LocalDate, warningDays: Int): Boolean {
    val d = daysLeft(today) ?: return false
    return d in 0..warningDays
}

/**
 * ▶ 할인율 계산 함수
 *  - 정책 Map<Int, Double> 형태로 전달받음 (ex: {3→0.0, 2→0.3, 1→0.5, 0→0.7})
 *  - daysLeft(today)로 남은 일수를 구해서 대응되는 할인율 반환
 *  - 정책에 해당 키가 없거나 유통기한이 넉넉하면 0.0 반환 (할인 없음)
 */
fun Product.discountRate(today: LocalDate, policy: Map<Int, Double>): Double {
    val d = daysLeft(today) ?: return 0.0
    return policy[d] ?: if (d >= (policy.keys.maxOrNull() ?: 0)) 0.0 else 0.0
}

/**
 * ▶ 실제 할인된 금액 계산 함수
 *  - 원가(price)에 할인율(discountRate)을 적용하여 최종 금액 반환
 *  - 계산식: ceil(price × (1 - rate))
 *  - Kotlin의 ceil()을 사용해 올림 처리 (소수점 단위 원 방지)
 */
fun Product.discountedPrice(today: LocalDate, policy: Map<Int, Double>): Int {
    val rate = discountRate(today, policy)
    return kotlin.math.ceil(price * (1 - rate)).toInt()
}

/**
 * ▶ 재고율 계산 함수
 *  - 현재 재고(stock)를 적정 재고(targetStock)으로 나눈 비율
 *  - targetStock이 0 이하일 경우 0.0 반환 (0으로 나누기 방지)
 *  - 예: 재고 5개, 적정재고 20개 → 0.25 (25%)
 */
fun Product.stockRate(): Double =
    if (targetStock <= 0) 0.0 else stock.toDouble() / targetStock.toDouble()
