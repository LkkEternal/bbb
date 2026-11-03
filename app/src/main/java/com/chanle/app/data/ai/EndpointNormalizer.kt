package com.chanle.app.data.ai

object EndpointNormalizer {

    private const val SUFFIX = "/chat/completions"

    fun normalize(raw: String): String {
        var url = raw.trim().replace(Regex("[\\s\\u3000]+"), "")
        if (url.isEmpty()) return ""
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            url = "https://$url"
        }
        url = url.trimEnd('/')

        if (url.contains(SUFFIX, ignoreCase = true)) {
            val hostStart = url.indexOf("//") + 2
            val rest = url.substring(hostStart)
            val slash = rest.indexOf('/')
            val host = if (slash >= 0) rest.substring(0, slash) else rest
            val path = if (slash >= 0) rest.substring(slash) else ""
            return "https://$host$path"
        }
        return "https://" + url.substringAfter("://") + SUFFIX
    }

    fun display(url: String): String = url.substringAfter("//", url)
}
