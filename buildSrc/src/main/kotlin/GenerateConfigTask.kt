import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.util.Properties

abstract class GenerateConfigTask : DefaultTask() {

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val className: Property<String>

    @get:InputFile
    abstract val inputFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val configFile = inputFile.get().asFile

        println("read app config: ${configFile.name}")

        val props = Properties().apply { load(configFile.inputStream()) }

        val file = outputDir.get().file("${className.get()}.kt").asFile
        file.parentFile.mkdirs()

        file.writeText(buildString {
            appendLine("package ${packageName.get()}")
            appendLine()
            appendLine("object ${className.get()} {")

            for ((k, v) in props.entries) {
                val key = (k as String).replace(Regex("[^A-Za-z0-9_]"), "_")
                val value = (v as String).trim()

                println("$key = $value")

                appendLine(when {
                    value.equals("true", true) || value.equals("false", true) ->
                        "\tconst val $key: Boolean = ${value.lowercase()}"

                    value.toIntOrNull() != null -> "\tconst val $key: Int = $value"

                    value.toLongOrNull() != null -> "\tconst val $key: Long = $value"

                    value.toDoubleOrNull() != null -> "\tconst val $key: Double = $value"

                    else -> {
                        val escaped = value.replace("\"", "\\\"")
                        "\tconst val $key: String = \"$escaped\""
                    }
                })
            }

            appendLine("}")
        })
    }
}