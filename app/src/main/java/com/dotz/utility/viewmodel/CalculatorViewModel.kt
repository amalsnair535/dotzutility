package com.dotz.utility.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Maximum history entries kept in memory */
private const val HISTORY_LIMIT = 50

data class CalculatorState(
    val display: String = "0",
    val expression: String = "",
    val history: List<String> = emptyList(),
    val justEvaluated: Boolean = false,
)

class CalculatorViewModel : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state = _state.asStateFlow()

    // Accumulator holding the raw expression string
    private var pending = ""

    fun onDigit(digit: String) {
        _state.update { s ->
            // If we just finished an evaluation, start fresh
            if (s.justEvaluated) {
                pending = digit
                s.copy(display = digit, expression = digit, justEvaluated = false)
            } else {
                // Prevent multiple leading zeros
                if (pending == "0" && digit == "0") return
                if (pending == "0" && digit != ".") pending = digit
                else pending += digit
                s.copy(display = pending, expression = pending)
            }
        }
    }

    fun onDecimal() {
        // Only add decimal if last token doesn't already contain one
        val lastToken = pending.split("+", "-", "×", "÷").last()
        if (!lastToken.contains(".")) {
            if (pending.isEmpty() || pending.last() in listOf('+', '-', '×', '÷')) {
                pending += "0."
            } else {
                pending += "."
            }
            _state.update { it.copy(display = pending, expression = pending, justEvaluated = false) }
        }
    }

    fun onOperator(op: String) {
        _state.update { s ->
            // Replace trailing operator if user changes mind
            if (pending.isNotEmpty() && pending.last() in listOf('+', '-', '×', '÷')) {
                pending = pending.dropLast(1) + op
            } else {
                pending += op
            }
            s.copy(display = pending, expression = pending, justEvaluated = false)
        }
    }

    fun onPercent() {
        try {
            val value = pending.toDouble() / 100.0
            pending = formatResult(value)
            _state.update { it.copy(display = pending, expression = pending) }
        } catch (_: Exception) { /* ignore if expression is complex */ }
    }

    fun onToggleSign() {
        try {
            val value = -pending.toDouble()
            pending = formatResult(value)
            _state.update { it.copy(display = pending, expression = pending) }
        } catch (_: Exception) { }
    }

    fun onEquals() {
        if (pending.isEmpty()) return
        try {
            val result = evaluate(pending)
            val formatted = formatResult(result)
            val entry = "$pending = $formatted"
            _state.update { s ->
                val newHistory = (listOf(entry) + s.history).take(HISTORY_LIMIT)
                s.copy(
                    display = formatted,
                    expression = entry,
                    history = newHistory,
                    justEvaluated = true
                )
            }
            pending = formatted
        } catch (_: Exception) {
            _state.update { it.copy(display = "Error", justEvaluated = true) }
            pending = ""
        }
    }

    fun onClear() {
        pending = ""
        _state.update { it.copy(display = "0", expression = "", justEvaluated = false) }
    }

    fun onBackspace() {
        if (_state.value.justEvaluated) { onClear(); return }
        if (pending.isNotEmpty()) {
            pending = pending.dropLast(1)
            _state.update {
                it.copy(
                    display = if (pending.isEmpty()) "0" else pending,
                    expression = pending
                )
            }
        }
    }

    fun clearHistory() {
        _state.update { it.copy(history = emptyList()) }
    }

    // ── Simple recursive-descent evaluator ──────────────────────────

    /** Evaluate an expression string using +, -, ×, ÷ with correct precedence */
    private fun evaluate(expr: String): Double {
        val tokens = tokenise(expr)
        var pos = 0

        fun parseNumber(): Double {
            val tok = tokens[pos++]
            return tok.toDouble()
        }

        fun parseFactor(): Double = parseNumber()

        fun parseTerm(): Double {
            var left = parseFactor()
            while (pos < tokens.size && (tokens[pos] == "×" || tokens[pos] == "÷")) {
                val op = tokens[pos++]
                val right = parseFactor()
                left = if (op == "×") left * right else left / right
            }
            return left
        }

        fun parseExpr(): Double {
            var left = parseTerm()
            while (pos < tokens.size && (tokens[pos] == "+" || tokens[pos] == "-")) {
                val op = tokens[pos++]
                val right = parseTerm()
                left = if (op == "+") left + right else left - right
            }
            return left
        }

        return parseExpr()
    }

    private fun tokenise(expr: String): List<String> {
        val result = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            when {
                expr[i] in listOf('+', '×', '÷') -> { result += expr[i].toString(); i++ }
                expr[i] == '-' -> {
                    // Unary minus at start or after operator
                    if (i == 0 || expr[i - 1] in listOf('+', '-', '×', '÷')) {
                        var j = i + 1
                        while (j < expr.length && (expr[j].isDigit() || expr[j] == '.')) j++
                        result += expr.substring(i, j)
                        i = j
                    } else {
                        result += "-"
                        i++
                    }
                }
                expr[i].isDigit() || expr[i] == '.' -> {
                    var j = i
                    while (j < expr.length && (expr[j].isDigit() || expr[j] == '.')) j++
                    result += expr.substring(i, j)
                    i = j
                }
                else -> i++
            }
        }
        return result
    }

    private fun formatResult(value: Double): String {
        return if (value == kotlin.math.floor(value) && !value.isInfinite())
            value.toLong().toString()
        else value.toBigDecimal().stripTrailingZeros().toPlainString()
    }
}
