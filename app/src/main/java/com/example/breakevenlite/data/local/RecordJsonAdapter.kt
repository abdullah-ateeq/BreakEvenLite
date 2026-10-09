package com.example.breakevenlite.data.local

import com.example.breakevenlite.data.model.CalculationRecord
import java.lang.StringBuilder

object RecordJsonAdapter {

    private fun escape(s: String): String {
        val sb = StringBuilder()
        for (c in s) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\b' -> sb.append("\\b")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun unescape(s: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                when (val next = s[i + 1]) {
                    '\\' -> { sb.append('\\'); i += 2 }
                    '"' -> { sb.append('"'); i += 2 }
                    'b' -> { sb.append('\b'); i += 2 }
                    'n' -> { sb.append('\n'); i += 2 }
                    'r' -> { sb.append('\r'); i += 2 }
                    't' -> { sb.append('\t'); i += 2 }
                    else -> { sb.append(next); i += 2 }
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    fun recordToJson(record: CalculationRecord): String {
        return buildString {
            append("{")
            append("\"id\":\"").append(escape(record.id)).append("\",")
            append("\"scenarioName\":\"").append(escape(record.scenarioName)).append("\",")
            append("\"notes\":\"").append(escape(record.notes)).append("\",")
            append("\"currency\":\"").append(escape(record.currency)).append("\",")
            append("\"fixedCosts\":\"").append(escape(record.fixedCosts)).append("\",")
            append("\"sellingPricePerUnit\":\"").append(escape(record.sellingPricePerUnit)).append("\",")
            append("\"variableCostPerUnit\":\"").append(escape(record.variableCostPerUnit)).append("\",")
            append("\"targetProfitEnabled\":").append(record.targetProfitEnabled).append(",")
            if (record.targetProfit != null) {
                append("\"targetProfit\":\"").append(escape(record.targetProfit)).append("\",")
            } else {
                append("\"targetProfit\":null,")
            }
            if (record.breakEvenUnits != null) {
                append("\"breakEvenUnits\":\"").append(escape(record.breakEvenUnits)).append("\",")
            } else {
                append("\"breakEvenUnits\":null,")
            }
            if (record.breakEvenSales != null) {
                append("\"breakEvenSales\":\"").append(escape(record.breakEvenSales)).append("\",")
            } else {
                append("\"breakEvenSales\":null,")
            }
            if (record.contributionMargin != null) {
                append("\"contributionMargin\":\"").append(escape(record.contributionMargin)).append("\",")
            } else {
                append("\"contributionMargin\":null,")
            }
            append("\"timestamp\":").append(record.timestamp).append(",")
            append("\"isFavorite\":").append(record.isFavorite)
            append("}")
        }
    }

    fun listToJson(list: List<CalculationRecord>): String {
        return buildString {
            append("[")
            list.forEachIndexed { index, record ->
                append(recordToJson(record))
                if (index < list.size - 1) append(",")
            }
            append("]")
        }
    }

    fun jsonToRecord(json: String): CalculationRecord? {
        val trimmed = json.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return null
        val inner = trimmed.substring(1, trimmed.length - 1)

        val map = parseJsonObject(inner)
        val id = map["id"] ?: return null

        return CalculationRecord(
            id = id,
            scenarioName = map["scenarioName"] ?: "",
            notes = map["notes"] ?: "",
            currency = map["currency"] ?: "PKR",
            fixedCosts = map["fixedCosts"] ?: "0",
            sellingPricePerUnit = map["sellingPricePerUnit"] ?: "0",
            variableCostPerUnit = map["variableCostPerUnit"] ?: "0",
            targetProfitEnabled = map["targetProfitEnabled"]?.toBoolean() ?: false,
            targetProfit = map["targetProfit"],
            breakEvenUnits = map["breakEvenUnits"],
            breakEvenSales = map["breakEvenSales"],
            contributionMargin = map["contributionMargin"],
            timestamp = map["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis(),
            isFavorite = map["isFavorite"]?.toBoolean() ?: false
        )
    }

    fun jsonToList(json: String): List<CalculationRecord> {
        val trimmed = json.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return emptyList()
        val content = trimmed.substring(1, trimmed.length - 1).trim()
        if (content.isEmpty()) return emptyList()

        val records = mutableListOf<CalculationRecord>()
        var depth = 0
        var inQuotes = false
        var isEscape = false
        var start = -1

        for (i in content.indices) {
            val c = content[i]
            if (c == '\\' && inQuotes) {
                isEscape = !isEscape
                continue
            }
            if (c == '"' && !isEscape) {
                inQuotes = !inQuotes
            }
            isEscape = false

            if (!inQuotes) {
                if (c == '{') {
                    if (depth == 0) start = i
                    depth++
                } else if (c == '}') {
                    depth--
                    if (depth == 0 && start != -1) {
                        val objStr = content.substring(start, i + 1)
                        jsonToRecord(objStr)?.let { records.add(it) }
                        start = -1
                    }
                }
            }
        }
        return records
    }

    private fun parseJsonObject(content: String): Map<String, String?> {
        val map = mutableMapOf<String, String?>()
        var i = 0
        val len = content.length

        while (i < len) {
            // Find key opening quote
            while (i < len && content[i] != '"') i++
            if (i >= len) break
            i++ // skip opening quote
            val keyStart = i
            var keyEscaped = false
            while (i < len && (content[i] != '"' || keyEscaped)) {
                keyEscaped = (content[i] == '\\' && !keyEscaped)
                i++
            }
            val rawKey = content.substring(keyStart, i)
            val key = unescape(rawKey)
            i++ // skip closing quote

            // Find colon
            while (i < len && content[i] != ':') i++
            if (i >= len) break
            i++ // skip colon

            // Skip whitespace
            while (i < len && (content[i] == ' ' || content[i] == '\t' || content[i] == '\n' || content[i] == '\r')) i++
            if (i >= len) break

            if (content[i] == '"') {
                // String value
                i++
                val valStart = i
                var valEscaped = false
                while (i < len && (content[i] != '"' || valEscaped)) {
                    valEscaped = (content[i] == '\\' && !valEscaped)
                    i++
                }
                val rawVal = content.substring(valStart, i)
                map[key] = unescape(rawVal)
                i++ // skip quote
            } else if (content.startsWith("null", i)) {
                map[key] = null
                i += 4
            } else {
                // Primitive number or boolean
                val valStart = i
                while (i < len && content[i] != ',' && content[i] != '}') i++
                val rawVal = content.substring(valStart, i).trim()
                map[key] = rawVal
            }

            // Find comma or end
            while (i < len && content[i] != ',') i++
            if (i < len && content[i] == ',') i++
        }
        return map
    }
}
