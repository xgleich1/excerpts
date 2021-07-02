import DoSomethingResult.SomethingFailed
import DoSomethingResult.SomethingSucceeded
import LoadSomethingResult.SomethingMissing
import LoadSomethingResult.SomethingPresent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Example of how I would structure a
 * feature starting at its view model.
 */

class SomethingViewModel(
        private val somethingRepository: SomethingRepository
) : ViewModel() {

    fun onButtonClick() {
        /**
         * The view model does not do any threading
         * decisions other than running everything
         * on main. Since it can only know that
         * the views are to be manipulated in
         * the main thread.
         *
         * Additionally this makes the doSomething()
         * call testable. You only need to swap
         * out Main in the unit-tests using
         * Dispatchers.setMain(Unconfined).
         */
        viewModelScope.launch(Main) {
            val doSomethingResult =
                    somethingRepository.doSomething()

            when(doSomethingResult) {
                is SomethingFailed -> {
                }
                is SomethingSucceeded -> {
                }
            }
        }
    }
}

class SomethingRepository(
        private val somethingService: SomethingService,
        private val somethingPersistence: SomethingPersistence) {

    /**
     * The repository executes operations which are
     * needed to do something. It can do threading
     * decisions, but only when the reason for the
     * decision (for example to do two operations
     * in parallel) are in the repository.
     */
    suspend fun doSomething(): DoSomethingResult {
        val loadSomethingResult =
                somethingService.loadSomething()

        return when(loadSomethingResult) {
            is SomethingPresent -> {
                somethingPersistence
                        .persistSomething(loadSomethingResult.something)

                SomethingSucceeded
            }
            is SomethingMissing -> {
                SomethingFailed(loadSomethingResult.exception)
            }
        }
    }
}

sealed class DoSomethingResult {
    object SomethingSucceeded : DoSomethingResult()

    data class SomethingFailed(val exception: Exception) : DoSomethingResult()
}

class SomethingService {
    /**
     * The service could, for example, create
     * a request and send it to the backend.
     *
     * Threading is decided here, since we
     * know that the request must be send
     * on a background thread.
     *
     * It's always best to keep the threading
     * decision close to its reason. This
     * eliminates questions like
     * "why is this in IO?".
     */
    suspend fun loadSomething(): LoadSomethingResult =
            withContext(IO) {
                SomethingPresent(Something())
            }
}

sealed class LoadSomethingResult {
    data class SomethingPresent(val something: Something) : LoadSomethingResult()

    data class SomethingMissing(val exception: Exception) : LoadSomethingResult()
}

class SomethingPersistence {
    /**
     * The persistence could, for example,
     * save something into a room database.
     */
    suspend fun persistSomething(something: Something) {
        withContext(IO) {
        }
    }
}

/**
 * In reality we use one class each for our
 * business / backend / persistence model
 * and map them to each other accordingly.
 */
class Something
