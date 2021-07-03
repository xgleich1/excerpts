package com.dg.eqs.util.rules

import org.junit.Test

/**
 * Convenience function which covers use cases
 * where you want another class to implement
 * a given interface. A specific use case could
 * be when your fragment requires the activity
 * to implement a callback or dependency provider
 * interface. There are better ways to model this
 * fragment-activity relationship though.
 */

inline fun <reified T> Any?.enforceContract() = this as? T
        ?: throw ClassCastException("Please implement ${T::class.java.simpleName}")

/**
 * Testing the logic to prevent future problems
 * when the enforceContract function has to change.
 */
class EnforceContractTest {
    @Test
    fun `Should not throw an exception when a class implements the given contract`() {
        ClassWithContract().enforceContract<Contract>()
    }

    @Test(expected = ClassCastException::class)
    fun `Should throw an exception when a class doesn't implement the given contract`() {
        ClassWithoutContract().enforceContract<Contract>()
    }

    @Test(expected = ClassCastException::class)
    fun `Should throw an exception when there's nothing to implement the given contract`() {
        // GIVEN
        val classWithContract: ClassWithContract? = null

        // WHEN
        classWithContract.enforceContract<Contract>()
    }

    private interface Contract

    private class ClassWithContract : Contract

    private class ClassWithoutContract
}