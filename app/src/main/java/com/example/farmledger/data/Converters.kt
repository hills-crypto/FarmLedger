package com.example.farmledger.data

import androidx.room.TypeConverter

// Converts custom enum types to strings for SQLite storage and back to enums when reading.
class Converters {
    // Converts ProjectType enum to String for database storage.
    @TypeConverter
    fun fromProjectType(type: ProjectType): String = type.name

    // Converts String from database back to ProjectType enum.
    @TypeConverter
    fun toProjectType(value: String): ProjectType = ProjectType.valueOf(value)

    // Converts EntryKind enum to String for database storage.
    @TypeConverter
    fun fromEntryKind(kind: EntryKind): String = kind.name

    // Converts String from database back to EntryKind enum.
    @TypeConverter
    fun toEntryKind(value: String): EntryKind = EntryKind.valueOf(value)

    // Converts PaymentType enum to String for database storage.
    @TypeConverter
    fun fromPaymentType(type: PaymentType): String = type.name

    // Converts String from database back to PaymentType enum.
    @TypeConverter
    fun toPaymentType(value: String): PaymentType = PaymentType.valueOf(value)
}
