import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Throws(InterruptedException::class)
suspend fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {}
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(t: T) {
            data = t
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }

    // when running within the runBlocking there is no problem, needs to be within suspend fun
    withContext(Dispatchers.Main) {
        this@getOrAwaitValue.observeForever(observer)
        afterObserve.invoke()
    }

    // don't wait indefinitely if the LiveData does not change.
    if (!latch.await(time, timeUnit)) {
        withContext(Dispatchers.Main) {
            this@getOrAwaitValue.removeObserver(observer)
        }
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}
