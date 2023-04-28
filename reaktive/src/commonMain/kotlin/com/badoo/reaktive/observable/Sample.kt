package com.badoo.reaktive.observable

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.plusAssign
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.atomic.getAndChange
import kotlin.time.Duration

/**
 * Returns an [Observable] that emits the most recently emitted element (if any)
 * emitted by the source [Observable] within periodic time intervals.
 *
 * Please refer to the corresponding RxJava [document](http://reactivex.io/RxJava/javadoc/io/reactivex/Observable.html#sample-long-java.util.concurrent.TimeUnit-io.reactivex.Scheduler-).
 */
fun <T> Observable<T>.sample(window: Duration, scheduler: Scheduler): Observable<T> =
    observable { emitter ->
        val disposables = CompositeDisposable()
        emitter.setDisposable(disposables)
        val executor = scheduler.newExecutor()
        disposables += executor

        subscribe(
            object : ObservableObserver<T> {
                private val lastValue = AtomicReference<SampleLastValue<T>?>(null)

                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable

                    executor.submit(delay = window, period = window) {
                        lastValue.getAndChange { null }?.also {
                            emitter.onNext(it.value)
                        }
                    }
                }

                override fun onNext(value: T) {
                    lastValue.value = SampleLastValue(value)
                }

                override fun onComplete() {
                    executor.cancel()
                    executor.submit(task = emitter::onComplete)
                }

                override fun onError(error: Throwable) {
                    executor.cancel()
                    executor.submit { emitter.onError(error) }
                }
            }
        )
    }

private class SampleLastValue<out T>(
    val value: T
)
