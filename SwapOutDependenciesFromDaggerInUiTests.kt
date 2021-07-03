import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.dg.eqs.base.injection.component.ApplicationComponent
import com.dg.eqs.base.injection.module.ApplicationModule
import com.dg.eqs.core.progression.eventlevel.roomdatabase.EventLevelRoomDatabase
import com.dg.eqs.core.progression.eventlevel.roomdatabase.EventLevelRoomEntity
import com.dg.eqs.core.progression.generatedlevel.roomdatabase.GeneratedLevelRoomDatabase
import com.dg.eqs.core.progression.presetlevel.roomdatabase.PresetLevelRoomDatabase
import com.dg.eqs.util.classes.TestEventLevelRoomDatabaseBuilder
import com.dg.eqs.util.classes.TestGeneratedLevelRoomDatabaseBuilder
import com.dg.eqs.util.classes.TestPresetLevelRoomDatabaseBuilder
import com.dg.eqs.util.dagger.TestDaggerMockRule
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.junit.After
import org.junit.Before
import org.junit.Rule

/**
 * A rule to ease working with fabio collinis amazing daggermock library.
 * Daggermock allows swapping out dependencies provided by dagger in ui-tests.
 */

open class AppDaggerMockRule(private vararg val daggerMockSetups: DaggerMockSetup)
    : DaggerMockRule<ApplicationComponent>(
        ApplicationComponent::class.java,
        ApplicationModule(application),
        // All ApplicationComponent modules
) {

    init {
        set { applicationComponent ->
            setupDependencies(applicationComponent)

            application.applicationComponent = applicationComponent
        }
    }

    private fun setupDependencies(component: ApplicationComponent) =
            daggerMockSetups.forEach {
                it.setupDependencies(this, component)
            }

    private companion object {
        private val application get() = getApplicationContext<App>()
    }
}

/**
 * Create dedicated & reusable setups which alter the dependency
 * graph when they are supplied to the app dagger mock rule.
 */
interface DaggerMockSetup {
    fun setupDependencies(rule: AppDaggerMockRule, component: ApplicationComponent)
}

/**
 * Use the setups to create dedicated rules which could be
 * useful for a subset of tests. This one here, for example,
 * swaps the production room databases with ones for tests
 * and allows their modification though public fields.
 */
class TestDaggerMockRule(
        private val testLevelRoomDatabaseSetup: TestLevelRoomDatabaseSetup = TestLevelRoomDatabaseSetup())
    : AppDaggerMockRule(testLevelRoomDatabaseSetup) {

    val testEventLevelRoomDao get() = testLevelRoomDatabaseSetup.testEventLevelRoomDatabase.dao
    val testPresetLevelRoomDao get() = testLevelRoomDatabaseSetup.testPresetLevelRoomDatabase.dao
    val testGeneratedLevelRoomDao get() = testLevelRoomDatabaseSetup.testGeneratedLevelRoomDatabase.dao
}

class TestLevelRoomDatabaseSetup : DaggerMockSetup {
    lateinit var testEventLevelRoomDatabase: EventLevelRoomDatabase private set
    lateinit var testPresetLevelRoomDatabase: PresetLevelRoomDatabase private set
    lateinit var testGeneratedLevelRoomDatabase: GeneratedLevelRoomDatabase private set


    override fun setupDependencies(rule: AppDaggerMockRule, component: ApplicationComponent) {
        testEventLevelRoomDatabase = TestEventLevelRoomDatabaseBuilder().build()
        testPresetLevelRoomDatabase = TestPresetLevelRoomDatabaseBuilder().build()
        testGeneratedLevelRoomDatabase = TestGeneratedLevelRoomDatabaseBuilder().build()

        rule.provides(EventLevelRoomDatabase::class.java, testEventLevelRoomDatabase)
        rule.provides(PresetLevelRoomDatabase::class.java, testPresetLevelRoomDatabase)
        rule.provides(GeneratedLevelRoomDatabase::class.java, testGeneratedLevelRoomDatabase)
    }
}

/**
 * Example: The test dagger mock rule is used to swap
 * out and alter the (room) test event level database.
 */
@get:Rule
val testDaggerMockRule = TestDaggerMockRule()

@Before
fun setUp() {
    val entity = EventLevelRoomEntity(0, "+±[+1,+2]", true, 1, 1)

    testDaggerMockRule.testEventLevelRoomDao.saveEntity(entity)
}

@After
fun tearDown() = testDaggerMockRule.testEventLevelRoomDao.deleteAllEntities()