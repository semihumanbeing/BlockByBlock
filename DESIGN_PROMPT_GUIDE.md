# 🧱 BlockByBlock 3D 블록/토이 스타일 에셋 생성 프롬프트 가이드
# (3D Toy Block Brick & Kitchen Asset Design Prompt Guide)

이 문서는 `shared/src/commonMain/composeResources/drawable/`의 기존 에셋(**가스레인지, 오븐, 믹서기, 전자레인지, 에어프라이어, 슬로우쿠커, 장바구니, 몰드 등**)과 완벽하게 일치하는 **3D 블록 브릭 토이 스타일 일러스트**를 생성하기 위한 공식 프롬프트 가이드입니다.

새로운 조리기구, 식재료, 소분 블록, UI 아이콘 및 뱃지 등을 제작할 때 아래의 **마스터 프롬프트 템플릿**을 복사하여 `[대상/아이템]` 부분만 변경해 사용하며, 생성된 이미지는 항상 **`shared/src/commonMain/composeResources/drawable/` 단일 경로**에만 저장합니다.

---

## 🎨 1. 핵심 비주얼 아이덴티티 (Design DNA)

| 항목 | 핵심 규칙 & 스타일 속성 |
| :--- | :--- |
| **조형 (Geometry)** | 실제 조립 블록(Toy Building Blocks & Plates)으로 조립된 귀여운 Chibi/MOC 미니어처 구조. 둥근 스터드(Studs), 매끄러운 타일(Flat Tiles), 슬로프 브릭 결합 |
| **소재 (Material)** | 고광택 ABS 플라스틱(High-gloss injection molded ABS plastic), 블록 간의 미세한 조립선(Seam lines), 완만한 베벨 엣지(Beveled edges) |
| **특수 부품 (Accents)** | 유리/액체/불꽃 표현 시 투명 클리어 블록(Trans-clear / Trans-blue / Trans-pink building block pieces) 사용 |
| **색상 (Palette)** | 경쾌하고 세련된 레트로 토이 & 파스텔 컬러 블로킹 (코발트 블루, 탠저린 오렌지, 켈리 그린, 레몬 옐로우, 민트 틸, 체리 레드 + 화이트/라이트 그레이 프레임) |
| **구도 (Camera)** | 3/4 아이소메트릭 뷰 (Isometric 3/4 angle, 30~45도 하이 앵글), 피사체 중앙 배치, 정방형(1:1) |
| **조명 & 배경 (Lighting)** | 부드러운 스튜디오 3점 조명(Soft 3-point studio lighting), 바닥에 닿는 자연스러운 연한 접촉 그림자(Soft contact shadow), **순수 화이트 단색 배경 (Pure white isolated background `#FFFFFF`)** 또는 투명 배경 |

---

## 🚀 2. 마스터 프롬프트 템플릿 (Master Prompt Template)

AI 이미지 생성기(Midjourney, DALL-E 3, Imagen 3, Stable Diffusion, Flux, Recraft 등)에 입력할 기본 공식입니다.

```text
A cute 3D toy block brick miniature model of [ITEM_NAME_AND_KEY_FEATURES], built entirely from realistic glossy plastic toy building blocks and bricks with visible circular block studs and smooth flat tiles. [ITEM_SPECIFIC_COLOR_AND_DETAILS]. High-gloss ABS plastic texture with subtle seam lines between bricks, vibrant clean color blocking with [ACCENT_COLORS]. Isolated on a solid seamless pure white background with a soft studio contact shadow underneath, isometric 3/4 high angle perspective view, centered composition, clean studio 3D render, Octane render style, cute chibi toy aesthetic, macro product photography, ultra-sharp focus, 8k resolution.
```

### 🚫 네거티브 프롬프트 (Negative Prompt)
```text
realistic metal, photorealistic human hands, photorealistic appliances, non-block geometry, continuous smooth non-block surfaces, wireframe, noisy background, dark background, complex scene, blurry, low resolution, messy colors, deformed bricks, photorealistic realism, sketches, flat vector 2d illustration
```

---

## 📋 3. 카테고리별 실전 프롬프트 예시 (Category Presets)

### 1) 신규 주방 가전 & 조리도구 (Kitchen Appliances)

#### 🍚 전기 밥솥 (Electric Rice Cooker)
```text
A cute 3D toy block brick miniature model of a modern Korean electric rice cooker, built entirely from realistic glossy plastic toy building blocks with circular block studs on top. Pure white and warm pastel yellow body, round cylindrical shape, metallic grey block handle, small black printed LED timer tile and round push buttons on the front. Isolated on a solid pure white background with a soft studio contact shadow underneath, isometric 3/4 high angle perspective view, centered composition, clean 3D Octane render, cute chibi toy aesthetic, 8k.
```

#### 🍞 토스터기 (Toaster with Toast)
```text
A cute 3D toy block brick miniature model of a retro pop-up bread toaster, built entirely from realistic glossy plastic toy building blocks. Bright cherry red body with white curved top bricks, metallic silver 1x1 lever on the side, two golden-brown block toast slices popping out of the top slots made of tan and yellow block plates. Isolated on a solid pure white background with a soft studio contact shadow underneath, isometric 3/4 high angle perspective, clean 3D render, cute toy aesthetic.
```

#### 🫖 전기 주전자 (Electric Kettle)
```text
A cute 3D toy block brick miniature model of a modern gooseneck electric kettle, built entirely from glossy mint green plastic block bricks. Sleek cylindrical body, curved grey block handle, slender curved spout, dark grey base with a small black round knob. Isolated on a solid pure white background with a soft contact shadow, isometric 3/4 view, clean studio lighting, 8k resolution.
```

#### 🍳 프라이팬 & 뒤집개 (Frying Pan with Egg)
```text
A cute 3D toy block brick miniature model of a black round skillet frying pan with a dark grey handle, containing a fried sunny-side-up egg made of a round white block plate and a bright yellow round 1x1 stud yolk, next to a red flat block spatula tool. Isolated on a pure white background with a soft contact shadow, isometric 3/4 view, high-gloss plastic finish, cute toy render.
```

---

### 2) 식재료 & 식단 블록 (Ingredients & Food Blocks)

#### 🧅 양파 (Block Onion)
```text
A cute 3D toy block brick miniature model of a whole yellow onion, built from stacked curved beige and light brown glossy plastic block slope bricks, with a small green block plant stem piece on top. Isolated on a pure white background with a soft contact shadow, isometric 3/4 view, vibrant toy aesthetic, clean 3D render.
```

#### 🥩 소고기 큐브 블록 (Block Beef Meat Cube)
```text
A cute 3D toy block brick miniature model of a frozen raw beef meat portion block, structured as a compact cubic brick unit with distinct circular block studs on top. Deep crimson red and marbled pink/white block plates layered together, looking like a cute frozen meal block. Isolated on a pure white background with a soft contact shadow, isometric 3/4 view, glossy plastic texture.
```

#### 🥕 당근 (Block Carrot)
```text
A cute 3D toy block brick miniature model of a fresh carrot, constructed from vibrant bright orange block slope bricks and round plates, with bright green block leaf foliage pieces at the top. Isolated on a pure white background with a soft studio shadow, isometric 3/4 angle, clean studio render.
```

#### 🥦 브로콜리 (Block Broccoli)
```text
A cute 3D toy block brick miniature model of a broccoli head, built from lime green and dark green block stud clusters and round dome bricks, with a light green block stem base. Isolated on a pure white background with a soft contact shadow, isometric 3/4 perspective, 3D toy render.
```

#### 🧈 버터 / 치즈 블록 (Block Butter / Cheese Brick)
```text
A cute 3D toy block brick miniature model of a rectangular golden yellow butter block, built from smooth yellow block tile bricks with 4 iconic circular studs on top. Isolated on a pure white background with a soft shadow, isometric 3/4 angle, glossy toy finish.
```

---

### 3) 몰드 & 냉동 보관함 (Silicone Molds & Trays)

#### 🧊 2구 몰드 (2-Slot Silicone Mold Tray)
- **프롬프트**:
  ```text
  A premium isometric 3D game asset of a high-grade food-grade silicone freezing mold tray with 2 deep square slots, mint teal color, soft matte translucent texture, glossy highlights, crisp round lego-style studs on edges, high resolution, clean white background, soft ambient occlusion shadow beneath, mobile game UI asset style, ray tracing render, 8k
  ```

#### 🧊 8구 이유식/양념 얼음틀 몰드 (8-Slot Mini Mold Tray)
```text
A cute 3D toy block brick miniature model of an 8-compartment (2x4 grid) mini baby food freezer tray, constructed from pastel coral pink block bricks, with 8 small transparent clear square well compartments. Isometric 3/4 high angle view, solid pure white background, studio lighting, cute toy render.
```

---

## 4. 에셋 명명 규칙 & 파일 규격

| 자산 분류 | 파일명 규칙 | 주요 색상 | 비주얼 특징 | 세부 렌더링 지침 |
| :--- | :--- | :--- | :--- | :--- |
| **조리 기구** | `tool_*.png` | 화이트, 메탈 그레이, 틸 | 토이 가전 스타일 | 둥근 모서리 + 디테일 버튼 다이얼 |
| **몰드 (1~15구)**| `mold_*.png` | 민트 틸 / 화이트 | 투명 슬롯 큐브 | 테두리 스터드 돌기 + 투명 사각 슬롯 |

---

### 4) 앱 UI 아이콘 & 뱃지 (UI Icons & Badges)

#### ⭐ 황금 별 뱃지 (Block Star Badge)
```text
A cute 3D toy block brick icon of a sparkling golden star badge, built from bright glossy yellow and orange block angle bricks with visible circular studs, framed with a white border. Isolated on a pure white background with a soft contact shadow, isometric 3/4 view, vibrant toy aesthetic.
```

#### 🛒 장바구니 (Block Shopping Cart)
```text
A cute 3D toy block brick miniature model of a grocery shopping cart, constructed from light grey block bar pieces with red accent rims and small black block wheels, holding miniature colorful toy food blocks. Pure white background, isometric 3/4 angle, cute toy 3D render.
```

#### ⏱️ 타이머 / 시계 (Block Kitchen Timer)
```text
A cute 3D toy block brick miniature model of a round kitchen mechanical timer, bright red and white cylindrical block build with a printed dial face tile and a black pointer needle. Pure white background, soft contact shadow, isometric 3/4 view.
```

---

## 🛠️ 4. 기존 에셋별 레퍼런스 코드 & 컬러 매핑

새로운 에셋을 추가할 때 기존 색상 팔레트와 겹치지 않게 매핑할 수 있는 기준표입니다:

| 품목 (Item) | 에셋 파일명 | 메인 컬러 (Main Hex/Tone) | 보조/강조 컬러 | 특징 포인트 |
| :--- | :--- | :--- | :--- | :--- |
| **가스레인지** | `gas_stove.png` | 코발트 블루 (`#1E88E5`) | 그레이 상판, 블랙 버너 | 반투명 시안 파란 불꽃 블록 (`trans-blue`) |
| **오븐** | `oven.png` | 탠저린 오렌지 (`#FB8C00`) | 화이트 프레임, 오렌지/레드 버튼 | 내부 투명 창 + 컵케이크 미니어처 |
| **블렌더** | `blender.png` | 켈리 그린 (`#43A047`) | 다크 그레이 뚜껑/손잡이 | 투명 용기 + 내부 핑크 스무디 블록 |
| **전자레인지** | `microwave.png` | 레몬 옐로우 (`#FBC02D`) | 화이트 본체, 블랙 다이얼 | 투명 창 속 조명 + 치킨 닭다리 블록 |
| **에어프라이어** | `air_fryer.png` | 파스텔 민트 (`#4DB6AC`) | 화이트 띠, 블랙 에어벤트 | 반쯤 열린 서랍 속 황금 감자튀김 |
| **슬로우쿠커** | `slow_cooker.png` | 체리 레드 (`#E53935`) | 그레이 손잡이/받침 | 투명 돔 뚜껑 + 블랙 전원선 & 플러그 |
| **조리도구 3종** | `other_utensils.png` | 블랙 / 레드 / 메탈릭 그레이 | 옐로우/화이트 계란 | 프라이팬 계란후라이 + 주걱 + 거품기 |
| **몰드 (1~15구)**| `mold_*.png` | 민트 틸 / 화이트 | 투명 슬롯 큐브 | 테두리 스터드 돌기 + 투명 사각 슬롯 |

---

## 💡 5. 프롬프트 작성 꿀팁 (Best Practices)

1. **스터드(Stud) 강조**: `"circular block studs on the top"` 또는 `"embossed studs"` 문구를 넣어주어야 특유의 조립 블록 느낌이 확실히 살아납니다.
2. **소재(Material) 명시**: `"glossy injection molded ABS plastic with subtle seam lines between bricks"`를 기재하여 일반 3D 점토나 일러스트가 아닌 실제 조립 블록 장난감 재질로 렌더링되도록 유도합니다.
3. **투명 부품 표현**: 창문, 유리 용기, 불꽃, 액체는 `"trans-clear / trans-blue transparent block pieces"`로 표현합니다.
4. **배경 통일**: 항상 `"Isolated on a solid pure white background with a soft studio contact shadow underneath"`로 고정하면 앱 UI에 삽입할 때 투명 PNG 변환(누끼 작업)이 매우 깔끔하게 진행됩니다.
