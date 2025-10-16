package com.bible

import java.util.Scanner
import kotlin.random.Random

fun main() {
    var a = max(55,30)
    println(a)
    var name : String = "김종환"
    println("제 이름은 ${name} 입니다")
    val random = Random.nextInt(1,99)
    println(random)
/*    val input = Scanner(System.`in`)
    var se : Int = input.nextInt()*/
    /*println(se)*/
    val list = listOf(1,2,3,4)
    for(i in 0.. (list.size-1))
        println(list[i])
    var c : Int? = null
    var w : Int = 2

}

fun max(a : Int, b : Int) : Int{
    return if (a>b) a else b
}

