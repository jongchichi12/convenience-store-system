package store.model

// 상품 카테고리 열거형으로 만들어 보기쉽고 타입 안정성 보장
enum class ProductCategory(val kor: String) {
    BEVERAGE("음료"),
    SNACK("과자류"),
    FOOD("식품류"),
    GOODS("생활용품") // 나중에 추가를 위한 생필품 카테고리도 추가해놓음
}
