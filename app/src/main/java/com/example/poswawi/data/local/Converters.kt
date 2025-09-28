package com.example.poswawi.data.local

import androidx.room.TypeConverter
import com.example.poswawi.data.model.DiscountType
import com.example.poswawi.data.model.EmployeeRole
import com.example.poswawi.data.model.PaymentMethod
import com.example.poswawi.data.model.ProductCategory
import com.example.poswawi.data.model.TaxRate
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromTaxRate(value: TaxRate?): String? = value?.name

    @TypeConverter
    fun toTaxRate(value: String?): TaxRate? = value?.let { TaxRate.valueOf(it) }

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod?): String? = value?.name

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod? = value?.let { PaymentMethod.valueOf(it) }

    @TypeConverter
    fun fromDiscountType(value: DiscountType?): String? = value?.name

    @TypeConverter
    fun toDiscountType(value: String?): DiscountType? = value?.let { DiscountType.valueOf(it) }

    @TypeConverter
    fun fromEmployeeRole(value: EmployeeRole?): String? = value?.name

    @TypeConverter
    fun toEmployeeRole(value: String?): EmployeeRole? = value?.let { EmployeeRole.valueOf(it) }

    @TypeConverter
    fun fromProductCategory(value: ProductCategory?): String? = value?.name

    @TypeConverter
    fun toProductCategory(value: String?): ProductCategory? = value?.let { ProductCategory.valueOf(it) }
}
