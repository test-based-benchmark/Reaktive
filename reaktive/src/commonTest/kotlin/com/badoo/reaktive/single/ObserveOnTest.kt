package com.badoo.reaktive.single

import com.badoo.reaktive.test.base.hasSubscribers
import com.badoo.reaktive.test.scheduler.TestScheduler
import com.badoo.reaktive.test.single.TestSingle
import com.badoo.reaktive.test.single.isError
import com.badoo.reaktive.test.single.isSuccess
import com.badoo.reaktive.test.single.test
import com.badoo.reaktive.test.single.value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveOnTest : SingleToSingleTests by SingleToSingleTests<Unit>({ observeOn(TestScheduler()) }) {

    private val scheduler = TestScheduler(isManualProcessing = true)
    private val upstream = TestSingle<Int>()
    private val observer = upstream.observeOn(scheduler).test()

    @Test
    fun subscribes_synchronously() {
        assertTrue(upstream.hasSubscribers)
    }

    @Test
    fun does_no_succeed_synchronously() {
        upstream.onSuccess(0)

        assertFalse(observer.isSuccess)
    }

    @Test
    fun succeeds_through_scheduler() {
        upstream.onSuccess(0)
        scheduler.process()

        assertEquals(0, observer.value)
    }

    @Test
    fun does_not_error_synchronously() {
        upstream.onError(Throwable())

        assertFalse(observer.isError)
    }

    @Test
    fun errors_through_scheduler() {
        val error = Throwable()
        upstream.onError(error)
        scheduler.process()

        assertTrue(observer.isError(error))
    }

    @Test
    fun disposes_executor_WHEN_disposed() {
        observer.dispose()

        assertTrue(scheduler.executors.all(TestScheduler.Executor::isDisposed))
    }
}