package store.model

import java.time.LocalDate

/**
 * 기본 상품 도메인 (Phase 1)
 *
 * @param name         상품명(고유)
 * @param price        정가(원)
 * @param category     카테고리
 * @param targetStock  적정 재고(개)
 * @param stock        현재 재고(개)
 * @param expiryDate   유통기한(없으면 null)
 */
data class Product(
    val name: String,
    val price: Int,
    val category: ProductCategory,
    val targetStock: Int,
    var stock: Int,
    val expiryDate: LocalDate? = null
)