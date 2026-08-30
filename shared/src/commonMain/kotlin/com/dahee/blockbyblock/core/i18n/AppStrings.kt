package com.dahee.blockbyblock.core.i18n

import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus
import com.dahee.blockbyblock.domain.model.IngredientUnit
import com.dahee.blockbyblock.domain.model.MoldGridPreset

interface AppStrings {
    // Common
    val appTitle: String
    val save: String
    val cancel: String
    val edit: String
    val delete: String
    val undo: String
    val done: String
    val add: String
    val addBtn: String
    val inUse: String
    val selected: String
    val unitSlot: String // slot(s)
    val unitPiece: String // piece(s)
    val customSlotBtn: String // Custom
    fun slotCount(count: Int): String
    fun pieceCount(count: Int): String
    fun totalSlots(slots: Int): String
    fun totalSlotsPortion(slots: Int): String

    // Navigation Tabs
    val tabToday: String
    val tabInventoryNav: String
    val tabMealPlan: String
    val tabEquipment: String

    // Cooking Tools
    fun cookingToolName(type: CookingToolType): String

    // Mold Presets
    fun moldPresetLabel(preset: MoldGridPreset): String
    val customCapacityLabel: String
    val customCapacityPlaceholder: String
    val moldColor: String

    // Home Screen
    val homeAppSubtitle: String
    val homeGreeting: String
    val homeBannerTitle: String
    val homeBannerSubtitle: String
    val homeQuickAddMoldTitle: String
    val homeEquipmentStatusTitle: String
    val homeManageBtn: String
    val homeRegisteredMolds: String
    val homeCookingTools: String
    val homeNoEquipmentRegistered: String
    val homeRegisterEquipmentNow: String
    val homeTodayMealTitle: String
    val homeTodayMealSubtitle: String
    val homeInventoryTitle: String
    val homeInventorySubtitle: String

    // Equipment Onboarding Screen (Screen 1)
    val onboardingTitle: String
    val onboardingSubtitle: String
    val onboardingCardTitle: String
    val onboardingCardDesc: String
    val onboardingStartBtn: String

    // Equipment Setup Screen (Screen 2)
    val setupTitle: String
    val setupSubtitle: String
    val moldSectionTitle: String
    val moldSectionSubtitle: String
    val toolsSectionTitle: String
    val toolsSectionSubtitle: String
    val setupSaveBtn: String
    val errorMinMoldRequired: String

    // Equipment List Screen (Screen 3)
    val listTitle: String
    val listSubtitle: String
    val editAllBtn: String
    val moldListSection: String
    val cookingToolListSection: String
    val noMoldsRegistered: String
    val noToolsRegistered: String

    // Single Mold Edit Dialog
    val editMoldDialogTitle: String
    val editMoldDialogDeleteBtn: String
    val editMoldDialogSaveBtn: String
    fun moldDetailSummary(capacityMl: Int, cellCount: Int, quantity: Int): String

    // Navigation Tab & ME Screen
    val tabMe: String
    val meTitle: String
    val meSubtitle: String
    val meProfileSection: String
    val meProfileName: String
    val meProfileDefaultName: String
    val meProfileDesc: String
    val meSettingsSection: String
    val meLanguageSetting: String
    val meLanguageDesc: String
    val meLanguageKo: String
    val meLanguageEn: String
    val meEquipmentSummaryTitle: String
    fun meEquipmentSummaryDesc(moldCount: Int, toolCount: Int): String
    val meAppVersion: String

    // Inventory & Ingredients (Phase 3)
    val inventoryTitle: String
    val inventorySubtitle: String
    val inventorySearchPlaceholder: String
    val inventoryQuickAddPlaceholder: String
    val inventoryQuickAddBtn: String
    val inventoryTabAll: String
    val inventoryTabInStock: String
    val inventoryTabShoppingCart: String
    val inventoryLoadMoreBtn: String
    fun inventoryShowingCount(showing: Int, total: Int): String
    val inventoryEmptyTitle: String
    val inventoryEmptyDesc: String
    val inventoryAutoSavedNotice: String
    val inventoryEditTitle: String
    val inventoryAddTitle: String
    val inventoryDeleteConfirm: String
    val inventoryNameLabel: String
    val inventoryQuantityLabel: String
    val inventoryUnitLabel: String
    val inventoryCategoryLabel: String
    val inventoryStatusToggleToCart: String
    val inventoryStatusToggleToStock: String
    val inventoryCartTip: String
    val inventorySearchBtn: String
    val cookBtn: String
    val catalogSearchTitle: String
    val catalogSearchPlaceholder: String
    val catalogTargetStock: String
    val catalogTargetCart: String
    val addInStockBtn: String
    val addCartBtn: String
    val alreadyAddedInStock: String
    val alreadyAddedCart: String
    val alreadyAddedConsumed: String
    val markAsConsumedBtn: String
    val moveToCartBtn: String
    val restoreToStockBtn: String
    fun alreadyExistsToast(name: String, status: String): String
    fun itemDeletedToast(name: String): String
    fun catalogAddCustomBtn(name: String): String
    val catalogNoResults: String
    fun ingredientCategoryName(category: IngredientCategory): String
    fun ingredientUnitName(unit: IngredientUnit): String
    fun ingredientStatusName(status: IngredientStatus): String
}
