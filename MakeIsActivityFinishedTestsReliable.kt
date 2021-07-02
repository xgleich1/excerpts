import androidx.test.rule.ActivityTestRule
import org.assertj.core.api.Assertions.assertThat

/**
 * Asserting that a activity finished proves to
 * be unreliable (especially in a ci-pipeline).
 *
 * The assert functions solve this problem
 * by using the waitFor retry mechanism.
 * (See UiTestHardeningWithWaitForFunctions)
 *
 * The functions are currently written for ui-tests
 * using ActivityTestRule instead of ActivityScenario.
 */

fun assertActivityFinished(rule: ActivityTestRule<*>) =
        waitForCondition { rule.activity.isFinishing }

fun assertActivityFinishedWithResult(rule: ActivityTestRule<*>, resultCode: Int) {
    waitForAssert {
        assertThat(rule.activityResult.resultCode).isEqualTo(resultCode)
    }

    assertActivityFinished(rule)
}