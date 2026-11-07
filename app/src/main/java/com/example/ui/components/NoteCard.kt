package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun isHexColorDark(hex: Long): Boolean {
    val r = ((hex shr 16) and 0xFF)
    val g = ((hex shr 8) and 0xFF)
    val b = (hex and 0xFF)
    val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
    return luminance < 130
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isCardDark = isHexColorDark(note.colorHex)

    // Using Color(hex.toInt()) guarantees 100% solid, fully-opaque color.
    // 0xFFFFFFFF.toInt() produces pure, crisp solid white.
    val cardColor = Color(note.colorHex.toInt())

    // High-contrast text colors based on card lightness
    val contentTextColor = if (isCardDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val subtleTextColor = if (isCardDark) Color(0xFFCBD5E1) else Color(0xFF475569)

    val isWhiteCard = (note.colorHex and 0xFFFFFFL) == 0xFFFFFFL
    val borderColor = if (note.isPinned) {
        if (isCardDark) Color(0xFFFDE047) else MaterialTheme.colorScheme.primary
    } else {
        if (isWhiteCard) {
            Color(0xFFCBD5E1) // Crisp neat border for white card
        } else if (isCardDark) {
            Color(0xFF475569)
        } else {
            cardColor.copy(alpha = 0.85f)
        }
    }

    val formattedDate = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.ENGLISH)
        .format(Date(note.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (note.isPinned) 4.dp else 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (note.isPinned) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Category badge & Pin indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCardDark) {
                        Color.Black.copy(alpha = 0.35f)
                    } else if (isWhiteCard) {
                        Color(0xFFF1F5F9)
                    } else {
                        Color.White.copy(alpha = 0.75f)
                    },
                    modifier = Modifier.border(
                        0.5.dp,
                        if (isCardDark) Color.White.copy(alpha = 0.2f) else Color(0xFFCBD5E1),
                        RoundedCornerShape(8.dp)
                    )
                ) {
                    Text(
                        text = note.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCardDark) Color(0xFFF1F5F9) else Color(0xFF334155),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (note.isPinned) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                if (isCardDark) Color.Black.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = stringResource(R.string.pin),
                            tint = if (isCardDark) Color(0xFFFDE047) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Pinned",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCardDark) Color(0xFFFDE047) else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentTextColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Content preview
            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentTextColor.copy(alpha = 0.90f),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Timestamp
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = subtleTextColor,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pin toggle button
                IconButton(
                    onClick = onPinClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("pin_note_${note.id}")
                ) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (note.isPinned) stringResource(R.string.unpin) else stringResource(R.string.pin),
                        tint = if (note.isPinned) {
                            if (isCardDark) Color(0xFFFDE047) else MaterialTheme.colorScheme.primary
                        } else subtleTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Share button
                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                note.title.ifBlank { "ILIA Notes" }
                            )
                            putExtra(
                                Intent.EXTRA_TEXT,
                                buildString {
                                    if (note.title.isNotBlank()) appendLine(note.title)
                                    append(note.content)
                                }
                            )
                        }
                        context.startActivity(
                            Intent.createChooser(shareIntent, "Share note")
                        )
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("share_note_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = stringResource(R.string.share),
                        tint = subtleTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Edit button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("edit_note_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = subtleTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_note_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = stringResource(R.string.delete),
                        tint = if (isCardDark) Color(0xFFF87171) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
