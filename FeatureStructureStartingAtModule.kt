import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dg.eqs.R
import com.dg.eqs.base.observation.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import javax.inject.Inject

/**
 * Example of how I would structure
 * a feature starting at its module.
 *
 * It uses the LiveData* approach explained in
 * the ObservationWithTheLiveDataApproach file.
 */

/**
 * SomethingFragment could be the entry point of
 * a feature. It would sit in a local module
 * or in a remote one downloaded as a aar.
 */
class SomethingFragment(
        /**
         * The feature gets its dependencies through a
         * ViewModelFactory. The factory could be a
         * (sealed) interface / implementation to
         * encapsulate the feature.
         */
        private val viewModelFactory: SomethingFragmentViewModelFactory
) : Fragment(R.layout.something__fragment_something) {

    private val viewModel: SomethingFragmentViewModel by viewModels { viewModelFactory }

    /**
     * The observable is the api surface of the feature.
     * The consuming activity or fragment subscribes
     * to it to listen to events from the feature
     * or to send events to it.
     */
    private val observable: SomethingFragmentObservable by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val somethingButton: Button =
                view.findViewById(R.id.somethingButton)

        /**
         * Events from the feature are delegated to the observable.
         */
        viewModel.somethingFinished.observe(viewLifecycleOwner,
                LiveDataCommandObserver(observable::onSomethingFinished))

        viewModel.somethingFailed.observe(viewLifecycleOwner,
                LiveDataEventObserver(observable::onSomethingFailed))

        somethingButton.setOnClickListener {
            viewModel.onSomethingButtonClicked()
        }
    }
}

/**
 * The observable is the connection point between the feature
 * and the consuming activity or fragment. The consumer can
 * listen to the public live data to receive events. The
 * feature uses the internal methods to send the events.
 */
class SomethingFragmentObservable : ViewModel() {
    private val _somethingFinished = MutableLiveData<LiveDataCommand>()
    private val _somethingFailed = MutableLiveData<LiveDataEvent<Exception>>()

    val somethingFinished: LiveData<LiveDataCommand> = _somethingFinished
    val somethingFailed: LiveData<LiveDataEvent<Exception>> = _somethingFailed


    internal fun onSomethingFinished() = _somethingFinished.emit()

    internal fun onSomethingFailed(exception: Exception) = _somethingFailed.emit(exception)
}

/**
 * This is how the approach looks in a activity. Could also be a fragment.
 */
class SomethingActivity : AppCompatActivity(R.layout.activity_something) {
    @Inject
    lateinit var fragmentFactory: SomethingFragmentFactory
    @Inject
    lateinit var viewModelFactory: SomethingActivityViewModelFactory

    private val viewModel: SomethingActivityViewModel by viewModels { viewModelFactory }

    private val somethingObservable: SomethingFragmentObservable by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()

        supportFragmentManager.fragmentFactory = fragmentFactory

        super.onCreate(savedInstanceState)

        connectObservable()
    }

    private fun injectDependencies() {
        // ...
    }

    /**
     * The consumer can (but does not need to)
     * listen to events from the feature.
     */
    private fun connectObservable() {
        somethingObservable.somethingFinished.observe(this,
                LiveDataCommandObserver(viewModel::onSomethingFinished))

        somethingObservable.somethingFailed.observe(this,
                LiveDataEventObserver(viewModel::onSomethingException))
    }
}

/**
 * Testing the observer to prevent future
 * problems when it has to change.
 */
@RunWith(MockitoJUnitRunner::class)
class SomethingFragmentObservableTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var observable: SomethingFragmentObservable

    @Mock
    private lateinit var somethingFinishedAction: () -> Unit
    @Mock
    private lateinit var somethingFailedAction: (Exception) -> Unit

    @Mock
    private lateinit var exception: Exception


    @Before
    fun setUp() {
        observable = createAndObserveObservable()
    }

    @Test
    fun `Should inform the module consumer when something finished`() {
        // WHEN
        observable.onSomethingFinished()

        // THEN
        verify(somethingFinishedAction).invoke()
    }

    @Test
    fun `Should inform the module consumer when something failed`() {
        // WHEN
        observable.onSomethingFailed(exception)

        // THEN
        verify(somethingFailedAction).invoke(exception)
    }

    private fun createAndObserveObservable(): SomethingFragmentObservable {
        val observable = SomethingFragmentObservable()

        observable.somethingFinished.observeForever(
                LiveDataCommandObserver(somethingFinishedAction))

        observable.somethingFailed.observeForever(
                LiveDataEventObserver(somethingFailedAction))

        return observable
    }
}