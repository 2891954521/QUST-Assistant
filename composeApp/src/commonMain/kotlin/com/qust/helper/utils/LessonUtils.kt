package com.qust.helper.utils

object LessonUtils {



	fun getWeeksFromBooleans(booleans: Collection<Boolean>): Long {
		var weeks = 0L
		var tmp = 1L
		for(i in booleans){
			if(i) weeks = weeks or tmp
			tmp = tmp shl 1
		}
		return weeks
	}
}