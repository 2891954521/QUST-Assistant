package com.qust.helper.next.common

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppDataDirectoryTest {

	private val root = Path.of("data")

	@Test
	fun blankProfileUsesApplicationDirectory() {
		assertEquals(root.resolve("com.qust.helper.next"), resolveAppDataDirectory(root, null))
		assertEquals(root.resolve("com.qust.helper.next"), resolveAppDataDirectory(root, "  "))
	}

	@Test
	fun fileNameProfileUsesUserDataRoot() {
		assertEquals(root.resolve("dev"), resolveAppDataDirectory(root, "dev"))
	}

	@Test
	fun directoryProfileIsUsedDirectly() {
		val dir = Path.of("custom", "data")
		assertEquals(dir.normalize(), resolveAppDataDirectory(root, dir.toString()))
		val absolute = root.toAbsolutePath()
		assertEquals(absolute.normalize(), resolveAppDataDirectory(Path.of("unused"), absolute.toString()))
	}

	@Test
	fun invalidProfileIsRejected() {
		assertFailsWith<IllegalArgumentException> {
			resolveAppDataDirectory(root, "..")
		}
	}
}
