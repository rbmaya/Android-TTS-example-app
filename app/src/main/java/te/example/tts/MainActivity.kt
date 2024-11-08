package te.example.tts

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import te.example.tts.ui.theme.TTSTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null
    private var voices by mutableStateOf(emptyList<Voice>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initializeTts()

        setContent {
            TTSTheme {
                var ruVoices by remember { mutableStateOf(emptyList<Voice>()) }
                var selectedVoice by remember { mutableStateOf<Voice?>(null) }

                LaunchedEffect(voices) {
                    ruVoices = voices.filter { it.locale.language == "ru" }
                    selectedVoice = tts?.voice
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        ExampleTextView(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = {
                                convertTextToSpeech(it)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            text = "Вариант голоса:"
                        )
                        VoiceSelector(
                            modifier = Modifier.fillMaxWidth(),
                            voices = ruVoices,
                            selected = selectedVoice
                        ) {
                            selectedVoice = it
                            tts?.setVoice(it)
                        }
                    }
                }
            }
        }
    }

    private fun initializeTts() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Toast.makeText(this, "TTS is successfully initialized!", Toast.LENGTH_LONG).show()
                setLanguageAndVoice()
            } else {
                Toast.makeText(this, "Failed to initialize TTS", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setLanguageAndVoice() {
        tts?.setLanguage(Locale("ru"))

        println("Available voices: ${tts!!.voices.joinToString("\n")}")
        voices = tts?.voices.orEmpty().toList()
    }

    private fun convertTextToSpeech(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()

        tts?.let {
            it.stop()
            it.shutdown()
        }
    }
}

@Composable
fun ExampleTextView(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    TextField(
        modifier = modifier.padding(24.dp),
        value = text,
        textStyle = TextStyle.Default,
        onValueChange = { newText ->
            text = newText
        },
        leadingIcon = {
            IconButton(
                onClick = {
                    onClick(text)
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSelector(
    modifier: Modifier,
    voices: List<Voice>,
    selected: Voice?,
    onSelected: (Voice) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier.padding(horizontal = 24.dp),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selected?.let { voices.indexOf(it).inc().toString() } ?: "",
            onValueChange = {},
            textStyle = TextStyle.Default,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            readOnly = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            voices.forEachIndexed { index, voice ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${index.inc()}",
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelected(voice)
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ExampleTextViewPreview() {
    ExampleTextView { }
}