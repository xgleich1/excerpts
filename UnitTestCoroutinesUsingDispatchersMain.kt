import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Remove the concurrency when unit-testing code
 * which uses a coroutine in Dispatchers.Main.
 */

class MainDispatcherRule : TestWatcher() {
    @ExperimentalCoroutinesApi
    override fun starting(description: Description) = Dispatchers.setMain(Unconfined)

    @ExperimentalCoroutinesApi
    override fun finished(description: Description) = Dispatchers.resetMain()
}

/**
 * Adding the rule is enough to
 * start writing unit-tests for
 * code using Dispatchers.Main.
 */
@get:Rule
val mainDispatcherRule = MainDispatcherRule()