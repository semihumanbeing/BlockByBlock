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

    fun onSelectCookingTool(toolType: CookingToolType?) {
        _uiState.update { state ->
            val next = if (state.selectedCookingToolType == toolType) null else toolType
            state.copy(selectedCookingToolType = next)
        }
    }

    fun onApplyHistoryBlock(history: FoodBlock) {
        _uiState.update { state ->
            val matchedIngredientIds = state.inStockIngredients
                .filter { history.mainIngredients.contains(it.name) }
                .map { it.id }
                .toSet()
            val moldId = history.moldId.takeIf { id -> state.availableMolds.any { it.id == id } }
                ?: state.selectedMoldId
            val mold = state.availableMolds.find { it.id == moldId }
            val moldCellCount = (mold?.cellCount ?: history.moldCellCount).coerceAtLeast(1)
            val moldCount = (history.quantity / moldCellCount).coerceAtLeast(1)

            state.copy(
                customBlockName = history.name,
                selectedBlockColorHex = history.blockColorHex,
                selectedCookingToolType = history.cookingToolType,
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

    fun onOpenCreateScreen() {
        _uiState.update { state ->
            val firstMold = state.availableMolds.firstOrNull()
            val initialMoldId = state.selectedMoldId ?: firstMold?.id
            val mold = state.availableMolds.find { it.id == initialMoldId }
            val initialMoldCount = 1
            val initialQty = (mold?.cellCount ?: 1) * initialMoldCount
            state.copy(
                isCreateScreenOpen = true,
                editingBlockId = null,
                selectedBlockColorHex = FoodBlockPalette.defaultColorHex,
                selectedIngredientIds = emptySet(),
                ingredientSearchQuery = "",
                ingredientPageIndex = 0,
                subIngredients = emptyList(),
                subIngredientInput = "",
                selectedMoldId = initialMoldId,
                selectedMoldCount = initialMoldCount,
                selectedCookingToolType = null,
                blockQuantity = initialQty,
                blockQuantityInput = initialQty.toString(),
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
                selectedCookingToolType = block.cookingToolType,
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
            selectedNames.joinToString(" & ") + " 블록"
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
            cookingToolType = state.selectedCookingToolType,
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
