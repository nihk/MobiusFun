package ca.nick.mobius4

import com.spotify.mobius.Effects
import com.spotify.mobius.Next
import com.spotify.mobius.Update

// The Update function performans any combination of updating the Model, triggering Effects, and doing nothing
// Model updates here will be rendered in the UI host
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