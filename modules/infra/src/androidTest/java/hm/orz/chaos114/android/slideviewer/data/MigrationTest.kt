package hm.orz.chaos114.android.slideviewer.data

import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory
import android.support.test.InstrumentationRegistry
import android.arch.persistence.room.testing.MigrationTestHelper
import org.junit.Rule
import android.support.test.runner.AndroidJUnit4
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @Rule
    var helper: MigrationTestHelper

    init {
        helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                AppDatabase::class.java!!.getCanonicalName(),
                FrameworkSQLiteOpenHelperFactory())
    }
}
