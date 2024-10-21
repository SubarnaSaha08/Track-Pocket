import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trackpocket.BakingViewModel
import com.example.trackpocket.R
import com.example.trackpocket.UiState

@Composable
fun BakingScreen(
    bakingViewModel: BakingViewModel = viewModel()
) {
    val selectedImage = remember { mutableStateOf<Bitmap?>(null) }
    val placeholderPrompt = stringResource(R.string.prompt_placeholder)
    val placeholderResult = stringResource(R.string.results_placeholder)
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            selectedImage.value = BitmapFactory.decodeStream(inputStream)
        }
    }

    // Wrapping the Column with a Box and setting the background color
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(context.getColor(R.color.background_grey)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Show selected image or prompt for image upload
            selectedImage.value?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(16.dp)
                )
            } ?: run {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Upload Image")
                }
            }

            // TextField and Button for prompt
            Row(
                modifier = Modifier.padding(all = 16.dp)
            ) {
                TextField(
                    value = prompt,
                    label = { Text(stringResource(R.string.label_prompt)) },
                    onValueChange = { prompt = it },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(end = 16.dp)
                        .background(color = Color(context.getColor(R.color.white)))
                        .align(Alignment.CenterVertically)
                )

                Column {
                    Button(
                        onClick = {
                            selectedImage.value?.let { bitmap ->
                                bakingViewModel.sendPrompt(bitmap, prompt)
                            }
                        },
                        enabled = prompt.isNotEmpty() && selectedImage.value != null,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(context.getColor(R.color.purple)), // Background color
                            contentColor = Color(context.getColor(R.color.white)) // Text color
                        )
                    ) {
                        Text(text = stringResource(R.string.action_go))
                    }

                    // Refresh Button
                    Button(
                        onClick = {
                            // Clear previous results and reset the state
                            result = placeholderResult
                            selectedImage.value = null // Optionally clear the selected image
                            prompt = placeholderPrompt // Reset the prompt
                        },
                        enabled = prompt.isNotEmpty() && selectedImage.value != null && uiState is UiState.Success,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(context.getColor(R.color.purple)), // Background color
                            contentColor = Color(context.getColor(R.color.white)) // Text color
                        )
                    ) {
                        Text(text = "Refresh")
                    }
                }
            }

            Column(
                modifier = Modifier.padding(all = 16.dp)
            ) {
                // Loading or displaying results
                if (uiState is UiState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize() // Ensure the Box takes up the available space
                            .padding(16.dp),
                        contentAlignment = Alignment.Center // Center the CircularProgressIndicator
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    var textColor = Color(context.getColor(R.color.black))
                    if (uiState is UiState.Error) {
                        textColor = Color(context.getColor(R.color.red))
                        result = (uiState as UiState.Error).errorMessage
                    } else if (uiState is UiState.Success) {
                        result = (uiState as UiState.Success).outputText
                    }
                    val scrollState = rememberScrollState()
                    Text(
                        text = result,
                        textAlign = TextAlign.Start,
                        color = textColor,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    )
                }

            }
        }
    }
}
