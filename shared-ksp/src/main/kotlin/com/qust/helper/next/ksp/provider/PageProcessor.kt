package com.qust.helper.next.ksp.provider

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.qust.helper.next.page.AbstractPage
import com.qust.helper.next.page.AbstractPageRegistry
import com.qust.helper.next.page.Page

class PageProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    options: Map<String, String>
) : SymbolProcessor {

    companion object {

        private val ANNOTATION = Page::class

        private val CLASS_NAME = requireNotNull(AbstractPage::class.qualifiedName)

        private val REGISTRY_NAME = requireNotNull(AbstractPageRegistry::class.qualifiedName)

        private val OUTPUT_PACKAGE = "com.qust.helper.next.ui.page"

        private val OUTPUT_CLASS = "GeneratedPageRegistry"
    }

    /**
     * process() 可能执行多轮，因此按页面类全名去重。
     */
    private val entriesByClassName = linkedMapOf<String, PageEntry>()

    /**
     * 用于在同一个 compilation 中检查重复路由。
     */
    private val entriesByPageId = linkedMapOf<String, PageEntry>()

    /**
     * 防止同一个错误在多轮处理中重复输出。
     */
    private val invalidClassNames = mutableSetOf<String>()

    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annoName = ANNOTATION.simpleName!!

        val symbols: List<KSAnnotated> = resolver.getSymbolsWithAnnotation(ANNOTATION.qualifiedName!!).toList()

        val baseType = resolver.getClassDeclarationByName(CLASS_NAME)?.asStarProjectedType() ?: return symbols

        val deferredSymbols = mutableListOf<KSAnnotated>()

        for(symbol in symbols) {
            if (!symbol.validate()) {
                deferredSymbols += symbol
                continue
            }

            symbol as? KSClassDeclaration ?: run {
                logger.error("@$annoName 注解只能用在 class 或 object 上", symbol)
                continue
            }

            val qualifiedName = symbol.qualifiedName?.asString() ?: run {
                logger.error(message = "@$annoName 注解不能应用于本地类 (local class) 或匿名类 (anonymous class)", symbol = symbol)
                continue
            }

            /*
             * 如果之前已经成功处理或者已经报告过该类的错误，
             * 后续轮次不再重复处理。
             */
            if (qualifiedName in entriesByClassName || qualifiedName in invalidClassNames) continue

            val pageId = readPageId(symbol) ?: run {
                reportClassError(declaration = symbol, message = "@$annoName 注解未提供 page id")
                continue
            }

            if (!validatePageId(symbol, pageId) ||
                !validateClassKind(logger, annoName, symbol) ||
                !validateVisibility(logger, annoName, symbol) ||
                !validateInheritance(logger, annoName, symbol, baseType)
            ){
                invalidClassNames += symbol.qualifiedName?.asString() ?: symbol.simpleName.asString()
                continue
            }

            val existing = entriesByPageId[pageId]

            if (existing != null && existing.qualifiedName != qualifiedName) {
                reportClassError(declaration = symbol, message = "Duplicate page id '$pageId'. It is already used by ${existing.qualifiedName}.")
                logger.error("Page id '$pageId' is also used by $qualifiedName.", existing.sourceFile)
                continue
            }

            val entry = PageEntry(
                id = pageId,
                qualifiedName = qualifiedName,
                className = symbol.toClassName(),
                sourceFile = symbol.containingFile
            )

            entriesByClassName[qualifiedName] = entry
            entriesByPageId[pageId] = entry
        }

        return deferredSymbols
    }

    /**
     * 所有 KSP round 结束后生成聚合注册表。
     *
     * 即使当前平台没有任何 @Page，也生成一个空注册表，
     * 从而保证 actual PlatformPageRegistration 始终可以引用它。
     */
    override fun finish() {
        if (generated) return
        generated = true
        generate(entries = entriesByClassName.values)
    }


    private fun readPageId(declaration: KSClassDeclaration): String? {
        val annotation = declaration.annotations.firstOrNull { annotation ->
            annotation.annotationType.resolve().declaration.qualifiedName?.asString() == ANNOTATION.qualifiedName!!
        } ?: return null

        return annotation.arguments.firstOrNull { argument ->
            argument.name?.asString() == "key"
        } ?.value as? String
    }


    private fun validatePageId(declaration: KSClassDeclaration, pageId: String): Boolean {
        if (pageId.isBlank()) {
            reportClassError(declaration = declaration, message = "@Page id cannot be empty or blank.")
            return false
        }

        if (pageId != pageId.trim()) {
            reportClassError(declaration = declaration, message = "@Page id cannot have leading or trailing whitespace: '$pageId'.")
            return false
        }

        if (pageId.any { it.isWhitespace() }) {
            reportClassError(declaration = declaration, message = "@Page id cannot contain whitespace: '$pageId'.")
            return false
        }

        return true
    }


    private fun generate(entries: Collection<PageEntry>) {
        val registryClass = ClassName.bestGuess(REGISTRY_NAME)

        val registerFunction = FunSpec
            .builder("registerInto")
            .addParameter(name = "registry", type = registryClass)
            .apply {
                entries.forEach { entry ->
                    addStatement("registry.register(%S, %T::class)", entry.id, entry.className)
                }
            }
            .build()

        val registryObject = TypeSpec
            .objectBuilder(OUTPUT_CLASS)
            .addModifiers(KModifier.INTERNAL)
            .addFunction(registerFunction)
            .build()

        val fileSpec = FileSpec
            .builder(packageName = OUTPUT_PACKAGE, fileName = OUTPUT_CLASS)
            .addFileComment("Generated by PageProcessor.\nDo not edit this file manually.")
            .addType(registryObject)
            .build()

        val sourceFiles = entries.mapNotNull(PageEntry::sourceFile).distinct().toTypedArray()

        val dependencies = Dependencies(aggregating = true, *sourceFiles)

        try {
            codeGenerator.createNewFile(
                dependencies = dependencies,
                packageName = OUTPUT_PACKAGE,
                fileName = OUTPUT_CLASS,
                extensionName = "kt"
            ).bufferedWriter().use { writer ->
                fileSpec.writeTo(writer)
            }
        } catch (exception: Exception) {
            logger.exception(exception)
            throw exception
        }
    }

    private fun reportClassError(declaration: KSClassDeclaration, message: String) {
        val key = declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()
        invalidClassNames += key
        logger.error(message, declaration)
    }
}

private data class PageEntry(
    val id: String,
    val qualifiedName: String,
    val className: ClassName,
    val sourceFile: KSFile?
)