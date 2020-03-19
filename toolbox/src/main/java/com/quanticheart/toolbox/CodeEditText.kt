/*
 *
 *  *                                     /@
 *  *                      __        __   /\/
 *  *                     /==\      /  \_/\/
 *  *                   /======\    \/\__ \__
 *  *                 /==/\  /\==\    /\_|__ \
 *  *              /==/    ||    \=\ / / / /_/
 *  *            /=/    /\ || /\   \=\/ /
 *  *         /===/   /   \||/   \   \===\
 *  *       /===/   /_________________ \===\
 *  *    /====/   / |                /  \====\
 *  *  /====/   /   |  _________    /      \===\
 *  *  /==/   /     | /   /  \ / / /         /===/
 *  * |===| /       |/   /____/ / /         /===/
 *  *  \==\             /\   / / /          /===/
 *  *  \===\__    \    /  \ / / /   /      /===/   ____                    __  _         __  __                __
 *  *    \==\ \    \\ /____/   /_\ //     /===/   / __ \__  ______  ____ _/ /_(_)____   / / / /__  ____ ______/ /_
 *  *    \===\ \   \\\\\\\/   ///////     /===/  / / / / / / / __ \/ __ `/ __/ / ___/  / /_/ / _ \/ __ `/ ___/ __/
 *  *      \==\/     \\\\/ / //////       /==/  / /_/ / /_/ / / / / /_/ / /_/ / /__   / __  /  __/ /_/ / /  / /_
 *  *      \==\     _ \\/ / /////        |==/   \___\_\__,_/_/ /_/\__,_/\__/_/\___/  /_/ /_/\___/\__,_/_/   \__/
 *  *        \==\  / \ / / ///          /===/
 *  *        \==\ /   / / /________/    /==/
 *  *          \==\  /               | /==/
 *  *          \=\  /________________|/=/
 *  *            \==\     _____     /==/
 *  *           / \===\   \   /   /===/
 *  *          / / /\===\  \_/  /===/
 *  *         / / /   \====\ /====/
 *  *        / / /      \===|===/
 *  *        |/_/         \===/
 *  *                       =
 *  *
 *  * Copyright(c) Developed by John Alves at 2020/3/18 at 7:37:59 for quantic heart studios
 *
 */
@file:Suppress("DEPRECATION", "unused")

package com.quanticheart.toolbox

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.quanticheart.toolbox.extentions.forEach
import com.quanticheart.toolbox.interfaces.OnCodeCompleteListener

class CodeEditText : AppCompatEditText, TextWatcher {

    /**
     * Callback for java
     */
    private var callback: OnCodeCompleteListener? = null

    /**
     * Paint
     */
    private lateinit var mLinesPaint: Paint
    private lateinit var mStrokePaint: Paint
    private lateinit var mTextPaint: Paint

    /**
     * Default values
     */
    private var textLength: Int = 0
    private lateinit var textWidths: FloatArray
    private var mMaskInput = false
    private var defStyleAttr = 0
    private var mMaxLength = 6
    private var mPrimaryColor = 0
    private var mSecondaryColor = 0
    private var mTextColor = 0
    private var mLineStrokeSelected = 2f //2dp by default
    private var mLineStroke = 1f //1dp by default
    private var mSpace = 18f //24 dp by default, space between the lines
    private var mCharSize = 0f
    private var mNumChars = 6f
    private var mLineSpacing = 10f //8dp by default, height of the text from our lines
    private var mMaskCharacter = ""
    /**
     * Styles
     */
    private val roundedBox = "rounded_box"
    private val squareBox = "square_box"
    private val underline = "underline"
    private val roundedUnderline = "rounded_underline"
    private var mBoxStyle: String? = roundedBox

    /**
     * Constructors
     */

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.defStyleAttr = defStyleAttr
        init(attrs)
    }

    /**
     * init functions
     */

    private fun init(attrs: AttributeSet) {
        configEditText()
        getAttrsFromTypedArray(attrs)

        val multi = resources.displayMetrics.density
        mLineStroke *= multi
        mLineStrokeSelected *= multi
        mLinesPaint = Paint(paint)
        mLinesPaint.strokeWidth = mLineStroke
        setBackgroundResource(0)
        mSpace *= multi //convert to pixels for our density
        mNumChars = mMaxLength.toFloat()
        //Disable copy paste
        super.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }
        })

        // When tapped, move cursor to end of text.
        super.setOnClickListener {
            setSelection(text!!.length)
        }
    }

    /**
     * Set Default config in edittext
     */
    private fun configEditText() {
        this.apply {
            isCursorVisible = false
            isClickable = false
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            keyListener = DigitsKeyListener.getInstance("0123456789")
            filters = arrayOf<InputFilter>(LengthFilter(mMaxLength))
        }
    }

    /**
     * create TypeArray
     */
    @SuppressLint("CustomViewStyleable")
    private fun getAttrsFromTypedArray(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CodeEditText, defStyleAttr, 0)
        mPrimaryColor = a.getColor(R.styleable.CodeEditText_code_primary_color, resources.getColor(R.color.default_orange))
        mSecondaryColor = a.getColor(R.styleable.CodeEditText_code_secondary_color, resources.getColor(R.color.light_gray))

        mBoxStyle = getStyleByEnum(a.getInt(R.styleable.CodeEditText_code_box_style, 0))

        (a.getString(R.styleable.CodeEditText_code_mask_character))?.substring(0, 1)?.let {
            mMaskCharacter = it
            mMaskInput = true
        }

        a.getColor(R.styleable.CodeEditText_code_text_color, resources.getColor(R.color.default_text_color)).let {
            mTextPaint = paint
            mTextColor = it
            mTextPaint.color = mTextColor
        }

        setBoxStyle()
        a.recycle()
    }

    /**
     * get style by Attrs/xml
     */

    private fun getStyleByEnum(styleCode: Int?): String {
        return when (styleCode) {
            0 -> roundedBox
            1 -> squareBox
            2 -> roundedUnderline
            3 -> underline
            else -> roundedBox
        }
    }

    /**
     * create box style
     */

    private fun setBoxStyle() {
        if (mBoxStyle != null && mBoxStyle!!.isNotEmpty()) {
            when (mBoxStyle) {
                underline, roundedUnderline -> {
                    setLineStyleView()
                }
                squareBox, roundedBox -> {
                    setBoxStyleView()
                }
                else -> {
                    setBoxStyleView()
                    mBoxStyle = roundedBox
                }
            }
        } else {
            setBoxStyleView()
            mBoxStyle = roundedBox
        }
    }

    private fun setBoxStyleView() {
        mStrokePaint = Paint(paint)
        mStrokePaint.strokeWidth = 4f
        mStrokePaint.style = Paint.Style.STROKE
    }

    private fun setLineStyleView() {
        mStrokePaint = Paint(paint)
        mStrokePaint.strokeWidth = 4f
        mStrokePaint.style = Paint.Style.FILL
    }

    override fun setCustomSelectionActionModeCallback(actionModeCallback: ActionMode.Callback) {
        throw RuntimeException("setCustomSelectionActionModeCallback() not supported.")
    }

    /**
     * On view create
     */
    private lateinit var canvas: Canvas
    private var startX: Int = 0

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        this.canvas = canvas
        drawView(canvas)
    }

    private fun drawView(canvas: Canvas) {
        val availableWidth = width - paddingRight - paddingLeft
        mCharSize = if (mSpace < 0) {
            availableWidth / (mNumChars * 2 - 1)
        } else {
            (availableWidth - mSpace * (mNumChars - 1)) / mNumChars
        }
        mLineSpacing = (height * .6).toFloat()

        this.startX = paddingLeft
        val bottom = height - paddingBottom
        val top = paddingTop

        textLength = text?.length ?: 0
        textWidths = FloatArray(textLength)

        paint.getTextWidths(text, 0, textLength, textWidths)
        mNumChars.forEach { i ->
            updateColorForLines(i <= textLength, i == textLength)
            when (mBoxStyle) {
                roundedUnderline -> try {
                    canvas.drawRoundRect(startX.toFloat(), bottom * .95f, startX + mCharSize, bottom.toFloat(), 16f, 16f, mStrokePaint)
                } catch (err: NoSuchMethodError) {
                    canvas.drawRect(startX.toFloat(), bottom * .95f, startX + mCharSize, bottom.toFloat(), mStrokePaint)
                }
                roundedBox -> try {
                    canvas.drawRoundRect(startX.toFloat(), top.toFloat(), startX + mCharSize, bottom.toFloat(), 8f, 8f, mLinesPaint)
                    canvas.drawRoundRect(startX.toFloat(), top.toFloat(), startX + mCharSize, bottom.toFloat(), 8f, 8f, mStrokePaint)
                } catch (err: NoSuchMethodError) {
                    canvas.drawRect(startX.toFloat(), top.toFloat(), startX + mCharSize, bottom.toFloat(), mLinesPaint)
                    canvas.drawRect(startX.toFloat(), top.toFloat(), startX + mCharSize, bottom.toFloat(), mStrokePaint)
                }
                underline -> canvas.drawRect(startX.toFloat(), bottom.toFloat() * .95f, startX + mCharSize, bottom.toFloat(), mStrokePaint)
                squareBox -> {
                    canvas.drawRect(startX.toFloat(), top.toFloat(), startX + mCharSize, bottom.toFloat(), mLinesPaint)
                    canvas.drawRect(startX.toFloat(), top.toFloat(), startX + mCharSize, bottom.toFloat(), mStrokePaint)
                }
                else -> {
                    canvas.drawRoundRect(startX.toFloat(), top.toFloat(), startX + mCharSize, bottom.toFloat(), 8f, 8f, mLinesPaint)
                    canvas.drawRoundRect(startX.toFloat(), top.toFloat(), startX + mCharSize, bottom.toFloat(), 8f, 8f, mStrokePaint)
                }
            }
            paintText(i)
            startX += if (mSpace < 0) {
                (mCharSize * 2).toInt()
            } else {
                (mCharSize + mSpace.toInt()).toInt()
            }
        }
    }

    /**
     * For paint text
     */
    private fun paintText(i: Int) {
        if (text!!.length > i) {
            val middle = startX + mCharSize / 2
            if (mMaskInput) {
                canvas.drawText(maskText, i, i + 1, middle - textWidths[0] / 2, mLineSpacing, paint)
            } else {
                canvas.drawText(text.toString(), i, i + 1, middle - textWidths[0] / 2, mLineSpacing, paint)
            }
        }
    }

    private val maskText: String
        get() {
            val length = text.toString().length
            val out = StringBuilder()
            for (i in 0 until length) {
                out.append(mMaskCharacter)
            }
            return out.toString()
        }

    /**
     * @param next Is the current char the next character to be input?
     */
    private fun updateColorForLines(next: Boolean, current: Boolean) {
        if (next) {
            mStrokePaint.color = mSecondaryColor
            mLinesPaint.color = mSecondaryColor
        } else {
            mStrokePaint.color = mSecondaryColor
            mLinesPaint.color = ContextCompat.getColor(context, android.R.color.white)
        }
        if (current) {
            mLinesPaint.color = ContextCompat.getColor(context, android.R.color.white)
            mStrokePaint.color = mPrimaryColor
        }
    }

    /**
     * show caracters
     */

    fun showCode() {
        mMaskInput = !mMaskInput
        val t = text.toString()
        removeTextChangedListener(textWatcher)
        setText(t)
        addTextChangedListener(textWatcher)
    }

    private var textWatcher: TextWatcher? = null
    /**
     * Callback
     */
    fun setOnCompleteListener(value: (String) -> Unit) {
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length.toFloat() == mNumChars) {
                    value(s.toString())
                }
            }
        }
        this.addTextChangedListener(textWatcher)
    }

    fun setOnCompleteListenerValidation(validation: (String, Boolean) -> Unit) {
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length.toFloat() == mNumChars) {
                    validation(s.toString(), true)
                } else {
                    validation(s.toString(), false)
                }
            }
        }
        this.addTextChangedListener(textWatcher)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable) {
        if (s.length.toFloat() == mNumChars) {
            callback?.code(s.toString())
            callback?.onCompleteCode(true)
        } else {
            callback?.onCompleteCode(false)
        }
    }

    fun setOnCompleteListener(listener: OnCodeCompleteListener) {
        callback = listener
        this.addTextChangedListener(this)
    }
}