package com.unsmah.pathpulse.ui.reports

import androidx.compose.ui.platform.LocalContext
import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import com.unsmah.pathpulse.R
import com.unsmah.pathpulse.ui.MainViewModel
import com.unsmah.pathpulse.ui.components.EmptyState
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReportItem(
    val id: Long,
    val title: String,
    val date: Long,
    val type: String,
    val sizeBytes: Long,
    val filePath: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var reports by remember { mutableStateOf<List<ReportItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reports_title)) },
                actions = {
                    IconButton(onClick = { /* Refresh */ }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (reports.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Description,
                title = stringResource(R.string.reports_empty),
                message = stringResource(R.string.reports_empty_message)
            )
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                items(reports, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        onClick = { /* View report */ },
                        onShare = { /* Share report */ },
                        onDelete = { showDeleteDialog = report.id }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { reportId ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete report?") },
            text = { Text("This report will be permanently deleted.") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        deleteReport(context, reportId)
                        showDeleteDialog = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    loadReports(context) { loadedReports ->
        reports = loadedReports
        isLoading = false
    }
}

@Composable
private fun ReportCard(
    report: ReportItem,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on type
            Icon(
                imageVector = when (report.type.lowercase()) {
                    "pdf" -> Icons.Default.Description
                    "png" -> Icons.Default.Image
                    else -> Icons.Default.Share
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(report.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatFileSize(report.sizeBytes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.reports_share),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.reports_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    }
}


private fun loadReports(context: android.content.Context, onResult: (List<ReportItem>) -> Unit) {
    runBlocking {
        try {
            val reportsDir = File(context.getExternalFilesDir(null), "reports")
            if (!reportsDir.exists()) {
                onResult(emptyList())
                return@runBlocking
            }
            
            val items = reportsDir.listFiles()
                ?.filter { it.isFile }
                ?.mapIndexed { index, file ->
                    ReportItem(
                        id = index.toLong(),
                        title = file.nameWithoutExtension,
                        date = file.lastModified(),
                        type = getFileExtension(file.name),
                        sizeBytes = file.length(),
                        filePath = file.absolutePath
                    )
                }
                ?.sortedByDescending { it.date }
                ?: emptyList()
            
            onResult(items)
        } catch (e: Exception) {
            onResult(emptyList())
        }
    }
}

private fun getFileExtension(filename: String): String {
    return filename.substringAfterLast(".", "unknown")
}

private fun deleteReport(context: android.content.Context, reportId: Long) {
    runBlocking {
        try {
            val reportsDir = File(context.getExternalFilesDir(null), "reports")
            val files = reportsDir.listFiles()
            if (files != null && reportId < files.size) {
                files[reportId.toInt()].delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
private fun CircularProgressIndicator() {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = Modifier.size(48.dp),
        color = MaterialTheme.colorScheme.primary
    )
}
