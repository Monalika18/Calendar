package com.example.calendar.data.repository

import com.example.calendar.data.local.EventDao
import com.example.calendar.data.local.EventEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class EventRepository(private val eventDao: EventDao) {
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    fun getEventsForDate(date: LocalDate): Flow<List<EventEntity>> = eventDao.getEventsForDate(date)

    suspend fun insert(event: EventEntity) {
        eventDao.insertEvent(event)
    }

    suspend fun update(event: EventEntity) {
        eventDao.updateEvent(event)
    }

    suspend fun delete(event: EventEntity) {
        eventDao.deleteEvent(event)
    }
}
