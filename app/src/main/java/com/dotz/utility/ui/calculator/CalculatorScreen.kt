package com.dotz.utility.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotz.utility.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    vm: CalculatorViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val clipboard = LocalClipboardManager.current
    var showHistory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Calculator", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { clipboard.setText(AnnotatedString(state.display)) }) {
                        Icon(Icons.Outlined.ContentCopy, "Copy result")
                    }
                    IconButton(onClick = { showHistory = !showHistory }) {
                        Icon(Icons.Outlined.History, "History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // History panel
            if (showHistory) {
                HistoryPanel(
                    history = state.history,
                    onClear = vm::clearHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                // Display area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = state.expression.ifEmpty { state.display },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = state.display,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Light,
                                fontSize = if (state.display.length > 12) 36.sp else 52.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // Button grid
            CalcGrid(
                modifier = Modifier.padding(12.dp),
                onDigit = vm::onDigit,
                onOperator = vm::onOperator,
                onDecimal = vm::onDecimal,
                onPercent = vm::onPercent,
                onToggleSign = vm::onToggleSign,
                onEquals = vm::onEquals,
                onClear = vm::onClear,
                onBackspace = vm::onBackspace,
            )
        }
    }
}

@Composable
private fun HistoryPanel(
    history: List<String>,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("History", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onClear) { Text("Clear") }
        }
        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No history yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(history) { entry ->
                    Text(
                        text = entry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.End
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

// ── Button grid layout ──────────────────────────────────────────────────────

private data class CalcButton(
    val label: String,
    val type: BtnType = BtnType.DIGIT
)

private enum class BtnType { DIGIT, OPERATOR, EQUALS, FUNCTION }

private val rows = listOf(
    listOf(
        CalcButton("AC", BtnType.FUNCTION), CalcButton("+/-", BtnType.FUNCTION),
        CalcButton("%",  BtnType.FUNCTION), CalcButton("÷",  BtnType.OPERATOR)
    ),
    listOf(
        CalcButton("7"), CalcButton("8"), CalcButton("9"), CalcButton("×", BtnType.OPERATOR)
    ),
    listOf(
        CalcButton("4"), CalcButton("5"), CalcButton("6"), CalcButton("-", BtnType.OPERATOR)
    ),
    listOf(
        CalcButton("1"), CalcButton("2"), CalcButton("3"), CalcButton("+", BtnType.OPERATOR)
    ),
    listOf(
        CalcButton("⌫", BtnType.FUNCTION), CalcButton("0"), CalcButton(".", BtnType.DIGIT),
        CalcButton("=", BtnType.EQUALS)
    ),
)

@Composable
private fun CalcGrid(
    modifier: Modifier = Modifier,
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onDecimal: () -> Unit,
    onPercent: () -> Unit,
    onToggleSign: () -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { btn ->
                    CalcKey(
                        btn = btn,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when {
                                btn.label == "AC"  -> onClear()
                                btn.label == "+/-" -> onToggleSign()
                                btn.label == "%"   -> onPercent()
                                btn.label == "⌫"   -> onBackspace()
                                btn.label == "="   -> onEquals()
                                btn.label == "."   -> onDecimal()
                                btn.type == BtnType.OPERATOR -> onOperator(btn.label)
                                else -> onDigit(btn.label)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcKey(
    btn: CalcButton,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val (bg, fg) = when (btn.type) {
        BtnType.EQUALS   -> colors.onSurface to colors.surface.also {}  // inverted
        BtnType.OPERATOR -> colors.secondaryContainer to colors.onSecondaryContainer
        BtnType.FUNCTION -> colors.surfaceVariant to colors.onSurfaceVariant
        else             -> colors.background to colors.onBackground
    }
    val equalsColors = Pair(colors.onSurface, colors.surface)

    val actualBg = if (btn.type == BtnType.EQUALS) equalsColors.first else bg
    val actualFg = if (btn.type == BtnType.EQUALS) equalsColors.second else fg

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(actualBg)
            .border(
                width = if (btn.type == BtnType.DIGIT) 1.dp else 0.dp,
                color = colors.outlineVariant,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = btn.label,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
            color = actualFg,
            fontWeight = if (btn.type == BtnType.EQUALS) FontWeight.Bold else FontWeight.Normal
        )
    }
}
