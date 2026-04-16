package com.example.calendar.data.remote.api

import com.example.calendar.data.remote.model.CalendarEventsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleCalendarApi {
    @GET("calendar/v3/calendars/{calendarId}/events")
    suspend fun getEvents(
        @Path("calendarId") calendarId: String,
        @Header("Authorization") token: String,
        @Query("timeMin") timeMin: String,
        @Query("timeMax") timeMax: String,
        @Query("singleEvents") singleEvents: Boolean = true,
        @Query("orderBy") orderBy: String = "startTime"
    ): CalendarEventsResponse
}
