package ca.nick.mobius4

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import com.spotify.mobius.Connectable
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.AndroidLogger
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.rx2.RxConnectables
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
        const val KEY_COUNT: String = "key_count"
        const val KEY_IS_CALCULATING: String = "key_is_calculating"
    }

    val effectsHandler: ObservableTransformer<Effect, Event> =
        RxMobius.subtypeEffectHandler<Effect, Event>()
            .addTransformer(PerformCalculation::class.java) { performCalculation(it) }
            .build()

    val loopFactory: MobiusLoop.Factory<Model, Event, Effect> =
        RxMobius.loop(Update(), effectsHandler)
            .logger(AndroidLogger<Model, Event, Effect>(TAG))

    val controller: MobiusLoop.Controller<Model, Event> =
        MobiusAndroid.controller(loopFactory, Model())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val connectable: Connectable<Model, Event> = RxConnectables.fromTransformer { connectViews(it) }
        controller.connect(connectable)

        savedInstanceState?.let {
            val count: Int = it.getInt(KEY_COUNT)
            val isCalculating: Boolean = it.getBoolean(KEY_IS_CALCULATING)
            controller.replaceModel(Model(count, isCalculating))
        }
    }

    override fun onResume() {
        super.onResume()
        controller.start()
    }

    override fun onPause() {
        super.onPause()
        controller.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.disconnect()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.model.apply {
            outState.putInt(KEY_COUNT, number)
            outState.putBoolean(KEY_IS_CALCULATING, isCalculating)
        }
    }

    fun connectViews(models: Observable<Model>): Observable<Event> {
        val disposable: Disposable = models
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { render(it) }

        val increment: Observable<Event> = RxView.clicks(increment)
            .map { Increment as Event }
        val decrement: Observable<Event> = RxView.clicks(decrement)
            .map { Decrement as Event }

        return Observable.merge(increment, decrement)
            .doOnDispose { disposable.dispose() }
    }

    fun render(model: Model) {
        model.apply {
            renderCalculating(isCalculating)
            renderCounterText(model.number)
        }
    }

    fun renderCalculating(isCalculating: Boolean) {
        progressBar.visibility = if (isCalculating) View.VISIBLE else View.INVISIBLE
        counter.visibility = if (isCalculating) View.INVISIBLE else View.VISIBLE
        increment.isEnabled = !isCalculating
        decrement.isEnabled = !isCalculating
    }

    fun renderCounterText(value: Int) {
        counter.text = value.toString()
    }

    fun performCalculation(performCalculation: Observable<PerformCalculation>): Observable<Event> {
        return performCalculation
            .delay(1, TimeUnit.SECONDS)
            .map { it.current + it.add }
            .map { DoneCalculating(newNumber = it) }
    }
}
