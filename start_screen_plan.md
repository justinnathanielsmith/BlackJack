This is a comprehensive plan designed to be fed as a prompt or context to an AI coding assistant (like GitHub Copilot, ChatGPT, or Claude) to generate the Jetpack Compose code for the start screen shown in the left panel of the image.

---

### **Context for the AI Coding Assistant**

**Role:** You are an expert Senior Android Developer specializing in Jetpack Compose.
**Objective:** Implement a pixel-perfect start screen UI for a memory match game, based *exactly* on the visual design provided in the reference image (left panel).
**Tech Stack:** Kotlin, Jetpack Compose, Material Design 3 concepts (but heavily customized).

### **1. Visual Analysis & Design System Definition**

Before building components, define the visual language.

**A. Color Palette (Approximate Hex Values based on image):**

* **Background Gradient:** A deep vertical gradient from Dark Navy (`#0A1128`) down to a slightly lighter Deep Blue (`#141E3C`).
* **Primary Accent (Active States, Glows, Main Buttons):** Bright Cyan-Blue (`#2D8EFF` or similar).
* **Secondary Accent (Text Glow):** A softer, neon blue glow around the title.
* **Inactive/Unselected Backgrounds:** Dark Blue/Grey with transparency (`#1E2A4F`).
* **Text - Primary:** White (`#FFFFFF`).
* **Text - Secondary/Labels:** Light Grey-Blue (`#B0C4DE`).

**B. Shapes & Styling:**

* **Corner Radius:** Heavy rounding on all elements. Buttons and segmented controls should have almost stadium-shaped corners (e.g., `RoundedCornerShape(50)` or `CircleShape` depending on height).
* **Depth & Effects:**
* **Buttons/Active States:** Must have a soft, subtle drop shadow and a slight inner gradient to give them a tactile, "popped-out" feel.
* **Title:** Needs a soft outer neon glow effect.
* **Header Cards:** Need a soft drop shadow to separate them from the background.



**C. Typography:**

* **Title:** Large, bold, clean Sans-Serif (e.g., Montserrat Bold or Roboto-Bold).
* **Labels & Button Text:** clean medium-weight Sans-Serif.

---

### **2. UI Component Breakdown**

The screen is a single vertical column centered horizontally. Break it down into these composables:

**A. `StartScreen` (Root Composable)**

* **Container:** A `Surface` or `Box` applying the background gradient.
* **Layout:** A `Column` with `horizontalAlignment = Alignment.CenterHorizontally` and padding.

**B. Header Section**

* **`GameTitleHeader` Composable:**
* A `Text` element labeled "Memory Match" with the glowing blue effect.
* An `Image` element below it displaying the two playing cards (Ace of Spades/Clubs) tilted slightly, with a shadow.



**C. Selection Section (The complex part)**

* **`GameSettingsSelector` Composable:** Contains the two toggle groupings.
* **`DifficultyLabel` Text:** "Difficulty".
* **`CustomSegmentedControl` (Reusable Component):** This is critical. Do *not* use a standard Material TabRow. It needs to look like a single capsule.
* *Implementation hint:* A `Box` containing a background `Row` (inactive states) and an animated `Box` sliding behind the selected item (active state).
* *Instances:* One for [Easy (8), Medium (12), Hard (16)] and one for [Standard, Time Attack].


* **`GameModeLabel` Text:** "Game Mode".



**D. Action Buttons Section**

* **`MainActionButton` Composable:**
* A reusable button style with the bright blue gradient, rounded corners, and shadow.
* *Instance 1 (Large):* "Resume Saved Game" with an arrow icon right.


* **`SecondaryActionsRow` Composable:**
* A `Row` containing two smaller instances of the `MainActionButton` style.
* *Instance 2 (Small Left):* "Settings" with a gear icon left.
* *Instance 3 (Small Right):* "Leaderboard" with a trophy icon left.



---

### **3. State Management Requirements (ViewModel Contract)**

Define the state the UI needs to observe and the events it needs to send.

**Data Classes / Enums:**

```kotlin
enum class Difficulty(val label: String, val pairs: Int) {
    EASY("Easy", 8), MEDIUM("Medium", 12), HARD("Hard", 16)
}

enum class GameMode(val label: String) {
    STANDARD("Standard"), TIME_ATTACK("Time Attack")
}

// The overall UI state
data class StartScreenUiState(
    val selectedDifficulty: Difficulty = Difficulty.EASY,
    val selectedGameMode: GameMode = Difficulty.TIME_ATTACK,
    val hasSavedGame: Boolean = true // To determine if "Resume" is enabled
)

```

**Events (Callbacks to pass to the composables):**

* `onDifficultySelected(Difficulty)`
* `onGameModeSelected(GameMode)`
* `onResumeClicked()`
* `onSettingsClicked()`
* `onLeaderboardClicked()`

---

### **4. Implementation Prompt Steps (For the AI)**

*Copy and paste these steps to the AI one by one or as a whole block to guide the generation.*

1. **Step 1: Setup Theme and Basic Structure.**
* Create a `Color.kt` file with the deep navy gradient colors and the bright cyan accent color defined in the visual analysis.
* Create the main root Composable `StartScreen` that applies the background gradient to a `Box` filling the screen. Inside the box, place a `Column` centered horizontally with appropriate padding (e.g., 24.dp).


2. **Step 2: Implement the Header.**
* Inside the Column, add the "Memory Match" title `Text`. Apply a custom `TextStyle` that includes a bright blue color and an outer shadow/glow effect using `Modifier.shadow` or a custom draw modifier to simulate neon.
* Below the text, add the image of the two cards (assume drawables `R.drawable.ic_header_cards` exists). Apply a soft drop shadow to the image modifier. Add vertical spacing below this.


3. **Step 3: Implement the Custom Segmented Control.**
* *Crucial Task:* Create a highly reusable composable named `NeonSegmentedControl<T>`.
* It should take a list of items `List<T>`, a currently selected item `T`, and an `onSelectionChanged: (T) -> Unit` callback.
* Use a `Box` with a deeply rounded background shape and dark color.
* Inside the Box, calculate the width of a single segment. Use an animated `Box` with the bright cyan color and shadow to slide behind the selected index.
* Overlay a `Row` of `Text` items (the labels) over the animated box. The text color should animate between white (selected) and light-blue/grey (unselected).


4. **Step 4: Assemble Settings Selectors.**
* In the main `Column`, add the "Difficulty" label text.
* Implement the `NeonSegmentedControl` using the `Difficulty` enum values. Ensure "Easy (8)" is shown as selected.
* Add vertical spacing.
* Add the "Game Mode" label text.
* Implement the `NeonSegmentedControl` using the `GameMode` enum values. Ensure "Time Attack" is shown as selected.


5. **Step 5: Implement Action Buttons.**
* Create a reusable `NeonStyleButton` composable. It should accept `text`, `icon`, and `onClick`. It must have a solid bright blue background, stadium shape, and a soft drop shadow to match the reference image.
* In the main `Column`, add a large `NeonStyleButton` for "Resume Saved Game".
* Below it, add a `Row` containing two smaller `NeonStyleButton`s for "Settings" and "Leaderboard", separated by spacing. Ensure they share the available width equally.


6. **Step 6: Final Polish.**
* Review vertical spacing between all elements to match the airy feel of the design. ensure the button column at the bottom feels anchored.