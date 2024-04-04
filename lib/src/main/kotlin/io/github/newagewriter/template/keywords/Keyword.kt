package io.github.newagewriter.template.keywords

abstract class Keyword {
    abstract fun find(content: String): MatchResult?
}