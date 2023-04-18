package com.badoo.reaktive.utils.atomic

import com.badoo.reaktive.utils.InternalReaktiveApi

@InternalReaktiveApi
actual class AtomicReference<T> actual constructor(initialValue: T) {

    private val delegate = java.util.concurrent.atomic.AtomicReference<T>(initialValue)

    actual var value: T
        get() = delegate.get()
        set(value) {
            delegate.set(value)
        }

    actual fun compareAndSet(expectedValue: T, newValue: T): Boolean = delegate.compareAndSet(expectedValue, newValue)
}
