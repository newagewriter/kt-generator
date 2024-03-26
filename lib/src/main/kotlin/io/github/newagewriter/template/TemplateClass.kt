package io.github.newagewriter.template

import javax.script.Bindings
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings


class TemplateClass(
    private val template: String
) {
    private val varMap: MutableMap<String, Any> = mutableMapOf()

    fun addVariable(name: String, value: Any): TemplateClass {
        varMap.put(name, value)
        return this
    }

    fun compile(): String {
        var result = template
        varMap.forEach { key, value ->
            when (value) {
                is Collection<*> -> {
                    val pattern = Regex("@foreach\\(\\\$$key(, separator=\"([^\"]+)\")?\\):([^@]+)@end")
                    var matches = pattern.find(result)
                    while (matches != null) {
                        val mapBlock = StringBuilder()
                        value.forEach { v ->
                            val separator = matches?.groupValues?.get(2) ?: ""
                            val statement = (matches?.groupValues?.get(3) ?: "").trimEnd()
                                .replace(Regex("\\\$((element)|(\\{element}))"), "$v")

                            mapBlock.append("$statement$separator")
                        }
                        result = result.replaceFirst(pattern, mapBlock.toString())
                        matches = matches.next()
                    }
                }

                is Map<*, *> -> {
                    val pattern =
                        Regex("@foreach\\(\\\$$key as ([a-zA-Z0-1]+)\\s*->\\s*([a-zA-Z0-1]+)(, separator=\"([^\"]+)\")?\\):([^@]+)@end")
                    var matches = pattern.find(result)
                    var index = 0
                    if (key == "mapperList") {
//                        println("found matches: ${matches?.groups}")
                        matches?.groups?.forEach {
                            println("group ${index++}: ${it?.value}")
                        }
                    }
                    while (matches != null) {
                        val mapBlock = StringBuilder()
                        value.forEach { k, v ->
                            val keyName = matches?.groupValues?.get(1)
                            val valueName = matches?.groupValues?.get(2)
                            val separator = matches?.groupValues?.get(4) ?: ""
                            val statement = (matches?.groupValues?.get(5) ?: "").trimEnd()
                                .replace(Regex("\\\$(($keyName)|(\\{$keyName}))"), "$k")
                                .replace(Regex("\\\$(($valueName)|(\\{$valueName}))"), "$v")

                            mapBlock.append("$statement$separator")
                        }
                        result = result.replaceFirst(pattern, mapBlock.toString())
                        matches = matches.next()
                    }
                }

                else -> {
                    val pattern = Regex("\\\$\\{$key}")
                    result = result.replace(pattern, value.toString())
                }
            }
        }
//        if (result.contains("GeneratedMapperFactory")) {
        val ifPattern = Regex("#if\\(([^#]+)\\):([^#]+)#else ([^#]+)#endif")
        var condMatches = ifPattern.find(result)
        var diff = 0
        val engine = scriptManager.getEngineByName("kotlin")
        val bindings: Bindings = SimpleBindings()
        bindings.putAll(varMap)
        while (condMatches != null) {
            condMatches.let { m ->
                val condition = m.groups[1]
                val resultOne = m.groups[2]
                val resultElse = m.groups[3]

                try {
                    println("contitiond to execute: ${condition?.value}")

                    val value = engine.eval(condition?.value, bindings) as Boolean
                    val subStr = result.substring(m.range.first - diff, m.range.last + 1 - diff)
                    val conditionResult = if (value) resultOne?.value else resultElse?.value
                    diff += subStr.length - (conditionResult ?: "").length
                    result = result.replace(subStr, conditionResult ?: "")
                } catch (ex: Exception) {
                    println("problem with eval: $ex, $ex.pr")
                    ex.printStackTrace(System.out)
                }
            }
            condMatches = condMatches.next()
//            }
        }
//        println("code to compile: $result")
//        val evv = engine.eval(result, bindings)
//        println("result: ${evv}")
        return result
    }

    companion object {
        private val scriptManager = ScriptEngineManager()

        init {
            scriptManager.getEngineByName("kotlin")?.let { engine ->
                try {
                    val myString = "test"
                    val value = engine.eval("\"com.st\" == \"\"")
                    println("result: $value")
                    println("multi conditions: ${engine.eval("\"$myString\" == \"test\" && $value == false")}")
//            ProcessorLogger.logD("[KB]", "condition: ${value}")
                } catch (ex: Exception) {
//            ProcessorLogger.logD("[KB]", "exception: $ex")
                }
            }
        }
    }
}
