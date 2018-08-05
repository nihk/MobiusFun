package ca.nick.mobius4

sealed class CounterEvent
object Increment : CounterEvent()
object Decrement : CounterEvent()
object DoneLoading : CounterEvent()