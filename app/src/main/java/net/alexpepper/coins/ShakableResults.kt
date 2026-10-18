package net.alexpepper.coins

import kotlinx.serialization.Serializable

@Serializable
data class ShakableArguments(
    val coinCount: Int = 0,
    val d4DieCount: Int = 0,
    val d6DieCount: Int = 0,
    val d8DieCount: Int = 0,
    val d10DieCount: Int = 0,
    val d12DieCount: Int = 0,
    val d20DieCount: Int = 0,
    val d100DieCount: Int = 0
)

@Serializable
data class ShakableResults(
    val coins: List<String?>? = null,
    val dice: List<DieResult>? = null
)

@Serializable
data class DieResult(
    val type: String,
    val value: Int
)
