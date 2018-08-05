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

    val effectsHandler: ObservableTransformer<CounterEffect, CounterEvent> =
        RxMobius.subtypeEffectHandler<CounterEffect, CounterEvent>()
            .addTransformer(StartLoading::class.java, ::startLoading)
            .build()

    val loopFactory: MobiusLoop.Factory<CounterModel, CounterEvent, CounterEffect> =
        RxMobius.loop(CounterUpdate(), effectsHandler)
            .logger(AndroidLogger<CounterModel, CounterEvent, CounterEffect>(TAG))

    val controller: MobiusLoop.Controller<CounterModel, CounterEvent> =
        MobiusAndroid.controller(loopFactory, CounterModel())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        controller.connect(RxConnectables.fromTransformer(::connectViews))

        savedInstanceState?.let {
            val count: Int = it.getInt(KEY_COUNT)
            val isLoading: Boolean = it.getBoolean(KEY_IS_LOADING)
            controller.replaceModel(CounterModel(count, isLoading))
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

    fun connectViews(models: Observable<CounterModel>): Observable<CounterEvent> {
        val disposable: Disposable = models
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { render(it) }

        val increment: Observable<CounterEvent> = RxView.clicks(increment)
            .map { Increment }
        val decrement: Observable<CounterEvent> = RxView.clicks(decrement)
            .map { Decrement }

        return Observable.merge(increment, decrement)
            .doOnDispose(disposable::dispose)
    }

    fun render(model: CounterModel) {
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

    fun startLoading(startLoading: Observable<StartLoading>): Observable<CounterEvent> {
        return startLoading
            .delay(1, TimeUnit.SECONDS)
            .map { DoneLoading }
    }
}
