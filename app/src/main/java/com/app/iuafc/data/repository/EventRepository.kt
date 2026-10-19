package com.app.iuafc.data.repository

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Event
import com.app.iuafc.data.model.EventRegistration
import kotlinx.coroutines.delay

interface EventRepository {
    suspend fun getEvents(): List<Event>
    suspend fun createEvent(event: Event)
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(eventId: String)
    suspend fun registerForEvent(eventId: String, memberId: String)
    suspend fun unregisterFromEvent(eventId: String, memberId: String)
    suspend fun getRegisteredEventIds(memberId: String): List<String>
}

class MockEventRepository(private val delayMillis: Long = 300) : EventRepository {
    // We use mutable lists so changes save temporarily while the app is running
    private val eventsList = MockData.events.toMutableList()
    private val registrationsList = MockData.eventRegistrations.toMutableList()

    override suspend fun getEvents(): List<Event> {
        delay(delayMillis)
        return eventsList.toList()
    }

    override suspend fun createEvent(event: Event) {
        delay(delayMillis)
        eventsList.add(event)
    }

    override suspend fun updateEvent(event: Event) {
        delay(delayMillis)
        val index = eventsList.indexOfFirst { it.id == event.id }
        if (index != -1) {
            eventsList[index] = event
        }
    }

    override suspend fun deleteEvent(eventId: String) {
        delay(delayMillis)
        eventsList.removeAll { it.id == eventId }
    }

    override suspend fun registerForEvent(eventId: String, memberId: String) {
        delay(delayMillis)
        // Don't double register
        if (registrationsList.none { it.eventId == eventId && it.memberId == memberId }) {
            registrationsList.add(EventRegistration(eventId, memberId))
        }
    }

    override suspend fun unregisterFromEvent(eventId: String, memberId: String) {
        delay(delayMillis)
        registrationsList.removeAll { it.eventId == eventId && it.memberId == memberId }
    }

    override suspend fun getRegisteredEventIds(memberId: String): List<String> {
        delay(delayMillis)
        return registrationsList.filter { it.memberId == memberId }.map { it.eventId }
    }
}