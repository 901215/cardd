package com.example.cardd

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.cardd.ui.theme.CarddTheme
import kotlinx.coroutines.delay

// 定義資料類別
data class Question(val question: String, val options: List<String>, val correctAnswer: String, val imageRes: Any)
data class Emotion(val name: String, val iconRes: Int)

sealed class Screen {
    object Home : Screen()
    object Learning : Screen()
    object Custom : Screen()
    object Express : Screen()
    object AllFlashcards : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarddTheme {
                MyApp()
            }
        }
    }
}

@Composable
fun MyApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val customFlashcards = remember { mutableStateListOf<Question>() }
    var selectedEmotion by remember { mutableStateOf<Emotion?>(null) }

    when (currentScreen) {
        is Screen.Home -> HomeScreen(onOptionSelected = { selectedScreen ->
            currentScreen = selectedScreen
        })
        is Screen.Learning -> LearningFlashcardsScreen(
            customFlashcards = customFlashcards,
            onBack = { currentScreen = Screen.Home }
        )
        is Screen.Custom -> CustomFlashcardsScreen(
            onBack = { currentScreen = Screen.Home },
            onAddFlashcard = { customFlashcards.add(it) }
        )
        is Screen.Express -> {
            selectedEmotion?.let {
                EmotionDetailScreen(it) {
                    selectedEmotion = null
                    currentScreen = Screen.Express
                }
            } ?: EmotionSelectionScreen(
                onEmotionSelected = { selectedEmotion = it; currentScreen = Screen.Express },
                onBack = { currentScreen = Screen.Home }
            )
        }
        is Screen.AllFlashcards -> AllFlashcardsScreen(
            customFlashcards = customFlashcards,
            onBack = { currentScreen = Screen.Home }
        )
    }
}

@Composable
fun HomeScreen(onOptionSelected: (Screen) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景
        Image(
            painter = rememberImagePainter(R.drawable.back1),  // 將 'background' 替換為您的背景圖片的資源名稱
            contentDescription = "",
            modifier = Modifier.fillMaxSize()
        )

        // 內容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = rememberImagePainter(R.drawable.app),
                contentDescription = "",
                modifier = Modifier.fillMaxWidth().size(100.dp)  // 讓圖片填滿寬度
            )

            Spacer(modifier = Modifier.height(16.dp))  // 加入一個間隔

            Button(onClick = { onOptionSelected(Screen.Learning) }) {
                Text("學習字卡")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onOptionSelected(Screen.Custom) }) {
                Text("自訂字卡")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onOptionSelected(Screen.Express) }) {
                Text("表達心情")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onOptionSelected(Screen.AllFlashcards) }) {
                Text("所有字卡")
            }
        }
    }
}


@Composable
fun LearningFlashcardsScreen(customFlashcards: List<Question>, onBack: () -> Unit) {
    val predefinedQuestions = listOf(
        Question("這是什麼顏色?", listOf("紅色", "藍色", "綠色"), "藍色", R.drawable.question_1),
        Question("這是什麼顏色?", listOf("綠色", "黃色", "橘色"), "橘色", R.drawable.question_2),
        Question("這是什麼顏色?", listOf("藍色", "白色", "黑色"), "黑色", R.drawable.question_3),
        Question("這是什麼形狀?", listOf("圓形", "正方形", "三角形"), "圓形", R.drawable.question_4),
        Question("這是什麼形狀?", listOf("圓形", "正方形", "長方形"), "正方形", R.drawable.question_5),
        Question("這是什麼形狀?", listOf("圓形", "正方形", "長方形"), "長方形", R.drawable.question_6),
        Question("這是什麼動物?", listOf("狗", "貓", "鳥"), "狗", R.drawable.question_7),
        Question("這是什麼動物?", listOf("狗", "貓", "鳥"), "貓", R.drawable.question_8),
        Question("這是什麼動物?", listOf("狗", "貓", "鳥"), "鳥", R.drawable.question_9),
        Question("這是什麼水果?", listOf("蘋果", "香蕉", "橘子"), "蘋果", R.drawable.question_10),
        Question("這是什麼水果?", listOf("蘋果", "香蕉", "橘子"), "香蕉", R.drawable.question_11),
        Question("這是什麼水果?", listOf("蘋果", "香蕉", "橘子"), "橘子", R.drawable.question_12)
    )

    val questions = remember { (predefinedQuestions + customFlashcards).shuffled().take(6) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var showCongrats by remember { mutableStateOf(false) }
    var showCompletion by remember { mutableStateOf(false) }
    var showCorrectAnswer by remember { mutableStateOf(false) }

    val currentQuestion = questions[currentQuestionIndex % questions.size] // Use modulo operator to wrap around the index

    if (showCompletion) {
        CompletionScreen(onBack)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (showCongrats) {
                CongratsScreen {
                    showCongrats = false
                    currentQuestionIndex++
                    if (currentQuestionIndex >= questions.size) {
                        showCompletion = true
                    }
                }
            } else {
                // Display current question with image
                when (val imageRes = currentQuestion.imageRes) {
                    is Int -> Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp)
                            .padding(bottom = 16.dp)
                    )
                    is Uri -> Image(
                        painter = rememberImagePainter(imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp)
                            .padding(bottom = 16.dp)
                    )
                }
                Text(
                    text = currentQuestion.question,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (!showCorrectAnswer) {
                    currentQuestion.options.forEach { option ->
                        AnswerOption(option = option, onOptionSelected = { answer ->
                            if (answer == currentQuestion.correctAnswer) {
                                showCongrats = true
                            } else {
                                showCorrectAnswer = true
                            }
                        })
                    }
                } else {
                    Text(
                        text = "正確答案: ${currentQuestion.correctAnswer}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LaunchedEffect(Unit) {
                        delay(2000) // 延迟2秒跳转到下一题
                        showCorrectAnswer = false
                        currentQuestionIndex++
                        if (currentQuestionIndex >= questions.size) {
                            showCompletion = true
                        }
                    }
                }
            }
        }
    }
    Button(onClick = onBack) {
        Text("返回")
    }
}




@Composable
fun CompletionScreen(onBack: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000) // 完成畫面顯示2秒
        onBack.invoke()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "恭喜完成所有題目!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CongratsScreen(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000) // 恭喜畫面顯示2秒
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "恭喜答對!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AnswerOption(option: String, onOptionSelected: (String) -> Unit) {
    Button(
        onClick = { onOptionSelected(option) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(option)
    }
}

@Composable
fun CustomFlashcardsScreen(onBack: () -> Unit, onAddFlashcard: (Question) -> Unit) {
    var questionText by remember { mutableStateOf("") }
    var option1 by remember { mutableStateOf("") }
    var option2 by remember { mutableStateOf("") }
    var option3 by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = questionText,
            onValueChange = { questionText = it },
            label = { Text("問題") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = option1,
            onValueChange = { option1 = it },
            label = { Text("選項1") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = option2,
            onValueChange = { option2 = it },
            label = { Text("選項2") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = option3,
            onValueChange = { option3 = it },
            label = { Text("選項3") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = correctAnswer,
            onValueChange = { correctAnswer = it },
            label = { Text("正確答案") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("選擇圖片")
        }
        selectedImageUri?.let { uri ->
            Image(
                painter = rememberImagePainter(uri),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(top = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val newQuestion = Question(
                questionText,
                listOf(option1, option2, option3),
                correctAnswer,
                selectedImageUri ?: Uri.EMPTY // 預設值
            )
            onAddFlashcard(newQuestion)
            onBack()
        }) {
            Text("新增字卡")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("返回")
        }
    }
}

@Composable
fun EmotionSelectionScreen(onEmotionSelected: (Emotion) -> Unit, onBack: () -> Unit) {
    val emotions = listOf(
        Emotion("快樂", R.drawable.ic_happy),
        Emotion("悲傷", R.drawable.ic_sad),
        Emotion("生氣", R.drawable.ic_angry),
        Emotion("驚喜", R.drawable.ic_surprised)
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(emotions) { emotion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEmotionSelected(emotion) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = emotion.iconRes),
                        contentDescription = emotion.name,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = emotion.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        // 返回按鈕
        Button(onClick = onBack, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
            Text("返回")
        }
    }
}


@Composable
fun EmotionDetailScreen(emotion: Emotion, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = emotion.iconRes),
            contentDescription = emotion.name,
            modifier = Modifier.size(128.dp)
        )
        Text(
            text = "${emotion.name}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(onClick = onBack) {
            Text("返回")
        }
    }
}

@Composable
fun AllFlashcardsScreen(customFlashcards: List<Question>, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(customFlashcards) { flashcard ->
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display flashcard image
                when (val imageRes = flashcard.imageRes) {
                    is Int -> Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp)
                    )
                    is Uri -> Image(
                        painter = rememberImagePainter(imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp)
                    )
                }
                Text(
                    text = flashcard.question,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "選項: ${flashcard.options.joinToString(", ")}",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "正確答案: ${flashcard.correctAnswer}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
    Button(onClick = onBack) {
        Text("返回")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CarddTheme {
        MyApp()
    }
}
