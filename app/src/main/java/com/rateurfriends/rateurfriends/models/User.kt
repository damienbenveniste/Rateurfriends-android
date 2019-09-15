package com.rateurfriends.rateurfriends.models

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.rateurfriends.rateurfriends.helperClasses.Globals
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min


@Parcelize
data class User (
        var userName: String = "",

        val phoneNumber: String = "",

        val userId: String = "",

        var country: String = "",

        val timeStamp: Long = System.currentTimeMillis() / 1000L,

        var totalStarNumber: Int = Category.initialCategories.size * 5,

        var totalVoteNumber: Int = Category.initialCategories.size

): Parcelable {

        companion object {

                @get:Exclude
                val levelArray: ArrayList<String> = arrayListOf(
                        "bubble 1", "bubble 2", "bubble 3",
                        "snowflake 1", "snowflake 2", "snowflake 3",
                        "bronze_medal 1", "bronze_medal 2", "bronze_medal 3",
                        "silver_medal 1", "silver_medal 2", "silver_medal 3",
                        "gold_medal 1", "gold_medal 2", "gold_medal 3",
                        "burning_medal 1", "burning_medal 2", "burning_medal 3",
                        "emerald_stone 1", "emerald_stone 2", "emerald_stone 3",
                        "sapphire_stone 1", "sapphire_stone 2", "sapphire_stone 3",
                        "ruby_stone 1", "ruby_stone 2", "ruby_stone 3",
                        "diamond_stone 1", "diamond_stone 2", "diamond_stone 3",
                        "pink_orb 1", "pink_orb 2", "pink_orb 3",
                        "blue_orb 1", "blue_orb 2", "blue_orb 3",
                        "orange_orb 1", "orange_orb 2", "orange_orb 3",
                        "black_orb 1", "black_orb 2", "black_orb 3",
                        "orange_flame 1", "orange_flame 2", "orange_flame 3",
                        "yellow_flame 1", "yellow_flame 2", "yellow_flame 3",
                        "red_flame 1", "red_flame 2", "red_flame 3",
                        "blue_flame 1", "blue_flame 2", "blue_flame 3",
                        "orange_flaming_star 1", "orange_flaming_star 2", "orange_flaming_star 3",
                        "yellow_flaming_star 1", "yellow_flaming_star 2", "yellow_flaming_star 3",
                        "red_flaming_star 1", "red_flaming_star 2", "red_flaming_star 3",
                        "blue_flaming_star 1", "blue_flaming_star 2", "blue_flaming_star 3",
                        "electric_star 1", "electric_star 2", "electric_star 3",
                        "black_hole 1", "black_hole 2", "black_hole 3",
                        "angel 1", "angel 2", "angel 3",
                        "dragon 1", "dragon 2", "dragon 3",
                        "demon 1", "demon 2", "demon 3"
                )
        }

        @IgnoredOnParcel
        var spareCategories: Int = 3
                get() = max(field, 0)

        @IgnoredOnParcel
        var spareStars: Int = 5
                get() = max(field, 0)

        @IgnoredOnParcel
        var meanStarNumber: Float = max(min(totalStarNumber.toFloat() / totalVoteNumber.toFloat(), 5f), 0f)
                get() = max(min(totalStarNumber.toFloat() / totalVoteNumber.toFloat(), 5f), 0f)

        @IgnoredOnParcel
        var level: String = getLevelName()
                get() = getLevelName()

        @Exclude
        fun levelNumber(): Int {
                return this.totalStarNumber / 1000
        }

        @Exclude
        private fun getNearestThousand(number: Int): Int {
                return (number) / 1000 * 1000
        }

        @Exclude
        fun getNearestStarThreshold(): Int {
                return getNearestThousand(this.totalStarNumber)
        }

        @Exclude
        private fun getLevelName(): String {
                var level = ""
                if (levelNumber() < levelArray.size && levelNumber() >= 0) {
                        level = levelArray[levelNumber()]
                }
                return level
        }

        @Exclude
        fun incrementSpareCategories(increment: Int) {
                this.spareCategories += increment
        }
}