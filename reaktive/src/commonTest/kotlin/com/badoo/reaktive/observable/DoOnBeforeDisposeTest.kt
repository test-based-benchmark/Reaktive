package com.badoo.reaktive.observable

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.test.base.assertDisposed
import com.badoo.reaktive.test.mockUncaughtExceptionHandler
import com.badoo.reaktive.test.observable.TestObservable
import com.badoo.reaktive.test.observable.test
import com.badoo.reaktive.utils.resetReaktiveUncaughtErrorHandler
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DoOnBeforeDisposeTest :
    ObservableToObservableTests by ObservableToObservableTestsImpl({ doOnBeforeDispose {} }),
    ObservableToObservableForwardTests by ObservableToObservableForwardTestsImpl({ doOnBeforeDispose {} }) {

    private val upstream = TestObservable<Int>()

    @AfterTest
    fun after() {
        resetReaktiveUncaughtErrorHandler()
    }

    @Test
    fun calls_action_before_disposing_upstream() {
        val callOrder = ArrayList<String>()

        observableUnsafe<Nothing> { observer ->
            observer.onSubscribe(
                Disposable {
                    callOrder += "dispose"
                }
            )
        }
            .doOnBeforeDispose {
                callOrder += "action"
            }
            .test()
            .dispose()

        assertEquals(listOf("action", "dispose"), callOrder)
    }

    @Test
    fun calls_action_WHEN_disposed_before_upstream_onSubscribe() {
        var isCalled = false

        observableUnsafe<Nothing> {}
            .doOnBeforeDispose { isCalled = true }
            .test()
            .dispose()

        assertTrue(isCalled)
    }

    @Test
    fun does_not_call_action_WHEN_upstream_emitted_value() {
        var isCalled = false

        upstream
            .doOnBeforeDispose { isCalled = true }
            .test()

        upstream.onNext(0)

        assertFalse(isCalled)
    }

    @Test
    fun does_not_call_action_WHEN_upstream_completed() {
        var isCalled = false

        upstream
            .doOnBeforeDispose { isCalled = true }
            .test()

        upstream.onComplete()

        assertFalse(isCalled)
    }

    @Test
    fun does_not_call_action_WHEN_upstream_produced_error() {
        var isCalled = false

        upstream
            .doOnBeforeDispose { isCalled = true }
            .test()

        upstream.onError(Throwable())

        assertFalse(isCalled)
    }

    @Test
    fun calls_uncaught_exception_handler_WHEN_downstream_disposed_and_exception_in_lambda() {
        val caughtException = mockUncaughtExceptionHandler()
        val error = Exception()

        val observer =
            upstream
                .doOnBeforeDispose { throw error }
                .test()

        observer.dispose()

        assertSame(error, caughtException.value)
    }

    @Test
    fun disposes_upstream_WHEN_downstream_disposed_and_exception_in_lambda() {
        mockUncaughtExceptionHandler()
        val error = Exception()

        val observer =
            upstream
                .doOnBeforeDispose { throw error }
                .test()

        observer.dispose()

        observer.assertDisposed()
    }
}
