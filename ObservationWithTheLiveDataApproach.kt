import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * The LiveData* approach fills gaps left open by google
 * (LiveDataCommand, LiveDataEvent) and removes noise
 * from the code (the different Observer classes).
 */

/**
 * This is how the approach looks in a activity.
 *
 * The first (LiveDataObserver) removes code noise
 * by making the observed text non null.
 *
 * The second (LiveDataCommand+Observer) executes a
 * lambda only once, which, for example, prevents
 * showing a toast again on device rotation.
 *
 * The third (LiveDataEvent+Observer) behaves the
 * same as the LiveDataCommand+Observer, but
 * with the means to include a value.
 */
viewModel.showText.observe(this,
        LiveDataObserver { textResId ->
            // Standard LiveData behaviour,
            // but "textResId" is non null.
        })

viewModel.showSignInToast.observe(this,
        LiveDataCommandObserver {
            // Only executed once.
        })

viewModel.showToastWithText.observe(this,
        LiveDataEventObserver { textResId ->
            // Only executed once,
            // "textResId" is non null.
        })

/**
 * This is how the approach looks in a view model.
 */
showText.emit(R.string.text)

showSignInToast.emit()

showToastWithText.emit(R.string.text)

/**
 * The approach is modeled using LiveData's Observer
 * interface, the LiveDataCommand & Event classes
 * and convenience extension methods.
 *
 * Beware: Command and event have the limitation
 * that only the first observer gets his lambda
 * executed when there's more than one observer.
 */
class LiveDataObserver<T>(
        private val recipient: (T) -> Unit
) : Observer<T> {

    override fun onChanged(value: T) = recipient(value)
}

class LiveDataCommandObserver(
        private val command: () -> Unit
) : Observer<LiveDataCommand> {

    override fun onChanged(liveDataCommand: LiveDataCommand) {
        liveDataCommand.execute(command)
    }
}

class LiveDataEventObserver<T : Any>(
        private val recipient: (T) -> Unit
) : Observer<LiveDataEvent<T>> {

    override fun onChanged(liveDataEvent: LiveDataEvent<T>) {
        liveDataEvent.getContentOnce()?.let(recipient)
    }
}

class LiveDataCommand {
    private var executed = false


    fun execute(command: () -> Unit) {
        if(!executed) {
            executed = true

            command()
        }
    }
}

class LiveDataEvent<T : Any>(private val content: T) {
    private var exposed = false


    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as LiveDataEvent<*>

        if(content != other.content) return false

        return true
    }

    override fun hashCode() = content.hashCode()

    override fun toString() = "LiveDataEvent($content)"

    fun getContentOnce() = if(!exposed) {
        exposed = true

        content
    } else {
        null
    }
}

fun <T : Any> MutableLiveData<T>.emit(value: T) {
    this.value = value
}

fun <T : Any> MutableLiveData<T>.post(value: T) {
    postValue(value)
}

fun MutableLiveData<LiveDataCommand>.emit() {
    value = LiveDataCommand()
}

fun MutableLiveData<LiveDataCommand>.post() {
    postValue(LiveDataCommand())
}

fun <T : Any> MutableLiveData<LiveDataEvent<T>>.emit(content: T) {
    value = LiveDataEvent(content)
}

fun <T : Any> MutableLiveData<LiveDataEvent<T>>.post(content: T) {
    postValue(LiveDataEvent(content))
}

/**
 * Testing the logic to prevent future problems
 * when the LiveData* approach has to change.
 */
@RunWith(MockitoJUnitRunner::class)
class LiveDataObserverTest {
    @Mock
    private lateinit var recipient: (Int) -> Unit


    @Test
    fun `Should deliver the live datas value once notified`() {
        // GIVEN
        val observer = LiveDataObserver(recipient)

        // WHEN
        observer.onChanged(1)

        // THEN
        verify(recipient).invoke(1)
    }
}

@RunWith(MockitoJUnitRunner::class)
class LiveDataCommandObserverTest {
    @Mock
    private lateinit var command: () -> Unit


    @Test
    fun `Should execute the command once notified`() {
        // GIVEN
        val observer = LiveDataCommandObserver(command)

        val liveDataCommand: LiveDataCommand = mock()

        // WHEN
        observer.onChanged(liveDataCommand)

        // THEN
        verify(liveDataCommand).execute(command)
    }
}

@RunWith(MockitoJUnitRunner::class)
class LiveDataEventObserverTest {
    @Mock
    private lateinit var recipient: (Int) -> Unit


    @Test
    fun `Should deliver the events content once notified`() {
        // GIVEN
        val observer = LiveDataEventObserver(recipient)

        val liveDataEvent = LiveDataEvent(1)

        // WHEN
        observer.onChanged(liveDataEvent)

        // THEN
        verify(recipient).invoke(1)
    }
}

class LiveDataCommandTest {
    @Test
    fun `A live data command executes its command only once`() {
        // GIVEN
        var commandExecutionCounter = 0

        val liveDataCommand = LiveDataCommand()

        // WHEN
        liveDataCommand.execute { ++commandExecutionCounter }
        liveDataCommand.execute { ++commandExecutionCounter }

        // THEN
        assertThat(commandExecutionCounter).isEqualTo(1)
    }
}

class LiveDataEventTest {
    @Test
    fun `Should compare two equal live data events`() {
        // GIVEN
        val eventA = LiveDataEvent(1)
        val eventB = LiveDataEvent(1)

        // THEN
        assertThat(eventA).isEqualTo(eventB)
    }

    @Test
    fun `Should convert a live data event to a string`() {
        // GIVEN
        val event = LiveDataEvent(1)

        // THEN
        assertThat(event).hasToString("LiveDataEvent(1)")
    }

    @Test
    fun `A live data event exposes its content only once`() {
        // GIVEN
        val event = LiveDataEvent(1)

        // THEN
        assertThat(event.getContentOnce()).isEqualTo(1)
        assertThat(event.getContentOnce()).isNull()
    }
}

class LiveDataExtTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @Test
    fun `A live data exposes its value once its emitted`() {
        // GIVEN
        val liveData = MutableLiveData<Int>()

        // WHEN
        liveData.emit(1)

        // THEN
        assertThat(liveData.value).isEqualTo(1)
    }

    @Test
    fun `A live data exposes its value once its posted`() {
        // GIVEN
        val liveData = MutableLiveData<Int>()

        // WHEN
        liveData.post(1)

        // THEN
        assertThat(liveData.value).isEqualTo(1)
    }
}

class LiveDataCommandExtTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @Test
    fun `A live data exposes its command once its emitted`() {
        // GIVEN
        val liveData = MutableLiveData<LiveDataCommand>()

        // WHEN
        liveData.emit()

        // THEN
        assertThat(liveData.value).isNotNull
    }

    @Test
    fun `A live data exposes its command once its posted`() {
        // GIVEN
        val liveData = MutableLiveData<LiveDataCommand>()

        // WHEN
        liveData.post()

        // THEN
        assertThat(liveData.value).isNotNull
    }
}

class LiveDataEventExtTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @Test
    fun `A live data exposes its event once its emitted`() {
        // GIVEN
        val liveData = MutableLiveData<LiveDataEvent<Int>>()

        // WHEN
        liveData.emit(1)

        // THEN
        assertThat(liveData.value).isEqualTo(LiveDataEvent(1))
    }

    @Test
    fun `A live data exposes its event once its posted`() {
        // GIVEN
        val liveData = MutableLiveData<LiveDataEvent<Int>>()

        // WHEN
        liveData.post(1)

        // THEN
        assertThat(liveData.value).isEqualTo(LiveDataEvent(1))
    }
}