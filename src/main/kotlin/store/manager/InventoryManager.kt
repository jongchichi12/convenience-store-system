package store.manager

import store.data.SampleData
import store.extensions.*
import store.model.Product
import store.util.pct
import store.util.won
import java.time.LocalDate

/**
 * InventoryManager
 * ----------------------------------------------------------------------------
 * 콘솔 리포트를 “섹션” 단위로 출력하는 리포팅 엔진.
 *
 * - 입력: 상품 리스트(products), 금일 판매량(todaySales)
 * - 정책: 재고 임계치(stockThreshold), 유통기한 경고일(expiryWarningDays),
 *         할인 정책(discountPolicy)
 * - today: 기준 날짜(기본은 실행 시점 LocalDate.now())
 *
 * 출력 섹션
 *  1) 🚨 긴급 재고 알림
 *  2) ⚠  유통기한 임박 + 할인 적용
 *  3) 📈 오늘의 베스트셀러 TOP 5
 *  4) 💰 매출 현황
 *  5) 🎯 경영 분석 리포트
 *  6) 📋 종합 운영 현황
 */
class InventoryManager(
    private val products: List<Product>,                 // 분석 대상 상품 목록
    private val todaySales: Map<String, Int>,            // “상품명 → 오늘 판매 수량”
    private val stockThreshold: Double = SampleData.stockThreshold, // 재고율 경고 기준(예: 0.30)
    private val expiryWarningDays: Int = SampleData.expiryWarningDays, // 유통기한 경고일수(예: 3)
    private val discountPolicy: Map<Int, Double> = SampleData.discountPolicy, // 남은 일수→할인율
    private val today: LocalDate = LocalDate.now()       // 기준 날짜(교체 가능: 테스트/시뮬)
) {
    // 빠른 조회를 위해 “상품명 → 상품” 맵
    private val pByName = products.associateBy { it.name }

    /** 리포트 전체 실행(섹션 순서 고정) */
    fun runReport() {
        println("=== 24시간 학교 편의점 스마트 재고 관리 시스템 ===\n")
        sectionLowStock()
        sectionExpiry()
        sectionTop5()
        sectionSalesSummary()
        sectionBizInsights()
        sectionTotals()
    }

    // -------------------------------------------------------------------------
    // 1) 🚨 긴급 재고 알림
    //  - 재고율(stock/targetStock) <= stockThreshold 인 상품만 골라서 경고
    //  - 발주 필요 수량 = (적정재고 - 현재재고).음수면 0으로 처리
    // -------------------------------------------------------------------------
    private fun sectionLowStock() {
        println("긴급 재고 알림 (재고율 ${(stockThreshold * 100).toInt()}% 이하)")

        val low = products
            .map { it to it.stockRate() }             // (상품, 재고율) 튜플
            .filter { (_, rate) -> rate <= stockThreshold }
            .sortedBy { it.second }                   // 재고율 낮은 순

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

    // -------------------------------------------------------------------------
    // 2) ⚠ 유통기한 임박 + 할인
    //  - today 기준으로 남은 일수가 0..expiryWarningDays 인 상품만 표시
    //  - 할인율은 discountPolicy(남은일수→할인율)로 결정
    //  - 출력 포맷: (정가 → 할인가) 형태로 바로 비교 가능하게
    // -------------------------------------------------------------------------
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
            val d = p.daysLeft(today)!!                              // 여기선 null 아님
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

    // -------------------------------------------------------------------------
    // 3) 📈 오늘의 베스트셀러 TOP 5
    //  - todaySales를 판매 수량 내림차순 정렬 후 상위 5개
    //  - 매출액 = (단가 × 수량)
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // 4) 💰 매출 현황
    //  - 총 판매 수량/매출 합계를 먼저 보여주고, 품목별 상세 라인 출력
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // 5) 🎯 경영 분석 리포트
    //  - 회전율(간이): 판매량 / 현재재고 (0 분모 방지)
    //  - 유통기한 있는 상품 중 최고 회전율, 전체 최저/최고 효율, 과다재고, 발주 권장
    // -------------------------------------------------------------------------
    private fun sectionBizInsights() {
        println("🎯  경영 분석 리포트 (입력 데이터 기반 분석)")

        val expirable = products.filter { it.expiryDate != null }
        val salesOf: (Product) -> Int = { p -> todaySales[p.name] ?: 0 }
        val turnover: (Product) -> Double = { p ->
            val stock = if (p.stock == 0) 1 else p.stock          // 0-division 방지
            salesOf(p) / stock.toDouble()
        }

        expirable.maxByOrNull(turnover)?.let { bestTurnover ->
            println("- 재고 회전율 최고: ${bestTurnover.name} (재고 ${bestTurnover.stock}개, 판매 ${salesOf(bestTurnover)}개 → ${pct(turnover(bestTurnover))} 회전)")
        }

        products.minByOrNull(turnover)?.let { worstTurnover ->
            println("- 재고 회전율 최저: ${worstTurnover.name} (재고 ${worstTurnover.stock}개, 판매 ${salesOf(worstTurnover)}개 → ${pct(turnover(worstTurnover))} 회전)")
        }

        products.maxByOrNull(turnover)?.let { bestEfficiency ->
            println("- 판매 효율 1위: ${bestEfficiency.name} (재고 ${bestEfficiency.stock}개로 ${salesOf(bestEfficiency)}개 판매 → ${pct(turnover(bestEfficiency))} 효율)")
        }

        // 과다 재고: 적정재고 초과분 많은 순으로 표기
        val overStock = products
            .filter { it.stock > it.targetStock }
            .sortedByDescending { it.stock - it.targetStock }

        if (overStock.isNotEmpty()) {
            val label = overStock.joinToString(", ") { "${it.name} (${it.stock}개)" }
            println("- 재고 과다 품목: $label")
        } else {
            println("- 재고 과다 품목: 없음")
        }

        // 발주 권장: 재고율 기준 이하인 품목들의 총 발주 수량 집계
        val toOrder = products.filter { it.stockRate() <= stockThreshold }
        val totalOrderQty = toOrder.sumOf { (it.targetStock - it.stock).coerceAtLeast(0) }
        println("- 발주 권장: 총 ${toOrder.size}개 품목, ${totalOrderQty}개 수량")
        println()
    }

    // -------------------------------------------------------------------------
    // 6) 📋 종합 운영 현황
    //  - 전반적 규모(품목/재고/가치) + 위험/판매 요약치 한 번에 확인
    // -------------------------------------------------------------------------
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
