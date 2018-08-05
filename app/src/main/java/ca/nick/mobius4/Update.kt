package ca.nick.mobius4

import com.spotify.mobius.Effects
import com.spotify.mobius.Next
import com.spotify.mobius.Update

// Updates the model based on Events and optionally triggers Effects
class Update : Update<Model, Event, Effect> {

    override fun update(
        model: Model,
        event: Event
    ): Next<Model, Effect> =
        when (event) {
            is Increment -> Next.next(
                model.copy(isCalculating = true),
                Effects.effects(PerformCalculation(model.number, 1))
            )

            is Decrement -> Next.next(
                model.copy(isCalculating = true),
                Effects.effects(PerformCalculation(model.number, -1))
            )

            is DoneCalculating -> Next.next(
                model.copy(
                    number = event.newNumber,
                    isCalculating = false
                )
            )
        }
}