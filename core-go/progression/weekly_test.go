package progression

import "testing"

func TestWeeklySteps(t *testing.T) {
	// Use known UTC dates: Monday 2026-08-24 .. Sunday 2026-08-30.
	RecordDailySteps("2026-08-23", 100) // previous Sunday -> excluded
	RecordDailySteps("2026-08-24", 1000)
	RecordDailySteps("2026-08-25", 2000)
	RecordDailySteps("2026-08-26", 3000)
	RecordDailySteps("2026-08-27", 4000)
	RecordDailySteps("2026-08-28", 5000)
	RecordDailySteps("2026-08-29", 6000)
	RecordDailySteps("2026-08-30", 7000)
	RecordDailySteps("2026-08-31", 999) // next Monday -> excluded

	got := WeeklySteps("2026-08-26")
	want := int64(1000 + 2000 + 3000 + 4000 + 5000 + 6000 + 7000)
	if got != want {
		t.Fatalf("WeeklySteps = %d, want %d", got, want)
	}
}
