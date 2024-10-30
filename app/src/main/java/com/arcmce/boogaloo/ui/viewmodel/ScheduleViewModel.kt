package com.arcmce.boogaloo.ui.viewmodel

import androidx.lifecycle.*
import com.arcmce.boogaloo.network.model.MixCloudCloudcast
import com.arcmce.boogaloo.network.model.ScheduleItem
import com.arcmce.boogaloo.util.toDayWithSuffix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class ScheduleViewModel() : ViewModel() {

    private val _radioSchedule = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val radioSchedule: StateFlow<List<ScheduleItem>> = _radioSchedule

    private val _daySchedule = MutableStateFlow<Map<String, List<ScheduleItem>>>(emptyMap())
    val daySchedule: StateFlow<Map<String, List<ScheduleItem>>> = _daySchedule

    private val _scheduleDays = MutableStateFlow<List<String>>(emptyList())
    val scheduleDays: StateFlow<List<String>> = _scheduleDays

    fun setRadioSchedule(radioSchedule: List<ScheduleItem>) {
        _radioSchedule.value = radioSchedule

        updateDaySchedule(radioSchedule)

    }


    fun updateDaySchedule(radioSchedule: List<ScheduleItem>) {
        val groupedSchedule = radioSchedule.groupBy { it.start.toDayWithSuffix() }
        _daySchedule.value = groupedSchedule

        val scheduleDays = groupedSchedule.keys.toList()
        _scheduleDays.value = scheduleDays
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
