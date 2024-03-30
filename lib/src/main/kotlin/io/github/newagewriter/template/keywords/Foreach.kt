package io.github.newagewriter.template.keywords

class Foreach(
    keyToFound: String
) : Keyword() {
    val pattern = Regex("#foreach\\(\\\$$keyToFound as ([a-zA-Z0-1]+)\\s*->\\s*([a-zA-Z0-1]+)(, separator=\"([^\"]+)\")?\\):([^#]+)#end")
    override fun find(content: String): MatchResult? {
        return pattern.find(content)
    }
}