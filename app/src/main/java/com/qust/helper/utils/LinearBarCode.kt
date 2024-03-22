package com.qust.helper.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

/**
 * 条形码库
 */
object LinearBarCode {
	private const val CODE_START_A = 103
	private const val CODE_START_B = 104
	private const val CODE_START_C = 105
	private const val CODE_CODE_A = 101
	private const val CODE_CODE_B = 100
	private const val CODE_CODE_C = 99
	private const val CODE_STOP = 106

	// Dummy characters used to specify control characters in input
	private const val ESCAPE_FNC_1 = '\u00f1'
	private const val ESCAPE_FNC_2 = '\u00f2'
	private const val ESCAPE_FNC_3 = '\u00f3'
	private const val ESCAPE_FNC_4 = '\u00f4'
	private const val CODE_FNC_1 = 102 // Code A, Code B, Code C
	private const val CODE_FNC_2 = 97 // Code A, Code B
	private const val CODE_FNC_3 = 96 // Code A, Code B
	private const val CODE_FNC_4_A = 101 // Code A
	private const val CODE_FNC_4_B = 100 // Code B

	/**
	 * 创建Code128格式的条形码
	 * @return
	 */
	fun createCode128Barcode(data: String, width: Int, height: Int): Bitmap {
		val code = encode(data)
		val inputWidth = code.size
		// Add quiet zone on both sides.
		val fullWidth = inputWidth + 10
		val outputWidth = Math.max(width, fullWidth)
		val outputHeight = Math.max(1, height)
		val multiple = outputWidth / fullWidth
		val leftPadding = (outputWidth - inputWidth * multiple) / 2
		val bitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)
		val paint = Paint(Paint.ANTI_ALIAS_FLAG)
		paint.color = Color.WHITE
		paint.strokeWidth = 0f
		canvas.drawRect(Rect(0, 0, outputWidth, outputHeight), paint)
		paint.color = Color.BLACK
		var i = 0
		var outputX = leftPadding
		while(i < inputWidth) {
			if(code[i]) {
				canvas.drawRect(outputX.toFloat(), 0f, (outputX + multiple).toFloat(), outputHeight.toFloat(), paint)
			}
			i++
			outputX += multiple
		}
		return bitmap
	}

	/**
	 * @return a byte array of horizontal pixels (0 = white, 1 = black)
	 */
	private fun encode(contents: String): BooleanArray {
		val length = contents.length
		// Check length
		// require(!(length < 1 || length > 80)) { "Contents length should be between 1 and 80 characters, but got $length" }
		// Check content
		for(i in 0 until length) {
			when(val c = contents[i]) {
				ESCAPE_FNC_1, ESCAPE_FNC_2, ESCAPE_FNC_3, ESCAPE_FNC_4 -> {}
				else -> require(c.code <= 127) { "Bad character in input: $c" }
			}
		}
		val patterns: MutableCollection<IntArray> = ArrayList() // temporary storage for patterns
		var checkSum = 0
		var checkWeight = 1
		var codeSet = 0 // selected code (CODE_CODE_B or CODE_CODE_C)
		var position = 0 // position in contents
		while(position < length) {
			//Select code to use
			val newCodeSet = chooseCode(contents, position, codeSet)

			//Get the pattern index
			var patternIndex: Int
			if(newCodeSet == codeSet) {
				// Encode the current character
				// First handle escapes
				when(contents[position]) {
					ESCAPE_FNC_1 -> patternIndex = CODE_FNC_1
					ESCAPE_FNC_2 -> patternIndex = CODE_FNC_2
					ESCAPE_FNC_3 -> patternIndex = CODE_FNC_3
					ESCAPE_FNC_4 -> patternIndex = if(codeSet == CODE_CODE_A) {
						CODE_FNC_4_A
					} else {
						CODE_FNC_4_B
					}

					else -> when(codeSet) {
						CODE_CODE_A -> {
							patternIndex = contents[position].code - ' '.code
							if(patternIndex < 0) {
								// everything below a space character comes behind the underscore in the code patterns table
								patternIndex += '`'.code
							}
						}

						CODE_CODE_B -> patternIndex = contents[position].code - ' '.code
						else -> {
							// CODE_CODE_C
							patternIndex = contents.substring(position, position + 2).toInt()
							position++ // Also incremented below
						}
					}
				}
				position++
			} else {
				// Should we change the current code?
				// Do we have a code set?
				patternIndex = if(codeSet == 0) {
					// No, we don't have a code set
					when(newCodeSet) {
						CODE_CODE_A -> CODE_START_A
						CODE_CODE_B -> CODE_START_B
						else -> CODE_START_C
					}
				} else {
					// Yes, we have a code set
					newCodeSet
				}
				codeSet = newCodeSet
			}

			// Get the pattern
			patterns.add(CODE_PATTERNS[patternIndex])

			// Compute checksum
			checkSum += patternIndex * checkWeight
			if(position != 0) {
				checkWeight++
			}
		}

		// Compute and append checksum
		checkSum %= 103
		patterns.add(CODE_PATTERNS[checkSum])

		// Append stop code
		patterns.add(CODE_PATTERNS[CODE_STOP])

		// Compute code width
		var codeWidth = 0
		for(pattern in patterns) {
			for(width in pattern) {
				codeWidth += width
			}
		}

		// Compute result
		val result = BooleanArray(codeWidth)
		var pos = 0
		for(pattern in patterns) {
			pos += appendPattern(result, pos, pattern)
		}
		return result
	}

	private fun findCType(value: CharSequence, start: Int): CType {
		val last = value.length
		if(start >= last) {
			return CType.UNCODABLE
		}
		var c = value[start]
		if(c == ESCAPE_FNC_1) {
			return CType.FNC_1
		}
		if(c < '0' || c > '9') {
			return CType.UNCODABLE
		}
		if(start + 1 >= last) {
			return CType.ONE_DIGIT
		}
		c = value[start + 1]
		return if(c < '0' || c > '9') {
			CType.ONE_DIGIT
		} else CType.TWO_DIGITS
	}

	private fun chooseCode(value: CharSequence, start: Int, oldCode: Int): Int {
		var lookahead = findCType(value, start)
		if(lookahead == CType.ONE_DIGIT) {
			return CODE_CODE_B
		}
		if(lookahead == CType.UNCODABLE) {
			if(start < value.length) {
				val c = value[start]
				if(c < ' ' || oldCode == CODE_CODE_A && c < '`') {
					// can continue in code A, encodes ASCII 0 to 95
					return CODE_CODE_A
				}
			}
			return CODE_CODE_B // no choice
		}
		if(oldCode == CODE_CODE_C) { // can continue in code C
			return CODE_CODE_C
		}
		if(oldCode == CODE_CODE_B) {
			if(lookahead == CType.FNC_1) {
				return CODE_CODE_B // can continue in code B
			}
			// Seen two consecutive digits, see what follows
			lookahead = findCType(value, start + 2)
			if(lookahead == CType.UNCODABLE || lookahead == CType.ONE_DIGIT) {
				return CODE_CODE_B // not worth switching now
			}
			if(lookahead == CType.FNC_1) { // two digits, then FNC_1...
				lookahead = findCType(value, start + 3)
				return if(lookahead == CType.TWO_DIGITS) { // then two more digits, switch
					CODE_CODE_C
				} else {
					CODE_CODE_B // otherwise not worth switching
				}
			}
			// At this point, there are at least 4 consecutive digits.
			// Look ahead to choose whether to switch now or on the next round.
			var index = start + 4
			while(findCType(value, index).also { lookahead = it } == CType.TWO_DIGITS) {
				index += 2
			}
			return if(lookahead == CType.ONE_DIGIT) { // odd number of digits, switch later
				CODE_CODE_B
			} else CODE_CODE_C
			// even number of digits, switch now
		}
		// Here oldCode == 0, which means we are choosing the initial code
		if(lookahead == CType.FNC_1) { // ignore FNC_1
			lookahead = findCType(value, start + 1)
		}
		return if(lookahead == CType.TWO_DIGITS) { // at least two digits, start in code C
			CODE_CODE_C
		} else CODE_CODE_B
	}

	/**
	 * @param target  encode black/white pattern into this array
	 * @param pos     position to start encoding at in `target`
	 * @param pattern lengths of black/white runs to encode
	 * @return the number of elements added to target.
	 */
	private fun appendPattern(target: BooleanArray, pos: Int, pattern: IntArray): Int {
		var pos = pos
		var color = true
		var numAdded = 0
		for(len in pattern) {
			for(j in 0 until len) {
				target[pos++] = color
			}
			numAdded += len
			color = !color // flip color after each segment
		}
		return numAdded
	}

	private val CODE_PATTERNS = arrayOf(
		intArrayOf(2, 1, 2, 2, 2, 2),
		intArrayOf(2, 2, 2, 1, 2, 2),
		intArrayOf(2, 2, 2, 2, 2, 1),
		intArrayOf(1, 2, 1, 2, 2, 3),
		intArrayOf(1, 2, 1, 3, 2, 2),
		intArrayOf(1, 3, 1, 2, 2, 2),
		intArrayOf(1, 2, 2, 2, 1, 3),
		intArrayOf(1, 2, 2, 3, 1, 2),
		intArrayOf(1, 3, 2, 2, 1, 2),
		intArrayOf(2, 2, 1, 2, 1, 3),
		intArrayOf(2, 2, 1, 3, 1, 2),
		intArrayOf(2, 3, 1, 2, 1, 2),
		intArrayOf(1, 1, 2, 2, 3, 2),
		intArrayOf(1, 2, 2, 1, 3, 2),
		intArrayOf(1, 2, 2, 2, 3, 1),
		intArrayOf(1, 1, 3, 2, 2, 2),
		intArrayOf(1, 2, 3, 1, 2, 2),
		intArrayOf(1, 2, 3, 2, 2, 1),
		intArrayOf(2, 2, 3, 2, 1, 1),
		intArrayOf(2, 2, 1, 1, 3, 2),
		intArrayOf(2, 2, 1, 2, 3, 1),
		intArrayOf(2, 1, 3, 2, 1, 2),
		intArrayOf(2, 2, 3, 1, 1, 2),
		intArrayOf(3, 1, 2, 1, 3, 1),
		intArrayOf(3, 1, 1, 2, 2, 2),
		intArrayOf(3, 2, 1, 1, 2, 2),
		intArrayOf(3, 2, 1, 2, 2, 1),
		intArrayOf(3, 1, 2, 2, 1, 2),
		intArrayOf(3, 2, 2, 1, 1, 2),
		intArrayOf(3, 2, 2, 2, 1, 1),
		intArrayOf(2, 1, 2, 1, 2, 3),
		intArrayOf(2, 1, 2, 3, 2, 1),
		intArrayOf(2, 3, 2, 1, 2, 1),
		intArrayOf(1, 1, 1, 3, 2, 3),
		intArrayOf(1, 3, 1, 1, 2, 3),
		intArrayOf(1, 3, 1, 3, 2, 1),
		intArrayOf(1, 1, 2, 3, 1, 3),
		intArrayOf(1, 3, 2, 1, 1, 3),
		intArrayOf(1, 3, 2, 3, 1, 1),
		intArrayOf(2, 1, 1, 3, 1, 3),
		intArrayOf(2, 3, 1, 1, 1, 3),
		intArrayOf(2, 3, 1, 3, 1, 1),
		intArrayOf(1, 1, 2, 1, 3, 3),
		intArrayOf(1, 1, 2, 3, 3, 1),
		intArrayOf(1, 3, 2, 1, 3, 1),
		intArrayOf(1, 1, 3, 1, 2, 3),
		intArrayOf(1, 1, 3, 3, 2, 1),
		intArrayOf(1, 3, 3, 1, 2, 1),
		intArrayOf(3, 1, 3, 1, 2, 1),
		intArrayOf(2, 1, 1, 3, 3, 1),
		intArrayOf(2, 3, 1, 1, 3, 1),
		intArrayOf(2, 1, 3, 1, 1, 3),
		intArrayOf(2, 1, 3, 3, 1, 1),
		intArrayOf(2, 1, 3, 1, 3, 1),
		intArrayOf(3, 1, 1, 1, 2, 3),
		intArrayOf(3, 1, 1, 3, 2, 1),
		intArrayOf(3, 3, 1, 1, 2, 1),
		intArrayOf(3, 1, 2, 1, 1, 3),
		intArrayOf(3, 1, 2, 3, 1, 1),
		intArrayOf(3, 3, 2, 1, 1, 1),
		intArrayOf(3, 1, 4, 1, 1, 1),
		intArrayOf(2, 2, 1, 4, 1, 1),
		intArrayOf(4, 3, 1, 1, 1, 1),
		intArrayOf(1, 1, 1, 2, 2, 4),
		intArrayOf(1, 1, 1, 4, 2, 2),
		intArrayOf(1, 2, 1, 1, 2, 4),
		intArrayOf(1, 2, 1, 4, 2, 1),
		intArrayOf(1, 4, 1, 1, 2, 2),
		intArrayOf(1, 4, 1, 2, 2, 1),
		intArrayOf(1, 1, 2, 2, 1, 4),
		intArrayOf(1, 1, 2, 4, 1, 2),
		intArrayOf(1, 2, 2, 1, 1, 4),
		intArrayOf(1, 2, 2, 4, 1, 1),
		intArrayOf(1, 4, 2, 1, 1, 2),
		intArrayOf(1, 4, 2, 2, 1, 1),
		intArrayOf(2, 4, 1, 2, 1, 1),
		intArrayOf(2, 2, 1, 1, 1, 4),
		intArrayOf(4, 1, 3, 1, 1, 1),
		intArrayOf(2, 4, 1, 1, 1, 2),
		intArrayOf(1, 3, 4, 1, 1, 1),
		intArrayOf(1, 1, 1, 2, 4, 2),
		intArrayOf(1, 2, 1, 1, 4, 2),
		intArrayOf(1, 2, 1, 2, 4, 1),
		intArrayOf(1, 1, 4, 2, 1, 2),
		intArrayOf(1, 2, 4, 1, 1, 2),
		intArrayOf(1, 2, 4, 2, 1, 1),
		intArrayOf(4, 1, 1, 2, 1, 2),
		intArrayOf(4, 2, 1, 1, 1, 2),
		intArrayOf(4, 2, 1, 2, 1, 1),
		intArrayOf(2, 1, 2, 1, 4, 1),
		intArrayOf(2, 1, 4, 1, 2, 1),
		intArrayOf(4, 1, 2, 1, 2, 1),
		intArrayOf(1, 1, 1, 1, 4, 3),
		intArrayOf(1, 1, 1, 3, 4, 1),
		intArrayOf(1, 3, 1, 1, 4, 1),
		intArrayOf(1, 1, 4, 1, 1, 3),
		intArrayOf(1, 1, 4, 3, 1, 1),
		intArrayOf(4, 1, 1, 1, 1, 3),
		intArrayOf(4, 1, 1, 3, 1, 1),
		intArrayOf(1, 1, 3, 1, 4, 1),
		intArrayOf(1, 1, 4, 1, 3, 1),
		intArrayOf(3, 1, 1, 1, 4, 1),
		intArrayOf(4, 1, 1, 1, 3, 1),
		intArrayOf(2, 1, 1, 4, 1, 2),
		intArrayOf(2, 1, 1, 2, 1, 4),
		intArrayOf(2, 1, 1, 2, 3, 2),
		intArrayOf(2, 3, 3, 1, 1, 1, 2)
	)

	// Results of minimal lookahead for code C
	private enum class CType {
		UNCODABLE,
		ONE_DIGIT,
		TWO_DIGITS,
		FNC_1
	}
}
