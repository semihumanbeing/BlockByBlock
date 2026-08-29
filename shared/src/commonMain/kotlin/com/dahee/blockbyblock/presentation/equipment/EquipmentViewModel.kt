package com.dahee.blockbyblock.presentation.equipment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.Equipment
import com.dahee.blockbyblock.domain.model.EquipmentCategory
import com.dahee.blockbyblock.domain.model.MoldGridPreset
import com.dahee.blockbyblock.domain.repository.EquipmentRepository
import com.dahee.blockbyblock.presentation.equipment.state.EquipmentScreenMode
import com.dahee.blockbyblock.presentation.equipment.state.EquipmentUiState
import com.dahee.blockbyblock.presentation.equipment.state.MoldDraftConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EquipmentViewModel(
    private val repository: EquipmentRepository
) : ViewModel() {

    private val _screenMode = MutableStateFlow(EquipmentScreenMode.ONBOARDING)
    private val _moldDrafts = MutableStateFlow(EquipmentUiState.defaultMoldDrafts)
    private val _selectedCookingTools = MutableStateFlow<Set<CookingToolType>>(emptySet())
    private val _editingMold = MutableStateFlow<Equipment?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<EquipmentUiState> = combine(
        repository.getEquipments(),
        _screenMode,
        _moldDrafts,
        _selectedCookingTools,
        _editingMold,
        _errorMessage
    ) { params ->
        val equipments = params[0] as List<Equipment>
        val currentMode = params[1] as EquipmentScreenMode
        val drafts = params[2] as List<MoldDraftConfig>
        val tools = params[3] as Set<CookingToolType>
        val editing = params[4] as Equipment?
        val error = params[5] as String?

        val effectiveMode = if (equipments.isNotEmpty() && currentMode == EquipmentScreenMode.ONBOARDING) {
            EquipmentScreenMode.LIST
        } else {
            currentMode
        }

        EquipmentUiState(
            screenMode = effectiveMode,
            allEquipments = equipments,
            moldDrafts = drafts,
            selectedCookingTools = tools,
            editingMold = editing,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EquipmentUiState()
    )

    // Transition from Screen 1 (Onboarding) to Screen 2 (Setup)
    fun onStartSetupFromOnboarding() {
        _errorMessage.value = null
        _moldDrafts.value = EquipmentUiState.defaultMoldDrafts
        _selectedCookingTools.value = emptySet()
        _screenMode.value = EquipmentScreenMode.SETUP
    }

    // Transition from Screen 3 (List) to Screen 2 (Setup/Edit All)
    fun onOpenEditScreen() {
        _errorMessage.value = null
        val currentEquipments = uiState.value.allEquipments

        // Hydrate drafts from stored equipment state
        val updatedDrafts = EquipmentUiState.defaultMoldDrafts.map { defaultDraft ->
            val existing = currentEquipments.find { it.category == EquipmentCategory.MOLD && it.moldPreset == defaultDraft.preset }
            if (existing != null) {
                defaultDraft.copy(
                    isSelected = true,
                    cellCount = existing.cellCount,
                    quantity = existing.quantity,
                    customCapacityMl = existing.customCapacityMl ?: 300,
                    moldColorHex = existing.moldColorHex
                )
            } else {
                defaultDraft.copy(isSelected = false)
            }
        }

        val existingToolTypes = currentEquipments
            .filter { it.category == EquipmentCategory.COOKING_TOOL }
            .mapNotNull { it.toolType }
            .toSet()

        _moldDrafts.value = updatedDrafts
        _selectedCookingTools.value = existingToolTypes
        _screenMode.value = EquipmentScreenMode.SETUP
    }

    // Cancel setup and return to list or onboarding
    fun onCancelSetup() {
        if (uiState.value.allEquipments.isNotEmpty()) {
            _screenMode.value = EquipmentScreenMode.LIST
        } else {
            _screenMode.value = EquipmentScreenMode.ONBOARDING
        }
    }

    // Toggle mold selection state
    fun onToggleMoldSelection(preset: MoldGridPreset) {
        _errorMessage.value = null
        _moldDrafts.update { list ->
            list.map { draft ->
                if (draft.preset == preset) {
                    draft.copy(isSelected = !draft.isSelected)
                } else {
                    draft
                }
            }
        }
    }

    // Adjust mold quantity in setup draft
    fun onUpdateMoldDraftQuantity(preset: MoldGridPreset, delta: Int) {
        _moldDrafts.update { list ->
            list.map { draft ->
                if (draft.preset == preset) {
                    val newQty = (draft.quantity + delta).coerceAtLeast(1)
                    draft.copy(quantity = newQty, isSelected = true)
                } else {
                    draft
                }
            }
        }
    }

    // Update mold slot count in setup draft
    fun onUpdateMoldDraftCellCount(preset: MoldGridPreset, cellCount: Int) {
        _moldDrafts.update { list ->
            list.map { draft ->
                if (draft.preset == preset) {
                    draft.copy(cellCount = cellCount, isSelected = true)
                } else {
                    draft
                }
            }
        }
    }

    // Update mold color in setup draft
    fun onUpdateMoldDraftColor(preset: MoldGridPreset, colorHex: String) {
        _moldDrafts.update { list ->
            list.map { draft ->
                if (draft.preset == preset) {
                    draft.copy(moldColorHex = colorHex, isSelected = true)
                } else {
                    draft
                }
            }
        }
    }

    // Update custom mold capacity in setup draft
    fun onUpdateMoldDraftCustomCapacity(capacityMl: Int) {
        _moldDrafts.update { list ->
            list.map { draft ->
                if (draft.preset == MoldGridPreset.CUSTOM) {
                    draft.copy(customCapacityMl = capacityMl, isSelected = true)
                } else {
                    draft
                }
            }
        }
    }

    // Toggle cooking tool selection
    fun onToggleCookingTool(toolType: CookingToolType) {
        _selectedCookingTools.update { current ->
            if (current.contains(toolType)) {
                current - toolType
            } else {
                current + toolType
            }
        }
    }

    // Save all equipment changes from Screen 2
    fun onSaveAllEquipment() {
        val selectedMolds = _moldDrafts.value.filter { it.isSelected }
        if (selectedMolds.isEmpty()) {
            _errorMessage.value = "Please select at least 1 mold."
            return
        }

        viewModelScope.launch {
            // Replace entire list with new selection
            val currentList = uiState.value.allEquipments
            currentList.forEach { repository.deleteEquipment(it.id) }

            // Register selected molds
            selectedMolds.forEach { moldDraft ->
                val moldName = if (moldDraft.preset == MoldGridPreset.CUSTOM) {
                    "Custom ${moldDraft.displayCapacity}ml ${moldDraft.cellCount}-slot Mold"
                } else {
                    "${moldDraft.preset.label} ${moldDraft.cellCount}-slot Mold"
                }

                val moldEquipment = Equipment(
                    id = "mold_${moldDraft.preset.name}_${kotlin.random.Random.nextInt(1000, 9999)}",
                    name = moldName,
                    category = EquipmentCategory.MOLD,
                    moldPreset = moldDraft.preset,
                    customCapacityMl = if (moldDraft.preset == MoldGridPreset.CUSTOM) moldDraft.customCapacityMl else null,
                    cellCount = moldDraft.cellCount,
                    quantity = moldDraft.quantity,
                    moldColorHex = moldDraft.moldColorHex,
                    isOwned = true
                )
                repository.addEquipment(moldEquipment)
            }

            // Register selected cooking tools
            _selectedCookingTools.value.forEach { toolType ->
                val toolEquipment = Equipment(
                    id = "tool_${toolType.name}_${kotlin.random.Random.nextInt(1000, 9999)}",
                    name = toolType.displayName,
                    category = EquipmentCategory.COOKING_TOOL,
                    toolType = toolType,
                    quantity = 1,
                    isOwned = true
                )
                repository.addEquipment(toolEquipment)
            }

            _errorMessage.value = null
            _screenMode.value = EquipmentScreenMode.LIST
        }
    }

    // Adjust mold quantity from Screen 3 list
    fun onIncreaseQuantity(equipmentId: String) {
        viewModelScope.launch {
            repository.updateQuantity(equipmentId, +1)
        }
    }

    fun onDecreaseQuantity(equipmentId: String) {
        viewModelScope.launch {
            repository.updateQuantity(equipmentId, -1)
        }
    }

    fun onSetEquipmentQuantity(equipmentId: String, quantity: Int) {
        viewModelScope.launch {
            repository.setQuantity(equipmentId, quantity)
        }
    }

    // Open single mold edit dialog
    fun onOpenMoldEditDialog(equipment: Equipment) {
        _editingMold.value = equipment
    }

    // Close single mold edit dialog
    fun onCloseMoldEditDialog() {
        _editingMold.value = null
    }

    // Save changes for single mold
    fun onSaveSingleMold(updatedEquipment: Equipment) {
        viewModelScope.launch {
            repository.updateEquipment(updatedEquipment)
            _editingMold.value = null
        }
    }

    // Delete single mold
    fun onDeleteSingleMold(equipmentId: String) {
        viewModelScope.launch {
            repository.deleteEquipment(equipmentId)
            _editingMold.value = null
        }
    }
}
