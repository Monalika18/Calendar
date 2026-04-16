package com.example.calendar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendar.data.local.AppDatabase
import com.example.calendar.data.local.EventEntity
import com.example.calendar.data.repository.EventRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

import com.example.calendar.data.remote.api.GoogleCalendarApi
import com.example.calendar.data.remote.model.GoogleEvent
import com.example.calendar.data.repository.GoogleCalendarRepository
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository
    private val googleRepository: GoogleCalendarRepository
    val allEvents: StateFlow<List<EventEntity>>

    private val _googleEvents = MutableStateFlow<List<GoogleEvent>>(emptyList())
    val googleEvents: StateFlow<List<GoogleEvent>> = _googleEvents.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _userAccount = MutableStateFlow<android.accounts.Account?>(null)

    private val _authIntent = MutableSharedFlow<android.content.Intent>()
    val authIntent = _authIntent.asSharedFlow()

    init {
        val eventDao = AppDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)
        allEvents = repository.allEvents.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        val googleApi = retrofit.create(GoogleCalendarApi::class.java)
        googleRepository = GoogleCalendarRepository(googleApi)
    }

    fun setUserAccount(account: android.accounts.Account?) {
        _userAccount.value = account
        if (account != null) {
            fetchGoogleEvents()
        }
    }

    fun fetchGoogleEvents() {
        val account = _userAccount.value ?: return
        viewModelScope.launch {
            try {
                val token = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.google.android.gms.auth.GoogleAuthUtil.getToken(
                        getApplication(),
                        account,
                        "oauth2:https://www.googleapis.com/auth/calendar.readonly"
                    )
                }
                
                val start = _currentMonth.value.atDay(1)
                val end = _currentMonth.value.atEndOfMonth()
                
                // Fetch primary calendar events
                android.util.Log.d("CalendarSync", "Fetching events for primary...")
                val events = googleRepository.getEvents(token, "primary", start, end)
                android.util.Log.d("CalendarSync", "Found ${events.size} primary events")
                
                // Fetch holidays
                android.util.Log.d("CalendarSync", "Fetching holidays...")
                val holidays = googleRepository.getEvents(token, "en.indian#holiday@group.v.calendar.google.com", start, end)
                android.util.Log.d("CalendarSync", "Found ${holidays.size} holidays")
                
                _googleEvents.value = events + holidays
            } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
                android.util.Log.w("CalendarSync", "User consent required")
                _authIntent.emit(e.intent ?: return@launch)
            } catch (e: Exception) {
                android.util.Log.e("CalendarSync", "Error fetching Google events", e)
                e.printStackTrace()
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _currentMonth.value = YearMonth.from(date)
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun setCurrentMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    fun addEvent(title: String, description: String, date: LocalDate, isBirthday: Boolean) {
        viewModelScope.launch {
            repository.insert(EventEntity(title = title, description = description, date = date, isBirthday = isBirthday))
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.delete(event)
        }
    }
}
