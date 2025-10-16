package store.model

import java.time.LocalDate

// ✅ 상품 정보를 담는 기본 데이터 클래스
// - name: 상품명 (고유 이름)
// - price: 상품의 정가(원 단위)
// - category: 상품이 속한 카테고리 (SNACK, BEVERAGE, FOOD, GOODS 등)
// - targetStock: 적정 재고 수량 (예: 목표 재고 30개)
// - stock: 현재 보유 재고 수량
// - expiryDate: 유통기한 (없을 경우 null, 즉 상시 판매 상품)
data class Product(
    val name: String,                 // 상품명
    val price: Int,                   // 정가 (원)
    val category: ProductCategory,    // 상품 카테고리
    val targetStock: Int,             // 적정 재고
    var stock: Int,                   // 현재 재고
    val expiryDate: LocalDate? = null // 유통기한 (null이면 상시판매)
)
