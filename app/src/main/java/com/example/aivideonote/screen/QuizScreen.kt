package com.example.aivideonote.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aivideonote.screen.auth.MyColors

@Composable
fun QuizUI(
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
    viewModel: GeminiViewModel = hiltViewModel(),
    score: (Int) -> Unit = {},
) {
    val points = viewModel.mcqList.count { it.selectedOption == it.answer }
    val mcqList = viewModel.mcqList

    LaunchedEffect(points) {
        score(points)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        state = scrollState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(mcqList) { index, mcq ->
            // Question Card

            Column {
                // Question Text
                Text(
                    text = mcq.question,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Options List
                mcq.options.forEach { option ->
                    val isSelected = mcq.selectedOption == option
                    val isCorrect = mcq.answer == option
                    val isRevealed = mcq.selectedOption != null

                    // Determine button appearance
                    val (containerColor, borderColor, textColor) = when {
                        isRevealed && isCorrect -> Triple(
                            Color(0x0A4CFF00),
                            MyColors().green,
                            MyColors().green

                        ) // Green for correct answer
                        isSelected -> Triple(
                            Color(0x0AFF0000),
                            Color(0xFFFF3B3B),
                            Color(0xFFD50000)
                        ) // Red for wrong selection
                        else -> Triple(
                            Color.Transparent,
                            Color.Gray,
                            Color.DarkGray
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (!isRevealed) {
                                viewModel.selectOption(index, option)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .fillMaxWidth(),
                        border = BorderStroke(1.dp, borderColor),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor)
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .fillMaxWidth(),
                            color = textColor,
                        )
                    }
                }

            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}