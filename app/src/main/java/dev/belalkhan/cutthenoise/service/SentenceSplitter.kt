package dev.belalkhan.cutthenoise.service

class SentenceSplitter {

    fun split(input: String): List<String> {
        return input
            .split(Regex("[.\n•-]"))
            .map { it.trim() }
            .filter { it.length > 10 }
            .take(10) // HARD LIMIT
    }
}
