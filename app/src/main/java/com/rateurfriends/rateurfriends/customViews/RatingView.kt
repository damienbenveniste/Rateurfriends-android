package com.rateurfriends.rateurfriends.customViews

import android.widget.LinearLayout
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.rateurfriends.rateurfriends.R
import kotlinx.android.synthetic.main.rating_view.view.*


class RatingView@JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0

) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    var ratings: Double = 0.0
        set(value) {
            field = value
            draw_stars()
        }

    init {

        LayoutInflater.from(context).inflate(R.layout.rating_view, this, true)
        orientation = HORIZONTAL

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.RatingView, 0, 0)
            val title = resources.getText(typedArray
                    .getResourceId(R.styleable.RatingView_RatingViewTitle, R.string.component_one))

            typedArray.recycle()
        }
    }


    fun draw_stars() {

        val ratings_round = ratings.toInt()
        val starsList = arrayOf(first_star, second_star, third_star, forth_star, fifth_star)
        for (i in 0..(ratings_round - 1)) {
            starsList[i].setImageResource(R.drawable.star_full)
        }

        if (ratings_round != 5) {
            val fraction = ratings - ratings.toInt().toDouble()
            if (fraction > 0.25 && fraction < 0.75) {
                starsList[ratings_round].setImageResource(R.drawable.star_half)

            } else if (fraction <=  0.25){
                starsList[ratings_round].setImageResource(R.drawable.star_empty)
            } else {
                starsList[ratings_round].setImageResource(R.drawable.star_full)
            }
        }

        if (ratings_round < 4) {
            for (i in (ratings_round + 1)..4) {
                starsList[i].setImageResource(R.drawable.star_empty)
            }
        }
    }
}