package com.example.calendar.ui.theme

import androidx.compose.ui.graphics.Color

// Palette
val BabyPink = Color(0xFFF8BBD0)
val BabyPinkLight = Color(0xFFFFEEFF)
val BabyPinkDark = Color(0xFFF48FB1)

val DarkBlue = Color(0xFF1A237E)
val DarkBlueLight = Color(0xFF3F51B5)
val DarkBlueDark = Color(0xFF000051)

// New Mapping: Background is Dark Blue, Elements are Baby Pink
val PrimaryLight = BabyPink
val OnPrimaryLight = Color(0xFF560027)
val PrimaryContainerLight = BabyPinkDark
val OnPrimaryContainerLight = Color.White

val SecondaryLight = BabyPinkLight
val OnSecondaryLight = DarkBlueDark
val SecondaryContainerLight = Color(0xFF560027)
val OnSecondaryContainerLight = BabyPinkLight

val BackgroundLight = DarkBlue
val SurfaceLight = DarkBlue
val OnBackgroundLight = Color.White
val OnSurfaceLight = Color.White

// Dark Theme - Same as Light for consistency
val PrimaryDark = BabyPink
val OnPrimaryDark = Color(0xFF560027)
val PrimaryContainerDark = BabyPinkDark
val OnPrimaryContainerDark = Color.White

val SecondaryDark = BabyPinkLight
val OnSecondaryDark = DarkBlueDark
val SecondaryContainerDark = Color(0xFF560027)
val OnSecondaryContainerDark = BabyPinkLight

val BackgroundDark = DarkBlue
val SurfaceDark = DarkBlue
val OnBackgroundDark = Color.White
val OnSurfaceDark = Color.White
