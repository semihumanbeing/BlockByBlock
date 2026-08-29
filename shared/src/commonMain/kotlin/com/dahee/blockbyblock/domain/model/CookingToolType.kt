package com.dahee.blockbyblock.domain.model

enum class CookingToolType(val displayName: String) {
    GAS_STOVE("레인지"),
    OVEN("오븐"),
    SLOW_COOKER("슬로우쿠커"),
    BLENDER("믹서기"),
    AIR_FRYER("에어프라이어"),
    MICROWAVE("전자레인지"),
    CUSTOM("기타 조리도구")
}
