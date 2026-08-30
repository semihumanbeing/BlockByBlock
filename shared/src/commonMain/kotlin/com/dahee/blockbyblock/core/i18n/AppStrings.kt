package com.dahee.blockbyblock.core.i18n

import com.dahee.blockbyblock.domain.model.BlockSizeCategory
import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus
import com.dahee.blockbyblock.domain.model.IngredientUnit
import com.dahee.blockbyblock.domain.model.MealType
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
    val back: String
    val inUse: String
    val selected: String
    val unitSlot: String // slot(s)
    val unitPiece: String // piece(s)
    val unitDay: String // day(s)
    val customSlotBtn: String // Custom
    fun slotCount(count: Int): String
    fun pieceCount(count: Int): String
    fun totalSlots(slots: Int): String
    fun totalSlotsPortion(slots: Int): String

    // Navigation Tabs
    val tabToday: String
    val tabInventoryNav: String
    val tabBlock: String
    val tabMealPlan: String
    val tabEquipment: String
    val tabMe: String

    // Cooking Tools
    fun cookingToolName(type: CookingToolType): String

    // Mold Presets & Sizing
    fun moldPresetLabel(preset: MoldGridPreset): String
    val customCapacityLabel: String
    val customCapacityPlaceholder: String
    val moldColor: String
    val moldQuickAddTitle: String
    val moldQuickAddSubtitle: String
    fun blockSizeName(category: BlockSizeCategory): String

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
    val homeInventoryTitle: String

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

    // Single Mold / Equipment Edit Dialog
    val editMoldDialogTitle: String
    val editMoldDialogDeleteBtn: String
    val editMoldDialogSaveBtn: String
    fun moldDetailSummary(capacityMl: Int, cellCount: Int, quantity: Int): String
    val equipmentEditTitle: String
    val equipmentRegisterTitle: String
    val moldPresetLabelText: String
    val slotsPerMoldLabel: String
    val quantityLabel: String
    val equipmentColorLabel: String
    val memoLabel: String
    val memoPlaceholder: String

    // ME Screen
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
    val meEquipmentManageTitle: String
    val meEquipmentManageSubtitle: String
    fun meEquipmentSummaryDesc(moldCount: Int, toolCount: Int): String
    val meAppVersion: String

    // Food Block Inventory (Phase 4)
    val blockTitle: String
    val blockSubtitle: String
    val blockTabAll: String
    val blockTabFreezer: String
    val blockTabFridge: String
    val blockEmptyTitle: String
    val blockEmptyDesc: String
    val blockCreateBtn: String
    val createBlockTitle: String
    val createBlockSubtitle: String
    val editBlockTitle: String
    val editBlockSubtitle: String
    val editBlockSubmitBtn: String
    val createBlockNameLabel: String
    val createBlockSectionIngredients: String
    val createBlockMainIngredientsSubLabel: String
    val createBlockSearchIngredientPlaceholder: String
    val createBlockNoMatchingIngredients: String
    val prevPageBtn: String
    val nextPageBtn: String
    fun selectedIngredientsCount(count: Int): String
    val createBlockNoIngredients: String
    val createBlockGoToInventory: String
    val createBlockSectionSubIngredients: String
    val createBlockSubIngredientsSubLabel: String
    val createBlockOwnedSeasoningsLabel: String
    val createBlockSubIngredientPlaceholder: String
    val createBlockAddSubBtn: String
    val createBlockSectionMold: String
    val createBlockMoldCountLabel: String
    fun createBlockMoldCountMax(count: Int): String
    val createBlockNoMolds: String
    val createBlockGoToEquipment: String
    val createBlockSectionStorage: String
    val createBlockQuantityLabel: String
    val createBlockStorageTypeLabel: String
    val createBlockFreezer: String
    val createBlockFridge: String
    val createBlockShelfLifeLabel: String
    fun createBlockDaysLabel(days: Int): String
    val createBlockSubmitBtn: String
    val createBlockNamePlaceholder: String
    val createBlockSuccessToast: String
    val shelfLifeExpired: String
    fun shelfLifeExpiringSoon(days: Int): String
    fun shelfLifeDays(days: Int): String
    val historyTitle: String
    val historySubtitle: String
    val createBlockSectionCookingTool: String
    val noCookingToolSelected: String
    val createBlockNoIngredientTooltip: String

    // Inventory & Ingredients (Phase 3)
    val inventoryTitle: String
    val inventorySubtitle: String
    val inventorySearchPlaceholder: String
    val inventoryQuickAddPlaceholder: String
    val inventoryQuickAddBtn: String
    val inventoryTabAll: String
    val inventoryTabInStock: String
    val inventoryTabShoppingCart: String
    val inventoryStatusConsumed: String
    val inventoryPantrySectionTitle: String
    val inventoryPantryMoveToCart: String
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
    val inventorySwipeToDeleteHint: String
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
    val categoryAll: String
    val cartEmptyTitle: String
    val cartEmptyDesc: String
    val ingredientNamePlaceholder: String
    fun ingredientCategoryName(category: IngredientCategory): String
    fun ingredientUnitName(unit: IngredientUnit): String
    fun ingredientStatusName(status: IngredientStatus): String

    // Meal Plan (Phase 5)
    fun mealTypeName(type: MealType): String
    val deleteMealRecordTitle: String
    fun deleteMealRecordConfirm(mealType: String): String
    val backToToday: String
    val mealPlanHint: String
    fun addMealBlockHint(mealType: String): String
    fun memoPrefix(memo: String): String
    val editArrow: String
    val addMealPlanBtn: String
    fun mealRecordDialogTitle(mealType: String): String
    val mealRecordEatingBlocksTitle: String
    val mealRecordRemoveHint: String
    val mealRecordEmptyEatingBlocksHint: String
    val mealRecordInventoryBlocksTitle: String
    val mealRecordAddHint: String
    val mealRecordSearchPlaceholder: String
    val mealRecordNoBlocksInStock: String
    val mealRecordNoMatchingBlocks: String
    val mealRecordMemoLabel: String
    val mealRecordMemoPlaceholder: String
    val mealRecordMinBlockRequired: String

    // Tutorial & Onboarding
    val tutorialWelcomeTitle: String
    val tutorialWelcomeSubtitle: String
    val tutorialNicknamePlaceholder: String
    val tutorialStartBtn: String
    val tutorialSkipBtn: String
    val tutorialStepEquipmentMsg: String
    val tutorialStepEquipmentNextBtn: String
    val tutorialStepInventoryMsg: String
    val tutorialStepInventoryAddedMsg: String
    val tutorialStepInventoryNextBtn: String
    val tutorialStepBlockMsg: String
    val tutorialStepBlockCreatedMsg: String
    val tutorialStepBlockNextBtn: String
    val tutorialStepMealPlanMsg: String
    val tutorialStepCompleteBtn: String
    val tutorialCongratulationsMsg: String
    val tutorialRestartBtn: String
}
