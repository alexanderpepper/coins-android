package net.alexpepper.coins

import kotlinx.serialization.Serializable

@Serializable
data class CoinResults(
    val coins: MutableList<String?>?
)
