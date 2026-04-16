package com.example.calendar.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendar.data.local.EventEntity
import com.example.calendar.ui.theme.CalendarTheme
import com.example.calendar.ui.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: CalendarViewModel = viewModel()) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val localEvents by viewModel.allEvents.collectAsState()
    val googleEvents by viewModel.googleEvents.collectAsState()

    val combinedEvents = remember(localEvents, googleEvents) {
        val googleAsLocal = googleEvents.map { ge ->
            val dateStr = ge.start?.date ?: ge.start?.dateTime?.substring(0, 10)
            EventEntity(
                title = ge.summary ?: "No Title",
                description = ge.description ?: "",
                date = dateStr?.let { LocalDate.parse(it) } ?: LocalDate.now(),
                isBirthday = ge.summary?.contains("Birthday", ignoreCase = true) == true
            )
        }
        localEvents + googleAsLocal
    }

    var showAddEventDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    MonthYearSelector(
                        currentMonth = currentMonth,
                        onMonthYearSelected = { viewModel.setCurrentMonth(it) }
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Previous Month")
                    }
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next Month")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddEventDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            DaysOfWeekHeader()
            Spacer(modifier = Modifier.height(8.dp))
            
            // Animated Calendar Grid
            AnimatedContent(
                targetState = currentMonth,
                transitionSpec = {
                    if (targetState.isAfter(initialState)) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                    }.using(
                        SizeTransform(clip = false)
                    )
                },
                label = "CalendarTransition"
            ) { targetMonth ->
                CalendarGrid(
                    currentMonth = targetMonth,
                    selectedDate = selectedDate,
                    allEvents = combinedEvents,
                    onDateSelected = { viewModel.selectDate(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            EventList(
                selectedDate = selectedDate,
                events = combinedEvents.filter { it.date == selectedDate },
                onDeleteEvent = { viewModel.deleteEvent(it) }
            )
        }
    }

    if (showAddEventDialog) {
        AddEventDialog(
            selectedDate = selectedDate,
            onDismiss = { showAddEventDialog = false },
            onSave = { title, desc, isBirthday ->
                viewModel.addEvent(title, desc, selectedDate, isBirthday)
                showAddEventDialog = false
            }
        )
    }
}

@Composable
fun EventList(
    selectedDate: LocalDate,
    events: List<EventEntity>,
    onDeleteEvent: (EventEntity) -> Unit
) {
    Column {
        Text(
            text = "Events for ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (events.isEmpty()) {
            Text(
                text = "No events for this day.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            events.forEach { event ->
                EventItem(event = event, onDelete = { onDeleteEvent(event) })
            }
        }
    }
}

@Composable
fun EventItem(event: EventEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isBirthday) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                if (event.description.isNotEmpty()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (event.isBirthday) {
                Icon(
                    imageVector = Icons.Default.Cake,
                    contentDescription = "Birthday",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            // Only show delete button for local events (id != 0)
            if (event.id != 0) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Event")
                }
            }
        }
    }
}

@Composable
fun AddEventDialog(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isBirthday by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Event for ${selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))}") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isBirthday,
                        onCheckedChange = { isBirthday = it }
                    )
                    Text("Is Birthday?")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onSave(title, description, isBirthday) },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MonthYearSelector(
    currentMonth: YearMonth,
    onMonthYearSelected: (YearMonth) -> Unit
) {
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }
    val months = (1..12).map { Month.of(it) }
    val currentYear = YearMonth.now().year
    val years = (currentYear - 50..currentYear + 50).toList()

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Month Selector
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { monthExpanded = true }
                .padding(4.dp)
        ) {
            Text(
                text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                style = MaterialTheme.typography.titleLarge
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Month")
            DropdownMenu(
                expanded = monthExpanded,
                onDismissRequest = { monthExpanded = false }
            ) {
                months.forEach { month ->
                    DropdownMenuItem(
                        text = { Text(month.getDisplayName(TextStyle.FULL, Locale.getDefault())) },
                        onClick = {
                            onMonthYearSelected(currentMonth.withMonth(month.value))
                            monthExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Year Selector
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { yearExpanded = true }
                .padding(4.dp)
        ) {
            Text(
                text = currentMonth.year.toString(),
                style = MaterialTheme.typography.titleLarge
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Year")
            DropdownMenu(
                expanded = yearExpanded,
                onDismissRequest = { yearExpanded = false }
            ) {
                years.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year.toString()) },
                        onClick = {
                            onMonthYearSelected(currentMonth.withYear(year))
                            yearExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    allEvents: List<EventEntity>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7 // 0 for Sunday
    val days = (1..daysInMonth).toList()
    val emptySlots = (0 until firstDayOfMonth).toList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(emptySlots) {
            Spacer(modifier = Modifier.aspectRatio(1f))
        }
        items(days) { day ->
            val date = currentMonth.atDay(day)
            val isSelected = date == selectedDate
            val isToday = date == LocalDate.now()
            val dayEvents = allEvents.filter { it.date == date }

            DayCell(
                day = day,
                isSelected = isSelected,
                isToday = isToday,
                hasEvents = dayEvents.isNotEmpty(),
                hasBirthday = dayEvents.any { it.isBirthday },
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    hasBirthday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isToday -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.Transparent
            },
            contentColor = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                )
                if (hasEvents) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .padding(top = 2.dp)
                                .background(
                                    color = if (hasBirthday) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    CalendarTheme {
        DashboardScreen()
    }
}
