package ca.nick.mobius4

// Effects are optional consequences of Events
// Effects can generate Events
sealed class Effect
data class PerformCalculation(val current: Int, val add: Int) : Effect()