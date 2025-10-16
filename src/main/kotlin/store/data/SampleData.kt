package store.data

import store.model.Product
import store.model.ProductCategory
import java.time.LocalDate

// 샘플 상품 제네릭 리스트에 담음 이거 object 한번 물어봐야 할듯
object SampleData {

    val products: List<Product> = listOf(
        Product("새우깡",        1500, ProductCategory.SNACK,    30, 5,  null),
        Product("콜라 500ml",   1500, ProductCategory.BEVERAGE, 25, 8,  null),
        Product("김치찌개 도시락", 5500, ProductCategory.FOOD,     20, 3,  LocalDate.now().plusDays(2)),
        Product("참치마요 삼각김밥", 1500, ProductCategory.FOOD,     15, 12, LocalDate.now().plusDays(1)),
        Product("딸기 샌드위치",  2800, ProductCategory.FOOD,     10, 2,  LocalDate.now()),
        Product("물 500ml",     1000, ProductCategory.BEVERAGE, 50, 25, null),
        Product("초코파이",      3000, ProductCategory.SNACK,    20, 15, LocalDate.now().plusDays(1)),
        Product("즉석라면",      1200, ProductCategory.FOOD,     40, 45, LocalDate.now().plusDays(30)),
    )
    // 오늘 판매량 예시로 입력 (상품명 -> 수량)
    val todaySales: Map<String, Int> = mapOf(
        "새우깡" to 15,
        "콜라 500ml" to 12,
        "참치마요 삼각김밥" to 10,
        "초코파이" to 8,
        "물 500ml" to 7,
        "딸기 샌드위치" to 3,
        "김치찌개 도시락" to 2,
    )

    // 시스템 설정값 과제에 나온 설정 그대로 사용
    const val stockThreshold: Double = 0.30      // 재고율 30% 이하 시 경고
    const val expiryWarningDays: Int = 3         // 3일 이내 임박

    // 남은 일수에 맞게 할인률 적용
    val discountPolicy: Map<Int, Double> = mapOf(
        3 to 0.0,   // 3일 이상: 0%
        2 to 0.3,   // 2일 남음: 30%
        1 to 0.5,   // 1일 남음: 50%
        0 to 0.7    // 당일: 70%
    )
}
