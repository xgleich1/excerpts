import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.Collections.synchronizedList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS


/**
 * Catch simple(!) problems involving concurrency in ui-tests. A simple problem
 * would be, for example, if someone uses a list instead of a synchronized list.
 */

fun executeConcurrently(threadCount: Int, action: () -> Unit) {
    val startSignal = CountDownLatch(1)
    val finishSignal = CountDownLatch(threadCount)

    repeat(threadCount) {
        val runnable = Runnable {
            startSignal.await()

            action()

            finishSignal.countDown()
        }

        Thread(runnable, "Thread Number $it").start()
    }

    startSignal.countDown()

    val allThreadsRunToCompletion = finishSignal.await(10, SECONDS)

    if(!allThreadsRunToCompletion) {
        throw Exception("Not all threads run to completion")
    }
}

@Test
fun should_concurrently_add_items_to_a_list() {
    // GIVEN
    val list = synchronizedList<Int>(mutableListOf())

    /**
     * The test crashes when using just a mutable list with
     * ArrayIndexOutOfBoundsException: length=23; index=464
     */
//    val synchronizedList = mutableListOf<Int>()

    // WHEN
    executeConcurrently(threadCount = 500) {
        list += 1
    }

    // THEN
    assertThat(list).hasSize(500)
}