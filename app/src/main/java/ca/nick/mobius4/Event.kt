package ca.nick.mobius4

// Events are rendered in the UI
sealed class Event
object Increment : Event()
object Decrement : Event()
data class DoneCalculating(val newNumber: Int) : Event()