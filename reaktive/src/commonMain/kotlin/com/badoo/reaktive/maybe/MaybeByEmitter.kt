package com.badoo.reaktive.maybe

import com.badoo.reaktive.base.tryCatch
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.SerialDisposable

/**
 * Creates a [Maybe] with manual signalling via [MaybeEmitter].
 *
 * Please refer to the corresponding RxJava [document](http://reactivex.io/RxJava/javadoc/io/reactivex/Maybe.html#create-io.reactivex.MaybeOnSubscribe-).
 */
inline fun <T> maybe(crossinline onSubscribe: (emitter: MaybeEmitter<T>) -> Unit): Maybe<T> =
    maybeUnsafe { observer ->
        val emitter = onSubscribeMaybe(observer)
        emitter.tryCatch { onSubscribe(emitter) }
    }

@PublishedApi
internal fun <T> onSubscribeMaybe(observer: MaybeObserver<T>): MaybeEmitter<T> =
    object : SerialDisposable(), MaybeEmitter<T> {
        override fun setDisposable(disposable: Disposable?) {
            set(disposable)
        }

        override fun onSuccess(value: T) {
            doIfNotDisposedAndDispose {
                observer.onSuccess(value)
            }
        }

        override fun onComplete() {
            doIfNotDisposedAndDispose(observer::onComplete)
        }

        override fun onError(error: Throwable) {
            doIfNotDisposedAndDispose {
                observer.onError(error)
            }
        }

        private inline fun doIfNotDisposedAndDispose(block: () -> Unit) {
            if (!isDisposed) {
                val disposable: Disposable? = clearAndDispose()
                block()
                disposable?.dispose()
            }
        }
    }.also {
        observer.onSubscribe(it)
    }
