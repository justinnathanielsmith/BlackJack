This is a comprehensive plan designed to be fed as a prompt or context to an AI coding assistant (like GitHub Copilot, ChatGPT, or Claude) to generate the Jetpack Compose code for the **gameplay screen** shown in the right panel of the image.

---

### **Context for the AI Coding Assistant**

**Role:** You are an expert Senior Android Developer specializing in Jetpack Compose, specifically skilled in complex UI animations, 3D transformations, and custom graphics.
**Objective:** Implement a high-performance, visually rich gameplay screen for a memory match game, based *exactly* on the visual design provided in the reference image (right panel).
**Tech Stack:** Kotlin, Jetpack Compose, Material Design 3 (heavily customized).

---

### **1. Visual Analysis & Design System Definition**

**A. Color Palette (Approximate Hex Values):**

* **Background Gradient:** Deep vertical gradient from Dark Navy (`#0A1128`) to Deep Blue (`#141E3C`).
* **HUD Text (Timer/Labels):** Light Grey-Blue (`#B0C4DE`).
* **HUD Values (Time/Score):** White (`#FFFFFF`).
* **Card Back:** Textured dark blue (`#1E2A4F`) with a lighter blue border.
* **Card Front Background:** Off-white/Cream (`#F8F9FA`).
* **Suit Colors:** Standard Red (`#E53935`) and Black (`#212121`).
* **Active/Selected Glow:** Vibrant Golden-Yellow (`#FFD700`) with a soft particle blur.
* **Multiplier Badge:** Glowing Blue-Cyan (`#2D8EFF`) hexagonal shape.

**B. Shapes, Depth & Textures:**

* **Card Shape:** Rectangular with moderately rounded corners (e.g., `RoundedCornerShape(12.dp)`).
* **Depth:** All cards (face up or down) must have a subtle drop shadow to separate them from the background.
* **Card Back Texture:** A subtle cross-hatch or diamond pattern is visible on the dark blue backs.
* **Active Glow:** The selected card (`9 ♦`) has an intense outer glow and subtle particle effects emitting from it.

**C. Typography:**

* **Card Ranks (Large Center):** Large, bold, legible Sans-Serif.
* **HUD Values:** Medium-weight, clean Sans-Serif.

---

### **2. UI Component Breakdown**

The screen is a root `Box` (for background) containing a `Column`.

**A. `GameScreenTopBar` (HUD Row)**

* A `Row` with `Arrangement.SpaceBetween` and padding.
* **Left Element:** A Column containing a small label "Timer" and a row with a clock icon and timer value "00:50".
* **Center Element:** A Column with label "Score" and value "265".
* **Right Element (`MultiplierBadge`):** A custom component. A glowing hexagonal/polygon shape containing text "x2". This likely requires a custom `Canvas` draw or a shaped `Surface` with a heavy shadow/glow modifier.

**B. `GameGrid` Area**

* A `LazyVerticalGrid` with `columns = GridCells.Fixed(4)`.
* It needs uniform horizontal and vertical content padding to center it on the screen and space the cards evenly.

**C. `MemoryCard` (The Hero Component)**

* This is a complex, state-aware component representing a single card slot. It needs to handle:
* **3D flip animation:** Rotating 180 degrees on the Y-axis.
* **Front/Back Content Swapping:** Showing the card back when rotation is < 90deg, and front when > 90deg.
* **Selected State:** Applying the golden glow border and shadow when active.



---

### **3. State Management Requirements (ViewModel Contract)**

Define the data structures the UI needs to react to.

**Data Models:**

```kotlin
enum class CardState {
    FACE_DOWN,
    FLIPPING, // Transient state during animation
    FACE_UP,  // Temporarily revealed
    MATCHED   // Permanently face up (or removed, depending on game design)
}

enum class Suit { HEARTS, DIAMONDS, CLUBS, SPADES }

data class CardData(
    val id: String,
    val rank: String, // e.g., "9", "A", "2"
    val suit: Suit,
    var state: CardState = CardState.FACE_DOWN,
    val isSelected: Boolean = false // Triggers the golden glow
)

// Overall Screen State
data class GameUiState(
    val cards: List<CardData>, // List of 16 cards
    val timeElapsedFormatted: String = "00:50",
    val score: Int = 265,
    val multiplier: Int = 2
)

```

**Events:**

* `onCardClicked(cardId: String)`

---

### **4. Implementation Prompt Steps (For the AI)**

*Inject these steps to guide the AI coding assistant.*

1. **Step 1: Scaffold and Top Bar (HUD).**
* Set up the root `Box` with the deep navy background gradient.
* Create the `GameScreenTopBar` composable. Implement the Timer and Score columns using simple Text components.
* *Challenge:* Implement the `MultiplierBadge`. Use a `Box` with a custom glowing blue modifier. Inside, use a `Surface` with a custom hexagonal `Shape` (or a rotated square with clipped corners) to achieve the gem-like look in the image.


2. **Step 2: Define Card Models and Grid Structure.**
* Define the `CardData`, `CardState`, and `Suit` data classes as outlined in the state management section.
* Create a dummy list of 16 `CardData` objects to populate the grid.
* Implement the `GameGrid` using `LazyVerticalGrid(GridCells.Fixed(4))`. Add necessary padding so the grid sits centrally below the Top Bar.


3. **Step 3: The Static Memory Card Component.**
* Create composables for `CardBack` and `CardFront`.
* `CardBack`: A `Surface` with rounded corners, the dark blue color, and a subtle texture (use a simple draw modifier for pattern if no drawable assets are available). Add elevation/shadow.
* `CardFront`: A `Surface` with off-white color. Use a `ConstraintLayout` or `Box` to position the large center rank and the smaller corner rank/suit icons. Color ranks red for Hearts/Diamonds, black for Clubs/Spades.


4. **Step 4: The 3D Flip Animation (Crucial).**
* Create the master `MemoryCard` composable that takes `CardData` as input.
* Use `animatableFloat` to control rotation (0f to 180f).
* Apply `Modifier.graphicsLayer`. **Important:** Set `cameraDistance = 12f * density` (or higher) to ensure a realistic 3D perspective during the flip, otherwise it will look flat. Set `rotationY` based on the animated value.
* Inside the card box, conditionally show `CardBack` if rotation is < 90 degrees, and `CardFront` if > 90 degrees. Remember to apply `rotationY(180f)` to the `CardFront` content so it doesn't appear mirrored when flipped.


5. **Step 5: Active State Glow & Finishing Touches.**
* Modify the `MemoryCard` composable. If `cardData.isSelected` is true (like the 9 of Diamonds in the image), apply a prominent border and a heavy, blurred drop shadow using the Golden-Yellow color to create the glow effect.
* Review padding and font sizes in the HUD to match the clean look of the reference image.