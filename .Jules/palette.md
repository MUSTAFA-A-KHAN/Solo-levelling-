## 2024-05-14 - Compose Custom Tabs Accessibility
**Learning:** Custom interactive elements (like segmented tabs) built using generic containers (Row/Box) and simple `.clickable` modifiers are announced as generic clickables by TalkBack (Android Screen Reader), which lacks context.
**Action:** When creating custom tabs in Jetpack Compose, always enhance `.clickable` (or `.selectable`) with explicit semantic metadata, such as `role = Role.Tab` and an `onClickLabel` describing the action, to improve the screen reader experience.
## 2024-05-15 - Improve button semantics for screen readers
**Learning:** Generic layout containers with clickable modifiers (like Box or Row) lack semantic context for screen readers by default.
**Action:** Always include `role = Role.Button` and `onClickLabel` with a clear description when making generic views clickable to improve the experience for TalkBack users.
