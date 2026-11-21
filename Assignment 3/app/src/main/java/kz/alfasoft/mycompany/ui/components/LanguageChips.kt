package kz.alfasoft.mycompany.ui.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.alfasoft.mycompany.data.model.LanguageSkill

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguageChips(languages: List<LanguageSkill>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        languages.forEach {
            AssistChip(onClick = {}, label = { Text("${it.language} — ${it.level}") })
        }
    }
}
