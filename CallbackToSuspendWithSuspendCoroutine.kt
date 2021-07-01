import SomethingSdk.SomethingCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * suspendCoroutine and suspendCancellableCoroutine makes it possible
 * to convert a callback based approach to a suspension based approach.
 */

class SomethingService(private val somethingSdk: SomethingSdk) {
    suspend fun doSomething(): Something = suspendCancellableCoroutine { continuation ->
        somethingSdk.doSomething(object : SomethingCallback {
            override fun onSuccess(something: Something) {
                /**
                 * It's essential to check if the continuation
                 * is still active since a cancelled one would
                 * not be and crash on resume or resumeWithException
                 */
                if(continuation.isActive) {
                    continuation.resume(something)
                }
            }

            override fun onFailure(e: Exception) {
                if(continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }
        })
    }
}

interface SomethingSdk {
    /**
     * Assume SomethingSdk is not in our control
     * and still uses a callback based approach.
     */
    fun doSomething(callback: SomethingCallback)

    interface SomethingCallback {
        fun onSuccess(something: Something)

        fun onFailure(e: Exception)
    }
}

class Something