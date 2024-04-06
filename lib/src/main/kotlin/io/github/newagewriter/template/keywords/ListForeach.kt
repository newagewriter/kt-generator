package io.github.newagewriter.template.keywords

class ListForeach(
    keyToFound: String
) : Keyword() {
    val pattern = Regex("#foreach\\(\\\$$keyToFound(, separator=\"([^\"]+)\")?\\):((.|^.|\\s)+?)#endforeach")
    override fun find(content: String): MatchResult? {
        return pattern.find(content)
    }
}