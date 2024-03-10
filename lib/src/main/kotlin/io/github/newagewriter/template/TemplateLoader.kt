package io.github.newagewriter.template

import java.io.FileNotFoundException

object TemplateLoader {
    private const val templateFolder = "template"

    @JvmStatic
    fun load(templateName: String): TemplateClass {
        val file = TemplateLoader.javaClass.classLoader.getResourceAsStream("$templateFolder/$templateName.template")
        return TemplateClass(
            file?.let {
                file.reader(Charsets.UTF_8).readText()
            } ?: throw FileNotFoundException("ERROR: LOAD failed. Cannot open file or file doesn't exist")
        )
    }
}
