package com.dg.eqs

import android.content.Intent
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.intent.Intents.intended
import org.assertj.core.api.Assert
import org.hamcrest.Matcher
import java.lang.Thread.sleep

/**
 * The waitFor functions are meant to harden ui tests
 * against flakiness. This is achieved by retrying a
 * assert/condition/intent matcher/view interaction
 * of your choosing. Only to be used when there is
 * really no other way to make the test reliable.
 */

private const val DEFAULT_TIMEOUT_IN_MS = 5000L
private const val DEFAULT_INTERVAL_IN_MS = 250L


fun waitForAssert(
        timeoutInMs: Long = DEFAULT_TIMEOUT_IN_MS,
        intervalInMs: Long = DEFAULT_INTERVAL_IN_MS,
        assert: () -> Assert<*, *>) {

    var elapsedTime = 0L

    var throwable = attempt { assert() }

    while(throwable != null) {
        if(elapsedTime > timeoutInMs) {
            throw AssertionError("Assert not successful " +
                    "during ${elapsedTime / 1000L} seconds", throwable)
        }

        sleep(intervalInMs)

        elapsedTime += intervalInMs

        throwable = attempt { assert() }
    }
}

fun waitForCondition(
        timeoutInMs: Long = DEFAULT_TIMEOUT_IN_MS,
        intervalInMs: Long = DEFAULT_INTERVAL_IN_MS,
        condition: () -> Boolean) {

    var elapsedTime = 0L

    while(!condition()) {
        if(elapsedTime > timeoutInMs) {
            throw AssertionError("Condition not met " +
                    "during ${elapsedTime / 1000L} seconds")
        }

        sleep(intervalInMs)

        elapsedTime += intervalInMs
    }
}

fun waitForIntent(
        intentMatcher: Matcher<Intent>,
        timeoutInMs: Long = DEFAULT_TIMEOUT_IN_MS,
        intervalInMs: Long = DEFAULT_INTERVAL_IN_MS) {

    var elapsedTime = 0L

    var throwable = attempt { intended(intentMatcher) }

    while(throwable != null) {
        if(elapsedTime > timeoutInMs) {
            throw AssertionError("Intent not matched " +
                    "during ${elapsedTime / 1000L} seconds", throwable)
        }

        sleep(intervalInMs)

        elapsedTime += intervalInMs

        throwable = attempt { intended(intentMatcher) }
    }
}

fun waitForViewInteraction(
        timeoutInMs: Long = DEFAULT_TIMEOUT_IN_MS,
        intervalInMs: Long = DEFAULT_INTERVAL_IN_MS,
        viewInteraction: () -> ViewInteraction) {

    var elapsedTime = 0L

    var throwable = attempt { viewInteraction() }

    while(throwable != null) {
        if(elapsedTime > timeoutInMs) {
            throw AssertionError("View interaction not successful " +
                    "during ${elapsedTime / 1000L} seconds", throwable)
        }

        sleep(intervalInMs)

        elapsedTime += intervalInMs

        throwable = attempt { viewInteraction() }
    }
}

private fun attempt(assertion: () -> Unit) = try {
    assertion()

    null
} catch(throwable: Throwable) {
    throwable
}