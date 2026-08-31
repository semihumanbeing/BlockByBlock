package com.dahee.blockbyblock.domain.model

/**
 * Mold Capacity to Toy Food Block Size Category definition:
 * - MINI: 2x2 (0 ~ 100ml, representative 75ml)
 * - SMALL: 1x4 (101 ~ 200ml, representative 125ml)
 * - MEDIUM: 2x4 (201 ~ 399ml, representative 250ml)
 * - LARGE: 3x4 (400ml ~, representative 500ml)
 */
enum class BlockSizeCategory(
    val titleKo: String,
    val titleEn: String,
    val blockSpec: String,
    val minCapacityMl: Int,
    val maxCapacityMl: Int
) {
    MINI(
        titleKo = "미니",
        titleEn = "Mini",
        blockSpec = "2x2",
        minCapacityMl = 0,
        maxCapacityMl = 100
    ),
    SMALL(
        titleKo = "스몰",
        titleEn = "Small",
        blockSpec = "1x4",
        minCapacityMl = 101,
        maxCapacityMl = 200
    ),
    MEDIUM(
        titleKo = "미디엄",
        titleEn = "Medium",
        blockSpec = "2x4",
        minCapacityMl = 201,
        maxCapacityMl = 399
    ),
    LARGE(
        titleKo = "라지",
        titleEn = "Large",
        blockSpec = "3x4",
        minCapacityMl = 400,
        maxCapacityMl = Int.MAX_VALUE
    );

    companion object {
        fun fromCapacity(capacityMl: Int): BlockSizeCategory {
            return when {
                capacityMl <= 100 -> MINI
                capacityMl in 101..200 -> SMALL
                capacityMl in 201..399 -> MEDIUM
                else -> LARGE
            }
        }
    }
}
