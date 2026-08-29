package com.dahee.blockbyblock.core.i18n

import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.MoldGridPreset

object KoStrings : AppStrings {
    override val appTitle = "BlockByBlock"
    override val save = "저장하기"
    override val cancel = "취소"
    override val edit = "수정"
    override val delete = "삭제"
    override val done = "완료"
    override val add = "추가"
    override val addBtn = "추가"
    override val inUse = "사용 중"
    override val selected = "선택됨"
    override val unitSlot = "칸"
    override val unitPiece = "개"
    override val customSlotBtn = "커스텀"
    override fun slotCount(count: Int) = "${count}칸"
    override fun pieceCount(count: Int) = "${count}개"
    override fun totalSlots(slots: Int) = "총 ${slots}칸"
    override fun totalSlotsPortion(slots: Int) = "총 ${slots}칸 소분"

    // Navigation Tabs
    override val tabToday = "오늘"
    override val tabInventoryNav = "보관함"
    override val tabMealPlan = "식단"
    override val tabEquipment = "장비"

    // Cooking Tools
    override fun cookingToolName(type: CookingToolType) = when (type) {
        CookingToolType.GAS_STOVE -> "레인지"
        CookingToolType.OVEN -> "오븐"
        CookingToolType.SLOW_COOKER -> "슬로우쿠커"
        CookingToolType.BLENDER -> "믹서기"
        CookingToolType.AIR_FRYER -> "에어프라이어"
        CookingToolType.MICROWAVE -> "전자레인지"
        CookingToolType.CUSTOM -> "기타 조리도구"
    }

    // Mold Presets
    override fun moldPresetLabel(preset: MoldGridPreset) = when (preset) {
        MoldGridPreset.ML_500 -> "500ml"
        MoldGridPreset.ML_250 -> "250ml"
        MoldGridPreset.ML_125 -> "125ml"
        MoldGridPreset.ML_75 -> "75ml"
        MoldGridPreset.CUSTOM -> "직접 입력"
    }
    override val customCapacityLabel = "1칸당 용량 (ml)"
    override val customCapacityPlaceholder = "용량(ml) 입력"
    override val moldColor = "몰드 색상"

    // Home Screen
    override val homeAppSubtitle = "체계적인 소분 식단 라이프"
    override val homeGreeting = "안녕하세요, 소분 마스터님! 🧊"
    override val homeBannerTitle = "스마트한 블록 소분 & 밀키트"
    override val homeBannerSubtitle = "나만의 몰드와 레시피로 주간 식단을 완성해보세요."
    override val homeQuickAddMoldTitle = "⚡ 빠른 몰드 규격 추가"
    override val homeEquipmentStatusTitle = "내 몰드 & 장비 현황"
    override val homeManageBtn = "관리 >"
    override val homeRegisteredMolds = "등록된 몰드"
    override val homeCookingTools = "보유 조리도구"
    override val homeNoEquipmentRegistered = "등록된 장비가 없습니다"
    override val homeRegisterEquipmentNow = "터치하여 소분 몰드와 조리 도구를 등록해보세요"
    override val homeTodayMealTitle = "오늘의 식단"
    override val homeTodayMealSubtitle = "오늘 섭취할 소분 식사 목록"
    override val homeInventoryTitle = "보관함 재고 현황"
    override val homeInventorySubtitle = "냉장 / 냉동 보관 중인 소분 블록 내역"

    // Equipment Onboarding Screen (Screen 1)
    override val onboardingTitle = "내 장비 등록"
    override val onboardingSubtitle = "보유 중인 소분 몰드와 조리기구를 등록하고 스마트하게 요리해보세요."
    override val onboardingCardTitle = "장비를 등록해볼까요?"
    override val onboardingCardDesc = "소분 몰드와 조리기구를 선택하면 딱 맞는 블록 레시피를 추천해 드려요."
    override val onboardingStartBtn = "장비 선택하기"

    // Equipment Setup Screen (Screen 2)
    override val setupTitle = "장비 등록 & 설정"
    override val setupSubtitle = "사용할 몰드와 보유 중인 조리 도구를 선택하세요"
    override val moldSectionTitle = "몰드 선택 (최소 1개 필수)"
    override val moldSectionSubtitle = "사이즈를 터치하여 칸 수와 색상을 설정하세요"
    override val toolsSectionTitle = "보유 조리 기구 (선택 사항)"
    override val toolsSectionSubtitle = "터치하여 보유 중인 조리 도구를 바로 선택하세요"
    override val setupSaveBtn = "저장하기"
    override val errorMinMoldRequired = "최소 1개 이상의 몰드를 선택해주세요."

    // Equipment List Screen (Screen 3)
    override val listTitle = "내 장비 목록"
    override val listSubtitle = "보유 중인 몰드 규격과 조리기구입니다"
    override val editAllBtn = "한번에 수정하기"
    override val moldListSection = "소분 몰드"
    override val cookingToolListSection = "조리 도구"
    override val noMoldsRegistered = "등록된 몰드가 없습니다."
    override val noToolsRegistered = "등록된 조리 도구가 없습니다."

    // Single Mold Edit Dialog
    override val editMoldDialogTitle = "몰드 수정"
    override val editMoldDialogDeleteBtn = "삭제"
    override val editMoldDialogSaveBtn = "수정 완료"
    override fun moldDetailSummary(capacityMl: Int, cellCount: Int, quantity: Int) =
        "1개당 ${cellCount}칸 (총 ${cellCount * quantity}칸)"

    // Navigation Tab & ME Screen
    override val tabMe = "MY"
    override val meTitle = "내 정보 & 설정"
    override val meSubtitle = "프로필과 앱 설정을 관리합니다"
    override val meProfileSection = "프로필"
    override val meProfileName = "닉네임"
    override val meProfileDefaultName = "소분 마스터"
    override val meProfileDesc = "스마트한 소분 밀프렙 라이프를 즐기는 중 🧱"
    override val meSettingsSection = "앱 설정"
    override val meLanguageSetting = "언어 설정"
    override val meLanguageDesc = "앱에서 사용할 언어를 선택하세요"
    override val meLanguageKo = "한국어"
    override val meLanguageEn = "English"
    override val meEquipmentSummaryTitle = "내 등록 장비 현황"
    override fun meEquipmentSummaryDesc(moldCount: Int, toolCount: Int) = "몰드 ${moldCount}개 · 조리도구 ${toolCount}개 등록됨"
    override val meAppVersion = "앱 버전 1.0.0"
}
