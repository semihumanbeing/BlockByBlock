package com.dahee.blockbyblock.presentation.block

import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.EquipmentCategory
import com.dahee.blockbyblock.domain.model.FoodBlock
import com.dahee.blockbyblock.domain.model.FoodBlockPalette
import com.dahee.blockbyblock.domain.model.IngredientStatus
import com.dahee.blockbyblock.domain.model.StorageType
import com.dahee.blockbyblock.domain.repository.EquipmentRepository
import com.dahee.blockbyblock.domain.repository.FoodBlockRepository
import com.dahee.blockbyblock.domain.repository.IngredientRepository
import com.dahee.blockbyblock.presentation.block.state.BlockUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BlockViewModel(
    private val foodBlockRepository: FoodBlockRepository,
    private val ingredientRepository: IngredientRepository,
    private val equipmentRepository: EquipmentRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(BlockUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        // Observe food blocks
        coroutineScope.launch {
            foodBlockRepository.observeFoodBlocks().collect { blocks ->
                _uiState.update { it.copy(blocks = blocks) }
            }
        }

        // Observe in-stock ingredients
        coroutineScope.launch {
            ingredientRepository.getAllIngredients().collect { ingredients ->
                val inStock = ingredients.filter { it.status == IngredientStatus.IN_STOCK }
                _uiState.update { state ->
                    val validSelected = state.selectedIngredientIds.filter { id ->
                        inStock.any { it.id == id }
                    }.toSet()
                    state.copy(
                        inStockIngredients = inStock,
                        selectedIngredientIds = validSelected
                    )
                }
            }
        }

        // Observe mold and cooking tool equipment
        coroutineScope.launch {
            equipmentRepository.getEquipments().collect { equipments ->
                val molds = equipments.filter { it.category == EquipmentCategory.MOLD && it.isOwned }
                val tools = equipments.filter { it.category == EquipmentCategory.COOKING_TOOL && it.isOwned }
                _uiState.update { state ->
                    val selectedMoldId = state.selectedMoldId?.takeIf { id -> molds.any { it.id == id } }
                        ?: molds.firstOrNull()?.id
                    state.copy(
                        availableMolds = molds,
                        availableCookingTools = tools,
                        selectedMoldId = selectedMoldId
                    )
                }
            }
        }
    }

    fun onCapacityFilterChange(capacityMl: Int?) {
        _uiState.update { it.copy(selectedCapacityMl = capacityMl) }
    }

    fun onBlockColorChange(colorHex: String) {
        _uiState.update { it.copy(selectedBlockColorHex = colorHex) }
    }

    fun onApplyHistoryBlock(history: FoodBlock) {
        _uiState.update { state ->
            if (state.customBlockName == history.name) {
                // Deselect / Reset to blank
                val mold = state.availableMolds.find { it.id == state.selectedMoldId } ?: state.availableMolds.firstOrNull()
                val initialQty = (mold?.cellCount ?: 1)
                state.copy(
                    customBlockName = "",
                    selectedBlockColorHex = "#FF7043",
                    selectedCookingToolTypes = emptySet(),
                    selectedCookingToolType = null,
                    selectedIngredientIds = emptySet(),
                    subIngredients = emptyList(),
                    selectedMoldCount = 1,
                    blockQuantity = initialQty,
                    blockQuantityInput = initialQty.toString(),
                    shelfLifeDaysInput = "90"
                )
            } else {
                val matchedIngredientIds = state.inStockIngredients
                    .filter { history.mainIngredients.contains(it.name) }
                    .map { it.id }
                    .toSet()
                val moldId = history.moldId.takeIf { id -> state.availableMolds.any { it.id == id } }
                    ?: state.selectedMoldId
                val mold = state.availableMolds.find { it.id == moldId }
                val moldCellCount = (mold?.cellCount ?: history.moldCellCount).coerceAtLeast(1)
                val moldCount = (history.quantity / moldCellCount).coerceAtLeast(1)

                val drafts = history.cookingInstructions.map {
                    CookingToolDraft(
                        toolType = it.toolType,
                        temperatureInput = it.temperature?.toString() ?: "",
                        timeMinutesInput = it.timeMinutes?.toString() ?: "",
                        timeSecondsInput = it.timeSeconds?.toString() ?: ""
                    )
                }
                state.copy(
                    customBlockName = history.name,
                    selectedBlockColorHex = history.blockColorHex,
                    selectedCookingTools = drafts,
                    selectedIngredientIds = matchedIngredientIds,
                    subIngredients = history.subIngredients,
                    selectedMoldId = moldId,
                    selectedMoldCount = moldCount,
                    blockQuantity = history.quantity,
                    blockQuantityInput = history.quantity.toString(),
                    storageType = history.storageType,
                    shelfLifeDaysInput = history.shelfLifeDays.toString()
                )
            }
        }
    }

    fun onOpenCreateScreen() {
        _uiState.update { state ->
            val defaultMoldId = state.availableMolds.firstOrNull()?.id
            val defaultMold = state.availableMolds.firstOrNull()
            val totalBlocks = defaultMold?.cellCount ?: 1
            state.copy(
                isCreateScreenOpen = true,
                editingBlockId = null,
                selectedBlockColorHex = "#FF7043",
                selectedIngredientIds = emptySet(),
                ingredientSearchQuery = "",
                ingredientPageIndex = 0,
                subIngredients = emptyList(),
                subIngredientInput = "",
                selectedMoldId = defaultMoldId,
                selectedMoldCount = 1,
                selectedCookingTools = emptyList(),
                blockQuantity = totalBlocks,
                blockQuantityInput = totalBlocks.toString(),
                storageType = StorageType.FREEZER,
                shelfLifeDaysInput = "90",
                customBlockName = ""
            )
        }
    }

    fun onOpenEditScreen(block: FoodBlock) {
        _uiState.update { state ->
            val matchedIngredientIds = state.inStockIngredients
                .filter { block.mainIngredients.contains(it.name) }
                .map { it.id }
                .toSet()
            val moldId = block.moldId.takeIf { id -> state.availableMolds.any { it.id == id } }
                ?: state.availableMolds.firstOrNull()?.id
            val mold = state.availableMolds.find { it.id == moldId }
            val moldCellCount = (mold?.cellCount ?: block.moldCellCount).coerceAtLeast(1)
            val moldCount = (block.quantity / moldCellCount).coerceAtLeast(1)
            val drafts = block.cookingInstructions.map {
                CookingToolDraft(
                    toolType = it.toolType,
                    temperatureInput = it.temperature?.toString() ?: "",
                    timeMinutesInput = it.timeMinutes?.toString() ?: "",
                    timeSecondsInput = it.timeSeconds?.toString() ?: ""
                )
            }

            state.copy(
                isCreateScreenOpen = true,
                editingBlockId = block.id,
                selectedBlockColorHex = block.blockColorHex,
                selectedIngredientIds = matchedIngredientIds,
                ingredientSearchQuery = "",
                ingredientPageIndex = 0,
                subIngredients = block.subIngredients,
                subIngredientInput = "",
                selectedMoldId = moldId,
                selectedMoldCount = moldCount,
                selectedCookingTools = drafts,
                blockQuantity = block.quantity,
                blockQuantityInput = block.quantity.toString(),
                storageType = block.storageType,
                shelfLifeDaysInput = block.shelfLifeDays.toString(),
                customBlockName = block.name
            )
        }
    }

    fun onCloseCreateScreen() {
        _uiState.update {
            it.copy(
                isCreateScreenOpen = false,
                editingBlockId = null
            )
        }
    }

    fun onIngredientSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                ingredientSearchQuery = query,
                ingredientPageIndex = 0
            )
        }
    }

    fun onIngredientPageChange(page: Int) {
        _uiState.update { state ->
            val maxPage = (state.totalIngredientPages - 1).coerceAtLeast(0)
            state.copy(ingredientPageIndex = page.coerceIn(0, maxPage))
        }
    }

    fun onToggleIngredient(ingredientId: String) {
        _uiState.update { state ->
            val current = state.selectedIngredientIds.toMutableSet()
            if (current.contains(ingredientId)) {
                current.remove(ingredientId)
            } else {
                current.add(ingredientId)
            }
            state.copy(selectedIngredientIds = current)
        }
    }

    fun onSubIngredientInputChange(text: String) {
        _uiState.update { it.copy(subIngredientInput = text) }
    }

    fun onAddSubIngredient() {
        val trimmed = _uiState.value.subIngredientInput.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { state ->
            if (state.subIngredients.contains(trimmed)) {
                state.copy(subIngredientInput = "")
            } else {
                state.copy(
                    subIngredients = state.subIngredients + trimmed,
                    subIngredientInput = ""
                )
            }
        }
    }

    fun onAddSubIngredientDirectly(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { state ->
            if (state.subIngredients.contains(trimmed)) {
                state
            } else {
                state.copy(subIngredients = state.subIngredients + trimmed)
            }
        }
    }

    fun onRemoveSubIngredient(name: String) {
        _uiState.update { state ->
            state.copy(subIngredients = state.subIngredients.filter { it != name })
        }
    }

    fun onSelectMold(moldId: String) {
        _uiState.update { state ->
            val mold = state.availableMolds.find { it.id == moldId }
            val moldCount = 1
            val totalBlocks = (mold?.cellCount ?: 1) * moldCount
            state.copy(
                selectedMoldId = moldId,
                selectedMoldCount = moldCount,
                blockQuantity = totalBlocks,
                blockQuantityInput = totalBlocks.toString()
            )
        }
    }

    fun onMoldCountChange(delta: Int) {
        _uiState.update { state ->
            val mold = state.selectedMold ?: return@update state
            val maxCount = mold.quantity.coerceAtLeast(1)
            val newCount = (state.selectedMoldCount + delta).coerceIn(1, maxCount)
            val totalBlocks = mold.cellCount * newCount
            state.copy(
                selectedMoldCount = newCount,
                blockQuantity = totalBlocks,
                blockQuantityInput = totalBlocks.toString()
            )
        }
    }

    fun onBlockQuantityInputChange(text: String) {
        val filtered = text.filter { it.isDigit() }
        val parsed = filtered.toIntOrNull() ?: 0
        _uiState.update { state ->
            state.copy(
                blockQuantityInput = filtered,
                blockQuantity = if (parsed > 0) parsed.coerceIn(1, 999) else state.blockQuantity
            )
        }
    }

    fun onBlockQuantityChange(delta: Int) {
        _uiState.update { state ->
            val current = state.blockQuantity
            val newQty = (current + delta).coerceIn(1, 999)
            state.copy(
                blockQuantity = newQty,
                blockQuantityInput = newQty.toString()
            )
        }
    }

    fun onToggleCookingTool(toolType: CookingToolType) {
        _uiState.update { state ->
            val existing = state.selectedCookingTools.find { it.toolType == toolType }
            val newTools = if (existing != null) {
                state.selectedCookingTools.filter { it.toolType != toolType }
            } else {
                val (defaultTemp, defaultMin, defaultSec) = when (toolType) {
                    CookingToolType.OVEN -> Triple("180", "20", "")
                    CookingToolType.AIR_FRYER -> Triple("180", "15", "")
                    CookingToolType.SLOW_COOKER -> Triple("90", "120", "")
                    CookingToolType.MICROWAVE -> Triple("", "3", "30")
                    CookingToolType.GAS_STOVE -> Triple("", "10", "")
                    CookingToolType.BLENDER -> Triple("", "1", "0")
                    CookingToolType.CUSTOM -> Triple("", "10", "")
                }
                state.selectedCookingTools + CookingToolDraft(
                    toolType = toolType,
                    temperatureInput = defaultTemp,
                    timeMinutesInput = defaultMin,
                    timeSecondsInput = defaultSec
                )
            }
            state.copy(selectedCookingTools = newTools)
        }
    }

    fun onSelectCookingTool(toolType: CookingToolType) = onToggleCookingTool(toolType)

    fun onCookingToolTempChange(toolType: CookingToolType, temp: String) {
        val filtered = temp.filter { it.isDigit() }
        _uiState.update { state ->
            val updated = state.selectedCookingTools.map {
                if (it.toolType == toolType) it.copy(temperatureInput = filtered) else it
            }
            state.copy(selectedCookingTools = updated)
        }
    }

    fun onCookingToolMinutesChange(toolType: CookingToolType, min: String) {
        val filtered = min.filter { it.isDigit() }
        _uiState.update { state ->
            val updated = state.selectedCookingTools.map {
                if (it.toolType == toolType) it.copy(timeMinutesInput = filtered) else it
            }
            state.copy(selectedCookingTools = updated)
        }
    }

    fun onCookingToolSecondsChange(toolType: CookingToolType, sec: String) {
        val filtered = sec.filter { it.isDigit() }
        _uiState.update { state ->
            val updated = state.selectedCookingTools.map {
                if (it.toolType == toolType) it.copy(timeSecondsInput = filtered) else it
            }
            state.copy(selectedCookingTools = updated)
        }
    }

    fun onShelfLifeDaysInputChange(text: String) {
        val filtered = text.filter { it.isDigit() }
        _uiState.update { it.copy(shelfLifeDaysInput = filtered) }
    }

    fun onCustomBlockNameChange(name: String) {
        _uiState.update { it.copy(customBlockName = name) }
    }

    fun onSubmitCreateBlock() {
        val state = _uiState.value
        val mold = state.selectedMold ?: return
        val isEditing = state.isEditing

        val selectedNames = if (state.selectedIngredientIds.isNotEmpty()) {
            state.inStockIngredients
                .filter { state.selectedIngredientIds.contains(it.id) }
                .map { it.name }
        } else {
            state.blocks.find { it.id == state.editingBlockId }?.mainIngredients ?: emptyList()
        }

        if (selectedNames.isEmpty() && state.customBlockName.isBlank()) return

        val blockName = if (state.customBlockName.isNotBlank()) {
            state.customBlockName.trim()
        } else {
            selectedNames.joinToString(" & ")
        }

        val days = state.parsedShelfLifeDays
        val existingBlock = if (isEditing) state.blocks.find { it.id == state.editingBlockId } else null
        val blockId = existingBlock?.id ?: "block_${0L}_${(1000..9999).random()}"
        val createdAt = existingBlock?.createdAt ?: 0L

        val blockToSave = FoodBlock(
            id = blockId,
            name = blockName,
            moldId = mold.id,
            moldName = mold.name,
            moldCapacityMl = mold.displayCapacity,
            moldCellCount = mold.cellCount,
            moldColorHex = mold.moldColorHex,
            moldPreset = mold.moldPreset,
            blockColorHex = state.selectedBlockColorHex,
            mainIngredients = selectedNames,
            subIngredients = state.subIngredients,
            quantity = state.blockQuantity,
            storageType = state.storageType,
            shelfLifeDays = days,
            cookingInstructions = state.selectedCookingTools.map { it.toCookingInstruction() },
            createdAt = createdAt
        )

        coroutineScope.launch {
            foodBlockRepository.saveFoodBlock(blockToSave)
            _uiState.update {
                it.copy(
                    isCreateScreenOpen = false,
                    editingBlockId = null
                )
            }
        }
    }

    fun onDeleteBlock(id: String) {
        coroutineScope.launch {
            foodBlockRepository.deleteFoodBlock(id)
        }
    }
}
