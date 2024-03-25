package io.newagewriter.template.example

import io.github.newagewriter.template.TemplateLoader
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import javax.script.ScriptEngineManager
import kotlin.io.path.pathString

fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    val scriptManager = ScriptEngineManager()
//    val engines = scriptManager.getEngineFactories()
//    println("eninges: ${engines.size}")
//    for (f in engines) {
//        println("Engine Name:" + f.engineName)
//    }
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
    val mapperList = mutableMapOf<String, String>(
        "TestModel" to "com.test",
        "TestModel2" to "",
        "TestModel3" to "com.test.ppt"
    )

    val mapperUtilsTemplate = TemplateLoader.load("GeneratedMapperFactory")
    mapperUtilsTemplate
        .addVariable("types", mapperList.keys)
        .addVariable("mapperList", mapperList)
    readKotlinFile(
        "com.newagewriter.processor.mapper",
        "GeneratedMapperFactory.kt",
        mapperUtilsTemplate.compile()
    )

    val mapperTemplate = TemplateLoader.load("MapperTemplate")
    mapperTemplate.addVariable("className", "TestModel")
    mapperTemplate.addVariable("classPackage", "com.test")
    mapperTemplate.addVariable("fields", mutableMapOf<String, String>("t:sd" to "fdf"))
    mapperTemplate.addVariable("map", mutableMapOf<String, String>("t:sd" to "fdf"))
    readKotlinFile(
        "com.newagewriter.processor.mapper",
        "TestModelMapper.kt",
        mapperTemplate.compile()
    )
}

fun readKotlinFile(packageName: String, fileName: String, content: String) {
    val dir = Paths.get("gen/${packageName.replace(".", "/")}")
    Files.createDirectories(dir)
    val writer = FileOutputStream(File(dir.pathString, fileName))
    writer.write(content.toByteArray(Charsets.UTF_8))
    writer.close()
}
