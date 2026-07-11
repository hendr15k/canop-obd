package com.canopobd.ui.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.model.*
import com.canopobd.ui.theme.*

@Composable
fun DiagnosticDetailDialog(
    initialDtcCode: String? = null,
    initialCategory: ProblemCategory? = null,
    onDismiss: () -> Unit
) {
    val allCases = remember { DiagnosticProblemCases.getAllCases() }

    var selectedCategory by remember {
        mutableStateOf(
            initialCategory ?: when {
                initialDtcCode != null -> {
                    allCases.find { c -> c.dtcCodes.any { it.equals(initialDtcCode, ignoreCase = true) } }?.category
                        ?: ProblemCategory.TURBO
                }
                else -> ProblemCategory.TURBO
            }
        )
    }

    var selectedCase by remember { mutableStateOf<ProblemCase?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = canopoSurface
        ) {
            if (selectedCase != null) {
                ProblemCaseDetailView(
                    problemCase = selectedCase!!,
                    onBack = { selectedCase = null },
                    onDismiss = onDismiss
                )
            } else {
                ProblemCategoryView(
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    allCases = allCases,
                    onCaseClick = { selectedCase = it },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun ProblemCategoryView(
    selectedCategory: ProblemCategory,
    onCategoryChange: (ProblemCategory) -> Unit,
    allCases: List<ProblemCase>,
    onCaseClick: (ProblemCase) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf(
        ProblemCategory.TURBO,
        ProblemCategory.SENSOR,
        ProblemCategory.EXHAUST,
        ProblemCategory.TIMING_CHAIN,
        ProblemCategory.FUEL_SYSTEM
    )

    val filteredCases = allCases.filter { it.category == selectedCategory }
        .sortedByDescending { it.severity.ordinal * 10 + it.frequency.sortWeight }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Diagnose-Fälle",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = canopoHighlight
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                CategoryChip(
                    category = category,
                    isSelected = category == selectedCategory,
                    onClick = { onCategoryChange(category) },
                    count = allCases.count { it.category == category }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredCases.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Keine Fälle in dieser Kategorie",
                    color = textDim,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(filteredCases) { index, problemCase ->
                    ProblemCaseCard(
                        problemCase = problemCase,
                        onClick = { onCaseClick(problemCase) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: ProblemCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    count: Int
) {
    val icon = when (category) {
        ProblemCategory.TURBO -> Icons.Filled.Speed
        ProblemCategory.SENSOR -> Icons.Filled.Sensors
        ProblemCategory.EXHAUST -> Icons.Filled.Air
        ProblemCategory.TIMING_CHAIN -> Icons.Filled.Build
        ProblemCategory.FUEL_SYSTEM -> Icons.Filled.LocalGasStation
        else -> Icons.Filled.Info
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) canopoAccent.copy(alpha = 0.2f) else canopoDark,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) canopoAccent else textDim,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.displayName,
                fontSize = 12.sp,
                color = if (isSelected) canopoAccent else textSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) canopoAccent else textDim.copy(alpha = 0.3f)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 10.sp,
                    color = if (isSelected) canopoDark else textSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ProblemCaseCard(
    problemCase: ProblemCase,
    onClick: () -> Unit
) {
    val severityColor = when (problemCase.severity) {
        DTCSeverity.CRITICAL -> gaugeRed
        DTCSeverity.WARNING -> gaugeOrange
        DTCSeverity.INFO -> gaugeCyan
        DTCSeverity.PERFORMANCE -> gaugeYellow
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = canopoDark,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = severityColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = problemCase.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (problemCase.isAstraJCommon) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = gaugeOrange.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "A14NET",
                                fontSize = 9.sp,
                                color = gaugeOrange,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = severityColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = problemCase.severity.label,
                            fontSize = 9.sp,
                            color = severityColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                problemCase.dtcCodes.take(3).forEach { code ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = gaugeCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = code,
                            fontSize = 10.sp,
                            color = gaugeCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (problemCase.dtcCodes.size > 3) {
                    Text(
                        text = "+${problemCase.dtcCodes.size - 3}",
                        fontSize = 10.sp,
                        color = textDim,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = problemCase.summary,
                fontSize = 11.sp,
                color = textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Build,
                        contentDescription = null,
                        tint = textDim,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = problemCase.diagnosticSteps.size.toString() + " Schritte",
                        fontSize = 10.sp,
                        color = textDim
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AttachMoney,
                        contentDescription = null,
                        tint = textDim,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "bis " + problemCase.estimatedCostWorkshop,
                        fontSize = 10.sp,
                        color = textDim
                    )
                }
            }
        }
    }
}

@Composable
private fun ProblemCaseDetailView(
    problemCase: ProblemCase,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val severityColor = when (problemCase.severity) {
        DTCSeverity.CRITICAL -> gaugeRed
        DTCSeverity.WARNING -> gaugeOrange
        DTCSeverity.INFO -> gaugeCyan
        DTCSeverity.PERFORMANCE -> gaugeYellow
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = textSecondary)
                }
                Text(
                    text = problemCase.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = canopoHighlight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = RoundedCornerShape(6.dp), color = severityColor.copy(alpha = 0.2f)) {
                Text(
                    text = problemCase.severity.label,
                    fontSize = 11.sp,
                    color = severityColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Surface(shape = RoundedCornerShape(6.dp), color = gaugeYellow.copy(alpha = 0.2f)) {
                Text(
                    text = problemCase.frequency.label,
                    fontSize = 11.sp,
                    color = gaugeYellow,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            if (problemCase.isAstraJCommon) {
                Surface(shape = RoundedCornerShape(6.dp), color = gaugeOrange.copy(alpha = 0.2f)) {
                    Text(
                        text = "Typisch A14NET",
                        fontSize = 11.sp,
                        color = gaugeOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            SectionHeader(title = "Fehlercodes", icon = Icons.Filled.Error)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                problemCase.dtcCodes.forEach { code ->
                    Surface(shape = RoundedCornerShape(6.dp), color = gaugeCyan.copy(alpha = 0.15f)) {
                        Text(
                            text = code,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = gaugeCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SectionHeader(title = "Zusammenfassung", icon = Icons.Filled.Info)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = canopoDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = problemCase.summary,
                    fontSize = 12.sp,
                    color = textPrimary,
                    modifier = Modifier.padding(10.dp),
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SectionHeader(title = "Symptome", icon = Icons.Filled.Visibility)
            problemCase.symptoms.forEach { symptom ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Circle, contentDescription = null, tint = gaugeOrange, modifier = Modifier.size(8.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = symptom, fontSize = 12.sp, color = textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SectionHeader(title = "Mögliche Ursachen", icon = Icons.Filled.QuestionMark)
            problemCase.possibleCauses.forEachIndexed { index, cause ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(shape = RoundedCornerShape(4.dp), color = gaugeYellow.copy(alpha = 0.2f)) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 10.sp,
                            color = gaugeYellow,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = cause, fontSize = 12.sp, color = textSecondary, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SectionHeader(title = "Diagnose-Schritte", icon = Icons.Filled.Build)
            problemCase.diagnosticSteps.forEachIndexed { index, step ->
                DiagnosticStepCard(index = index + 1, step = step)
                if (index < problemCase.diagnosticSteps.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SectionHeader(title = "Kosten-Schätzung", icon = Icons.Filled.AttachMoney)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CostCard(
                    title = "DIY (Selber)",
                    cost = problemCase.estimatedCostDIY,
                    color = gaugeGreen,
                    modifier = Modifier.weight(1f)
                )
                CostCard(
                    title = "Werkstatt",
                    cost = problemCase.estimatedCostWorkshop,
                    color = gaugeOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            if (problemCase.technicalNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(title = "Technische Hinweise", icon = Icons.Filled.Lightbulb)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = gaugeYellow.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp)) {
                        Icon(
                            Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = gaugeYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = problemCase.technicalNotes,
                            fontSize = 11.sp,
                            color = gaugeYellow.copy(alpha = 0.9f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            if (problemCase.relatedDtcCodes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(title = "Verwandte Fehlercodes", icon = Icons.Filled.Link)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    problemCase.relatedDtcCodes.forEach { code ->
                        Surface(shape = RoundedCornerShape(6.dp), color = textDim.copy(alpha = 0.15f)) {
                            Text(
                                text = code,
                                fontSize = 11.sp,
                                color = textSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = canopoAccent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = canopoAccent
        )
    }
}

@Composable
private fun DiagnosticStepCard(index: Int, step: DiagnosticStep) {
    val stepColor = when (step.type) {
        DiagnosticStepType.VISUAL_INSPECTION -> gaugeCyan
        DiagnosticStepType.MEASUREMENT -> canopoAccent
        DiagnosticStepType.TEST -> gaugeYellow
        DiagnosticStepType.REPLACEMENT -> gaugeOrange
        DiagnosticStepType.RESET -> gaugeGreen
    }

    val stepIcon = when (step.type) {
        DiagnosticStepType.VISUAL_INSPECTION -> Icons.Filled.Visibility
        DiagnosticStepType.MEASUREMENT -> Icons.Filled.Speed
        DiagnosticStepType.TEST -> Icons.Filled.BugReport
        DiagnosticStepType.REPLACEMENT -> Icons.Filled.SwapHoriz
        DiagnosticStepType.RESET -> Icons.Filled.Refresh
    }

    Surface(shape = RoundedCornerShape(10.dp), color = canopoDark) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(4.dp), color = stepColor.copy(alpha = 0.2f)) {
                    Text(
                        text = "$index",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = stepColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = stepIcon,
                    contentDescription = null,
                    tint = stepColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = step.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = step.description,
                fontSize = 11.sp,
                color = textSecondary,
                lineHeight = 16.sp
            )
            if (step.expectedValue.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = gaugeGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Sollwert: " + step.expectedValue, fontSize = 10.sp, color = gaugeGreen)
                }
            }
            if (step.warningNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = gaugeOrange, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = step.warningNote, fontSize = 10.sp, color = gaugeOrange)
                }
            }
        }
    }
}

@Composable
private fun CostCard(title: String, cost: String, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(8.dp), color = canopoDark, modifier = modifier) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 10.sp, color = textDim)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = cost, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
