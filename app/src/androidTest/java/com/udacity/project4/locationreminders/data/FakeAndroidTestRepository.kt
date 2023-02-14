/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.udacity.project4.locationreminders.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersDao

import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.runBlocking
import java.util.LinkedHashMap

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
class FakeAndroidTestRepository : ReminderDataSource {

    var tasksServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()



    override suspend fun getReminder(taskId: String): Result<ReminderDTO> {
        tasksServiceData[taskId]?.let {
            return Result.Success(it)
        }
        return Result.Error("Could not find reminder")
    }


    override suspend fun saveReminder(reminder: ReminderDTO) {
        tasksServiceData[reminder.id] = reminder
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(tasksServiceData.isEmpty()) {
            return Result.Error("No reminders")
        }
        return Result.Success(tasksServiceData.values.toList())
    }

    override suspend fun deleteAllReminders() {
        tasksServiceData.clear()
    }
}
