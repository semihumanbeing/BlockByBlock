package com.dahee.blockbyblock.presentation.tutorial

enum class TutorialStep {
    WELCOME_PROFILE,    // 0. Welcome & Nickname Dialog
    EQUIPMENT_SETUP,    // 1. MY > Equipment Management (Mold selection)
    INVENTORY_SETUP,    // 2. Ingredients & Storage (Add ingredients)
    CREATE_BLOCK,       // 3. Block Inventory > Create Block Screen (Make first food block)
    MEAL_PLAN,          // 4. Meal Plan Screen (Place block into slot)
    CONGRATULATIONS,    // 5. Completed celebration banner (auto fade out)
    COMPLETED           // 6. Regular normal mode
}
