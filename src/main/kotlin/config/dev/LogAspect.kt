/*
 * Copyright (C) 2016 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.hska.kunde.config.dev

import brave.Tracer
import java.util.Arrays
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

/**
 * Logging von Methodenaufrufen durch _AspectJ_ und _Logback_/_SLF4J_.
 * Mit Ideen von _MDC_ (= Mapped Diagnostic Context):
 * [https://logback.qos.ch/manual/mdc.html].
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @param tracer Tracer für Brave.
 */
@Aspect
@Component
@Suppress("TooManyFunctions", "unused")
class LogAspect(val tracer: Tracer) {
    /**
     * Protokollierung der Argumente und des Rückgabewertes einer Methode.
     *
     * @param joinPoint Ein `ProceedingJoinPoint` für _AspectJ_.
     * @return Rückgabewert des protokollierten Methodenaufrufs.
     */
    @Around("execution(* de.hska.*.rest.*Handler.*(..))" +
            " || execution(* de.hska.*.service.*Service*.*(..))" +
            " || execution(* de.hska.*.config.security.*Service*.*(..))")
    @Suppress("UselessPostfixExpression")
    fun logMethod(joinPoint: ProceedingJoinPoint): Any {
        lateinit var result: Any
        with(joinPoint) {
            val clazz = target.javaClass
            val log = loggerMap.computeIfAbsent(clazz) { getLogger(clazz) }

            if (!log.isDebugEnabled) {
                return proceed()
            }

            val methodName = signature.name

            // Span fuer Zipkin
            val span = tracer.nextSpan().name("$clazz.$methodName").start()

            // Methodenaufruf protokollieren
            logMethodBegin(log, methodName, args)

            // Eigentlicher Methodenaufruf
            result = proceed()

            // Ende der eigentlichen Methode protokollieren
            logMethodEnd(log, methodName, result)
            span.finish()
        }
        return result
    }

    @Suppress("NestedBlockDepth")
    private fun logMethodBegin(log: Logger, methodName: String, args: Array<Any?>?) {
        var argsStr = ""
        args?.let {
            val sb = StringBuilder(capacityStringBuilder)
            val anzahlArgs = args.size
            with(sb) {
                append(": ")
                for (i in 0 until anzahlArgs) {
                    val arg = args[i]
                    val argStr = if (arg == null) "null" else toString(arg)
                    append(argStr)
                    append(paramSeparator)
                }
                val laenge = length
                delete(laenge - paramSeparatorLength, laenge - 1)
            }
            argsStr = sb.toString()
        }
        log.debug("$methodName BEGINN$argsStr")
    }

    private fun logMethodEnd(log: Logger, methodName: String, result: Any?) {
        val endStr = if (result == null)
            "$methodName ENDE"
        else "$methodName ENDE: ${toString(result)}"
        log.debug(endStr)
    }

    private fun toString(obj: Any): String {
        if (obj is Collection<*>) {
            // die Elemente nur bei kleiner Anzahl ausgeben
            // sonst nur die Anzahl der Elemente
            val anzahl = obj.size
            return if (anzahl > maxElem) {
                "$count${obj.size}"
            } else obj.toString()
        }

        return if (obj.javaClass.isArray) {
            // Array in String konvertieren: Element fuer Element
            arrayToString(obj)
        } else obj.toString()

        // Objekt, aber keine Collection und kein Array
    }

    private fun arrayToString(obj: Any): String {
        val componentClass = obj.javaClass.componentType
        if (!componentClass.isPrimitive) {
            return arrayOfObject(obj)
        }

        // Array von primitiven Werten: byte, short, int, long, float, double,
        // boolean, char
        val className = componentClass.name
        return arrayOfPrimitive(obj, className)
    }

    private fun arrayOfObject(obj: Any): String {
        @Suppress("UNCHECKED_CAST")
        val arr = obj as Array<Any>
        return if (arr.size > maxElem) {
            "$count${arr.size}"
        } else Arrays.toString(arr)
    }

    private fun arrayOfPrimitive(obj: Any, className: String) =
            when (className) {
                "byte" -> arrayOfByte(obj)
                "short" -> arrayOfShort(obj)
                "int" -> arrayOfInt(obj)
                "long" -> arrayOfLong(obj)
                "float" -> arrayOfFloat(obj)
                "double" -> arrayOfDouble(obj)
                else -> arrayOfOtherPrimitive(obj, className)
            }

    private fun arrayOfOtherPrimitive(obj: Any, className: String) =
            when (className) {
                "boolean" -> arrayOfBoolean(obj)
                "char" -> arrayOfChar(obj)
                else -> "<<UNKNOWN PRIMITIVE ARRAY>>"
            }

    private fun arrayOfByte(obj: Any): String {
        val arr = obj as ByteArray
        return if (arr.size > maxElem) {
            "$count${arr.size}"
        } else Arrays.toString(arr)
    }

    private fun arrayOfShort(obj: Any): String {
        val arr = obj as ShortArray
        return if (arr.size > maxElem) {
            "$count${arr.size}"
        } else Arrays.toString(arr)
    }

    private fun arrayOfInt(obj: Any): String {
        val arr = obj as IntArray
        return if (arr.size > maxElem) {
            "$count${arr.size}"
        } else Arrays.toString(arr)
    }

    private fun arrayOfLong(obj: Any): String {
        val arr = obj as LongArray
        return if (arr.size > maxElem) {
            "$count${arr.size}"
        } else Arrays.toString(arr)
    }

    private fun arrayOfFloat(obj: Any): String {
        val arr = obj as FloatArray
        return if (arr.size > maxElem) {
            "$count${arr.size}"
        } else Arrays.toString(arr)
    }

    private fun arrayOfDouble(obj: Any): String {
        val arr = obj as DoubleArray
        return if (arr.size > maxElem) {
            "$count${arr.size}"
        } else Arrays.toString(arr)
    }

    private fun arrayOfBoolean(obj: Any): String {
        val arr = obj as BooleanArray
        return if (arr.size > maxElem) {
            "$count${arr.size}"
        } else Arrays.toString(arr)
    }

    private fun arrayOfChar(obj: Any): String {
        val arr = obj as CharArray
        return if (arr.size > maxElem) {
            "$count${arr.size}"
        } else Arrays.toString(arr)
    }

    @Suppress("MayBeConstant")
    private companion object {
        val count = "Anzahl: "
        // bei Collections wird ab 5 Elementen nur die Anzahl ausgegeben
        const val maxElem = 4

        const val capacityStringBuilder = 64
        val paramSeparator = ", "
        val paramSeparatorLength = paramSeparator.length

        val loggerMap = HashMap<Class<*>, Logger>()
    }
}
