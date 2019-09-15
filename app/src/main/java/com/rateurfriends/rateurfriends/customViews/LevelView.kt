package com.rateurfriends.rateurfriends.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.models.User




class LevelView@JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0

) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    var levelText: String = ""
        set(value) {
            field = value
            drawLevels()
        }

    init {

        LayoutInflater.from(context).inflate(R.layout.level_view, this, true)
        orientation = HORIZONTAL

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.LevelView, 0, 0)
            typedArray.recycle()
        }
    }

    private fun drawLevels() {

        this.removeAllViews()

        if (levelText in User.levelArray) {

            val list = levelText.split(" ")
            val fileName = list[0].trim()
            val number = list[1].toInt()

            val imageId = resources.getIdentifier(fileName, "drawable", context.getPackageName())
            val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            lp.setMargins(2, 0, 2, 0)

            for (i in 1..number) {

                val imageView = ImageView(context)
                imageView.setImageResource(imageId)
                this.addView(imageView)
                imageView.layoutParams = lp
                imageView.layoutParams.width = this.layoutParams.width / 3 - 4
                imageView.layoutParams.height = this.layoutParams.width / 3 - 4

            }

            this.gravity = Gravity.CENTER
        }
    }

}