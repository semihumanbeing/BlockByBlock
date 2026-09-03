package com.dahee.blockbyblock.presentation.block.state

import com.dahee.blockbyblock.domain.model.CookingInstruction
import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.Equipment
import com.dahee.blockbyblock.domain.model.FoodBlock
import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientCategory

data class CookingToolDraft(
    val toolType: CookingToolType,
    val temperatureInput: String = "",
    val timeMinutesInput: String = "",
    val timeSecondsInput: String = ""
) {
    val parsedTemperature: Int?
        get() = temperatureInput.trim().toIntOrNull()

    val parsedTimeMinutes: Int?
        get() = timeMinutesInput.trim().toIntOrNull()

    val parsedTimeSeconds: Int?
        get() = timeSecondsInput.trim().toIntOrNull()

    fun toCookingInstruction(): CookingInstruction = CookingInstruction(
        toolType = toolType,
        temperature = parsedTemperature,
        timeMinutes = parsedTimeMinutes,
        timeSeconds = parsedTimeSeconds
    )
}

data class BlockUiState(
    val blocks: List<FoodBlock> = emptyList(),
    val selectedCapacityMl: Int? = null, // null for All, or specific capacity in ml
    val isCreateScreenOpen: Boolean = false,

    // Create Block Form State
    val inStockIngredients: List<Ingredient> = emptyList(),
    val selectedIngredientIds: Set<String> = emptySet(),
    val ingredientSearchQuery: String = "",
    val ingredientPageIndex: Int = 0,

    val subIngredients: List<String> = emptyList(),
    val subIngredientInput: String = "",

    val availableMolds: List<Equipment> = emptyList(),
    val selectedMoldId: String? = null,
    val selectedMoldCount: Int = 1, // Number of molds to use (1..mold.quantity)

    val availableCookingTools: List<Equipment> = emptyList(),
    val selectedCookingTools: List<CookingToolDraft> = emptyList(),

    val blockQuantity: Int = 1,
    val blockQuantityInput: String = "1",
    val selectedBlockColorHex: String = "#FF7043", // 3D Food Block Color
    val shelfLifeDaysInput: String = "90",
    val customBlockName: String = "",
    val editingBlockId: String? = null
) {
    val isEditing: Boolean
        get() = editingBlockId != null

    val selectedCookingToolTypes: Set<CookingToolType>
        get() = selectedCookingTools.map { it.toolType }.toSet()

    companion object {
        const val INGREDIENTS_PAGE_SIZE = 4
    }

    val distinctMoldCapacities: List<Int>
        get() {
            val fromMolds = availableMolds.map { it.displayCapacity }
            val fromBlocks = blocks.map { it.moldCapacityMl }
            return (fromMolds + fromBlocks).distinct().sortedDescending()
        }

    val historyBlocks: List<FoodBlock>
        get() = blocks.distinctBy { it.name.trim() }

    val filteredBlocks: List<FoodBlock>
        get() = if (selectedCapacityMl == null) {
            blocks
        } else {
            blocks.filter { it.moldCapacityMl == selectedCapacityMl }
        }

    val inStockMainIngredients: List<Ingredient>
        get() = inStockIngredients.filter { it.category != IngredientCategory.SAUCE_SEASONING }

    val inStockSeasonings: List<Ingredient>
        get() = inStockIngredients.filter { it.category == IngredientCategory.SAUCE_SEASONING }

    val filteredInStockIngredients: List<Ingredient>
        get() {
            val list = inStockMainIngredients
            if (ingredientSearchQuery.isBlank()) return list
            val query = ingredientSearchQuery.trim()
            return list.filter { it.name.contains(query, ignoreCase = true) }
        }

    val totalIngredientPages: Int
        get() {
            val count = filteredInStockIngredients.size
            return if (count == 0) 1 else (count + INGREDIENTS_PAGE_SIZE - 1) / INGREDIENTS_PAGE_SIZE
        }

    val pagedInStockIngredients: List<Ingredient>
        get() {
            val list = filteredInStockIngredients
            if (list.isEmpty()) return emptyList()
            val safePage = ingredientPageIndex.coerceIn(0, totalIngredientPages - 1)
            val start = safePage * INGREDIENTS_PAGE_SIZE
            val end = (start + INGREDIENTS_PAGE_SIZE).coerceAtMost(list.size)
            return list.subList(start, end)
        }

    val selectedMold: Equipment?
        get() = availableMolds.find { it.id == selectedMoldId }

    val parsedShelfLifeDays: Int
        get() = shelfLifeDaysInput.trim().toIntOrNull() ?: 90

    val canSubmit: Boolean
        get() = (customBlockName.isNotBlank() || selectedIngredientIds.isNotEmpty()) && selectedMoldId != null && blockQuantity > 0
}
