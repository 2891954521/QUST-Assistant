package com.qust.helper.next.module.lesson

import com.qust.helper.entity.lesson.Lesson

object LessonUtils {

	/**
	 * 将布尔格式的课程上课时间转换为Long形式
	 */
	fun getWeeksFromBooleans(booleans: Collection<Boolean>): Long {
		var weeks = 0L
		var tmp = 1L
		for(i in booleans){
			if(i) weeks = weeks or tmp
			tmp = tmp shl 1
		}
		return weeks
	}


	/**
	 * 合并课程表
	 * 返还两个列表，分别是 新增的课程，更新的课程，删除课程
	 * 分开是为了给数据库用，让数据库可以正常更新
	 */
	fun mergeLesson(old: List<Lesson>, new: List<Lesson>): Triple<List<Lesson>, List<Lesson>, List<Lesson>> {
		val newLessons = mutableListOf<Lesson>()
		val updateLessons = mutableListOf<Lesson>()
		val deleteLessons = mutableListOf<Lesson>()

		val idMap = mutableMapOf<String, MutableList<Lesson>>()
		for(lesson in old){
			// 按照 lessonId 将已有课程分组，没有 lessonId 的一般为用户自定义课程
			if(lesson.lessonId.isNotEmpty()){
				idMap.getOrPut(lesson.lessonId){ mutableListOf() }.add(lesson)
			}
		}

		for(lesson in new) {
			if(lesson.lessonId.isEmpty()) {
				// 没有 lessonId 的直接新增
				newLessons.add(lesson)
				continue
			}

			val list = idMap[lesson.lessonId]
			if(list == null){
				// 这个 lessonId 没出现过，是新课程
				newLessons.add(lesson)
				continue
			}

			var index = 0
			var find = false
			// 从旧课表中 lessonId 相同的课程里找上课时间相同的
			while(index < list.size){
				if(list[index].startMinute == lesson.startMinute && list[index].endMinute == lesson.endMinute){
					find = true
					updateLessons.add(lesson.copy(id = list[index].id)) // 这里要复制原课程的Id让数据库知道是哪个更新了
					list.removeAt(index) // 从列表里删除这个课程
					break
				}
				index++
			}
			// 没找到上课时间相符的，说明是新课程
			if(!find) newLessons.add(lesson)

			// 旧课表中没有被匹配的课程添加到删除列表
			for(lessons in idMap.values){
				deleteLessons.addAll(lessons)
			}
		}
		return Triple(newLessons, updateLessons, deleteLessons)
	}
}