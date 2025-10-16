package store

import store.data.SampleData
import store.manager.InventoryManager

/** 실행 진입점 */
fun main() {
    val manager = InventoryManager(
        products = SampleData.products,
        todaySales = SampleData.todaySales
    )
    manager.runReport()
}