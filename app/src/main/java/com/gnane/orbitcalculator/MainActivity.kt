package com.gnane.orbitcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { OrbitCalculator() }
    }
}

private val Space = Color(0xFF090D1D)
private val Panel = Color(0xFF151B35)
private val Lavender = Color(0xFFB7A6FF)
private val Aqua = Color(0xFF7CE7E1)
private val Ink = Color(0xFFF5F3FF)

@androidx.compose.runtime.Composable
fun OrbitCalculator() {
    var display by remember { mutableStateOf("0") }
    var stored by remember { mutableStateOf<Double?>(null) }
    var pending by remember { mutableStateOf<String?>(null) }
    var fresh by remember { mutableStateOf(true) }
    val digits = listOf(listOf("7", "8", "9", "÷"), listOf("4", "5", "6", "×"), listOf("1", "2", "3", "−"), listOf("0", ".", "±", "+"))

    fun number() = display.toDoubleOrNull() ?: 0.0
    fun format(value: Double): String = DecimalFormat("0.##########").format(value)
    fun apply() {
        val left = stored ?: return
        val result = when (pending) {
            "+" -> left + number()
            "−" -> left - number()
            "×" -> left * number()
            "÷" -> if (number() == 0.0) Double.NaN else left / number()
            else -> number()
        }
        display = if (result.isNaN() || result.isInfinite()) "Error" else format(result)
        stored = null
        pending = null
        fresh = true
    }
    fun tap(key: String) {
        when (key) {
            "AC" -> { display = "0"; stored = null; pending = null; fresh = true }
            "⌫" -> { display = if (display.length > 1) display.dropLast(1) else "0"; fresh = false }
            "±" -> if (display != "0") display = if (display.startsWith("-")) display.drop(1) else "-$display"
            "%" -> display = format(number() / 100)
            "+", "−", "×", "÷" -> {
                if (pending != null && !fresh) apply()
                stored = number(); pending = key; fresh = true
            }
            "=" -> apply()
            "." -> if (fresh) { display = "0."; fresh = false } else if (!display.contains(".")) display += "."
            else -> { display = if (fresh || display == "0" || display == "Error") key else display + key; fresh = false }
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Space) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 34.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ORBIT", color = Aqua, fontSize = 14.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = display,
                        modifier = Modifier.fillMaxWidth(),
                        color = Ink,
                        fontSize = if (display.length > 10) 42.sp else 64.sp,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(18.dp))
                    Box(Modifier.fillMaxWidth().height(2.dp).background(Lavender.copy(alpha = .45f)))
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    KeyRow(listOf("AC", "⌫", "%", "÷"), ::tap, setOf("÷"))
                    digits.forEach { row -> KeyRow(row, ::tap, setOf("÷", "×", "−", "+")) }
                    KeyRow(listOf("00", "=", "="), ::tap, emptySet())
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun KeyRow(keys: List<String>, onTap: (String) -> Unit, operators: Set<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        keys.forEach { key ->
            val isEquals = key == "="
            val color = when {
                isEquals -> Aqua
                key in operators -> Lavender
                key in setOf("AC", "⌫", "%") -> Color(0xFF252D50)
                else -> Panel
            }
            Box(
                modifier = Modifier.weight(if (isEquals) 1.5f else 1f).height(68.dp)
                    .background(color, RoundedCornerShape(22.dp))
                    .clickable { onTap(key) },
                contentAlignment = Alignment.Center
            ) {
                Text(key, color = if (isEquals) Space else Ink, fontSize = 24.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
