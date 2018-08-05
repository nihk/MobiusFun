package ca.nick.mobius4

// Events trigger the Update function
sealed class Event
object Increment : Event()
object Decrement : Event()
data class DoneCalculating(val newNumber: Int) : Event()