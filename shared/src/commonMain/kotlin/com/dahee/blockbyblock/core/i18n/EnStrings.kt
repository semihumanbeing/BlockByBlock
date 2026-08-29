package com.dahee.blockbyblock.core.i18n

import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.MoldGridPreset

object EnStrings : AppStrings {
    override val appTitle = "BlockByBlock"
    override val save = "Save"
    override val cancel = "Cancel"
    override val edit = "Edit"
    override val delete = "Delete"
    override val done = "Done"
    override val add = "Add"
    override val addBtn = "Add"
    override val inUse = "In Use"
    override val selected = "Selected"
    override val unitSlot = "s"
    override val unitPiece = " ea"
    override val customSlotBtn = "Custom"
    override fun slotCount(count: Int) = if (count == 1) "1 slot" else "$count slots"
    override fun pieceCount(count: Int) = if (count == 1) "1 mold" else "$count molds"
    override fun totalSlots(slots: Int) = "$slots slots total"
    override fun totalSlotsPortion(slots: Int) = "$slots total portions"

    // Navigation Tabs
    override val tabToday = "Today"
    override val tabInventoryNav = "Storage"
    override val tabMealPlan = "Meals"
    override val tabEquipment = "Equipment"

    // Cooking Tools
    override fun cookingToolName(type: CookingToolType) = when (type) {
        CookingToolType.GAS_STOVE -> "Stove"
        CookingToolType.OVEN -> "Oven"
        CookingToolType.SLOW_COOKER -> "Slow Cooker"
        CookingToolType.BLENDER -> "Blender"
        CookingToolType.AIR_FRYER -> "Air Fryer"
        CookingToolType.MICROWAVE -> "Microwave"
        CookingToolType.CUSTOM -> "Other Tools"
    }

    // Mold Presets
    override fun moldPresetLabel(preset: MoldGridPreset) = when (preset) {
        MoldGridPreset.ML_500 -> "500ml"
        MoldGridPreset.ML_250 -> "250ml"
        MoldGridPreset.ML_125 -> "125ml"
        MoldGridPreset.ML_75 -> "75ml"
        MoldGridPreset.CUSTOM -> "Custom"
    }
    override val customCapacityLabel = "Capacity per slot (ml)"
    override val customCapacityPlaceholder = "Enter ml"
    override val moldColor = "Mold Color"

    // Home Screen
    override val homeAppSubtitle = "Smart Block Portion Meal Prep"
    override val homeGreeting = "Hello, Meal Prep Master! 🧊"
    override val homeBannerTitle = "Smart Portion Blocks & Meal Kits"
    override val homeBannerSubtitle = "Plan your weekly meals easily with your molds and recipes."
    override val homeQuickAddMoldTitle = "⚡ Quick Add Mold"
    override val homeEquipmentStatusTitle = "My Molds & Cooking Tools"
    override val homeManageBtn = "Manage >"
    override val homeRegisteredMolds = "Registered Molds"
    override val homeCookingTools = "Cooking Tools"
    override val homeNoEquipmentRegistered = "No equipment registered yet"
    override val homeRegisterEquipmentNow = "Tap to register molds and cooking tools"
    override val homeTodayMealTitle = "Today's Meal"
    override val homeTodayMealSubtitle = "Portion meals scheduled for today"
    override val homeInventoryTitle = "Storage Inventory"
    override val homeInventorySubtitle = "Stored portion blocks in fridge & freezer"

    // Equipment Onboarding Screen (Screen 1)
    override val onboardingTitle = "Equipment Setup"
    override val onboardingSubtitle = "Register your silicone molds and cooking tools to start cooking smart."
    override val onboardingCardTitle = "Let's set up your equipment"
    override val onboardingCardDesc = "Select your molds and tools to receive personalized block recipe suggestions."
    override val onboardingStartBtn = "Select Equipment"

    // Equipment Setup Screen (Screen 2)
    override val setupTitle = "Equipment Setup"
    override val setupSubtitle = "Choose your silicone molds and cooking tools"
    override val moldSectionTitle = "Select Molds (At least 1 required)"
    override val moldSectionSubtitle = "Tap a mold to customize slots and color"
    override val toolsSectionTitle = "Cooking Tools (Optional)"
    override val toolsSectionSubtitle = "Tap to select the tools you have"
    override val setupSaveBtn = "Save"
    override val errorMinMoldRequired = "Please select at least 1 mold."

    // Equipment List Screen (Screen 3)
    override val listTitle = "My Equipment"
    override val listSubtitle = "Your silicone mold presets and cooking tools"
    override val editAllBtn = "Edit All"
    override val moldListSection = "Silicone Molds"
    override val cookingToolListSection = "Cooking Tools"
    override val noMoldsRegistered = "No molds registered."
    override val noToolsRegistered = "No cooking tools registered."

    // Single Mold Edit Dialog
    override val editMoldDialogTitle = "Edit Mold"
    override val editMoldDialogDeleteBtn = "Delete"
    override val editMoldDialogSaveBtn = "Save Changes"
    override fun moldDetailSummary(capacityMl: Int, cellCount: Int, quantity: Int) =
        "$cellCount slots each ($quantity molds, ${cellCount * quantity} slots total)"

    // Navigation Tab & ME Screen
    override val tabMe = "MY"
    override val meTitle = "My Info & Settings"
    override val meSubtitle = "Manage your profile and app preferences"
    override val meProfileSection = "Profile"
    override val meProfileName = "Nickname"
    override val meProfileDefaultName = "Meal Prep Master"
    override val meProfileDesc = "Enjoying smart block portion meal prep 🧱"
    override val meSettingsSection = "App Settings"
    override val meLanguageSetting = "Language"
    override val meLanguageDesc = "Choose your preferred language"
    override val meLanguageKo = "한국어"
    override val meLanguageEn = "English"
    override val meEquipmentSummaryTitle = "Registered Equipment"
    override fun meEquipmentSummaryDesc(moldCount: Int, toolCount: Int) = "$moldCount molds · $toolCount cooking tools"
    override val meAppVersion = "App Version 1.0.0"
}
