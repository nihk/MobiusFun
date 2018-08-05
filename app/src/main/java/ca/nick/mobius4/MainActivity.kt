package ca.nick.mobius4

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding2.view.RxView
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
        const val KEY_IS_LOADING: String = "key_is_loading"
    }

    val effectsHandler: ObservableTransformer<Effect, Event> =
        RxMobius.subtypeEffectHandler<Effect, Event>()
            .addTransformer(ShortDelay::class.java, ::startLoading)
            .build()

    val loopFactory: MobiusLoop.Factory<Model, Event, Effect> =
        RxMobius.loop(Update(), effectsHandler)
            .logger(AndroidLogger<Model, Event, Effect>(TAG))

    val controller: MobiusLoop.Controller<Model, Event> =
        MobiusAndroid.controller(loopFactory, Model())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        controller.connect(RxConnectables.fromTransformer(::connectViews))

        savedInstanceState?.let {
            val count: Int = it.getInt(KEY_COUNT)
            val isLoading: Boolean = it.getBoolean(KEY_IS_LOADING)
            controller.replaceModel(Model(count, isLoading))
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
            outState.putInt(KEY_COUNT, count)
            outState.putBoolean(KEY_IS_LOADING, isLoading)
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
            .doOnDispose(disposable::dispose)
    }

    fun render(model: Model) {
        model.apply {
            renderLoading(isLoading)
            renderCounterText(model.count)
        }
    }

    fun renderLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
        counter.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        increment.isEnabled = !isLoading
        decrement.isEnabled = !isLoading
    }

    fun renderCounterText(value: Int) {
        counter.text = value.toString()
    }

    fun startLoading(startLoading: Observable<ShortDelay>): Observable<Event> {
        return startLoading
            .delay(1, TimeUnit.SECONDS)
            .map { DoneLoading }
    }
}
