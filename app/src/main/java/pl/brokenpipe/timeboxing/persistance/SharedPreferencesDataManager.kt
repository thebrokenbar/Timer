/*
 * *
 *  * Copyright 2017 Grzegorz Wierzchanowski
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package pl.brokenpipe.timeboxing.persistance

import android.content.SharedPreferences

@Suppress("UNCHECKED_CAST")
class SharedPreferencesDataManager(val sharedPreferences: SharedPreferences): SimpleDataManager {

    override fun <T> setValue(key: String, value: T, clazz: Class<T>) {
        when(clazz) {
            Float::class.java -> {
                sharedPreferences.edit().putFloat(key, value as Float).apply()
            }
            Int::class.java -> {
                sharedPreferences.edit().putInt(key, value as Int).apply()
            }
            Boolean::class.java -> {
                sharedPreferences.edit().putBoolean(key, value as Boolean).apply()
            }
            Long::class.java -> {
                sharedPreferences.edit().putLong(key, value as Long).apply()
            }
            String::class.java -> {
                sharedPreferences.edit().putString(key, value as String).apply()
            }
            else -> throw IllegalArgumentException("${clazz.canonicalName} is not supported")
        }
    }

    override fun <T> getValue(key: String, defaultValue: T, clazz: Class<T>): T {
        when(clazz) {
            Float::class.java -> {
                return sharedPreferences.getFloat(key, defaultValue as Float) as T
            }
            Int::class.java -> {
                return sharedPreferences.getInt(key, defaultValue as Int) as T
            }
            Boolean::class.java -> {
                return sharedPreferences.getBoolean(key, defaultValue as Boolean) as T
            }
            Long::class.java -> {
                return sharedPreferences.getLong(key, defaultValue as Long) as T
            }
            String::class.java -> {
                return sharedPreferences.getString(key, defaultValue as String) as T
            }
            else -> throw IllegalArgumentException("${clazz.canonicalName} is not supported")
        }
    }
}