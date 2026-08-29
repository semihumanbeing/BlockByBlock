# BlockByBlock (블록바이블록) - 프로젝트 컨텍스트 & 개발 현황

> **AI 가이드**: 다른 탭이나 새로운 세션에서 이 프로젝트를 이어받았을 때, 이 문서를 가장 먼저 읽고 프로젝트 아키텍처, 사용자 요구사항, UI 규칙, 완료된 기능 및 다음 구현 작업을 즉시 파악할 수 있도록 작성된 공식 컨텍스트 문서입니다.

---

## 1. 프로젝트 개요 & 기술 스택

- **프로젝트 명**: BlockByBlock (스마트 블록 소분 & 밀키트 식단 관리 앱)
- **플랫폼**: **Compose Multiplatform (CMP)**
  - Android (Debug APK / JVM)
  - Web (Kotlin/Wasm & JS, 포트 `8080`)
  - iOS (Kotlin Native / SwiftUI Wrapper)
- **개발 언어**: Kotlin 2.4.x / Jetpack & JetBrains Compose 1.11.x
- **설계 아키텍처**: MVI/MVVM + Clean Architecture (Presentation - Domain - Data - Core)

---

## 2. MVP 기획 명세서 (Google Sheets 연동)

- **공식 기획 문서**: `https://docs.google.com/spreadsheets/d/1XUX3_N4Bmj06ogNwywL1xj3BxTbf4BWZr3VeDZjsBS4/edit?usp=sharing`
- **핵심 도메인 5단계**:
  1. ✅ **장비 및 몰드 관리 (Equipment & Molds)** - *완료*
  2. ✅ **다국어 지원 (i18n: 한국어 & 영어)** - *완료*
  3. ⏳ **식재료 관리 (Ingredients)** - *다음 단계*
  4. ⏳ **장바구니 (Shopping List)** - *대기*
  5. ⏳ **블록 재고 생성/소분 (Block Inventory)** - *대기*
  6. ⏳ **오늘의 식단 & 사운드 이펙트 (Meal Plan & Snap Sound)** - *대기*

## 2.5 앱 디자인 시스템 & 테마 (Natural Warm Kitchen Theme)
- **테마 컨셉**: **내추럴 웜 키친 (Warm & Cozy Meal Prep)** - 따뜻한 홈쿠킹, 건강한 유기농 식단, 포근하고 아늑한 카페/키친 무드
- **Brand Primary**: `Sage Forest Green (#3D7A68)`, `Deep Forest Green (#295648)`, `Soft Milk Sage Tint (#E8F3EE)`
- **Warm Accents**: `Warm Terracotta Brick (#E07A5F)`, `Warm Honey Butter Yellow (#F4D06F)`, `Fresh Herb Green (#5B9E7A)`, `Warm Brick Red (#D9534F)`
- **Silicone Mold 7 Colors**: `Warm Mist Blue (#A3C4D3)`, `Sage Olive Mint (#A8D5BA)`, `Warm Blush Rose (#F5B7B1)`, `Warm Honey Butter Yellow (#F9E79F)`, `Soft Berry Lilac (#D7BDE2)`, `Apricot Peach (#FAD7A0)`, `Warm Mineral Sand Gray (#D5DBDB)`
- **Background & Canvas**: `Warm Cozy Milk Ivory (#FAF6F0, #FFFFFF)` + `Warm Linen Biscuit (#F3EEE6)` + `Warm Espresso Charcoal (#2C241E)`

---

## 3. 완료된 기능 & 상세 구현 내역

### 1) 장비 및 몰드 관리 (Phase 1 완료)
- **1번 온보딩 화면 (`EquipmentOnboardingScreen.kt`)**:
  - 투명 배경의 3D 블록 조리도구 & 소분 몰드 스택 히어로 비주얼 (`equipment_hero_stack.png`) + "장비를 등록해볼까요?" + [장비 선택하기] 버튼
- **2번 장비 등록/설정 화면 (`EquipmentSetupScreen.kt`)**:
  - **소분 몰드 인터랙션 (미선택 시 흐리게 + 중앙 [+ 추가] 버튼)**:
    - **미선택 상태**: 좌측 몰드 이미지를 **흐리게(`alpha = 0.40f`)** 처리하고, 우측 영역 중앙에 눈에 띄는 **`[+ 추가]` 초록색 플로팅 버튼**을 배치하여 탭하면 바로 추가됨을 직관적으로 안내
    - **선택된 상태**: 몰드가 선명한 100% 컬러로 팝업되며, 우측에 수량 스텝퍼(`[▲ 1개 ▼]`), 칸 수 칩, 원형 컬러 팔레트가 활성화됨
    - **하얀색 사각 박스 완전 배제**: 배경은 투명(`Color.Transparent`)과 18dp 라운딩, 0.5dp 미세 테두리로 구성
    - **초록색 버튼 텍스트**: `[저장하기]`, `[장비 선택하기]`, `[+ 추가]` 등 모든 버튼 글씨를 선명한 순백색(`Color.White`)으로 고정
    - **좌측**: 68~70dp 대형 실리콘 몰드 이미지 (`SouperMoldBrickView`) + 바로 밑에 고정된 작은 용량 텍스트 (`500ml`, `250ml` 등)
  - **몰드 규격 프리셋 & 칸 수 커스텀 (모바일 한 줄 최적화 3개 프리셋 + [커스텀] 버튼)**:
    - `500ml`: 1, 2, 4칸 + `[커스텀]`
    - `250ml`: 2, 4, 8칸 + `[커스텀]`
    - `125ml`: 4, 8, 12칸 + `[커스텀]`
    - `75ml`: 8, 16, 24칸 + `[커스텀]`
    - `직접 입력(CUSTOM)`: 8, 16, 24칸 + `[커스텀]` (용량 ml 직접 입력)
    - **`CustomSlotChip` 동작**: 평소에는 `[커스텀]`(또는 비프리셋 선택 시 `[N칸]`) 버튼으로 노출되며, 클릭 시 즉시 숫자 입력창으로 전환 및 가상 숫자 키패드(`KeyboardType.Number`) 자동 팝업
  - **조리기구 원터치 선택**:
    - 사각 테두리 박스 없이 **순수 일러스트 이미지(56dp) + 하단 중앙 정렬 텍스트** 구조
    - 가로 중앙 정렬 (`Arrangement.Center`), 선택 시 체크 뱃지 및 투명도/크기 강조
    - 7종 (3D 블록 토이 스타일 일러스트): `레인지(GAS_STOVE)`, `오븐(OVEN)`, `슬로우쿠커(SLOW_COOKER)`, `믹서기(BLENDER)`, `에어프라이어(AIR_FRYER)`, `전자레인지(MICROWAVE)`, `기타 조리도구(CUSTOM)`
- **3번 내 장비 목록 화면 (`EquipmentListScreen.kt`)**:
  - 우측 상단 **[한번에 수정하기]** 버튼
  - 몰드 카드: `[몰드 이미지]` + `500ml` 뱃지 + `2칸` 뱃지 + `[▲ 수량 스텝퍼 ▼]` (중복 텍스트 완전 배제)
  - 개별 몰드 터치 시 **`SingleMoldEditDialog` 모달 팝업** 오픈 (즉시 칸수/색상/수량 수정 및 삭제 지원)
- **동적 몰드 렌더러 (`SouperMoldBrickView.kt`) & 3D 블록 몰드 에셋**:
  - Skia 캔버스에 3D 블록 스터드(Studs), 광택 하이라이트, 입체 음각 슬롯 테두리 적용으로 실시간 색상/칸수 반영
  - **칸 수별 동적 슬롯 레이아웃**:
    - `1칸` (1줄), `2칸` (세로 2줄), `3칸` (세로 3줄)
    - `4칸` (세로 4줄 - 가로 바 4개 적층), `6칸` (세로 6줄)
    - `8칸` (4행 2열), `12칸` (4행 3열 얼음틀)
    - `15칸/16칸/24칸` (5행 3열 / 4행 4열 / 6행 4열 얼음틀 그리드)
  - 3D 블록 브릭 스타일 프리셋 몰드 에셋(`mold_1slot`, `mold_4slot`, `mold_6slot`, `mold_15slot`) 완비

### 2) ME 화면 및 다국어 설정 (MY & i18n Phase 2 완료)
- **하단 네비게이션 5개 탭**: `오늘(TODAY)`, `보관함(INVENTORY)`, `식단(MEAL_PLAN)`, `장비(EQUIPMENT)`, `MY(ME)`
- **ME 화면 (`MeScreen.kt`)**:
  - **프로필 카드**: 귀여운 아바타 + 닉네임 + 밀프렙 한 줄 소개
  - **언어 설정 카드**: `한국어(KO)` 및 `English(EN)` 세그먼트 카드 선택기를 통해 앱 전체 언어를 원터치로 즉시 전환
  - **앱 정보 카드**: 앱 명칭 및 버전 정보 표시
- **상단 헤더 정리**: 홈 및 장비 화면 상단에 있던 `KO/EN` 토글 버튼을 제거하여 깔끔하고 세련된 헤더 룩 완성
- **패키지**: `com.dahee.blockbyblock.presentation.me` 및 `com.dahee.blockbyblock.core.i18n`
- **구조**:
  - `AppStrings.kt`: 전체 화면 공통 다국어 인터페이스
  - `KoStrings.kt`: 한국어 리소스
  - `EnStrings.kt`: 영어 리소스
  - `I18n.kt`: `LocalStrings`, `LocalAppLanguage` CompositionLocal
- **동작**: `App.kt` 최상단에서 상태를 관리하며 ME 탭에서 언어 변경 시 홈, 하단 네비게이션, 장비 화면, 팝업 전체가 실시간 즉시 번역 전환됨.

---

## 4. 핵심 코드 맵 (Codebase Map)

```
shared/src/commonMain/kotlin/com/dahee/blockbyblock/
├── App.kt                                # 앱 진입점, 네비게이션 탭, i18n CompositionLocalProvider
├── core/
│   ├── i18n/
│   │   ├── AppLanguage.kt                # KO / EN enum
│   │   ├── AppStrings.kt                 # 다국어 인터페이스
│   │   ├── KoStrings.kt                  # 한국어 리소스
│   │   ├── EnStrings.kt                  # 영어 리소스
│   │   └── I18n.kt                       # CompositionLocal 및 LanguageToggleChip
│   ├── theme/
│   │   ├── AppColors.kt                  # 컬러 팔레트 & 헥스 변환 & 몰드 파스텔 색상 6종
│   │   └── AppTheme.kt                   # BlockByBlockTheme
│   └── ui/
│       ├── EditableNumberStepper.kt      # 직접 숫자 입력 가능 + ▲ ▼ 증감 스텝퍼
│       ├── AppButton.kt, AppCard.kt, AppChip.kt, AppTextField.kt
├── domain/
│   ├── model/
│   │   ├── Equipment.kt                  # 장비/몰드 엔티티
│   │   ├── EquipmentCategory.kt          # MOLD, COOKING_TOOL
│   │   ├── MoldGridPreset.kt             # ML_500, ML_250, ML_125, ML_75, CUSTOM
│   │   └── CookingToolType.kt            # GAS_STOVE, OVEN, SLOW_COOKER, BLENDER...
│   └── repository/EquipmentRepository.kt
├── data/
│   └── repository/InMemoryEquipmentRepository.kt # 장비 CRUD & Flow 반응형 저장소
└── presentation/
    ├── home/
    │   └── HomeScreen.kt                 # 홈 대시보드 (장비 요약, 오늘 식단, 보관함)
    ├── navigation/
    │   ├── AppBottomNav.kt               # 하단 4개 탭 (오늘, 보관함, 식단, 장비)
    │   └── NavTab.kt
    └── equipment/
        ├── EquipmentViewModel.kt         # 장비 MVI 상태 머신
        ├── EquipmentScreen.kt            # 장비 화면 컨테이너 (Onboarding -> Setup -> List)
        ├── components/
        │   ├── SouperMoldBrickView.kt    # 실시간 동적 컬러/칸수 실리콘 몰드 뷰
        │   ├── CookingToolVisual.kt      # 멀티플랫폼 조리기구 그래픽 이미지 뷰
        │   └── SingleMoldEditDialog.kt   # 개별 몰드 수정/삭제 모달 팝업
        ├── screens/
        │   ├── EquipmentOnboardingScreen.kt
        │   ├── EquipmentSetupScreen.kt
        │   └── EquipmentListScreen.kt
        └── state/
            └── EquipmentUiState.kt       # ScreenMode, Drafts, SelectedTools
```

---

## 5. 엄격한 UI / UX 규칙 (User Directives)

1. **이모지 사용 금지**: 조리기구 및 몰드는 Compose 멀티플랫폼 이미지 리소스 및 Skia 그래픽(`SouperMoldBrickView`, `CookingToolVisual`)만 사용합니다.
2. **조리기구 스타일**: 네모 테두리 박스 없이 투명 배경의 **큰 이미지(56dp) + 하단 중앙 작은 글씨(12sp)** 구조를 유지하며, 가로 중앙 정렬합니다.
3. **몰드 설정 카드 레이아웃**:
   - 좌측: 대형 몰드 이미지 + 이미지 바로 아래에 고정된 작은 용량 글자 (`500ml` 등).
   - 우측: 상단 개수 스텝퍼(`1개`), 중단 칸수 칩 & 스텝퍼(우측 정렬), 하단 컬러 팔레트(우측 정렬, 레이블 글자 없음).
   - 카드 어디를 터치해도 추가/취소 토글이 자연스럽게 동작해야 함.
4. **목록 화면 간결성**: 중복 텍스트를 배제하고 `[몰드 이미지]`, `500ml`, `2칸`, `[수량 스텝퍼]`만 깔끔하게 노출합니다.
5. **다국어(i18n) 지원**: 모든 신규 화면 및 컴포넌트의 텍스트는 하드코딩하지 않고 `LocalStrings.current`의 `AppStrings`를 참조합니다.
6. **3D 블록/토이 에셋 생성 가이드 준수**: 식재료, 도구, 뱃지 등 신규 이미지 에셋 제작 시 반드시 `DESIGN_PROMPT_GUIDE.md`의 마스터 프롬프트 템플릿(3D Toy block brick miniature, ABS plastic, isometric 3/4 view, pure white background)을 사용합니다.

---

## 6. 다음 구현 로드맵 (Next Steps)

1. **식재료 관리 (Ingredients Management)**:
   - 재료명, 보유량/단위(`g`, `lbs`, `개`), 보유 중 vs 장바구니 토글, 25개 페이징 검색, CRUD
2. **장바구니 (Shopping List)**:
   - 장보기 목록 관리 및 구매 완료 시 보유 식재료로 일괄 전환
3. **블록 재고 생성 & 소분 (Block Inventory)**:
   - 몰드 용량 선택, 제작일자, 보관 방식(냉장/냉동), 유통기한(기본 90일), 재료 차감 및 롤백
4. **오늘의 식단 & 스냅 사운드 (Meal Plan & Audio)**:
   - 주간 식단 구성, 소분 블록 소비 기록, 블록 맞물리는 사운드 효과
