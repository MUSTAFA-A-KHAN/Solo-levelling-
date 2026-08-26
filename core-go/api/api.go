package api

import (
	"sololeveling/core-go/models"
	"sololeveling/core-go/progression"
)

// This package acts as the bridge for gomobile.
// It exposes flat functions to simplify Kotlin calls.

type PlayerResult struct {
	Level                    int32
	Xp                       int64
	NextLevelXp              int64
	AvailableAttributePoints int32
	RankValue                int32
	RankTitle                string
	Footsteps                int64
	LastSyncTime             int64
	Strength                 float64
	Agility                  float64
	Vitality                 float64
	Intelligence             float64
	Discipline               float64
	Endurance                float64
}

// Map from internal state to return result
func mapStateToResult(state *models.PlayerState) *PlayerResult {
	res := &PlayerResult{
		Level:                    state.Level,
		Xp:                       state.Xp,
		NextLevelXp:              state.NextLevelXp,
		AvailableAttributePoints: state.AvailableAttributePoints,
		RankValue:                state.RankValue,
		RankTitle:                state.RankTitle,
		Footsteps:                state.Footsteps,
		LastSyncTime:             state.LastSyncTime,
	}
	if state.Attributes != nil {
		res.Strength = state.Attributes.Strength
		res.Agility = state.Attributes.Agility
		res.Vitality = state.Attributes.Vitality
		res.Intelligence = state.Attributes.Intelligence
		res.Discipline = state.Attributes.Discipline
		res.Endurance = state.Attributes.Endurance
	}
	return res
}

// CalculateNextLevelXp exposes progression calc.
func CalculateNextLevelXp(level int32) int64 {
	return progression.CalculateNextLevelXp(level)
}

// ProcessHealthData exposes health data processing.
func ProcessHealthData(
	level int32, xp int64, nextLevelXp int64, availablePoints int32,
	rankValue int32, rankTitle string, footsteps int64, lastSyncTime int64,
	strength, agility, vitality, intelligence, discipline, endurance float64,
	steps int64, workoutMinutes int64, syncTime int64,
) *PlayerResult {

	state := &models.PlayerState{
		Level:                    level,
		Xp:                       xp,
		NextLevelXp:              nextLevelXp,
		AvailableAttributePoints: availablePoints,
		RankValue:                rankValue,
		RankTitle:                rankTitle,
		Footsteps:                footsteps,
		LastSyncTime:             lastSyncTime,
		Attributes: &models.PlayerAttributes{
			Strength:     strength,
			Agility:      agility,
			Vitality:     vitality,
			Intelligence: intelligence,
			Discipline:   discipline,
			Endurance:    endurance,
		},
	}

	progression.ProcessHealthData(state, steps, workoutMinutes, syncTime)

	return mapStateToResult(state)
}

// AddXp exposes xp addition.
func AddXp(
	level int32, xp int64, nextLevelXp int64, availablePoints int32,
	rankValue int32, rankTitle string, footsteps int64, lastSyncTime int64,
	strength, agility, vitality, intelligence, discipline, endurance float64,
	xpGained int64,
) *PlayerResult {

	state := &models.PlayerState{
		Level:                    level,
		Xp:                       xp,
		NextLevelXp:              nextLevelXp,
		AvailableAttributePoints: availablePoints,
		RankValue:                rankValue,
		RankTitle:                rankTitle,
		Footsteps:                footsteps,
		LastSyncTime:             lastSyncTime,
		Attributes: &models.PlayerAttributes{
			Strength:     strength,
			Agility:      agility,
			Vitality:     vitality,
			Intelligence: intelligence,
			Discipline:   discipline,
			Endurance:    endurance,
		},
	}

	progression.AddXp(state, xpGained)
    // Evaluate rank again after adding xp
    progression.EvaluateRank(state)

	return mapStateToResult(state)
}

// RecordDailySteps exposes per-day step recording for weekly aggregation.
func RecordDailySteps(date string, steps int64) {
	progression.RecordDailySteps(date, steps)
}

// GetDailySteps exposes the recorded steps for a single UTC date key.
func GetDailySteps(date string) int64 {
	return progression.GetDailySteps(date)
}

// WeeklySteps exposes the ISO-week (Mon–Sun) footsteps total for the given
// UTC date key ("2006-01-02").
func WeeklySteps(refDate string) int64 {
	return progression.WeeklySteps(refDate)
}

// WeeklyStepsFromTime exposes the ISO-week footsteps total for the week
// containing the given unix timestamp.
func WeeklyStepsFromTime(syncTime int64) int64 {
	return progression.WeeklyStepsFromTime(syncTime)
}

// EvaluateRank exposes rank evaluation.
func EvaluateRank(
	level int32, xp int64, nextLevelXp int64, availablePoints int32,
	rankValue int32, rankTitle string, footsteps int64, lastSyncTime int64,
	strength, agility, vitality, intelligence, discipline, endurance float64,
) *PlayerResult {

	state := &models.PlayerState{
		Level:                    level,
		Xp:                       xp,
		NextLevelXp:              nextLevelXp,
		AvailableAttributePoints: availablePoints,
		RankValue:                rankValue,
		RankTitle:                rankTitle,
		Footsteps:                footsteps,
		LastSyncTime:             lastSyncTime,
		Attributes: &models.PlayerAttributes{
			Strength:     strength,
			Agility:      agility,
			Vitality:     vitality,
			Intelligence: intelligence,
			Discipline:   discipline,
			Endurance:    endurance,
		},
	}

	progression.EvaluateRank(state)

	return mapStateToResult(state)
}
