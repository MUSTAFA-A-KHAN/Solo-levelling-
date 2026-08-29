package com.sololeveling.system.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sololeveling.system.domain.model.AttributeType
import com.sololeveling.system.domain.model.ActivityRequirement
import com.sololeveling.system.domain.model.HydrationData
import java.lang.reflect.Type

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromAttributeRewardsMap(value: Map<AttributeType, Double>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAttributeRewardsMap(value: String?): Map<AttributeType, Double> {
        if (value == null) {
            return emptyMap()
        }
        val mapType: Type = object : TypeToken<Map<AttributeType, Double>>() {}.type
        return gson.fromJson(value, mapType) ?: emptyMap()
    }

    @TypeConverter
    fun fromActivityRequirement(value: ActivityRequirement?): String? {
        if (value == null) return null
        return gson.toJson(value)
    }

    @TypeConverter
    fun toActivityRequirement(value: String?): ActivityRequirement? {
        if (value == null) return null
        val type: Type = object : TypeToken<ActivityRequirement>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromHydrationData(value: HydrationData?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toHydrationData(value: String?): HydrationData? {
        if (value == null) return null
        val type: Type = object : TypeToken<HydrationData>() {}.type
        return gson.fromJson(value, type) ?: HydrationData()
    }
}
