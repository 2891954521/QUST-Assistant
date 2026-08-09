package com.qust.helper.next.ksp.provider

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Visibility

internal fun validateClassKind(logger: KSPLogger, annotationName: String, declaration: KSClassDeclaration): Boolean {
    when (declaration.classKind) {
        ClassKind.CLASS, ClassKind.OBJECT -> Unit

        else -> {
            logger.error(
                message = buildString {
                    append("@$annotationName can only be applied to a concrete ")
                    append("class or object. Actual kind: ")
                    append(declaration.classKind)
                },
                symbol = declaration
            )
            return false
        }
    }

    if (declaration.isAbstract()) {
        logger.error(message = "@$annotationName cannot be applied to an abstract class.", symbol = declaration)
        return false
    }

    if (Modifier.SEALED in declaration.modifiers) {
        logger.error(message = "@$annotationName cannot be applied to a sealed base class.", symbol = declaration)
        return false
    }

    if (Modifier.INNER in declaration.modifiers) {
        logger.error(message = "@$annotationName cannot be applied to an inner class.", symbol = declaration)
        return false
    }

    return true
}


/**
 * 生成代码处于同一 Kotlin module，因此可以访问 internal 类型；
 * 但是不能访问 private/protected 页面或位于 private 外层类中的页面。
 */
internal fun validateVisibility(logger: KSPLogger, annotationName: String, declaration: KSClassDeclaration): Boolean {
    var current: KSDeclaration? = declaration

    while (current != null) {
        when (current.getVisibility()) {
            Visibility.PRIVATE,
            Visibility.PROTECTED,
            Visibility.LOCAL -> {
                logger.error(
                    message = buildString {
                        append("@$annotationName class and all of its containing ")
                        append("declarations must be public or internal. ")
                        append("Inaccessible declaration: ")
                        append(current.qualifiedName?.asString() ?: current.simpleName.asString())
                    },
                    symbol = declaration
                )
                return false
            }

            else -> Unit
        }

        current = current.parentDeclaration
    }

    return true
}


internal fun validateInheritance(logger: KSPLogger, annotationName: String, declaration: KSClassDeclaration, baseType: KSType): Boolean {
    val type = declaration.asStarProjectedType()

    if (!baseType.isAssignableFrom(type)) {
        logger.error(
            message = buildString {
                append("@$annotationName class ")
                append(declaration.qualifiedName?.asString())
                append(" must inherit from ")
                append(baseType.declaration.qualifiedName?.asString())
                append(".")
            },
            symbol = declaration
        )
        return false
    }

    return true
}