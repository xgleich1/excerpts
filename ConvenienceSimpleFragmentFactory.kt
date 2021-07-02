import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import kotlin.reflect.KClass

/**
 * Convenience class to ease scenarios where
 * a activity shows one fragment which has a
 * constructor and thus requires a factory.
 */

abstract class SimpleFragmentFactory(
        fragmentClass: KClass<*>
) : FragmentFactory() {

    private val fragmentClassName = fragmentClass.java.name


    override fun instantiate(classLoader: ClassLoader, className: String) =
            if(className == fragmentClassName) {
                instantiateFragment()
            } else {
                super.instantiate(classLoader, className)
            }

    protected abstract fun instantiateFragment(): Fragment
}

/**
 * Due to the SimpleFragmentFactory being intentionally
 * abstract, users are forced to create a class which
 * then can be easily found using the class search.
 */
class SomethingFragmentFactory
    : SimpleFragmentFactory(SomethingFragment::class) {

    override fun instantiateFragment() = SomethingFragment()
}

class SomethingFragment : Fragment()

/**
 * Bonus: Convenience extension function to create
 * the fragment (for the first time) in the activity.
 */
inline fun <reified T : Fragment> FragmentActivity.instantiateFragment() =
        supportFragmentManager
                .fragmentFactory
                .instantiate(classLoader, T::class.java.name)

/**
 * Testing the logic to prevent future problems
 * when the SimpleFragmentFactory has to change.
 */
class SimpleFragmentFactoryTest {
    @Test
    fun `Should instantiate the fragment when its matching the provided class name`() {
        // GIVEN
        val fragment = FragmentA()

        val fragmentFactory = object
            : SimpleFragmentFactory(FragmentA::class) {

            override fun instantiateFragment() = fragment
        }

        // WHEN
        val instantiatedFragment = fragmentFactory
                .instantiate(mock(), FragmentA::class.java.name)

        // THEN
        assertThat(instantiatedFragment).isEqualTo(fragment)
    }

    @Test
    fun `Should not instanciate the fragment when the fragment doesn't match the provided class name`() {
        // GIVEN
        val fragmentFactory = object
            : SimpleFragmentFactory(FragmentA::class) {

            override fun instantiateFragment() = FragmentA()
        }

        // WHEN
        val instantiatedFragment = fragmentFactory
                .instantiate(mock(), FragmentB::class.java.name)

        // THEN
        assertNull(instantiatedFragment)
    }

    private class FragmentA : Fragment()

    private class FragmentB : Fragment()
}