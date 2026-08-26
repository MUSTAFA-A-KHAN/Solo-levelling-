package models

// We use basic types for gomobile compatibility

type PlayerAttributes struct {
	Strength     float64
	Agility      float64
	Vitality     float64
	Intelligence float64
	Discipline   float64
	Endurance    float64
}

type PlayerState struct {
	Level                    int32
	Xp                       int64
	NextLevelXp              int64
	AvailableAttributePoints int32
	RankValue                int32
	RankTitle                string
	Footsteps                int64
	LastSyncTime             int64

	// Flatten attributes to make it easier for gomobile if needed, or keep struct
	Attributes *PlayerAttributes
}

// Ranks
const (
	RankE_Value = 1
	RankE_Title = "E-Rank"
	RankD_Value = 2
	RankD_Title = "D-Rank"
	RankC_Value = 3
	RankC_Title = "C-Rank"
	RankB_Value = 4
	RankB_Title = "B-Rank"
	RankA_Value = 5
	RankA_Title = "A-Rank"
	RankS_Value = 6
	RankS_Title = "S-Rank"
)
