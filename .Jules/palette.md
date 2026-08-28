## 2024-05-14 - Compose Custom Tabs Accessibility
**Learning:** Custom interactive elements (like segmented tabs) built using generic containers (Row/Box) and simple `.clickable` modifiers are announced as generic clickables by TalkBack (Android Screen Reader), which lacks context.
**Action:** When creating custom tabs in Jetpack Compose, always enhance `.clickable` (or `.selectable`) with explicit semantic metadata, such as `role = Role.Tab` and an `onClickLabel` describing the action, to improve the screen reader experience.

## 2025-02-18 - Compose Custom Buttons Accessibility
**Learning:** Custom buttons built using generic containers like `Box` or `Row` with simple `.clickable` modifiers are announced as generic unlabelled clickables by TalkBack.
**Action:** Always provide explicit semantic labels and roles directly in the modifier (e.g., `.clickable(onClick = ..., onClickLabel = "Action description", role = Role.Button)`) to ensure screen readers correctly announce these generic containers as interactive buttons and describe their functionality.