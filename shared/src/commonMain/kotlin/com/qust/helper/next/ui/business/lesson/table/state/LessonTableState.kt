package com.qust.helper.next.ui.business.lesson.table.state

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.qust.helper.next.ui.business.lesson.table.LessonCellInfo
import com.qust.helper.next.entity.lesson.LessonTableData
import com.qust.helper.next.ui.business.lesson.table.model.LessonCandidate
import com.qust.helper.next.ui.business.lesson.table.model.LessonGroup
import com.qust.helper.next.ui.business.lesson.table.model.LessonKey

@Stable
class LessonTableState internal constructor(
	val pagerState: PagerState,
) {
	val currentWeek: Int get() = pagerState.currentPage

	internal val selection: SnapshotStateMap<Long, LessonKey> = mutableStateMapOf()

	suspend fun scrollToWeek(week: Int, animate: Boolean = true) {
		val target = week.coerceIn(0, (pagerState.pageCount - 1).coerceAtLeast(0))
		if(animate) pagerState.animateScrollToPage(target) else pagerState.scrollToPage(target)
	}

	internal fun displayedIndex(group: LessonGroup, candidates: List<LessonCandidate>): Int {
		return displayedCandidateIndex(selection, group, candidates)
	}

	internal fun cycle(cell: LessonCellInfo): LessonCellInfo = cycleSelection(selection, cell)
}

internal fun displayedCandidateIndex(
	selection: Map<Long, LessonKey>,
	group: LessonGroup,
	candidates: List<LessonCandidate>,
): Int {
	if(candidates.isEmpty()) return 0
	val selected = selection[group.id] ?: return 0
	val index = candidates.indexOfFirst { it.entry.key == selected }
	return if(index >= 0) index else 0
}

internal fun cycleSelection(
	selection: MutableMap<Long, LessonKey>,
	cell: LessonCellInfo,
): LessonCellInfo {
	if(cell.candidates.size <= 1) return cell
	val nextIndex = (cell.displayedIndex + 1) % cell.candidates.size
	selection[cell.group.id] = cell.candidates[nextIndex].entry.key
	return cell.copy(displayedIndex = nextIndex)
}

@Composable
fun rememberLessonTableState(data: LessonTableData, initialWeek: Int = 0): LessonTableState {
	val totalWeek = data.totalWeek.coerceAtLeast(1)
	val pagerState = rememberPagerState(
		initialPage = initialWeek.coerceIn(0, totalWeek - 1),
		pageCount = { data.totalWeek.coerceAtLeast(1) },
	)
	val state = remember { LessonTableState(pagerState) }
	LaunchedEffect(data.totalWeek) {
		val maxPage = data.totalWeek.coerceAtLeast(1) - 1
		if(pagerState.currentPage > maxPage) {
			pagerState.scrollToPage(maxPage)
		}
	}
	return state
}
