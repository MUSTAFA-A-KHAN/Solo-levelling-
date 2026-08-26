package progression

import (
	"math"
	"sololeveling/core-go/models"
)

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
