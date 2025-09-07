package com.example.spaceflightapp.core.utils.helpers

 fun relevanceScore(title: String, query: String): Int {
    if (query.isBlank()) return 0
    val t = title.lowercase()
    val q = query.lowercase()
    if (t == q) return 4
    if (t.startsWith(q)) return 3
    val word = Regex("\\b${Regex.escape(q)}\\b", RegexOption.IGNORE_CASE)
    if (word.containsMatchIn(title)) return 2
    return if (t.contains(q)) 1 else 0
}
