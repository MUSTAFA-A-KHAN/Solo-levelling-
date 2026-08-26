package progression

import (
	"math"
	"sync"
	"time"

	"sololeveling/core-go/models"
)

// dailySteps records footsteps keyed by calendar date (UTC "2006-01-02").
// It is an in-memory store used to derive weekly step counts.
var (
	dailyStepsMu sync.Mutex
	dailySteps   = make(map[string]int64)
)

// dateKey returns the UTC calendar date key for a unix timestamp.
func dateKey(unix int64) string {
	return time.Unix(unix, 0).UTC().Format("2006-01-02")
}

// RecordDailySteps adds steps to the given UTC date key (format "2006-01-02").
// It is safe for concurrent use.
func RecordDailySteps(date string, steps int64) {
	if steps <= 0 {
		return
	}
	dailyStepsMu.Lock()
	dailySteps[date] += steps
	dailyStepsMu.Unlock()
}

// SetDailySteps overwrites the steps recorded for the given UTC date key
// (format "2006-01-02") with the absolute daily total. It is safe for
// concurrent use and is idempotent, so it will not double-count when called
// repeatedly with the same day's total.
func SetDailySteps(date string, steps int64) {
	dailyStepsMu.Lock()
	if steps <= 0 {
		dailySteps[date] = 0
	} else {
		dailySteps[date] = steps
	}
	dailyStepsMu.Unlock()
}

// GetDailySteps returns the recorded steps for a given UTC date key.
func GetDailySteps(date string) int64 {
	dailyStepsMu.Lock()
	defer dailyStepsMu.Unlock()
	return dailySteps[date]
}

// WeeklySteps returns the total footsteps for the ISO week (Mon–Sun) that
// contains refDate. refDate must be a UTC date key ("2006-01-02").
func WeeklySteps(refDate string) int64 {
	t, err := time.Parse("2006-01-02", refDate)
	if err != nil {
		return 0
	}
	// Find Monday of the week (Go: Monday = 1).
	offset := int(t.Weekday()-time.Monday)
	if offset < 0 {
		offset += 7
	}
	start := t.AddDate(0, 0, -offset)

	var total int64
	dailyStepsMu.Lock()
	for i := 0; i < 7; i++ {
		d := start.AddDate(0, 0, i).Format("2006-01-02")
		total += dailySteps[d]
	}
	dailyStepsMu.Unlock()
	return total
}

// WeeklyStepsFromTime returns the weekly footsteps for the ISO week containing
// the given unix timestamp.
func WeeklyStepsFromTime(unix int64) int64 {
	return WeeklySteps(dateKey(unix))
}

// CalculateNextLevelXp calculates XP needed for the next level.
func CalculateNextLevelXp(level int32) int64 {
	// Curve: Base 100, increases non-linearly
	return int64(100 * math.Pow(1.2, float64(level-1)))
}

// AddXp adds XP to a player state and handles level ups.
func AddXp(state *models.PlayerState, xpGained int64) {
	state.Xp += xpGained

	for state.Xp >= state.NextLevelXp {
		state.Xp -= state.NextLevelXp
		state.Level++
		state.AvailableAttributePoints += 3 // 3 points per level
		state.NextLevelXp = CalculateNextLevelXp(state.Level)
	}
}

// EvaluateRank determines the rank based on level and attributes.
func EvaluateRank(state *models.PlayerState) {
	var totalAttributes float64
	if state.Attributes != nil {
		totalAttributes = state.Attributes.Strength + state.Attributes.Agility +
			state.Attributes.Vitality + state.Attributes.Intelligence +
			state.Attributes.Discipline + state.Attributes.Endurance
	}

	if state.Level >= 100 && totalAttributes >= 2000 {
		state.RankValue = models.RankS_Value
		state.RankTitle = models.RankS_Title
	} else if state.Level >= 80 && totalAttributes >= 1200 {
		state.RankValue = models.RankA_Value
		state.RankTitle = models.RankA_Title
	} else if state.Level >= 60 && totalAttributes >= 700 {
		state.RankValue = models.RankB_Value
		state.RankTitle = models.RankB_Title
	} else if state.Level >= 40 && totalAttributes >= 400 {
		state.RankValue = models.RankC_Value
		state.RankTitle = models.RankC_Title
	} else if state.Level >= 20 && totalAttributes >= 200 {
		state.RankValue = models.RankD_Value
		state.RankTitle = models.RankD_Title
	} else {
		state.RankValue = models.RankE_Value
		state.RankTitle = models.RankE_Title
	}
}

// ProcessHealthData converts health data into XP and attributes.
func ProcessHealthData(state *models.PlayerState, steps int64, workoutMinutes int64, syncTime int64) {
	if steps <= 0 && workoutMinutes <= 0 {
		state.LastSyncTime = syncTime
		return
	}

	xpFromSteps := steps / 100
	xpFromWorkouts := workoutMinutes * 10
	totalXpGained := xpFromSteps + xpFromWorkouts

	agilityGain := float64(steps/100) * 0.01
	enduranceGain := float64(steps/100) * 0.01
	strengthGain := float64(workoutMinutes) * 0.1
	vitalityGain := float64(workoutMinutes) * 0.1

	if state.Attributes == nil {
		state.Attributes = &models.PlayerAttributes{
			Strength:     10.0,
			Agility:      10.0,
			Vitality:     10.0,
			Intelligence: 10.0,
			Discipline:   10.0,
			Endurance:    10.0,
		}
	}

	state.Attributes.Agility += agilityGain
	state.Attributes.Endurance += enduranceGain
	state.Attributes.Strength += strengthGain
	state.Attributes.Vitality += vitalityGain

	AddXp(state, totalXpGained)
	state.Footsteps += steps
	state.LastSyncTime = syncTime

	EvaluateRank(state)
}
