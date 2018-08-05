package ca.nick.mobius4

import com.spotify.mobius.Effects
import com.spotify.mobius.Next
import com.spotify.mobius.Update

class Update : Update<Model, Event, Effect> {

    override fun update(
        model: Model,
        event: Event
    ): Next<Model, Effect> = when (event) {
        is Increment -> Next.next(model.copy(count = model.count + 1, isLoading = true), Effects.effects(ShortDelay))
        is Decrement -> Next.next(model.copy(count = model.count - 1, isLoading = true), Effects.effects(ShortDelay))
        is DoneLoading -> Next.next(model.copy(isLoading = false))
    }
}