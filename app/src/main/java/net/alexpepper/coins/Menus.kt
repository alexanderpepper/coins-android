package net.alexpepper.coins

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DieCountSetting(
    val dieType: String,
    val argumentName: String,
    val count: MutableIntState
)

@Composable
fun MainMenu(
    isCoins: Boolean,
    coinCount: Int,
    diceCounts: List<DieCountSetting>,
    onCoinCountChange: (Int) -> Unit,
    onDieCountChange: (String, Int) -> Unit,
    onStart: () -> Unit,
    onInstructions: () -> Unit
) {
    MenuPanel {
        MenuTitle(if (isCoins) "REAL COINS" else "REAL DICE")
        MenuAction("START SHAKING", onStart)
        if (isCoins) {
            CoinCountControl(coinCount, onCoinCountChange)
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                diceCounts.forEach { setting ->
                    DieCountControl(setting.dieType, setting.count.intValue) {
                        onDieCountChange(setting.dieType, it)
                    }
                }
            }
        }
        MenuAction("INSTRUCTIONS", onInstructions)
    }
}

@Composable
fun ResultsMenu(
    isCoins: Boolean,
    results: ShakableResults,
    onShakeAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    MenuPanel {
        MenuTitle("RESULTS")
        if (isCoins) {
            CoinResults(results.coins.orEmpty().filterNotNull())
        } else {
            DiceResults(results.dice.orEmpty())
        }
        Spacer(Modifier.height(12.dp))
        MenuAction("SHAKE AGAIN", onShakeAgain)
        MenuAction("MAIN MENU", onMainMenu)
    }
}

@Composable
private fun MenuPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .widthIn(max = 400.dp)
            .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
private fun MenuTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 36.sp,
        fontWeight = FontWeight.ExtraLight,
        letterSpacing = 3.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun MenuAction(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraLight,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun CoinCountControl(count: Int, onCountChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(106.dp)
                .border(1.dp, Color.White, CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = Color.Black,
                fontSize = 66.sp,
                fontWeight = FontWeight.ExtraLight
            )
        }
        Text(
            text = if (count == 1) "COIN" else "COINS",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraLight,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )
        CountControls(
            count = count,
            minimum = 1,
            maximum = 10,
            onCountChange = onCountChange
        )
    }
}

@Composable
private fun DieCountControl(type: String, count: Int, onCountChange: (Int) -> Unit) {
    Column(
        modifier = Modifier.width(140.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(dieDrawable(type)),
                contentDescription = type,
                modifier = Modifier.size(38.dp)
            )
            Text(
                text = count.toString(),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraLight,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )
        }
        CountControls(
            count = count,
            minimum = 0,
            maximum = 4,
            onCountChange = onCountChange
        )
    }
}

@Composable
private fun CountControls(
    count: Int,
    minimum: Int,
    maximum: Int,
    onCountChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = { if (count > minimum) onCountChange(count - 1) },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        ) {
            Text("-", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraLight)
        }
        TextButton(
            onClick = { if (count < maximum) onCountChange(count + 1) },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        ) {
            Text("+", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraLight)
        }
    }
}

@Composable
private fun CoinResults(coins: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 3
    ) {
        coins.forEach { coin ->
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(90.dp)
                    .border(1.dp, Color.White, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = coin.uppercase(),
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DiceResults(dice: List<DieResult>) {
    val total = dice.sumOf { it.value }
    FlowRow(
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        maxItemsInEachRow = 2
    ) {
        dice.filter { it.type in DIE_TYPES }.forEach { result ->
            Row(
                modifier = Modifier.width(140.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(dieDrawable(result.type)),
                    contentDescription = result.type,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = result.value.toString(),
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraLight,
                    modifier = Modifier.width(58.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "TOTAL:",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraLight,
            letterSpacing = 2.sp
        )
        Text(
            text = total.toString(),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraLight,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun InstructionsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("INSTRUCTIONS") },
        text = { Text("Hold your device and shake it to toss the coins or roll the dice.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

private val DIE_TYPES = setOf("d4", "d6", "d8", "d10", "d12", "d20", "d100")

private fun dieDrawable(type: String): Int = when (type) {
    "d4" -> R.drawable.d4
    "d6" -> R.drawable.d6
    "d8" -> R.drawable.d8
    "d10" -> R.drawable.d10
    "d12" -> R.drawable.d12
    "d20" -> R.drawable.d20
    "d100" -> R.drawable.d100
    else -> error("Unknown die type: $type")
}
