package com.example.calendar.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CalendarEventsResponse(
    @Json(name = "items") val items: List<GoogleEvent> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GoogleEvent(
    @Json(name = "id") val id: String,
    @Json(name = "summary") val summary: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "start") val start: EventDateTime?,
    @Json(name = "end") val end: EventDateTime?
)

@JsonClass(generateAdapter = true)
data class EventDateTime(
    @Json(name = "date") val date: String?, // For all-day events (YYYY-MM-DD)
    @Json(name = "dateTime") val dateTime: String? // For timed events (RFC3339)
)
