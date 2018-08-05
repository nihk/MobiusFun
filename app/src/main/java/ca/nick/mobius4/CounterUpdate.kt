package ca.nick.mobius4

import com.spotify.mobius.Effects
import com.spotify.mobius.Next
import com.spotify.mobius.Update

class CounterUpdate : Update<CounterModel, CounterEvent, CounterEffect> {

    override fun update(
        model: CounterModel,
        event: CounterEvent
    ): Next<CounterModel, CounterEffect> = when (event) {
        is Increment -> Next.next(model.copy(count = model.count + 1, isLoading = true), Effects.effects(StartLoading))
        is Decrement -> Next.next(model.copy(count = model.count - 1, isLoading = true), Effects.effects(StartLoading))
        is DoneLoading -> Next.next(model.copy(isLoading = false))
    }
}