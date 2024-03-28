package com.example.diaryapp.presentation.component


import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.diaryapp.model.Diary
import com.example.diaryapp.model.Mood
import com.example.diaryapp.ui.theme.Elevation
import com.example.diaryapp.utils.toInstant
import io.realm.kotlin.ext.realmListOf
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

//lecture 27 recall.....
@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun DiaryHolder(diary: Diary, onClick: (String) -> Unit) {
    val localDensity = LocalDensity.current
    var componentHeight by remember { mutableStateOf(0.dp) }
    var galleryLoading by remember { mutableStateOf(false) }
    var galleryOpened by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = MutableInteractionSource()
        ) { onClick(diary._id.toString()) }) {
        Spacer(modifier = Modifier.width(14.dp))
        Surface(
            modifier = Modifier
                .width(2.dp)
                .height(14.dp), tonalElevation = Elevation.Level1
        ) {}
    }
    Spacer(modifier = Modifier.width(20.dp))
    Surface(
        modifier = Modifier
            .clip(shape = Shapes().medium)
            .onGloballyPositioned {
                componentHeight = with(localDensity) { it.size.height.toDp() }
            },
        tonalElevation = Elevation.Level1
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            DiaryHeader(moodName = diary.mood, time = diary.date.toInstant())
            Text(
                text = diary.description,
                modifier = Modifier.padding(all = 14.dp),
                style = TextStyle(fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            if (diary.images.isNotEmpty()) {
                ShowGalleryButton(
                    galleryOpened = galleryOpened,
                    onClick = {
                        galleryOpened = !galleryOpened
                    }
                )
            }
//            AnimatedVisibility(
//                visible = galleryOpened && !galleryLoading,
//                enter = fadeIn() + expandVertically(
//                    animationSpec = spring(
//                        dampingRatio = Spring.DampingRatioMediumBouncy,
//                        stiffness = Spring.StiffnessLow
//                    )
            AnimatedVisibility(visible = galleryOpened) {
                Column(modifier = Modifier.padding(all = 14.dp)) {
                    Gallery(images = diary.images)
                }
            }

        }
    }
}

@Composable
fun DiaryHeader(moodName: String, time: Instant) {
    val mood by remember { mutableStateOf(Mood.valueOf(moodName)) }
    val formatter = remember {
        DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(mood.containerColor)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                modifier = Modifier.size(18.dp),
                painter = painterResource(id = mood.icon),
                contentDescription = "Mood Icon",
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = mood.name,
                color = mood.contentColor,
                style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
            )
        }
        Text(
            text = formatter.format(time),
            color = mood.contentColor,
            style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
        )
    }
}



@Composable
fun ShowGalleryButton(galleryOpened: Boolean, onClick: () -> Unit) {

    TextButton(onClick = { onClick }) {
        Text(
            text = if (galleryOpened) "Hide Gallery" else "Show Gallery",
            style = TextStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize)
        )
    }
}

@Composable
@Preview
fun DiaryHolderPreview() {
    DiaryHolder(diary = Diary().apply {
        title = "My Diary"
        description =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
        mood = Mood.Happy.name
        images = realmListOf("","")
    }, onClick = {} )
}