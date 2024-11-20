package com.arcmce.boogaloo.ui.viewmodel

import androidx.lifecycle.*
import com.arcmce.boogaloo.network.model.MixCloudCloudcast
import com.arcmce.boogaloo.network.model.ScheduleItem
import com.arcmce.boogaloo.util.toDayWithSuffix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.ZonedDateTime


class ScheduleViewModel() : ViewModel() {

    private val _radioSchedule = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val radioSchedule: StateFlow<List<ScheduleItem>> = _radioSchedule

    // Map of date -> list of schedule items
    private val _scheduleByDate = MutableStateFlow<Map<String, List<ScheduleItem>>>(emptyMap())
    val scheduleByDate: StateFlow<Map<String, List<ScheduleItem>>> = _scheduleByDate

    // List of unique day names
    private val _uniqueDayNames = MutableStateFlow<List<String>>(emptyList())
    val uniqueDayNames: StateFlow<List<String>> = _uniqueDayNames

    private val _formattedDayNames = MutableStateFlow<List<String>>(emptyList())
    val formattedDayNames: StateFlow<List<String>> = _formattedDayNames

    // Index of the current day in the uniqueDayNames list
    private val _currentDayIndex = MutableStateFlow(0)
    val currentDayIndex: StateFlow<Int> = _currentDayIndex

    // Index of the current show within the current day's schedule
    private val _currentShowIndex = MutableStateFlow(0)
    val currentShowIndex: StateFlow<Int> = _currentShowIndex

//    // The list of schedule items for the currently selected day
//    private val _currentDaySchedule = MutableStateFlow<List<ScheduleItem>>(emptyList())
//    val currentDaySchedule: StateFlow<List<ScheduleItem>> = _currentDaySchedule

    fun setRadioSchedule(radioSchedule: List<ScheduleItem>) {
        _radioSchedule.value = radioSchedule

        updateScheduleByDate(radioSchedule)
        updateCurrentDayAndShow()
    }

    fun updateScheduleByDate(radioSchedule: List<ScheduleItem>) {
        val groupedSchedule = radioSchedule.groupBy { it.start.toDayWithSuffix() }
        _scheduleByDate.value = groupedSchedule

        val dayNames = groupedSchedule.keys.toList()
        _uniqueDayNames.value = dayNames
        updateFormattedDays(dayNames)
    }

    private fun updateFormattedDays(uniqueDays: List<String>) {
        val now = ZonedDateTime.now()

        val formattedDays = uniqueDays.map { day ->
            when (day) {
                now.minusDays(1).toDayWithSuffix() -> "Yesterday"
                now.toDayWithSuffix() -> "Today"
                now.plusDays(1).toDayWithSuffix() -> "Tomorrow"
                else -> day
            }
        }
        _formattedDayNames.value = formattedDays
    }

    fun updateCurrentDayAndShow() {
        val currentTime = ZonedDateTime.now()

        val dayWithSuffix = currentTime.toDayWithSuffix()
        val dayIndex = _uniqueDayNames.value.indexOf(dayWithSuffix).takeIf { it >= 0 } ?: 0
        _currentDayIndex.value = dayIndex

        val todaySchedule = _scheduleByDate.value[dayWithSuffix].orEmpty()
//        _currentDaySchedule.value = todaySchedule

        val showIndex = todaySchedule.indexOfFirst { scheduleItem ->
            currentTime.isAfter(scheduleItem.start) && currentTime.isBefore(scheduleItem.end)
        }.takeIf { it >= 0 } ?: 0
        _currentShowIndex.value = showIndex
    }

//    fun getScheduleDays(radioSchedule: List<ScheduleItem>) {
//        val scheduleDays = radioSchedule.takeIf { it.isNotEmpty() }?.map { item ->
//            item.start.toDayWithSuffix()
//        }?.distinct() ?: emptyList()
//
//        _scheduleDays.value = scheduleDays
//
//
//    }
//
//    fun getDaySchedules(radioSchedule: List<ScheduleItem>) {
//
//    }

}
