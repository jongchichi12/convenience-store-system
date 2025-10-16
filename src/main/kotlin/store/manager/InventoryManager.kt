package store.manager

import store.data.SampleData
import store.extensions.*
import store.model.Product
import store.util.pct
import store.util.won
import java.time.LocalDate

/**
 * Phase 3 – 시스템 통합 매니저
 *  - 재고 경고
 *  - 유통기한 관리 + 할인
 *  - 베스트셀러 TOP 5
 *  - 매출 요약
 *  - 경영 분석 리포트
 *  - 종합 현황
 */
class InventoryManager(
    private val products: List<Product>,
    private val todaySales: Map<String, Int>,
    private val stockThreshold: Double = SampleData.stockThreshold,
    private val expiryWarningDays: Int = SampleData.expiryWarningDays,
    private val discountPolicy: Map<Int, Double> = SampleData.discountPolicy,
    private val today: LocalDate = LocalDate.now()
) {
    private val pByName = products.associateBy { it.name }

    /** 콘솔 리포트 전체 실행 */
    fun runReport() {
        println("=== 24시간 학교 편의점 스마트 재고 관리 시스템 ===\n")

        sectionLowStock()
        sectionExpiry()
        sectionTop5()
        sectionSalesSummary()
        sectionBizInsights()
        sectionTotals()
    }

    /** 🚨 긴급 재고 알림 */
    private fun sectionLowStock() {
        println("긴급 재고 알림 (재고율 ${(stockThreshold * 100).toInt()}% 이하)")
        val low = products
            .map { it to it.stockRate() }
            .filter { (_, rate) -> rate <= stockThreshold }
            .sortedBy { it.second }

        if (low.isEmpty()) {
            println("- 해당 없음\n")
            return
        }

        low.forEach { (p, rate) ->
            val need = (p.targetStock - p.stock).coerceAtLeast(0)
            println("- ${p.name}(${p.category.kor}): 현재 ${p.stock}개 → 적정재고 ${p.targetStock}개 (${need}개 발주 필요) [재고율: ${pct(rate)}]")
        }
        println()
    }

    /** ⚠ 유통기한 임박 + 할인 */
    private fun sectionExpiry() {
        println("⚠  유통기한 관리 (${expiryWarningDays}일 이내 임박 상품)")

        val soon = products
            .filter { it.isExpiringSoon(today, expiryWarningDays) }
            .sortedBy { it.daysLeft(today) ?: Int.MAX_VALUE }

        if (soon.isEmpty()) {
            println("- 해당 없음\n")
            return
        }

        soon.forEach { p ->
            val d = p.daysLeft(today)!!
            val rate = p.discountRate(today, discountPolicy)
            val discounted = p.discountedPrice(today, discountPolicy)
            val label = when (d) {
                0 -> "당일까지"
                1 -> "1일 남음"
                2 -> "2일 남음"
                else -> "${d}일 남음"
            }
            println("- ${p.name}: $label → 할인률 ${(rate * 100).toInt()}% 적용 (${won(p.price)} → ${won(discounted)})")
        }
        println()
    }

    /** 📈 TOP 5 베스트셀러 */
    private fun sectionTop5() {
        println("📈  오늘의 베스트셀러 TOP 5")
        val top = todaySales.entries
            .sortedByDescending { it.value }
            .take(5)

        var rank = 1
        top.forEach { (name, qty) ->
            val price = pByName[name]?.price ?: 0
            val revenue = price * qty
            println("${rank}위: $name (${qty}개 판매, 매출 ${won(revenue)})")
            rank++
        }
        println()
    }

    /** 💰 매출 요약 */
    private fun sectionSalesSummary() {
        println("💰  매출 현황")

        val lines = todaySales.entries.sortedByDescending { it.value }
        val totalQty = lines.sumOf { it.value }
        val totalRevenue = lines.sumOf { (name, qty) -> (pByName[name]?.price ?: 0) * qty }
        println("- 오늘 총 매출: ${won(totalRevenue)} (${totalQty}개 판매)")
        lines.forEach { (name, qty) ->
            val price = pByName[name]?.price ?: 0
            println("  * $name: ${won(price * qty)} (${qty}개 × ${won(price)})")
        }
        println()
    }

    /** 🎯 경영 분석 */
    private fun sectionBizInsights() {
        println("🎯  경영 분석 리포트 (입력 데이터 기반 분석)")

        // 편의상: 회전율/효율 = (오늘 판매량 / 현재 재고) * 100
        // - 회전율 최고: '유통기한 있는 상품' 중 최대
        val expirable = products.filter { it.expiryDate != null }
        val salesOf: (Product) -> Int = { p -> todaySales[p.name] ?: 0 }
        val turnover: (Product) -> Double = { p ->
            val stock = if (p.stock == 0) 1 else p.stock
            salesOf(p) / stock.toDouble()
        }

        val bestTurnover = expirable.maxByOrNull(turnover)
        if (bestTurnover != null) {
            println("- 재고 회전율 최고: ${bestTurnover.name} (재고 ${bestTurnover.stock}개, 판매 ${salesOf(bestTurnover)}개 → ${pct(turnover(bestTurnover))} 회전)")
        }

        val worstTurnover = products.minByOrNull(turnover)
        if (worstTurnover != null) {
            println("- 재고 회전율 최저: ${worstTurnover.name} (재고 ${worstTurnover.stock}개, 판매 ${salesOf(worstTurnover)}개 → ${pct(turnover(worstTurnover))} 회전)")
        }

        val bestEfficiency = products.maxByOrNull(turnover)
        if (bestEfficiency != null) {
            println("- 판매 효율 1위: ${bestEfficiency.name} (재고 ${bestEfficiency.stock}개로 ${salesOf(bestEfficiency)}개 판매 → ${pct(turnover(bestEfficiency))} 효율)")
        }

        // 과다 재고: 적정재고 대비 초과
        val overStock = products.filter { it.stock > it.targetStock }
            .sortedByDescending { it.stock - it.targetStock }
        if (overStock.isNotEmpty()) {
            val label = overStock.joinToString(", ") { "${it.name} (${it.stock}개)" }
            println("- 재고 과다 품목: $label")
        } else {
            println("- 재고 과다 품목: 없음")
        }

        // 발주 권장 (재고율 기준)
        val toOrder = products.filter { it.stockRate() <= stockThreshold }
        val totalOrderQty = toOrder.sumOf { (it.targetStock - it.stock).coerceAtLeast(0) }
        println("- 발주 권장: 총 ${toOrder.size}개 품목, ${totalOrderQty}개 수량")
        println()
    }

    /** 📋 종합 현황 */
    private fun sectionTotals() {
        println("종합 운영 현황 (시스템 처리 결과)")

        val totalKinds = products.size
        val totalStock = products.sumOf { it.stock }
        val stockValue = products.sumOf { it.stock * it.price }
        val lowCount = products.count { it.stockRate() <= stockThreshold }
        val expiringCount = products.count { it.isExpiringSoon(today, expiryWarningDays) }
        val totalSalesQty = todaySales.values.sum()

        println("- 전체 등록 상품: ${totalKinds}종")
        println("- 현재 총 재고: ${totalStock}개")
        println("- 현재 재고가치: ${won(stockValue)}")
        println("- 재고 부족 상품: ${lowCount}종 (${(stockThreshold * 100).toInt()}% 이하)")
        println("- 유통기한 임박: ${expiringCount}종 (${expiryWarningDays}일 이내)")
        println("- 오늘 총 판매: ${totalSalesQty}개")
        println("- 시스템 처리 완료: 100%")
        println()
    }
}