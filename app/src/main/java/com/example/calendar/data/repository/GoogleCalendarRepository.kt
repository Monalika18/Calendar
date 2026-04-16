package com.example.calendar.data.repository

import com.example.calendar.data.remote.api.GoogleCalendarApi
import com.example.calendar.data.remote.model.GoogleEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GoogleCalendarRepository(private val api: GoogleCalendarApi) {

    suspend fun getEvents(token: String, calendarId: String, startDate: LocalDate, endDate: LocalDate): List<GoogleEvent> = withContext(Dispatchers.IO) {
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val timeMin = startDate.atStartOfDay().atOffset(java.time.ZoneOffset.UTC).format(formatter)
        val timeMax = endDate.atTime(23, 59, 59).atOffset(java.time.ZoneOffset.UTC).format(formatter)

        try {
            val response = api.getEvents(
                calendarId = calendarId,
                token = "Bearer $token",
                timeMin = timeMin,
                timeMax = timeMax
            )
            response.items
        } catch (e: Exception) {
            emptyList()
        }
    }
}
