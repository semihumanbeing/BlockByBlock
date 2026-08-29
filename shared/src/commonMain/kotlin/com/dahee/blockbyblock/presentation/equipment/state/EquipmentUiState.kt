package com.dahee.blockbyblock.presentation.equipment.state

import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.Equipment
import com.dahee.blockbyblock.domain.model.EquipmentCategory
import com.dahee.blockbyblock.domain.model.MoldGridPreset

enum class EquipmentScreenMode {
    ONBOARDING, // Screen 1: "Let's register equipment" onboarding
    SETUP,      // Screen 2: Size configuration & quick tool selector
    LIST        // Screen 3: Equipment list with [Edit All] button
}

data class MoldDraftConfig(
    val preset: MoldGridPreset,
    val isSelected: Boolean = false,
    val customCapacityMl: Int = 300,
    val cellCount: Int = 4,
    val quantity: Int = 1,
    val moldColorHex: String = "#BAE6FD"
) {
    val displayCapacity: Int
        get() = if (preset == MoldGridPreset.CUSTOM) customCapacityMl else preset.capacityMl
}

data class EquipmentUiState(
    val screenMode: EquipmentScreenMode = EquipmentScreenMode.ONBOARDING,
    val allEquipments: List<Equipment> = emptyList(),
    val moldDrafts: List<MoldDraftConfig> = defaultMoldDrafts,
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
            MoldDraftConfig(preset = MoldGridPreset.ML_500, isSelected = true, cellCount = 2, quantity = 1, moldColorHex = "#BAE6FD"),
            MoldDraftConfig(preset = MoldGridPreset.ML_250, isSelected = false, cellCount = 4, quantity = 1, moldColorHex = "#A7F3D0"),
            MoldDraftConfig(preset = MoldGridPreset.ML_125, isSelected = false, cellCount = 6, quantity = 1, moldColorHex = "#FECDD3"),
            MoldDraftConfig(preset = MoldGridPreset.ML_75, isSelected = false, cellCount = 12, quantity = 1, moldColorHex = "#FEF08A"),
            MoldDraftConfig(preset = MoldGridPreset.CUSTOM, isSelected = false, customCapacityMl = 300, cellCount = 12, quantity = 1, moldColorHex = "#E9D5FF")
        )
    }
}
