## 2024-05-19 - Holographic ProgressBar Accessibility
**Learning:** Custom Canvas drawings are inherently invisible to screen readers. For our custom progress bars to be useful to all players, they need semantic meaning attached to them using `ProgressBarRangeInfo`.
**Action:** Always use `Modifier.semantics { progressBarRangeInfo = ... }` on any custom Canvas component that visually indicates progress within the System.
