# 🧱 BlockByBlock (블록바이블록)

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.7.3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Kotlin/Wasm](https://img.shields.io/badge/Kotlin-Wasm-F48400?style=for-the-badge&logo=webassembly&logoColor=white)
![Android](https://img.shields.io/badge/Android-Native-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![iOS](https://img.shields.io/badge/iOS-Native-000000?style=for-the-badge&logo=apple&logoColor=white)

<br/>

**소분 몰드로 식단을 레고처럼 조립하는 스마트 밀프렙 & 냉동고 블록 관리 앱**  
*A modular meal-prep & freezer inventory manager that lets you assemble food blocks into lunchboxes like LEGO bricks.*

<br/>

[📱 핵심 기능](#-핵심-기능-key-features) • [🛠️ 기술 스택](#-기술-스택--아키텍처) • [🚀 시작하기](#-시작하기-getting-started) • [📂 프로젝트 구조](#-프로젝트-구조)

</div>

---

## 📖 소개 (About The Project)

**BlockByBlock**은 대용량 요리나 남은 음식을 큐브 몰드(Souper Cubes 등)에 소분해 얼린 뒤, 이를 **‘3D 푸드 블록’**으로 등록하여 도시락통에 레고처럼 조합해 먹을 수 있는 **크로스플랫폼 스마트 밀프렙 앱**입니다.

### 💡 기획 배경 및 해결하고자 하는 문제 (Problem & Solution)
* 🧊 **냉동실 방치 식재료 ZERO**: 대용량으로 밀프렙해 얼려두고 잊어버려 버려지는 식재료 낭비를 줄입니다.
* 🍱 **매일 쉬워지는 식단 조립**: 매일 "오늘 뭐 먹지?" 고민할 필요 없이, 냉동실에 보관된 푸드 블록들을 4구/5구 도시락통에 직관적으로 조합합니다.
* 🍳 **즉시 확인하는 맞춤 조리법**: 냉동실에서 블록을 꺼냈을 때 어떤 도구(전자레인지, 에어프라이어, 오븐 등)로 몇 분 동안 돌려야 하는지 바로 확인하고 맛있게 섭취합니다.

---

## ✨ 핵심 기능 (Key Features)

### 1. 🧱 3D 푸드 블록 시스템 (Food Block Modeling)
* **몰드 맞춤 수량 계산**: 소분 몰드 용량(`500ml`, `250ml`, `125ml`, `75ml`, `직접 입력`) 및 사용 몰드 개수에 따라 생산되는 푸드 블록 개수를 자동 계산합니다.
* **5가지 컬러 코딩**: 식재료 및 영양군에 따라 블록 색상을 지정하고 Skia 캔버스 기반의 입체 3D 블록 뷰로 확인합니다.
* **배달·완제품 간편 등록**: 식재료를 일일이 고를 수 없는 배달 음식이나 완제품 요리도 메뉴명만으로 손쉽게 블록을 생성할 수 있습니다.
* **이전 메뉴 빠른 불러오기**: 이전에 만들었던 블록명을 원터치 칩으로 추천받아 간편하게 재등록할 수 있습니다.

### 2. 🍱 레고 도시락통 & 식단 플래너 (Bento Box & Meal Planner)
* **직관적인 식단 조립**: 4구/5구 도시락통에 냉동실의 푸드 블록을 클릭/터치하여 원하는 조합으로 구성합니다.
* **일별 식단 (최대 5끼)**: 아침, 점심, 저녁, 간식 등 하루 최대 5개 식사를 직관적인 타임라인으로 관리합니다.
* **주별 식단 그리드**: 월~일 일주일치 식단 계획을 한눈에 조망하고 모듈형으로 관리합니다.
* **인벤토리 실시간 차감**: 식단표를 저장하면 보관함의 냉동 블록 재고가 자동으로 차감 연동됩니다.

### 3. 🍳 조리 방법 설정 & 소비기한 관리 (Cooking Method & Shelf Life)
* **도구별 맞춤 세팅**:
  * **오븐 / 에어프라이어 / 슬로우쿠커**: 조리 온도(`°C`) 및 조리 시간(`분`) 설정
  * **전자레인지**: 조리 시간(`분`, `초`) 설정
  * **레인지 / 믹서기**: 도구별 최적 조리/전처리 가이드 설정
* **D-Day 소비기한 알림**: 냉동/냉장 보관된 블록의 남은 소비기한을 실시간으로 추적하여 신선하게 소진하도록 돕습니다.

### 4. 📦 스마트 인벤토리 & 장비 관리 (Inventory & Equipment)
* **블록 보관함**: 냉동실 및 냉장실에 저장된 푸드 블록의 수량, 조리법, 소비기한을 실시간으로 확인합니다.
* **식재료 관리**: 보유 중인 식재료, 소비기한, 장바구니 목록을 카테고리별로 관리합니다.
* **내 장비 관리**: 보유 중인 소분 몰드(용량, 칸수, 색상) 및 주방 조리도구를 등록하고 관리합니다.

---

## 🛠️ 기술 스택 & 아키텍처 (Tech Stack & Architecture)

BlockByBlock은 최신 **Kotlin Multiplatform (KMP)**과 **Compose Multiplatform**을 기반으로 구축된 단일 코드베이스 멀티플랫폼 프로젝트입니다.

| 분류 | 기술 스택 |
| :--- | :--- |
| **Language** | Kotlin 2.1.0 |
| **UI Framework** | Compose Multiplatform 1.7.3 (Web Wasm, Android, iOS) |
| **Web Target** | Kotlin/Wasm (WebAssembly GC 최적화) |
| **Architecture** | Clean Architecture + MVI Pattern (UiState, StateFlow, Coroutines) |
| **Graphics** | Skia Canvas Rendering (3D 블록 스터드 및 입체 몰드 뷰) |
| **Design System** | Warm Honey & Toy Block Palette (`#F4D06F`, `#FF7043`, `#2D3142`) |
| **Internationalization** | Full i18n (한국어 `ko`, 영어 `en` 동적 언어 전환) |

### 🏛️ 클린 아키텍처 레이어 구조
```
shared/src/commonMain/kotlin/com/dahee/blockbyblock/
├── core/                  # 테마, 색상, 컴포넌트, 다국어(i18n), 유틸리티
├── domain/
│   ├── model/             # FoodBlock, Equipment, Ingredient, MealPlan, StorageType
│   └── repository/        # 비즈니스 인터페이스 (FoodBlockRepository 등)
├── data/
│   ├── repository/        # 메모리/로컬 스토리지 구현체
│   └── source/            # 기본 시드 데이터 및 프리셋
└── presentation/
    ├── auth/              # 로그인 / 회원가입 / 약관 동의
    ├── mealplan/          # 도시락통 식단 플래너 (일별/주별)
    ├── block/             # 3D 블록 생성, 히스토리, 보관함
    ├── inventory/         # 식재료 보관함 & 장바구니
    ├── equipment/         # 몰드 및 조리도구 장비 관리
    └── profile/           # 사용자 프로필 & 다국어 설정
```

---

## 🚀 시작하기 (Getting Started)

### 📌 사전 요구 사항
* **JDK 17** 이상
* **Android Studio** (최신 Ladybug / Meerkat 권장) 또는 **IntelliJ IDEA**
* **Xcode** (iOS 빌드 시 필요, macOS 환경)
* **Chrome / Safari** (Wasm-GC 지원 최신 브라우저)

### 💻 실행 방법

#### 1. Web (Kotlin/Wasm) 실행
```bash
# Wasm 개발 서버 실행 (권장)
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# 브라우저 접속: http://localhost:8080
```

#### 2. Android 앱 실행
```bash
# Debug APK 빌드
./gradlew :androidApp:assembleDebug

# 또는 Android Studio에서 androidApp 모듈 실행
```

#### 3. iOS 앱 실행
```bash
# Xcode에서 iosApp 프로젝트 열기
open iosApp/iosApp.xcodeproj

# 시뮬레이터 또는 실제 기기에서 Run (Cmd + R)
```

---

## 📂 프로젝트 구조 (Project Structure)

```
BlockByBlock/
├── androidApp/            # Android 애플리케이션 진입점
├── iosApp/                # iOS 애플리케이션 진입점 (SwiftUI 래퍼)
├── webApp/                # WebAssembly (Wasm-GC) 진입점 및 리소스
│   └── src/webMain/resources/
│       ├── index.html     # 스플래시 로더 및 폰트 프리로드
│       └── styles.css
└── shared/                # 공통 비즈니스 로직 및 Compose Multiplatform UI
    ├── commonMain/        # 공통 플랫폼 코드 (UI, ViewModel, Domain, Data)
    ├── androidMain/       # Android 특화 구현
    ├── iosMain/           # iOS 특화 구현
    └── wasmJsMain/        # WebAssembly 특화 구현
```

---

## 🌐 다국어 지원 (Internationalization)

BlockByBlock은 한국어와 영어를 기본 지원합니다. 시스템 언어를 자동 감지하며, 내 프로필 설정에서 언제든지 언어를 전환할 수 있습니다.

* 🇰🇷 **한국어 (Korean)**: 기본 언어 및 한글 폰트(`NotoSansKR`) 최적화
* 🇺🇸 **영어 (English)**: 글로벌 사용자를 위한 완전한 영문 UI 지원

---
Copyright © 2026 BlockByBlock. All rights reserved.
