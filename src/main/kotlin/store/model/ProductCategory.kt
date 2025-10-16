package store.model

/** 상품 카테고리 */
enum class ProductCategory(val kor: String) {
    BEVERAGE("음료"),
    SNACK("과자류"),
    FOOD("식품류"),
    GOODS("생활용품")
}