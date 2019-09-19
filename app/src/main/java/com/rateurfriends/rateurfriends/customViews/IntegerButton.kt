package com.rateurfriends.rateurfriends.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import com.rateurfriends.rateurfriends.R
import android.text.Editable
import android.text.TextWatcher




class IntegerButton@JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0

) : LinearLayout(context, attrs, defStyle, defStyleRes), View.OnClickListener {

    internal var plusButton: AppCompatButton
    internal var minusButton: AppCompatButton
    internal var integerEditText: EditText
    internal var integerValue: Int = 0


    init {

        val view = LayoutInflater.from(context).inflate(R.layout.integer_button_view, this, true)
        orientation = HORIZONTAL

        minusButton = view.findViewById(R.id.bt_minus) as AppCompatButton
        integerEditText = view.findViewById(R.id.ed_integer_text) as EditText
        plusButton = view.findViewById(R.id.bt_plus) as AppCompatButton

        minusButton.setClickable(true)
        plusButton.setClickable(true)

        minusButton.setOnClickListener(this)
        plusButton.setOnClickListener(this)

        integerEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var value = 0
                if (s.length != 0) {
                    value = maxOf(s.toString().toInt(), 0)
                }
                integerValue = value
            }
        })

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.IntegerButton, 0, 0)
            typedArray.recycle()
        }
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.bt_minus -> decreaseInteger()
            R.id.bt_plus -> incrementInteger()
        }
    }

    private fun incrementInteger() {
        var value = 0
        val s = integerEditText.text
        if (s.length != 0) {
            value = maxOf(s.toString().toInt(), 0)
        }

        value += 1
        integerEditText.setText(value.toString())
        integerValue = value

    }

    private fun decreaseInteger() {
        var value = 0
        val s = integerEditText.text
        if (s.length != 0) {
            value = s.toString().toInt()
            value = maxOf(value - 1, 0)
        }

        integerEditText.setText(value.toString())
        integerValue = value
    }

}