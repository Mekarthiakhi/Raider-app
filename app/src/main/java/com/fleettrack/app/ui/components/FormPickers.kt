package com.fleettrack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceCard
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class SearchableDropdownOption(
    val value: String,
    val label: String,
    val subtitle: String? = null
)

/**
 * Searchable modal selector for dropdown lists across mobile screens & forms.
 */
@Composable
fun SearchableOptionDialog(
    title: String,
    options: List<SearchableDropdownOption>,
    selectedValue: String,
    searchPlaceholder: String = "Search option...",
    onSelect: (SearchableDropdownOption) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredOptions = remember(options, query) {
        val q = query.trim()
        if (q.isEmpty()) options
        else options.filter { opt ->
            opt.label.contains(q, ignoreCase = true) ||
                opt.value.contains(q, ignoreCase = true) ||
                (opt.subtitle?.contains(q, ignoreCase = true) == true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text(
                text = title,
                color = TextDark,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(
                            text = searchPlaceholder,
                            color = TextMutedGray,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = TextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredOptions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching items found",
                            color = TextGray,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredOptions, key = { "${it.value}__${it.label}" }) { option ->
                            val isSelected = option.value == selectedValue || option.label == selectedValue
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) PrimaryPurpleContainer else SurfaceCard)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryPurple else SurfaceBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        onSelect(option)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option.label,
                                        color = if (isSelected) PrimaryPurple else TextDark,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!option.subtitle.isNullOrBlank()) {
                                        Text(
                                            text = option.subtitle,
                                            color = TextGray,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextGray, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

/**
 * Form dropdown field backed by SearchableOptionDialog. Never clipped on mobile dialogs.
 */
@Composable
fun SearchableDropdownField(
    label: String,
    selectedValue: String,
    options: List<SearchableDropdownOption>,
    onOptionSelected: (SearchableDropdownOption) -> Unit,
    modifier: Modifier = Modifier,
    dialogTitle: String = "Select $label",
    searchPlaceholder: String = "Search...",
    placeholder: String = "Select..."
) {
    var showDialog by remember { mutableStateOf(false) }

    val displayLabel = remember(selectedValue, options) {
        options.firstOrNull { it.value == selectedValue || it.label == selectedValue }?.label
            ?: selectedValue.ifBlank { placeholder }
    }

    if (showDialog) {
        SearchableOptionDialog(
            title = dialogTitle,
            options = options,
            selectedValue = selectedValue,
            searchPlaceholder = searchPlaceholder,
            onSelect = onOptionSelected,
            onDismiss = { showDialog = false }
        )
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = displayLabel,
            onValueChange = {},
            readOnly = true,
            label = if (label.isNotBlank()) { { Text(label) } } else null,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Open dropdown",
                    tint = PrimaryPurple
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = SurfaceBorder,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDialog = true }
        )
    }
}

/**
 * Clickable pill dropdown trigger that opens SearchableOptionDialog.
 */
@Composable
fun SearchableDropdownPill(
    label: String,
    selectedValue: String,
    options: List<SearchableDropdownOption>,
    dialogTitle: String,
    searchPlaceholder: String,
    onOptionSelected: (SearchableDropdownOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        SearchableOptionDialog(
            title = dialogTitle,
            options = options,
            selectedValue = selectedValue,
            searchPlaceholder = searchPlaceholder,
            onSelect = onOptionSelected,
            onDismiss = { showDialog = false }
        )
    }

    Box(
        modifier = modifier
            .clip(PillShape)
            .background(SurfaceWhite)
            .border(1.dp, SurfaceBorder, PillShape)
            .clickable { showDialog = true }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Open $dialogTitle",
                tint = TextGray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Read-only Date input field that enforces calendar selection via Material3 DatePickerDialog.
 * Prevents free-form text keyboard input completely.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    outputPattern: String = "dd MMM yyyy",
    placeholder: String = "Select Date"
) {
    var showPicker by remember { mutableStateOf(false) }

    val initialMillis = remember(selectedDate) {
        parseFlexibleDateMillis(selectedDate)
    }

    if (showPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val pickedMillis = datePickerState.selectedDateMillis
                        if (pickedMillis != null) {
                            onDateSelected(formatUtcDateMillis(pickedMillis, outputPattern))
                        }
                        showPicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = PillShape
                ) {
                    Text("Confirm", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            readOnly = true,
            label = if (label.isNotBlank()) { { Text(label) } } else null,
            placeholder = { Text(placeholder, color = TextMutedGray) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Pick Date",
                    tint = PrimaryPurple
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = SurfaceBorder,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true }
        )
    }
}

private fun parseFlexibleDateMillis(dateText: String): Long {
    val clean = dateText.trim()
    val patterns = listOf("dd MMM yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy")
    for (p in patterns) {
        try {
            val sdf = SimpleDateFormat(p, Locale.ENGLISH).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }
            val parsed = sdf.parse(clean)
            if (parsed != null) return parsed.time
        } catch (_: Exception) {
        }
    }
    return System.currentTimeMillis()
}

private fun formatUtcDateMillis(millis: Long, pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return sdf.format(Date(millis))
}

