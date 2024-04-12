package io.github.newagewriter.template

import io.github.newagewriter.template.keywords.MapForeach
import io.github.newagewriter.template.keywords.ListForeach
import io.github.newagewriter.template.validate.ValidationException
import javax.script.Bindings
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings


class TemplateClass(
    private val fileName: String,
    private val template: String
) {
    private val varMap: MutableMap<String, Any> = mutableMapOf()

    fun addVariable(name: String, value: Any): TemplateClass {
        varMap.put(name, value)
        return this
    }

    fun compile(): String {
        validate()
        var result = template
        varMap.forEach { (key, value) ->
            when (value) {
                is Collection<*> -> {
                    val forEach = ListForeach(key)
                    var matches = forEach.find(result)
                    while (matches != null) {
                        val mapBlock = StringBuilder()
                        value.forEach { v ->
                            val separator = matches?.groupValues?.get(2) ?: ""
                            val statement = (matches?.groupValues?.get(3) ?: "").trimEnd()
                                .replace(Regex("\\\$((element)|(\\{element}))"), "$v")
                            mapBlock.append("$statement$separator")
                        }
                        result = result.replaceFirst(forEach.pattern, mapBlock.toString())
                        matches = matches.next()
                    }
                }

                is Map<*, *> -> {
                    val forEach = MapForeach(key)
                    var matches = forEach.find(result)
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

                        result = result.replaceFirst(forEach.pattern, mapBlock.toString())
                        matches = matches.next()
                    }
                }

                else -> {
                    val pattern = Regex("\\\$\\{$key}")
                    result = result.replace(pattern, value.toString())
                }
            }
        }
        val ifPattern = Regex("#if\\(([^#]+)\\):([^#]+)#else ([^#]+)#endif")

        val engine = scriptManager.getEngineByName("kotlin") ?: scriptManager.engineFactories[0]?.scriptEngine

        engine?.let {
            var condMatches = ifPattern.find(result)
            var diff = 0
            val bindings: Bindings = SimpleBindings()
            bindings.putAll(varMap)
            while (condMatches != null) {
                condMatches.let { m ->
                    val condition = m.groups[1]
                    val resultOne = m.groups[2]
                    val resultElse = m.groups[3]

                    try {
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
            }
        }
        return result
    }

    private fun validate(): Boolean {
        validateCommands()
        val variablesNeeded = varMap.keys
        val variablesInTemplate = getVariablesFromTemplate()
        val missingVariable = variablesInTemplate.minus(variablesNeeded)
        if (missingVariable.isNotEmpty()) {
            throw ValidationException("Missing variables to generate file: $missingVariable")
        }

        return true
    }

    private fun getVariablesFromTemplate(): MutableSet<String> {
        val forEachVars = checkVarsDeclareInForEach()
        val result = mutableSetOf<String>()
        val pattern = Regex("\\\$(([a-zA-Z0-9]+)|(\\{([a-zA-Z0-9]+)}))")
        var matches = pattern.find(template)

        do {
            matches?.let { m ->
                var varName = m.groupValues[1]
                if (varName.startsWith("{")) {
                    varName = varName.substring(1, varName.length - 1)
                }
                if (!forEachVars.contains(varName) && !result.contains(varName)) {
                    result.add(varName)
                } else {

                }
            }
            matches = matches?.next()
        } while (matches != null)
        return result
    }

    private fun checkVarsDeclareInForEach(): Set<String> {
        val forEachRegex = Regex("#foreach\\(\\\$([a-zA-Z0-1]+)( as ([a-zA-Z0-1]+)\\s*->\\s*([a-zA-Z0-1]+))?(, separator=\"([^\"]+)\")?\\):")
        var matches = forEachRegex.find(template)
        val result = mutableSetOf<String>()
        do {
            matches?.let { m ->
                val keyName = m.groupValues.getOrNull(3)
                val valueName = m.groupValues.getOrNull(4)
                if (keyName.isNullOrBlank()) {
                    if(!result.contains(DEFAULT_FOREACH_NAME)) {
                        result.add(DEFAULT_FOREACH_NAME)
                    } else {

                    }
                } else {
                    if (!result.contains(keyName)) {
                        result.add(keyName)
                    }
                    if (valueName != null && !result.contains(valueName)) {
                        result.add(valueName)
                    } else {

                    }
                }
            }
            matches = matches?.next()
        } while (matches != null)
        return result
    }

    private fun validateCommands() {
        val forEachCommand = "#foreach"
        val forEachEndCommand = "#endforeach"
        val ifCommand = "#if"
        val endIfCommand = "#endif"
        val stack = mutableListOf<Pair<String, Int>>()
        val ifStack = mutableListOf<Pair<String, Int>>()
        val endWithoutForeach = mutableListOf<Pair<String, Int>>()
        val endWithoutIf = mutableListOf<Pair<String, Int>>()
        val findingList = listOf(forEachCommand, forEachEndCommand, ifCommand, endIfCommand, "\n")
        var finder = template.indexOfAny(findingList)
        var line = 1
        while (finder != -1) {
            val subStr = template.subSequence(finder, (finder + 11).coerceAtMost(template.length - 1))
            when {
                subStr.startsWith(forEachCommand) -> stack.add(forEachCommand to line)
                subStr.startsWith(forEachEndCommand) -> {
                    if (stack.isEmpty()) {
                        endWithoutForeach.add(forEachEndCommand to line)
                    } else {
                        stack.removeLast()
                    }
                }
                subStr.startsWith(ifCommand) -> ifStack.add(ifCommand to line)
                subStr.startsWith(endIfCommand) -> {
                    if (ifStack.isEmpty()) {
                        endWithoutIf.add(endIfCommand to line)
                    } else {
                        ifStack.removeLast()
                    }
                }
                else -> line++
            }
            finder = template.indexOfAny(findingList, finder + 1)
        }
        when {
            stack.size > 0 -> throw ValidationException("Missing #endforeach command. Foreach open but there no #endforeach for them. File: ${getAbsoluteFileName()}:${stack.first().second}")
            ifStack.size > 0 -> throw ValidationException("Missing #endif command. Command #if open but there no #endif for them. File: ${getAbsoluteFileName()}:${ifStack.first().second}")
            endWithoutForeach.size > 0 -> throw ValidationException("Too many #endforeach command in file: ${getAbsoluteFileName()}:${endWithoutForeach.first().second}")
            endWithoutIf.size > 0 -> throw ValidationException("Too many #endif command in file: ${getAbsoluteFileName()}:${endWithoutIf.first().second}")
        }
    }

    private fun getAbsoluteFileName(): String {
        return if (fileName.startsWith("/")) {
            fileName.substring(1)
        } else {
            fileName
        }
    }

    companion object {
        private val scriptManager = ScriptEngineManager()
        private const val DEFAULT_FOREACH_NAME = "element"
    }
}
