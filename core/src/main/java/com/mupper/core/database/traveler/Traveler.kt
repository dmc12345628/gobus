package com.mupper.core.database.traveler

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mupper.core.database.CurrentPosition

/**
 * Created by jesus.medina on 12/2019.
 * Mupper
 */
@Entity(tableName = "traveler")
class Traveler(
    @PrimaryKey @ColumnInfo(name = "email") val email: String,
    @Embedded val currentPosition: CurrentPosition,
    @ColumnInfo(name = "is_traveling") val isTraveling: Boolean
)