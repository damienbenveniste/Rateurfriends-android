package com.rateurfriends.rateurfriends.models

class Product(

) {

    companion object {

        val skuList = listOf(
                "five_stars",
                "twenty_stars",
                "hundred_stars",
                "thousand_stars",
                "one_quality",
                "five_qualities",
                "twenty_qualities",
                "hundred_qualities",
                "thousand_qualities"
        )

        val spareStarsMap = mapOf(
                "five_stars" to 5,
                "twenty_stars" to 20,
                "hundred_stars" to 100,
                "thousand_stars" to 1000
        )

        val spareCategoriesMap = mapOf(
                "one_quality" to 1,
                "five_qualities" to 5,
                "twenty_qualities" to 20,
                "hundred_qualities" to 100,
                "thousand_qualities" to 1000
        )

    }
}