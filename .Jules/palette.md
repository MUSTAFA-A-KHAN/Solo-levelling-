## 2024-05-14 - Compose Custom Tabs Accessibility
**Learning:** Custom interactive elements (like segmented tabs) built using generic containers (Row/Box) and simple `.clickable` modifiers are announced as generic clickables by TalkBack (Android Screen Reader), which lacks context.
**Action:** When creating custom tabs in Jetpack Compose, always enhance `.clickable` (or `.selectable`) with explicit semantic metadata, such as `role = Role.Tab` and an `onClickLabel` describing the action, to improve the screen reader experience.
## 2024-05-15 - Improve button semantics for screen readers
**Learning:** Generic layout containers with clickable modifiers (like Box or Row) lack semantic context for screen readers by default.
**Action:** Always include `role = Role.Button` and `onClickLabel` with a clear description when making generic views clickable to improve the experience for TalkBack users.

## 2025-02-18 - Compose Custom Buttons Accessibility
**Learning:** Custom buttons built using generic containers like `Box` or `Row` with simple `.clickable` modifiers are announced as generic unlabelled clickables by TalkBack.
**Action:** Always provide explicit semantic labels and roles directly in the modifier (e.g., `.clickable(onClick = ..., onClickLabel = "Action description", role = Role.Button)`) to ensure screen readers correctly announce these generic containers as interactive buttons and describe their functionality.

## 2025-03-02 - Custom Segmented Control Selection and Color Animations
**Learning:** Replacing `.clickable` with `.selectable` on custom tab containers ensures screen readers accurately announce `selected` status and tab role without custom semantics hacking, while `animateColorAsState` provides smooth visual feedback during state transitions.
**Action:** For custom segmented controls, combine `.selectable(selected = isSelected, role = Role.Tab, onClick = ...)` with `animateColorAsState` for background and text colors to deliver responsive UI feedback and accessible screen reader interaction.

## 2026-09-18 - Tactile Press Scale Feedback with Accessible Semantics
**Learning:** Wrapping custom interactive containers with `graphicsLayer { scaleX = scale; scaleY = scale }` animated via `interactionSource.collectIsPressedAsState()` provides responsive, tactile press feedback without triggering expensive recompositions or layout passes.
**Action:** For interactive custom cards or containers, pair spring-animated scale feedback (`0.96f` on press) with `.clickable(interactionSource = ..., indication = null, role = Role.Button, onClickLabel = ...)` for both tactile visual response and TalkBack screen reader accessibility.
