package com.qust.helper.next.ui.business.lesson.table.axis

import com.qust.helper.next.entity.lesson.Lesson
import com.qust.helper.next.entity.lesson.TimeTable
import kotlin.math.max

class SlotTimeAxis private constructor(
	override val segments: List<TimeSegment>,
	private val offsets: FloatArray,
	override val totalWeight: Float,
) : LessonTimeAxis {

	override val startMinute: Int = segments.first().startMinute
	override val endMinute: Int = segments.last().endMinute

	data class Config(
		val shortGapMinutes: Int = 15,
		val longGapWeight: Float = 0.3f,
		val minExtraWeight: Float = 0.5f,
	)

	companion object Factory : LessonTimeAxisFactory {

		const val MIN_CELL_WEIGHT = 0.35f

		operator fun invoke(config: Config = Config()): LessonTimeAxisFactory {
			return LessonTimeAxisFactory { timeTable, lessons -> build(timeTable, lessons, config) }
		}

		override fun create(timeTable: TimeTable, lessons: List<Lesson>): LessonTimeAxis {
			return build(timeTable, lessons, Config())
		}

		internal fun build(timeTable: TimeTable, lessons: List<Lesson>, config: Config): SlotTimeAxis {
			val segments = buildSegments(timeTable, lessons, config)
			val offsets = FloatArray(segments.size)
			var acc = 0f
			for(i in segments.indices) {
				offsets[i] = acc
				acc += segments[i].weight
			}
			return SlotTimeAxis(segments, offsets, acc)
		}

		private fun buildSegments(timeTable: TimeTable, lessons: List<Lesson>, config: Config): List<TimeSegment> {
			val count = timeTable.count
			if(count == 0) {
				val minStart = lessons.minOfOrNull { it.startMinute } ?: 0
				val maxEnd = lessons.maxOfOrNull { it.endMinute } ?: 60
				val start = floorTo10(minStart)
				val end = max(start + 1, ceilTo10(maxEnd))
				return listOf(TimeSegment(start, end, 1f, TimeSegment.Kind.EXTRA, -1))
			}

			val avgSlotMinutes = timeTable.sliceLength.average().toFloat().coerceAtLeast(1f)
			val firstSlotStart = timeTable.startMinute.first()
			val lastSlotEnd = timeTable.endMinute.last()
			val segments = ArrayList<TimeSegment>(count * 2 + 2)

			val minLessonStart = lessons.minOfOrNull { it.startMinute }
			if(minLessonStart != null && minLessonStart < firstSlotStart) {
				val extraStart = floorTo10(minLessonStart)
				if(extraStart < firstSlotStart) {
					val length = (firstSlotStart - extraStart).toFloat()
					segments += TimeSegment(
						startMinute = extraStart,
						endMinute = firstSlotStart,
						weight = max(length / avgSlotMinutes, config.minExtraWeight),
						kind = TimeSegment.Kind.EXTRA,
						slotIndex = -1,
					)
				}
			}

			for(i in 0 until count) {
				segments += TimeSegment(
					startMinute = timeTable.startMinute[i],
					endMinute = timeTable.endMinute[i],
					weight = 1f,
					kind = TimeSegment.Kind.SLOT,
					slotIndex = i,
				)
				if(i >= count - 1) continue
				val gapStart = timeTable.endMinute[i]
				val gapEnd = timeTable.startMinute[i + 1]
				val g = gapEnd - gapStart
				if(g <= 0) continue

				val intersects = lessons.any { it.startMinute < gapEnd && it.endMinute > gapStart }
				var weight = if(g <= config.shortGapMinutes) 0f else config.longGapWeight
				if(intersects) {
					weight = max(config.longGapWeight, g / avgSlotMinutes)
					if(weight == 0f) {
						weight = max(g / avgSlotMinutes, MIN_CELL_WEIGHT)
					}
				}
				segments += TimeSegment(
					startMinute = gapStart,
					endMinute = gapEnd,
					weight = weight,
					kind = TimeSegment.Kind.GAP,
					slotIndex = -1,
				)
			}

			val maxLessonEnd = lessons.maxOfOrNull { it.endMinute }
			if(maxLessonEnd != null && maxLessonEnd > lastSlotEnd) {
				val extraEnd = ceilTo10(maxLessonEnd)
				if(extraEnd > lastSlotEnd) {
					val length = (extraEnd - lastSlotEnd).toFloat()
					segments += TimeSegment(
						startMinute = lastSlotEnd,
						endMinute = extraEnd,
						weight = max(length / avgSlotMinutes, config.minExtraWeight),
						kind = TimeSegment.Kind.EXTRA,
						slotIndex = -1,
					)
				}
			}

			return segments
		}
	}

	override fun offsetOf(minute: Int): Float {
		if(segments.isEmpty()) return 0f
		val m = minute.coerceIn(startMinute, endMinute)
		var low = 0
		var high = segments.lastIndex
		while(low < high) {
			val mid = (low + high + 1) ushr 1
			if(segments[mid].startMinute <= m) low = mid else high = mid - 1
		}
		val seg = segments[low]
		val base = offsets[low]
		if(m <= seg.startMinute || seg.weight == 0f || seg.endMinute <= seg.startMinute) return base
		if(m >= seg.endMinute) return base + seg.weight
		val ratio = (m - seg.startMinute).toFloat() / (seg.endMinute - seg.startMinute).toFloat()
		return base + ratio * seg.weight
	}
}
