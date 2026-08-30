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
- **핵심 도메인 단계 현황**:
  1. ✅ **장비 및 몰드 관리 (Equipment & Molds)** - *완료*
  2. ✅ **다국어 지원 (i18n: 한국어 & 영어)** - *완료*
  3. ✅ **식재료 및 장바구니 & 소진 상태 관리 (Ingredients & Shopping Cart & Consumed Lifecycle)** - *완료*
  4. ⏳ **블록 재고 생성/소분 (Block Inventory)** - *다음 단계*
  5. ⏳ **오늘의 식단 & 사운드 이펙트 (Meal Plan & Snap Sound)** - *대기*

---

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
    - **좌측**: 68~70dp 대형 실리콘 몰드 이미지 (`MoldView`) + 바로 밑에 고정된 작은 용량 텍스트 (`500ml`, `250ml` 등)
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
  - 우측 상단 **[한번에 수정하기]** 버튼 (`ButtonVariant.WARM_YELLOW`, 웜 허니 버터 `#F4D06F`)
  - 몰드 카드: `[몰드 이미지]` + `500ml` 뱃지 + `2칸` 뱃지 + `[▲ 수량 스텝퍼 ▼]` (중복 텍스트 완전 배제)
  - 개별 몰드 터치 시 **`SingleMoldEditDialog` 모달 팝업** 오픈 (즉시 칸수/색상/수량 수정 및 삭제 지원)
- **동적 몰드 렌더러 (`MoldView.kt`) & 3D 블록 몰드 에셋**:
  - Skia 캔버스에 3D 블록 스터드(Studs), 광택 하이라이트, 입체 음각 슬롯 테두리 적용으로 실시간 색상/칸수 반영
  - **칸 수별 동적 슬롯 레이아웃**:
    - `1칸` (1줄), `2칸` (세로 2줄), `3칸` (세로 3줄)
    - `4칸` (세로 4줄 - 가로 바 4개 적층), `6칸` (세로 6줄)
    - `8칸` (4행 2열), `12칸` (4행 3열 얼음틀)
    - `15칸/16칸/24칸` (5행 3열 / 4행 4열 / 6행 4열 얼음틀 그리드)

### 2) ME 화면 및 다국어 설정 (MY & i18n Phase 2 완료)
- **하단 네비게이션 5개 탭**: `오늘(TODAY)`, `보관함(INVENTORY)`, `식단(MEAL_PLAN)`, `장비(EQUIPMENT)`, `MY(ME)`
- **ME 화면 (`MeScreen.kt`)**:
  - **프로필 카드**: 귀여운 아바타 + 닉네임 + 밀프렙 한 줄 소개
  - **언어 설정 카드**: `한국어(KO)` 및 `English(EN)` 세그먼트 카드 선택기를 통해 앱 전체 언어를 원터치로 즉시 전환
  - **앱 정보 카드**: 앱 명칭 및 버전 정보 표시
- **상단 헤더 정리**: 홈 및 장비 화면 상단에 있던 `KO/EN` 토글 버튼을 제거하여 깔끔하고 세련된 헤더 룩 완성
- **패키지**: `com.dahee.blockbyblock.presentation.me` 및 `com.dahee.blockbyblock.core.i18n`

### 3) 식재료 보관함 & 장바구니 & 소진 상태 관리 (Phase 3 완료)
- **식재료 3대 상태 라이프사이클 (3-Status Lifecycle)**:
  - `IN_STOCK (보유중)`: 현재 냉장고/냉동고에 보관 중인 식재료
  - `CONSUMED (소진됨)`: 요리에 전부 사용하여 다 쓴 식재료
  - `SHOPPING_CART (장바구니)`: 마트에서 사야 할 식재료 목록
- **3-State 순환 원터치 체크리스트 (`onToggleChecklistStatus`)**:
  - **`[보유중 🟢]`** 체크 클릭 ➔ **`[소진됨 💨]`**으로 전환 ("다 썼어요!")
  - **`[소진됨 💨]`** 체크(장바구니 힌트 아이콘) 클릭 ➔ **`[장바구니 🛒]`**로 전환 ("사러 가요!")
  - **`[장바구니 🛒]`** 체크 클릭 ➔ **`[보유중 🟢]`**으로 복귀 ("장보기 완료!")
- **탭별 명확한 분리 및 초간편 인터랙션 UX**:
  - **`전체` 탭**:
    - `보유중`, `장바구니`, `소진됨` 상태의 모든 식재료를 한눈에 조회
    - 소진된 재료는 **은은한 반투명 카드 + `[소진됨]` 배지 + `[🛒 장바구니로]` & `[보유중으로]` 복귀 원터치 버튼** 제공
  - **`보유중` 탭**:
    - 현재 실제 냉장/냉동고에 보관 중인 **순수 보유중 식재료만 표시** (`inStockCount`만 노출, 소진 개수/카드 제외)
    - 재료 우측의 **`[소진]`** 원터치 버튼 클릭 시 즉시 `소진됨`으로 전환
  - **`장바구니` 탭**:
    - 불필요한 우측 버튼을 배제하고, 좌측 원형 체크박스 터치 시 바로 `보유중`으로 쏙 이동
- **왼쪽 스와이프 삭제 & 1-Tap 실행 취소 (Swipe-to-Delete with Undo)**:
  - 각 식재료 카드를 왼쪽으로 쓸어넘기면(`SwipeToDismissBox`) 웜 레드 배경과 휴지통 아이콘이 나타나며 즉시 삭제 처리
  - 삭제 즉시 상단에 **`['재료명'이(가) 삭제되었습니다. | 실행 취소]`** 플로팅 배너가 4초간 노출되어 1초 만에 안전 복구 가능
- **상태 변경 안내 토스트 배제**:
  - 일상적인 체크리스트 토글, 소진, 장바구니 이동 등 상태 변경 시에는 상단 팝업 없이 즉시 매끄럽게 화면에 반영 (삭제 Undo 배너만 필요 시 노출)
- **양(수량) 입력 필드 숨김 처리 (Hidden Quantity UI)**:
  - 번거로운 수량 입력 단계를 걷어내어 **1초 만에 초간편 등록/상태 변경**이 가능하도록 UI에서 숨김 처리 (`SHOW_QUANTITY_UI = false`)
  - 추후 필요 시 즉시 활성화할 수 있도록 내부 수량 스텝퍼 및 단위 코드는 완벽히 보존
- **스마트 재료 검색 & 소진 재료 재활성화**:
  - 헤더 우측 **`[🔍 재료 검색]`** (`AppColors.Accent`, `#E07A5F` 테라코타) 버튼으로 60여 종의 마스터 DB 탐색
  - 현재 `보유중` 또는 `장바구니`에 활성 등록된 재료는 `[✓ 보유중 등록됨]` / `[✓ 장바구니 등록됨]` 배지로 중복 추가 방지
  - **소진된(`CONSUMED`) 재료는 검색 창에서 `[+ 보유중]` 및 `[+ 장바구니]` 추가 버튼이 활성화**되어 중복 생성 없이 기존 항목의 상태를 원하는 타겟(`보유중` 또는 `장바구니`)으로 즉시 복귀/전환 지원
  - **검색 결과 및 커스텀 추가 버튼**: **`[+ 보유중]`**(초록)과 **`[+ 장바구니]`**(오렌지) 버튼을 **가로 양옆으로 동일한 크기(68×30dp)**로 나란히 배치하여 원터치 선택감 향상
- **플랫폼별 적응형(Adaptive) '요리하기' 버튼**:
  - **웹 (Web Wasm / JS)**: 헤더 우측 `[재료 검색]` 버튼 옆에 동일한 높이·크기·스타일의 **`[🍳 요리하기]` 와이드 캡슐 버튼** 배치
  - **모바일 (Android & iOS)**: 화면 우측 중앙에 3D 토이 프라이팬 에셋(`other_utensils.png`)과 '요리하기'가 들어간 **동그란 원형 플로팅 액션 버튼(Circle FAB, 60dp)** 배치
- **웹 Skia 엑스박스(Tofu) 100% 차단**:
  - 유니코드 컬러 이모지 대신 텍스트 카테고리(`육류 / 해산물`, `채소 / 과일` 등) 및 Compose Vector Icon으로 안전하게 정돈
- **패키지**: `com.dahee.blockbyblock.presentation.inventory` 및 `domain/model/Ingredient.kt`, `data/datasource/MasterIngredientCatalog.kt`, `data/repository/InMemoryIngredientRepository.kt`

### 4) 웹 전용 스플래시 로딩 화면 (Front-Facing 3D Toy Bento Snap-In)
- **정면 뷰 3D 토이 브릭 음식 도시락 (Front-Facing Toy Bento Module)**:
  - 앞에서 바라본 직관적인 완구 아이콘 형태로, 3가지 개별 음식 부품이 완벽하게 맞물리는 구조:
    - **🥦 1. 채소 블록 (Green / 250ml)**: 좌측 라운드 코너, 신선한 그린 그라데이션과 3개의 스터드, 상단/좌측 광택 림
    - **🧀 2. 밥/계란/치즈 블록 (Yellow / 125ml)**: 중앙 사각 파티션과 3개의 스터드, 정교한 플라스틱 챔퍼
    - **🥩 3. 고기/패티 블록 (Reddish-Brown / 125ml)**: 우측 라운드 코너와 3개의 스터드, 하단 3D 돌출 깊이(4px)
  - **슉 → 착! 순차 조립 스냅 애니메이션 (`blockSnapIn`)**:
    - 각 블록이 오른쪽 바깥(`translateX(95px)`)에서 왼쪽으로 빠르게 날아와 미세 오버슛(-3px) 후 **순서대로 하나씩 '착!' 소리 나듯 결합**
    - `0.0s (채소)` ➔ `0.16s (밥/치즈)` ➔ `0.32s (고기)`의 시차를 두고 날아와 견고한 3색 도시락 블록으로 조립 유지 후 부드러운 루프 반복
- **라이프사이클 완전 동기화**:
  - Wasm 엔진 및 한글 웹 폰트(`Noto Sans KR`)가 글자를 그릴 준비를 마칠 때까지 스플래시가 안정적으로 유지된 후 부드럽게 페이드아웃(`fade-out`)

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
│   │   ├── CookingToolType.kt            # GAS_STOVE, OVEN, SLOW_COOKER, BLENDER...
│   │   ├── Ingredient.kt                 # 식재료 엔티티
│   │   ├── IngredientCategory.kt         # MEAT_SEAFOOD, VEGETABLE, GRAIN_CARB...
│   │   ├── IngredientStatus.kt           # IN_STOCK, CONSUMED, SHOPPING_CART
│   │   ├── IngredientUnit.kt             # GRAM, PIECE, LBS, ML
│   │   └── CatalogIngredient.kt          # 마스터 카탈로그 모델
│   └── repository/
│       ├── EquipmentRepository.kt
│       └── IngredientRepository.kt
├── data/
│   ├── datasource/MasterIngredientCatalog.kt # 60여종 표준 식재료 마스터 DB
│   └── repository/
│       ├── InMemoryEquipmentRepository.kt    # 장비 CRUD & Flow 반응형 저장소
│       └── InMemoryIngredientRepository.kt   # 식재료 CRUD & 상태/수량/단위 관리
└── presentation/
    ├── home/
    │   └── HomeScreen.kt                 # 홈 대시보드 (장비 요약, 오늘 식단, 보관함)
    ├── navigation/
    │   ├── AppBottomNav.kt               # 하단 5개 탭 (오늘, 보관함, 식단, 장비, MY)
    │   └── NavTab.kt
    ├── equipment/
    │   ├── EquipmentViewModel.kt         # 장비 MVI 상태 머신
    │   ├── EquipmentScreen.kt            # 장비 화면 컨테이너 (Onboarding -> Setup -> List)
    │   ├── components/
    │   │   ├── MoldView.kt               # 실시간 동적 컬러/칸수 실리콘 몰드 뷰
    │   │   ├── CookingToolVisual.kt      # 조리기구 멀티플랫폼 비주얼 컴포넌트
    │   │   ├── SingleMoldEditDialog.kt   # 단일 몰드 원터치 수정/삭제 팝업
    │   │   ├── EquipmentItemCard.kt      # 장비 아이템 카드
    │   │   └── EquipmentAddEditDialog.kt # 장비 등록/수정 모달
    │   └── screens/
    │       ├── EquipmentOnboardingScreen.kt  # 1번 온보딩 화면
    │       ├── EquipmentSetupScreen.kt       # 2번 몰드/기구 설정 화면
    │       └── EquipmentListScreen.kt        # 3번 내 장비 목록 화면
    ├── inventory/
    │   ├── IngredientViewModel.kt        # 식재료/장바구니/소진 MVI 뷰모델
    │   ├── InventoryScreen.kt            # 보관함 & 장바구니 메인 화면
    │   ├── state/
    │   │   ├── IngredientTab.kt          # ALL, IN_STOCK, SHOPPING_CART
    │   │   └── IngredientUiState.kt      # 보관함 UI 상태
    │   └── components/
    │       ├── IngredientItemCard.kt         # 3-State 순환 체크리스트 카드
    │       ├── IngredientSearchAddDialog.kt  # 마스터 카탈로그 검색 & 추가 팝업
    │       └── IngredientAddEditDialog.kt    # 수동 재료 등록/수정 모달
    └── me/
        └── MeScreen.kt                   # 프로필 & 언어 전환 화면
```

---

## 5. 엄격한 UI / UX 규칙 (User Directives)

1. **이모지 사용 금지**: Skiko Web 엑스박스 방지를 위해 Kotlin String에 유니코드 컬러 이모지를 사용하지 않고 Compose Vector Graphic 아이콘을 사용합니다.
2. **조리기구 스타일**: 네모 테두리 박스 없이 투명 배경의 **큰 이미지(56dp) + 하단 중앙 작은 글씨(12sp)** 구조를 유지하며, 가로 중앙 정렬합니다.
3. **몰드 설정 카드 레이아웃**:
   - 좌측: 대형 몰드 이미지 + 이미지 바로 아래에 고정된 작은 용량 글자 (`500ml` 등).
   - 우측: 상단 개수 스텝퍼(`1개`), 중단 칸수 칩 & 스텝퍼(우측 정렬), 하단 컬러 팔레트(우측 정렬, 레이블 글자 없음).
   - 카드 어디를 터치해도 추가/취소 토글이 자연스럽게 동작해야 함.
4. **목록 화면 간결성**: 중복 텍스트를 배제하고 `[몰드 이미지]`, `500ml`, `2칸`, `[수량 스텝퍼]`만 깔끔하게 노출합니다.
5. **다국어(i18n) 지원**: 모든 신규 화면 및 컴포넌트의 텍스트는 하드코딩하지 않고 `LocalStrings.current`의 `AppStrings`를 참조합니다.
6. **3D 블록/토이 에셋 생성 가이드 준수**: 식재료, 도구, 뱃지 등 신규 이미지 에셋 제작 시 반드시 `DESIGN_PROMPT_GUIDE.md`의 마스터 프롬프트 템플릿(3D Toy block brick miniature, ABS plastic, isometric 3/4 view, pure white background)을 사용합니다.
7. **샘플(목업) 데이터 생성 절대 금지**: 새 페이지/기능 개발 시 임의의 대량 샘플 데이터를 채워넣지 않고, 항상 깨끗한 빈 상태(`emptyList()`)로 시작합니다. (사용자가 직접 생성하며 테스트)
8. **주석 영어 작성**: 모든 Kotlin 소스 코드 주석(`//`, `/** */`)은 100% 영문으로만 작성합니다.
9. **금지 상표명 배제**: `LEGO`, `레고`, `Souper` 등의 키워드를 코드, 주석, 문서에 일절 사용하지 않습니다.
10. **화면 QA 요청 포맷 규칙**: 사용자가 `"ㅇㅇ화면 QA해줘"`라고 요청할 때는 긴 서론이나 별도 마크다운 테이블 없이, **오직 엑셀/구글 스프레드시트 붙여넣기 전용 TSV 코드 블록(문제점\t영향도\t권장 조치) 하나로만 즉시 응답**합니다.
11. **단일 이미지 에셋 저장 경로 원칙**: 모든 이미지/그래픽 에셋은 오직 **`shared/src/commonMain/composeResources/drawable/` 단일 경로**에만 저장합니다. 루트의 `/images` 등 별도 폴더로 분산 저장하지 않으며, 향후 신규 이미지 생성 시에도 반드시 이 경로로 직접 저장합니다.
12. **임의의 UI 설명/안내 문구 추가 절대 금지**: 부가적인 부가 설명 텍스트나 힌트 문구를 임의로 추가하지 않고, 오직 사용자가 명시적으로 요청한 텍스트만 UI에 반영합니다.

---

## 6. 다음 구현 로드맵 (Next Steps)

1. **블록 재고 생성 & 소분 (Block Inventory - Phase 4)**:
   - 몰드 용량 선택, 제작일자, 보관 방식(냉장/냉동), 유통기한(기본 90일), 재료 차감 및 롤백
2. **오늘의 식단 & 스냅 사운드 (Meal Plan & Audio - Phase 5)**:
   - 주간 식단 구성, 소분 블록 소비 기록, 블록 맞물리는 사운드 효과
