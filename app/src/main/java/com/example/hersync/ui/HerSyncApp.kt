package com.example.hersync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import android.widget.Toast
import com.example.hersync.data.DailyLogEntry
import com.example.hersync.data.LogRepository
import com.example.hersync.cycle.CycleCalculator
import com.example.hersync.cycle.CyclePhase
import com.example.hersync.cycle.CyclePrediction
import com.example.hersync.ui.theme.AccentPink
import com.example.hersync.ui.theme.Charcoal
import com.example.hersync.ui.theme.FertileMint
import com.example.hersync.ui.theme.FollicularCell
import com.example.hersync.ui.theme.FollicularPurple
import com.example.hersync.ui.theme.LutealCell
import com.example.hersync.ui.theme.LutealOrange
import com.example.hersync.ui.theme.OvulationCell
import com.example.hersync.ui.theme.OvulationTeal
import com.example.hersync.ui.theme.PeriodCell
import com.example.hersync.ui.theme.ProductivityBrown
import com.example.hersync.ui.theme.ProductivityCream
import com.example.hersync.ui.theme.ProductivityGreen
import com.example.hersync.ui.theme.ProductivityLavender
import com.example.hersync.ui.theme.ProductivityMaroon
import com.example.hersync.ui.theme.ProductivityMint
import com.example.hersync.ui.theme.ProductivityPurple
import com.example.hersync.ui.theme.ProductivityRose
import com.example.hersync.ui.theme.SurfaceCard
import com.example.hersync.ui.theme.TextMuted
import com.example.hersync.ui.theme.TextPrimary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

data class CycleFormState(
    val lastPeriodStart: LocalDate = LocalDate.of(2026, 4, 8),
    val cycleLength: Int = 28,
    val periodDuration: Int = 5,
    val feeling: String = "great",
    val hasSynced: Boolean = true,
)

private sealed class Dest(val route: String, val label: String) {
    data object Sync : Dest("sync", "sync")
    data object Cycle : Dest("cycle", "cycle")
    data object Wellness : Dest("wellness", "wellness")
    data object Log : Dest("log", "log")
}

private val bottomDestinations = listOf(
    Dest.Sync to Icons.Filled.Home,
    Dest.Cycle to Icons.Filled.CalendarMonth,
    Dest.Wellness to Icons.Filled.AutoAwesome,
    Dest.Log to Icons.Outlined.EditNote,
)

@Composable
fun HerSyncApp() {
    var form by remember {
        mutableStateOf(CycleFormState())
    }
    val navController = rememberNavController()
    val appContext = LocalContext.current.applicationContext
    val logRepository = remember { LogRepository(appContext) }

    Scaffold(
        containerColor = Charcoal,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard,
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                bottomDestinations.forEach { (dest, icon) ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentPink,
                            selectedTextColor = AccentPink,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Sync.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(Dest.Sync.route) {
                SyncScreen(
                    state = form,
                    onStateChange = { form = it },
                )
            }
            composable(Dest.Cycle.route) {
                CycleCalendarScreen(
                    state = form,
                    onChangePeriodStart = { picked ->
                        form = form.copy(lastPeriodStart = picked)
                    },
                )
            }
            composable(Dest.Wellness.route) {
                ProductivityScreen(state = form)
            }
            composable(Dest.Log.route) {
                LogScreen(repository = logRepository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SyncScreen(
    state: CycleFormState,
    onStateChange: (CycleFormState) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val zone = ZoneId.systemDefault()
    // DatePickerState only uses initialSelectedDateMillis once. Recreate it when lastPeriodStart changes
    // so the dialog always reflects the latest chosen value.
    val datePickerState = key(state.lastPeriodStart) {
        rememberDatePickerState(
            initialSelectedDateMillis = state.lastPeriodStart.atStartOfDay(zone).toInstant().toEpochMilli(),
        )
    }

    val prediction: CyclePrediction? = remember(state.hasSynced, state.lastPeriodStart, state.cycleLength) {
        if (state.hasSynced) {
            CycleCalculator.predict(state.lastPeriodStart, state.cycleLength)
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = "HerSync",
            style = MaterialTheme.typography.displaySmall,
            color = AccentPink,
        )
        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = state.lastPeriodStart.format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()),
                        ),
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        label = { Text("last period start date") },
                        trailingIcon = { Icon(Icons.Filled.Today, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // OutlinedTextField can swallow taps; this overlay guarantees date picker opens.
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true },
                    )
                }
                OutlinedTextField(
                    value = state.cycleLength.toString(),
                    onValueChange = { v ->
                        v.toIntOrNull()?.let { onStateChange(state.copy(cycleLength = it.coerceIn(1, 90))) }
                    },
                    label = { Text("cycle length (days)") },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.periodDuration.toString(),
                    onValueChange = { v ->
                        v.toIntOrNull()?.let {
                            onStateChange(state.copy(periodDuration = it.coerceIn(1, 14)))
                        }
                    },
                    label = { Text("period duration (days)") },
                    modifier = Modifier.weight(1f),
                )
                FeelingDropdown(
                    feeling = state.feeling,
                    onFeelingChange = { onStateChange(state.copy(feeling = it)) },
                    modifier = Modifier.weight(1f),
                )
            }

            Button(
                onClick = { onStateChange(state.copy(hasSynced = true)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text("sync my cycle")
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ResultColumn(
                title = "NEXT PERIOD",
                titleColor = AccentPink,
                mainText = prediction?.let { CycleCalculator.shortDate(it.nextPeriodStart) } ?: "—",
                subText = prediction?.let { daysUntilLabel(it.daysUntilNextPeriod) } ?: " ",
                modifier = Modifier.weight(1f),
            )
            ResultColumn(
                title = "FERTILE WINDOW",
                titleColor = FertileMint,
                mainText = prediction?.let {
                    CycleCalculator.shortRange(it.fertileStart, it.fertileEnd)
                } ?: "—",
                subText = prediction?.let { "ovulation ~${CycleCalculator.shortDate(it.ovulationDate)}" } ?: " ",
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { ms ->
                            val picked = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
                            onStateChange(state.copy(lastPeriodStart = picked))
                        }
                        showDatePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun daysUntilLabel(days: Long): String = when {
    days < 0 -> "${-days} days ago"
    days == 0L -> "today"
    days == 1L -> "in 1 day"
    else -> "in $days days"
}

@Composable
private fun FeelingDropdown(
    feeling: String,
    onFeelingChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf("great", "good", "okay", "low")
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = feeling,
            onValueChange = {},
            readOnly = true,
            label = { Text("how are you feeling?") },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onFeelingChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ResultColumn(
    title: String,
    titleColor: Color,
    mainText: String,
    subText: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = titleColor,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = mainText,
            style = MaterialTheme.typography.displaySmall,
            color = titleColor,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subText,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )
    }
}

@Composable
private fun CycleCalendarScreen(
    state: CycleFormState,
    onChangePeriodStart: (LocalDate) -> Unit,
) {
    val cycleLength = state.cycleLength.coerceAtLeast(1)
    val periodDuration = state.periodDuration.coerceIn(1, cycleLength)
    val prediction = remember(state.lastPeriodStart, state.cycleLength) {
        CycleCalculator.predict(state.lastPeriodStart, state.cycleLength)
    }
    val ovulationDate = prediction.ovulationDate

    val ovDay = CycleCalculator.ovulationCycleDay(cycleLength)
    val ovStart = (ovDay - 1).coerceAtLeast(1)
    val ovEnd = (ovDay + 2).coerceAtMost(cycleLength)
    val follicularDays = (ovStart - periodDuration - 1).coerceAtLeast(0)
    val ovDays = (ovEnd - ovStart + 1).coerceAtLeast(1)
    val lutealDays = (cycleLength - periodDuration - follicularDays - ovDays).coerceAtLeast(0)

    val p = periodDuration.toFloat() / cycleLength
    val f = follicularDays.toFloat() / cycleLength
    val o = ovDays.toFloat() / cycleLength
    val l = lutealDays.toFloat() / cycleLength

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = "your cycle",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp)),
        ) {
            Box(
                Modifier
                    .weight(p.coerceAtLeast(0.02f))
                    .fillMaxSize()
                    .background(AccentPink),
            )
            Box(
                Modifier
                    .weight(f.coerceAtLeast(0.02f))
                    .fillMaxSize()
                    .background(FollicularPurple),
            )
            Box(
                Modifier
                    .weight(o.coerceAtLeast(0.02f))
                    .fillMaxSize()
                    .background(OvulationTeal),
            )
            Box(
                Modifier
                    .weight(l.coerceAtLeast(0.02f))
                    .fillMaxSize()
                    .background(LutealOrange),
            )
        }

        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendDot(AccentPink, "menstrual")
            LegendDot(FollicularPurple, "follicular")
            LegendDot(OvulationTeal, "ovulation")
            LegendDot(LutealOrange, "luteal")
        }

        Spacer(Modifier.height(20.dp))

        val today = LocalDate.now()
        val monthStarts = listOf(
            today.withDayOfMonth(1).minusMonths(1),
            today.withDayOfMonth(1),
            today.withDayOfMonth(1).plusMonths(1),
        )
        val monthTitleFormat = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

        monthStarts.forEachIndexed { index, monthStart ->
            val firstOfMonth = LocalDate.of(monthStart.year, monthStart.month, 1)
            if (index > 0) Spacer(Modifier.height(24.dp))
            Text(
                text = firstOfMonth.format(monthTitleFormat),
                style = MaterialTheme.typography.titleMedium,
                color = AccentPink,
            )
            Spacer(Modifier.height(8.dp))
            WeekdayHeaderRow()
            Spacer(Modifier.height(8.dp))
            SingleMonthCycleGrid(
                firstOfMonth = firstOfMonth,
                lastPeriodStart = state.lastPeriodStart,
                cycleLength = cycleLength,
                periodDuration = periodDuration,
                ovulationDate = ovulationDate,
                onChangePeriodStart = onChangePeriodStart,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun WeekdayHeaderRow() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { d ->
            Text(
                text = d,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SingleMonthCycleGrid(
    firstOfMonth: LocalDate,
    lastPeriodStart: LocalDate,
    cycleLength: Int,
    periodDuration: Int,
    ovulationDate: LocalDate,
    onChangePeriodStart: (LocalDate) -> Unit,
) {
    val daysInMonth = firstOfMonth.lengthOfMonth()
    val startOffset = firstOfMonth.dayOfWeek.value.let { if (it == 7) 0 else it }
    val totalCells = startOffset + daysInMonth
    val rows = ceil(totalCells / 7f).toInt()

    // Column + Row grid (not LazyVerticalGrid): lazy grids inside verticalScroll often
    // measure incorrectly and the last month(s) fail to appear.
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            index < startOffset || index >= totalCells -> {
                                Spacer(Modifier.size(44.dp))
                            }
                            else -> {
                                val dayOfMonth = index - startOffset + 1
                                val date = firstOfMonth.plusDays(dayOfMonth.toLong() - 1L)
                                val cycleDay =
                                    CycleCalculator.cycleDay(lastPeriodStart, date, cycleLength)

                                if (cycleDay == null) {
                                    DayNumberCell(
                                        dayNumber = dayOfMonth,
                                        background = SurfaceCard,
                                        borderColor = Color.Transparent,
                                        onClick = { onChangePeriodStart(date) },
                                    )
                                } else {
                                    val isStart = date == lastPeriodStart
                                    val isEnd =
                                        date == lastPeriodStart.plusDays(periodDuration.toLong() - 1L)
                                    val isOv = date == ovulationDate

                                    CycleDayCell(
                                        dayNumber = dayOfMonth,
                                        cycleDay = cycleDay,
                                        cycleLength = cycleLength,
                                        periodDuration = periodDuration,
                                        isStart = isStart,
                                        isEnd = isEnd,
                                        isOvulation = isOv,
                                        onClick = { onChangePeriodStart(date) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayNumberCell(
    dayNumber: Int,
    background: Color,
    borderColor: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = dayNumber.toString(),
            style = MaterialTheme.typography.bodyMedium,
            // For "no cycle prediction yet" days we use a dark background (SurfaceCard).
            // Use light text color so the day number remains visible.
            color = TextPrimary,
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

@Composable
private fun CycleDayCell(
    dayNumber: Int,
    cycleDay: Int,
    cycleLength: Int,
    periodDuration: Int,
    isStart: Boolean,
    isEnd: Boolean,
    isOvulation: Boolean,
    onClick: () -> Unit,
) {
    val phase = CycleCalculator.phaseForCycleDay(cycleDay, cycleLength, periodDuration)
    val bg = when (phase) {
        CyclePhase.Menstrual -> PeriodCell
        CyclePhase.Follicular -> FollicularCell
        CyclePhase.Ovulation -> OvulationCell
        CyclePhase.Luteal -> LutealCell
    }
    val border = if (isStart) AccentPink else Color.Transparent
    val label = when {
        isStart -> "start"
        isEnd -> "end"
        isOvulation -> "ov"
        else -> null
    }
    Column(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(2.dp, border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = dayNumber.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = Charcoal,
        )
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = Charcoal.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun ProductivityScreen(state: CycleFormState) {
    val today = LocalDate.now()
    val cycleDay = CycleCalculator.cycleDay(state.lastPeriodStart, today, state.cycleLength)
    val c = state.cycleLength.coerceAtLeast(1)
    val pd = state.periodDuration.coerceIn(1, c)
    val ovDay = CycleCalculator.ovulationCycleDay(c)
    val ovStart = (ovDay - 1).coerceAtLeast(1)
    val ovEnd = (ovDay + 2).coerceAtMost(c)
    val follicularEnd = (ovStart - 1).coerceAtLeast(pd + 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "productivity & wellness windows",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
        )
        ProductivityCard(
            dayRange = "Days 1–$pd",
            title = "now: rest & gentle movement",
            body = "energy dips — prioritize sleep, light walks, journaling. avoid intense studying or high-stakes tasks. good time for reviewing notes, not learning new content.",
            background = ProductivityCream,
            textColor = ProductivityBrown,
            highlighted = cycleDay != null && cycleDay in 1..pd,
        )
        ProductivityCard(
            dayRange = if (follicularEnd >= pd + 1) "Days ${pd + 1}–$follicularEnd" else "Days ${pd + 1}–${pd + 1}",
            title = "building momentum",
            body = "estrogen rising — great for starting new projects, learning new concepts, brainstorming, and creative work. social energy peaks too.",
            background = ProductivityLavender,
            textColor = ProductivityPurple,
            highlighted = cycleDay != null && follicularEnd >= pd + 1 && cycleDay in (pd + 1)..follicularEnd,
        )
        ProductivityCard(
            dayRange = "Days $ovStart–$ovEnd",
            title = "peak productivity window",
            body = "your power zone. memory, focus, and verbal skills are sharpest. best time for exams, presentations, interviews, and deep study sessions.",
            background = ProductivityMint,
            textColor = ProductivityGreen,
            highlighted = cycleDay != null && cycleDay in ovStart..ovEnd,
        )
        val lutealStart = (ovEnd + 1).coerceAtMost(c)
        ProductivityCard(
            dayRange = "Days $lutealStart–$c",
            title = "stress-prone & inward",
            body = "progesterone peaks then drops — mood may dip, PMS possible. stick to routine tasks, revise known material. reduce social commitments, rest is productive too.",
            background = ProductivityRose,
            textColor = ProductivityMaroon,
            highlighted = cycleDay != null && cycleDay in lutealStart..c,
        )
    }
}

@Composable
private fun ProductivityCard(
    dayRange: String,
    title: String,
    body: String,
    background: Color,
    textColor: Color,
    highlighted: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(
                width = if (highlighted) 2.dp else 0.dp,
                color = AccentPink,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
    ) {
        Text(dayRange, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.85f))
        Spacer(Modifier.height(6.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = textColor)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = textColor)
    }
}

@Composable
private fun LogScreen(repository: LogRepository) {
    val moods = listOf(
        "energized", "calm", "focused", "anxious", "irritable", "sad", "hopeful", "tired",
    )
    val symptoms = listOf(
        "cramps", "bloating", "headache", "tender breasts", "acne", "cravings",
        "insomnia", "low energy", "back pain", "nausea",
    )
    val flows = listOf("none", "light", "medium", "heavy")

    var showHistory by remember { mutableStateOf(false) }
    var refresh by remember { mutableStateOf(0) }
    val entries = remember(refresh) { repository.getAllDescending() }

    var moodPick by remember { mutableStateOf<String?>(null) }
    var symptomPicks by remember { mutableStateOf(setOf<String>()) }
    var flowPick by remember { mutableStateOf<String?>(null) }
    var notesText by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(refresh, showHistory) {
        if (!showHistory) {
            val epochDay = LocalDate.now().toEpochDay()
            repository.getForDateEpochDay(epochDay)?.let { e ->
                moodPick = e.mood
                symptomPicks = e.symptoms.toSet()
                flowPick = e.flow.ifBlank { "none" }
                notesText = e.notes
            } ?: run {
                moodPick = null
                symptomPicks = emptySet()
                flowPick = null
                notesText = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        LogTabRow(
            showHistory = showHistory,
            onToday = { showHistory = false },
            onHistory = { showHistory = true },
        )
        Spacer(Modifier.height(16.dp))

        when {
            showHistory && entries.isEmpty() -> {
                Text(
                    text = "No saved logs yet. Add an entry under today's log.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
            showHistory -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(entries, key = { it.dateEpochDay }) { entry ->
                        LogHistoryCard(entry)
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCard)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text("mood", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        ChipGrid(items = moods, columns = 3) { m ->
                            LogChip(
                                label = m,
                                selected = moodPick == m,
                                onClick = { moodPick = if (moodPick == m) null else m },
                            )
                        }

                        Text("symptoms", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        ChipGrid(items = symptoms, columns = 3) { s ->
                            LogChip(
                                label = s,
                                selected = s in symptomPicks,
                                onClick = {
                                    symptomPicks = if (s in symptomPicks) symptomPicks - s else symptomPicks + s
                                },
                            )
                        }

                        Text("flow", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        ChipGrid(items = flows, columns = 2) { f ->
                            LogChip(
                                label = f,
                                selected = flowPick == f,
                                onClick = { flowPick = if (flowPick == f) null else f },
                            )
                        }

                        Text("notes", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("anything else on your mind...", color = TextMuted)
                            },
                            minLines = 3,
                            maxLines = 8,
                        )

                        OutlinedButtonFullWidth(
                            text = "save today's entry",
                            onClick = {
                                val mood = moodPick
                                if (mood == null) {
                                    Toast.makeText(context, "Please select your mood first.", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButtonFullWidth
                                }
                                val entry = DailyLogEntry(
                                    dateEpochDay = LocalDate.now().toEpochDay(),
                                    mood = mood,
                                    symptoms = symptomPicks.toList().sorted(),
                                    flow = flowPick ?: "none",
                                    notes = notesText.trim(),
                                )
                                repository.upsert(entry)
                                refresh++
                                Toast.makeText(
                                    context,
                                    "Saved for ${LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))}",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogTabRow(
    showHistory: Boolean,
    onToday: () -> Unit,
    onHistory: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LogSegmentTab(
            label = "today's log",
            selected = !showHistory,
            onClick = onToday,
            modifier = Modifier.weight(1f),
        )
        LogSegmentTab(
            label = "history",
            selected = showHistory,
            onClick = onHistory,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LogSegmentTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) TextPrimary else TextMuted,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) TextPrimary else TextMuted,
        )
    }
}

@Composable
private fun LogHistoryCard(entry: DailyLogEntry) {
    val date = LocalDate.ofEpochDay(entry.dateEpochDay)
    val titleFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .padding(16.dp),
    ) {
        Text(
            text = date.format(titleFmt),
            style = MaterialTheme.typography.titleSmall,
            color = AccentPink,
        )
        Spacer(Modifier.height(10.dp))
        Text("mood · ${entry.mood}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Text("flow · ${entry.flow}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        if (entry.symptoms.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = entry.symptoms.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
        if (entry.notes.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(entry.notes, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }
    }
}

@Composable
private fun ChipGrid(
    items: List<String>,
    columns: Int,
    content: @Composable (String) -> Unit,
) {
    val rows = items.chunked(columns.coerceAtLeast(1))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        content(item)
                    }
                }
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun OutlinedButtonFullWidth(
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, TextMuted),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
    ) {
        Text(text)
    }
}

@Composable
private fun LogChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = if (selected) AccentPink else TextPrimary,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, if (selected) AccentPink else TextMuted, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}
