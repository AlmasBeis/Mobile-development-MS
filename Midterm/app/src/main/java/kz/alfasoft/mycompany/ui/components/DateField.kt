package kz.alfasoft.mycompany.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.LocalDate

/** Поле только для чтения: тап по нему открывает выбор даты. */
@Composable
fun DateField(
    label: String,
    value: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
) {
    Box(modifier) {
        OutlinedTextField(
            value = value?.let { "%02d.%02d.%d".format(it.dayOfMonth, it.monthValue, it.year) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("дд.мм.гггг") },
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        // Поверх текстового поля, чтобы тап по любой его части открывал календарь.
        Box(Modifier.matchParentSize().clickable(onClick = onClick))
    }
}
