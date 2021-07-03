import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Instrumentation.ActivityResult
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not

/**
 * The following rules are designed ease intent stubbing and verification in
 * ui-tests. On top of this they help with isolating ui-tests from each other.
 */

/**
 * This rules allows stubbing and verification of intents fired
 * in onCreate/onStart/onResume. Espresso's IntentsTestRule
 * calls Intents.init() too late to make this possible.
 */
open class InitIntentsBeforeActivityLaunchedTestRule<T : Activity>
    : ActivityTestRule<T> {
    
    constructor(activityClass: Class<T>)
            : super(activityClass)

    constructor(activityClass: Class<T>, initialTouchMode: Boolean)
            : super(activityClass, initialTouchMode)

    constructor(activityClass: Class<T>, initialTouchMode: Boolean, launchActivity: Boolean)
            : super(activityClass, initialTouchMode, launchActivity)

    override fun beforeActivityLaunched() {
        try {
            Intents.init()
        } catch(e: Exception) {
            /**
             * When a test fails Intents.init() might be called twice,
             * which results in an exception. This happens because
             * the failing test does not call Intents.release().
             */
        }

        super.beforeActivityLaunched()
    }

    override fun afterActivityFinished() {
        super.afterActivityFinished()

        Intents.release()
    }
}

/**
 * This rule stubs all intents pointing to activities inside the
 * app. In contrast to intents that open, for example, the browser.
 */
open class StubInternalIntentsTestRule<T : Activity>
    : InitIntentsBeforeActivityLaunchedTestRule<T> {

    private val activityClass: Class<T>


    constructor(activityClass: Class<T>)
            : super(activityClass) {

        this.activityClass = activityClass
    }

    constructor(activityClass: Class<T>, initialTouchMode: Boolean)
            : super(activityClass, initialTouchMode) {

        this.activityClass = activityClass
    }

    constructor(activityClass: Class<T>, initialTouchMode: Boolean, launchActivity: Boolean)
            : super(activityClass, initialTouchMode, launchActivity) {

        this.activityClass = activityClass
    }

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()

        intending(allOf(isInternal(), not(hasComponent(activityClass.name))))
                .respondWith(ActivityResult(RESULT_OK, null))
    }
}

/**
 * This rule stubs all intents pointing to a different app, for example the
 * browser. In contrast to intents that point to an activity inside the app.
 */
open class StubExternalIntentsTestRule<T : Activity>
    : InitIntentsBeforeActivityLaunchedTestRule<T> {

    constructor(activityClass: Class<T>)
            : super(activityClass)

    constructor(activityClass: Class<T>, initialTouchMode: Boolean)
            : super(activityClass, initialTouchMode)

    constructor(activityClass: Class<T>, initialTouchMode: Boolean, launchActivity: Boolean)
            : super(activityClass, initialTouchMode, launchActivity)

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()

        intending(not(isInternal())).respondWith(ActivityResult(RESULT_OK, null))
    }
}
