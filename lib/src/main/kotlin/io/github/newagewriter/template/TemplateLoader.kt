package io.github.newagewriter.template

import java.io.FileNotFoundException

object TemplateLoader {
    private const val templateFolder = "template"

    @JvmStatic
    fun load(templateName: String): TemplateClass {
        val file = TemplateLoader::class.java.classLoader.getResourceAsStream("$templateFolder/$templateName.template")
        return TemplateClass(
            TemplateClass::class.java.classLoader.getResource("$templateFolder/$templateName.template")?.path ?: "",
            file?.let {
                file.reader(Charsets.UTF_8).readText()
            } ?: throw FileNotFoundException("ERROR: LOAD failed. Cannot open file or file doesn't exist")
        )
    }
}
