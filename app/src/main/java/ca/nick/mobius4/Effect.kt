package ca.nick.mobius4

// Effects are optional consequences of Events via the Update function
// Effects can trigger Events
sealed class Effect
data class PerformCalculation(val current: Int, val addAmount: Int) : Effect()