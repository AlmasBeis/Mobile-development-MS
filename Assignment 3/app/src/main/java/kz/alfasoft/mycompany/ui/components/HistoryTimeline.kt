package kz.alfasoft.mycompany.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.unit.dp
import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.data.model.LeaveEvent
import kz.alfasoft.mycompany.data.model.LeaveEventType
import java.time.LocalDateTime

fun eventLabel(type: LeaveEventType) = when (type) {
    LeaveEventType.CREATED -> "Создана"
    LeaveEventType.EDITED -> "Изменена"
    LeaveEventType.SUBMITTED -> "Отправлена на согласование"
    LeaveEventType.APPROVED -> "Согласована"
    LeaveEventType.REJECTED -> "Отклонена"
    LeaveEventType.CANCELLED -> "Отозвана"
    LeaveEventType.RETURNED_TO_DRAFT -> "Возвращена в черновик"
}

fun formatDateTime(at: LocalDateTime) = "${formatDate(at.toLocalDate())} %02d:%02d".format(at.hour, at.minute)

@Composable
private fun eventColor(type: LeaveEventType): Color = when (type) {
    LeaveEventType.APPROVED -> MaterialTheme.colorScheme.secondary
    LeaveEventType.REJECTED -> MaterialTheme.colorScheme.error
    LeaveEventType.SUBMITTED -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.outline
}

/** Вертикальная лента событий: точка, линия к следующему событию, что/кто/когда и комментарий. */
@Composable
fun HistoryTimeline(
    items: List<Pair<LeaveEvent, Employee?>>,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        items.forEachIndexed { index, (event, actor) ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxHeight()) {
                    Box(
                        Modifier
                            .padding(top = 4.dp)
                            .size(12.dp)
                            .background(eventColor(event.type), CircleShape),
                    )
                    if (index != items.lastIndex) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.outlineVariant),
                        )
                    }
                }
                Column(Modifier.padding(bottom = 12.dp)) {
                    Text(eventLabel(event.type), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${actor?.displayName ?: event.actorId} · ${formatDateTime(event.at)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    event.comment?.let {
                        Text("«$it»", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
