package com.dahee.blockbyblock.presentation.equipment.state

import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.Equipment
import com.dahee.blockbyblock.domain.model.EquipmentCategory
import com.dahee.blockbyblock.domain.model.MoldCapacityUnit
import com.dahee.blockbyblock.domain.model.MoldGridPreset

enum class EquipmentScreenMode {
    ONBOARDING, // Screen 1: "Let's register equipment" onboarding
    SETUP,      // Screen 2: Size configuration & quick tool selector
    LIST        // Screen 3: Equipment list with [Edit All] button
}

data class MoldDraftConfig(
    val id: String = "draft_${preset.name}",
    val preset: MoldGridPreset,
    val name: String = "",
    val isSelected: Boolean = false,
    val capacityMl: Int = preset.capacityMl.coerceAtLeast(10),
    val cellCount: Int = 4,
    val quantity: Int = 1,
    val moldColorHex: String = "#BAE6FD"
) {
    val displayCapacity: Int
        get() = if (capacityMl > 0) capacityMl else if (preset == MoldGridPreset.CUSTOM) 200 else preset.capacityMl
}

data class EquipmentUiState(
    val screenMode: EquipmentScreenMode = EquipmentScreenMode.SETUP,
    val allEquipments: List<Equipment> = emptyList(),
    val moldDrafts: List<MoldDraftConfig> = defaultMoldDrafts,
    val capacityUnit: MoldCapacityUnit = MoldCapacityUnit.ML,
    val selectedCookingTools: Set<CookingToolType> = emptySet(),
    val editingMold: Equipment? = null, // Used for single mold edit dialog
    val errorMessage: String? = null
) {
    val moldEquipments: List<Equipment>
        get() = allEquipments.filter { it.category == EquipmentCategory.MOLD }

    val toolEquipments: List<Equipment>
        get() = allEquipments.filter { it.category == EquipmentCategory.COOKING_TOOL }

    companion object {
        val defaultMoldDrafts = listOf(
            MoldDraftConfig(id = "draft_ML_500", preset = MoldGridPreset.ML_500, isSelected = true, capacityMl = 500, cellCount = 2, quantity = 1, moldColorHex = "#BAE6FD"),
            MoldDraftConfig(id = "draft_ML_250", preset = MoldGridPreset.ML_250, isSelected = false, capacityMl = 250, cellCount = 4, quantity = 1, moldColorHex = "#A7F3D0"),
            MoldDraftConfig(id = "draft_ML_125", preset = MoldGridPreset.ML_125, isSelected = false, capacityMl = 125, cellCount = 6, quantity = 1, moldColorHex = "#FECDD3"),
            MoldDraftConfig(id = "draft_ML_30", preset = MoldGridPreset.ML_30, isSelected = false, capacityMl = 30, cellCount = 16, quantity = 1, moldColorHex = "#FEF08A"),
            MoldDraftConfig(id = "draft_CUSTOM_1", preset = MoldGridPreset.CUSTOM, isSelected = false, capacityMl = 200, cellCount = 6, quantity = 1, moldColorHex = "#E9D5FF")
        )
    }
}
