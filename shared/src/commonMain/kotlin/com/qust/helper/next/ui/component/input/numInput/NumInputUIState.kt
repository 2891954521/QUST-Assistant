package com.qust.helper.next.ui.component.input.numInput

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.math.BigDecimal


/**
 *
 * @see PopNumInputUIState
 *
 * @param defaultContent 初始值
 * @param enableDot 是否允许输入小数点
 * @param onUpdate 输入内容后的回调
 * @param onDone 点击确定后的回调
 */
open class NumInputUIState(
    defaultContent: String = "0",
    val enableDot: Boolean = true,
    val enableNegative: Boolean = false,
    val onUpdate: (String) -> Unit = { },
    val onDone: (String) -> Unit
) {

    /**
     * 当前输入的值
     *
     * 需要设置已输入的内容请使用 [setValue]
     *
     * @see setValue
     */
    var content by mutableStateOf("")
        internal set

    var hasDot = false
        internal set

    private val sb = StringBuilder()

    init {
        setValue(defaultContent)
    }

    /**
     * 设置内容
     */
    fun setValue(content: String){
        try {
            setString(BigDecimal(content).stripTrailingZeros().toPlainString())
            this.content = content
        }catch(_: Exception){ }
    }

    /**
     * 点击确认
     */
    open fun clickDone(){
        val result = getResult()
        if(result != null){
            content = result
            onDone(result)
        }
    }


    /**
     * 追加内容
     */
    fun append(char: Char): String? {
        when(char){
            '\b' -> {
                if(sb.isNotEmpty()){
                    val ch = sb[sb.length - 1]
                    if(ch == '.') hasDot = false
                    sb.setLength(sb.length - 1)
                    return sb.toString()
                }
            }

            '.' -> {
                if(!hasDot){
                    if(sb.isEmpty()) sb.append('0')
                    sb.append(char)
                    hasDot = true
                    return sb.toString()
                }
            }

            '-' -> {
                if(sb.isNotEmpty() && sb[0] == '-'){
                    sb.deleteCharAt(0)
                }else{
                    sb.insert(0, '-')
                }
                return sb.toString()
            }

            else -> {
                if(sb.length == 1 && sb[sb.length - 1] == '0') sb.setLength(0)
                sb.append(char)
                return sb.toString()
            }
        }
        return null
    }

    /**
     * 获取结果
     */
    protected fun getResult(): String? {
        if(sb.isEmpty()) return "0"
        try{
            val s = sb.toString()
            if(s == "-") return null
            val num = BigDecimal(s).stripTrailingZeros().toPlainString()
            setString(num)
            return num
        }catch(_: Exception){
            return null
        }
    }

    private fun setString(content: String){
        sb.clear()
        sb.append(content)
        hasDot = '.' in content
    }
}