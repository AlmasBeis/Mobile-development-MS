package kz.alfasoft.mycompany.ui.leaveform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.alfasoft.mycompany.data.model.LeaveType
import kz.alfasoft.mycompany.domain.FormField
import kz.alfasoft.mycompany.domain.LeaveRequestValidator
import kz.alfasoft.mycompany.ui.AppViewModelProvider
import kz.alfasoft.mycompany.ui.components.DateField
import kz.alfasoft.mycompany.ui.components.SectionCard
import kz.alfasoft.mycompany.ui.components.daysLabel
import kz.alfasoft.mycompany.ui.components.leaveTypeLabel
import kz.alfasoft.mycompany.ui.components.pluralRu
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private enum class PickerTarget { START, END }

@Composable
fun LeaveFormScreen(
    onBack: () -> Unit,
    onSubmitted: (approverName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LeaveFormViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnSubmitted by rememberUpdatedState(onSubmitted)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LeaveFormEvent.Submitted -> currentOnSubmitted(event.approverName)
            }
        }
    }
    LeaveFormContent(
        state = state,
        onBack = onBack,
        onTypeChange = viewModel::onTypeChange,
        onStartChange = viewModel::onStartChange,
        onEndChange = viewModel::onEndChange,
        onSubstituteChange = viewModel::onSubstituteChange,
        onCommentChange = viewModel::onCommentChange,
        onSubmit = viewModel::submit,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaveFormContent(
    state: LeaveFormUiState,
    onBack: () -> Unit,
    onTypeChange: (LeaveType) -> Unit,
    onStartChange: (LocalDate) -> Unit,
    onEndChange: (LocalDate) -> Unit,
    onSubstituteChange: (String?) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Открыт ли календарь — чисто UI-состояние, переживает поворот.
    var picker by rememberSaveable { mutableStateOf<PickerTarget?>(null) }
    val form = state.form

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text("Новая заявка", style = MaterialTheme.typography.titleMedium)
        }

        if (state.loaded && !state.canRequestLeave) {
            Text(
                "Ваши отпуска оформляются через HR-отдел",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Доступно: ${daysLabel(state.available)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            SectionCard(title = "Тип отпуска") {
                Column(Modifier.selectableGroup()) {
                    LeaveType.entries.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = form.type == type, role = Role.RadioButton, onClick = { onTypeChange(type) })
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = form.type == type, onClick = null)
                            Text(leaveTypeLabel(type), Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DateField("Дата начала", form.start, onClick = { picker = PickerTarget.START }, error = state.errors[FormField.START])
                DateField("Дата окончания", form.end, onClick = { picker = PickerTarget.END }, error = state.errors[FormField.END])
                state.errors[FormField.PERIOD]?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                SummaryText(state)
            }

            SubstituteDropdown(state, onSubstituteChange)

            OutlinedTextField(
                value = form.comment,
                onValueChange = onCommentChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (form.type == LeaveType.UNPAID) "Причина (обязательно)" else "Комментарий") },
                minLines = 3,
                isError = state.errors.containsKey(FormField.COMMENT),
                supportingText = {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(state.errors[FormField.COMMENT].orEmpty())
                        Text("${form.comment.length}/${LeaveRequestValidator.MAX_COMMENT_LENGTH}")
                    }
                },
            )
        }

        Button(
            onClick = onSubmit,
            enabled = state.isValid && !state.submitting,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            if (state.submitting) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text("Отправить на согласование")
            }
        }
    }

    picker?.let { target ->
        val today = state.today ?: LocalDate.now()
        val current = if (target == PickerTarget.START) form.start else form.end
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = current?.toUtcMillis(),
            initialDisplayedMonthMillis = (current ?: today.plusDays(1)).toUtcMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { picker = null },
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedDateMillis != null,
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            if (target == PickerTarget.START) onStartChange(date) else onEndChange(date)
                        }
                        picker = null
                    },
                ) { Text("Выбрать") }
            },
            dismissButton = { TextButton(onClick = { picker = null }) { Text("Отмена") } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

@Composable
private fun SummaryText(state: LeaveFormUiState) {
    val days = state.days ?: return
    val total = "$days ${pluralRu(days, "календарный день", "календарных дня", "календарных дней")}"
    val remaining = state.available - days
    val text = if (state.form.type == LeaveType.ANNUAL) {
        // При превышении остатка ошибка уже показана выше, отрицательный остаток не выводим.
        if (remaining >= 0) "Итого: $total. После согласования останется $remaining" else "Итого: $total"
    } else {
        "Итого: $total. Остаток дней не изменится"
    }
    Text(text, style = MaterialTheme.typography.bodyLarge)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubstituteDropdown(state: LeaveFormUiState, onChange: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selected = state.colleagues.firstOrNull { it.id == state.form.substituteId }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.displayName ?: "Не выбран",
            onValueChange = {},
            readOnly = true,
            label = { Text("Замещающий сотрудник") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Не выбран") }, onClick = { onChange(null); expanded = false })
            state.colleagues.forEach { colleague ->
                DropdownMenuItem(
                    text = { Text("${colleague.displayName} · ${colleague.position}") },
                    onClick = { onChange(colleague.id); expanded = false },
                )
            }
        }
    }
}
