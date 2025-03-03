package com.dcelysia.outsourceserviceproject.UI

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import com.dcelysia.outsourceserviceproject.R

/**
 * 验证码输入框,重写EditText的绘制方法实现。
 * @author RAE
 */
class CodeEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    private var mTextColor: Int = 0

    interface OnTextFinishListener {
        fun onTextFinish(text: CharSequence, length: Int)
    }

    // 输入的最大长度
    private var mMaxLength: Int = 4
    // 边框宽度
    private var mStrokeWidth: Int = 0
    // 边框高度
    private var mStrokeHeight: Int = 0
    // 边框之间的距离
    private var mStrokePadding: Int = 20

    private val mRect = Rect()

    // 输入结束监听
    private var mOnInputFinishListener: OnTextFinishListener? = null

    // 方框的背景
    private var mStrokeDrawable: Drawable? = null

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CodeEditText)
        val indexCount = typedArray.indexCount
        for (i in 0 until indexCount) {
            val index = typedArray.getIndex(i)
            when (index) {
                R.styleable.CodeEditText_strokeHeight -> mStrokeHeight = typedArray.getDimension(index, 60f).toInt()
                R.styleable.CodeEditText_strokeWidth -> mStrokeWidth = typedArray.getDimension(index, 60f).toInt()
                R.styleable.CodeEditText_strokePadding -> mStrokePadding = typedArray.getDimension(index, 20f).toInt()
                R.styleable.CodeEditText_strokeBackground -> mStrokeDrawable = typedArray.getDrawable(index)
                R.styleable.CodeEditText_strokeLength -> mMaxLength = typedArray.getInteger(index, 4)
            }
        }
        typedArray.recycle()

        mStrokeDrawable ?: throw NullPointerException("stroke drawable not allowed to be null!")

        setMaxLength(mMaxLength)
        isLongClickable = false
        // 去掉背景颜色
        setBackgroundColor(Color.TRANSPARENT)
        // 不显示光标
        isCursorVisible = false
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return false
    }

    /**
     * 设置最大长度
     */
    private fun setMaxLength(maxLength: Int) {
        if (maxLength >= 0) {
            filters = arrayOf(InputFilter.LengthFilter(maxLength))
        } else {
            filters = arrayOf()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var width = measuredWidth
        var height = measuredHeight
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        // 判断高度是否小于推荐高度
        if (height < mStrokeHeight) {
            height = mStrokeHeight
        }

        // 判断宽度是否小于推荐宽度
        val recommendWidth = mStrokeWidth * mMaxLength + mStrokePadding * (mMaxLength - 1)
        if (width < recommendWidth) {
            width = recommendWidth
        }

        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(width, widthMode),
            MeasureSpec.makeMeasureSpec(height, heightMode)
        )
    }

    override fun onDraw(canvas: Canvas) {
        mTextColor = currentTextColor
        setTextColor(Color.TRANSPARENT)
        super.onDraw(canvas)
        setTextColor(mTextColor)
        // 重绘背景颜色
        drawStrokeBackground(canvas)
        // 重绘文本
        drawText(canvas)
    }

    /**
     * 重绘背景
     */
    private fun drawStrokeBackground(canvas: Canvas) {
        // 绘制方框背景颜色
        mRect.set(0, 0, mStrokeWidth, mStrokeHeight)
        val count = canvas.saveCount
        canvas.save()
        for (i in 0 until mMaxLength) {
            mStrokeDrawable?.setBounds(mRect)
            mStrokeDrawable?.setState(intArrayOf(android.R.attr.state_enabled))
            mStrokeDrawable?.draw(canvas)
            val dx = mRect.right + mStrokePadding
            // 移动画布
            canvas.save()
            canvas.translate(dx.toFloat(), 0f)
        }

        canvas.restoreToCount(count)
        canvas.translate(0f, 0f)

        // 修复：确保激活索引不超过最大值
        val activatedIndex = editableText.length.coerceAtMost(mMaxLength - 1)
        mRect.left = (mStrokeWidth + mStrokePadding) * activatedIndex
        mRect.right = mRect.left + mStrokeWidth

        mStrokeDrawable?.setState(intArrayOf(android.R.attr.state_focused))
        mStrokeDrawable?.setBounds(mRect)
        mStrokeDrawable?.draw(canvas)
    }

    /**
     * 重绘文本
     */
    private fun drawText(canvas: Canvas) {
        val count = canvas.saveCount
        canvas.translate(0f, 0f)
        val length = editableText.length
        for (i in 0 until length) {
            val text = editableText[i].toString()
            val textPaint = paint
            textPaint.color = mTextColor
            // 获取文本大小
            textPaint.getTextBounds(text, 0, 1, mRect)
            // 计算(x,y) 坐标
            val x = mStrokeWidth / 2 + (mStrokeWidth + mStrokePadding) * i - mRect.centerX()
            val y = canvas.height / 2 + mRect.height() / 2
            canvas.drawText(text, x.toFloat(), y.toFloat(), textPaint)
        }
        canvas.restoreToCount(count)
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        // 当前文本长度
        val textLength = editableText.length

        if (textLength == mMaxLength) {
            hideSoftInput()
            mOnInputFinishListener?.onTextFinish(editableText.toString(), mMaxLength)
        }
    }

    fun hideSoftInput() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    /**
     * 设置输入完成监听
     */
    fun setOnTextFinishListener(onInputFinishListener: OnTextFinishListener) {
        this.mOnInputFinishListener = onInputFinishListener
    }
}