package ca.nick.mobius4

// Effects are optional consequences of Events
// Effects can generate Events
sealed class Effect
object ShortDelay : Effect()