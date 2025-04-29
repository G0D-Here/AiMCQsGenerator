package com.example.aivideonote.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aivideonote.data.Resource
import com.example.aivideonote.data.remote.AuthUiState
import com.example.aivideonote.screen.auth.AuthViewModel
import com.example.aivideonote.screen.auth.MyColors
import com.example.aivideonote.screen.navigation.CustomQuizScreen

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: GeminiViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    navController: NavController
) {
    val clipboardManager = LocalClipboardManager.current

    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val controller = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var isExpanded by remember { mutableStateOf(false) }
    var mcqOptions by remember { mutableStateOf(false) }
    var mcqQuant by remember { mutableIntStateOf(10) }
    var score by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("Do You Know?\uD83E\uDD28") }
    var showDifficulty by remember { mutableStateOf(false) }
    var langClicked by remember { mutableStateOf(false) }
    var lang by remember { mutableStateOf("English") }
    var profileCard by remember { mutableStateOf(false) }

    val scrollState = rememberLazyListState()
    var searchBarVisible by remember { mutableStateOf(false) }
    var off by remember { mutableIntStateOf(0) }

    LaunchedEffect(scrollState) {
        profileCard = false

        snapshotFlow { scrollState.firstVisibleItemScrollOffset }
            .collect { offset ->
                off = offset
                searchBarVisible = when {
                    offset == 0 -> true
                    else -> false
                }
            }
    }

    LaunchedEffect(off) {
        profileCard = false
    }

    LaunchedEffect(score) {
        text = viewModel.feedback(score, mcqQuant)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Row(
            Modifier
                .background(Color.Transparent)
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text,
                color = Color(0xFF000000),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            ElevatedCard(
                Modifier
                    .wrapContentSize(),
                elevation = CardDefaults.elevatedCardElevation(4.dp),
                colors = CardDefaults.elevatedCardColors(MyColors().green)
            ) {
                Box(Modifier.padding(6.dp)) {
                    Text(
                        text = "Score $score/$mcqQuant",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

            }
            IconButton(onClick = {
//                profileCard = !profileCard
                navController.navigate(CustomQuizScreen())
            }) {
                Icon(imageVector = Icons.Filled.Person, null)
            }
        }
        Column(
            modifier
                .padding(10.dp)
                .background(Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                state.let { it ->
                    when (it) {
                        is Resource.Error -> Text(it.message)
                        is Resource.Loading -> {
                            PulsingRippleLoader()
                        }

                        is Resource.Success -> {
                            if (it.data?.isEmpty() == true) Text(
                                "Write\nSomeThing!",
                                fontSize = MaterialTheme.typography.displayMedium.fontSize,
                                lineHeight = 45.sp,
                                color = Color.Black
                            )
                            QuizUI(scrollState = scrollState) { score = it }
                        }
                    }
                }
            }


        }
        if (viewModel.camera) {
            CameraScreen(modifier = Modifier.fillMaxSize()) {
                viewModel.prompt = it
                viewModel.camera = false
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .wrapContentSize()
        ) {
            when (val authState = authViewModel.authState.collectAsState().value) {
                is AuthUiState.Error -> {}
                AuthUiState.Idle -> {}
                AuthUiState.Loading -> {}
                is AuthUiState.Success -> {

                    AnimatedVisibility(
                        visible = profileCard,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        ElevatedCard(
                            Modifier.padding(10.dp),
                            elevation = CardDefaults.elevatedCardElevation(4.dp),
                            colors = CardDefaults.elevatedCardColors(Color.White)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text(
                                    text = "Email: \n" + authState.data.email,
                                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                    fontFamily = FontFamily.Serif
                                )
                                HorizontalDivider(Modifier.padding(8.dp), color = MyColors().green)
                                Text(
                                    text = "Name: \n" + authState.data.username,
                                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                    fontFamily = FontFamily.Serif
                                )
                                HorizontalDivider(Modifier.padding(8.dp), color = MyColors().green)
                                Text(text = "Key: " + authState.data.key)
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(onClick = {
                                        clipboardManager.setText(AnnotatedString(authState.data.key))
                                    }, colors = ButtonDefaults.buttonColors(Color.Black)) {
                                        Text("Copy Key")
                                    }
                                    OutlinedButton(onClick = {
                                        authViewModel.logout()
                                    }, border = BorderStroke(1.dp, color = Color.Black)) {
                                        Text("Logout", color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = searchBarVisible,
            modifier = Modifier
//                .padding(2.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            ElevatedCard(
                modifier = Modifier,
                colors = CardDefaults.cardColors(Color(0xFFFFFFFF)),
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Transparent),
                ) {
                    val interactionSource = remember { MutableInteractionSource() }

                    TextField(
                        value = viewModel.prompt,
                        onValueChange = { viewModel.prompt = it },
                        label = { Text("Be specific") },
                        placeholder = {
                            Text(
                                "Marvel Cinematic Universe etc",
                                color = Color.LightGray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                isExpanded = it.isFocused
                            },
                        maxLines = 10,
                        interactionSource = interactionSource,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                isExpanded = false
                                controller?.hide()
                                focusManager.clearFocus()
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                        )
                    )
                }

                AnimatedVisibility(showDifficulty) {
                    DifficultyLevel(viewModel.buttonClicked) { name, int ->
                        viewModel.difficulty = name
                        viewModel.buttonClicked = int
                        showDifficulty = false
                    }
                }

                AnimatedContent(mcqOptions) { moreOptions ->
                    if (moreOptions) MCQsOptions {
                        mcqQuant = it
                        mcqOptions = false
                    }
                    else
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(1.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(40.dp))
                                    .weight(.6f)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = { mcqOptions = true },
                                            onTap = {
                                                score = 0
                                                isExpanded = false
                                                viewModel.askGemini(
                                                    text = "\n extract out $mcqQuant ${viewModel.difficulty} mcq in $lang language out of it and do not give generic starting and ending but in the form of Json so that i can copy paste"
                                                )
                                            })
                                    },
                                colors = CardDefaults.elevatedCardColors(
                                    Color(0xFF000000)
                                )
                            ) {
                                Box(
                                    Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Give $mcqQuant MCQs",
                                        Modifier,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            LevelButton(
                                modifier = Modifier
                                    .padding(0.dp)
                                    .weight(0.53f),
                                text = "Difficulty",
                                onClick = {
                                    showDifficulty = !showDifficulty
                                })

                            LevelButton(
                                Modifier
                                    .padding(0.dp)
                                    .weight(0.32f),
                                text = if (langClicked) "En" else "Hi",
                                onClick = {
                                    langClicked = !langClicked
                                    lang = if (langClicked) "English" else "Hindi"
                                })
                            IconButton(
                                onClick = { viewModel.camera = true },
                                modifier = Modifier
                                    .weight(.2f)
                                    .padding(2.dp),
                            ) {
                                Icon(Icons.Filled.Camera, "Open Camera")
                            }
                        }
                }

            }
        }


    }

}


@Composable
fun DifficultyLevel(clicked: Int = 0, onClick: (String, Int) -> Unit = { _, _ -> }) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LevelButton(
            text = "Easy",
            color = if (clicked == 0) Color.Green else Color.White,
        ) { onClick(it, 0) }
        LevelButton(
            text = "Medium",
            color = if (clicked == 1) Color.Yellow else Color.White,
        ) { onClick(it, 1) }
        LevelButton(
            text = "Hard",
            color = if (clicked == 2) Color.Red else Color.White,
        ) { onClick(it, 2) }
    }
}

@Composable
fun PulsingRippleLoader() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val color by infiniteTransition.animateColor(
        initialValue = Color.Black,
        targetValue = MyColors().green,
        animationSpec = infiniteRepeatable(

            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse

        )
    )
    Box(
        modifier = Modifier
            .wrapContentSize()
            .scale(scale)
            .background(
                color = color.copy(alpha = alpha),
                shape = CircleShape
            )
    ) {
        Box(Modifier.size(30.dp))
    }

}


@Composable
fun MCQsOptions(onClick: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LevelButton(text = "10 MCQs", onClick = { onClick(10) })
        LevelButton(
            text = "20 MCQs",
            onClick = { onClick(20) })
        LevelButton(text = "30 MCQs", onClick = { onClick(30) })
    }
}

@Composable
fun LevelButton(
    modifier: Modifier = Modifier,
    text: String = "",
    color: Color = Color.White,
    textColor: Color = Color.Black,
    onClick: (String) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .padding(8.dp)
            .border(1.dp, color, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 👈 disables ripple
                onClick = { onClick(text) }
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
