package org.fnives.test.showcase.android.testutil

import android.app.Instrumentation
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.fnives.test.showcase.android.testutil.robolectric.RobolectricMigrationTestRule

inline fun <reified Database : RoomDatabase> SharedMigrationTestRule(
    instrumentation: Instrumentation,
): SharedMigrationTestRule =
    createAndroidClassOrRobolectric(
        androidClassFactory = { androidClass ->
            val constructor = androidClass.getConstructor(
                Instrumentation::class.java,
                Class::class.java
            )
            constructor.newInstance(instrumentation, Database::class.java) as SharedMigrationTestRule
        },
        robolectricFactory = {
            RobolectricMigrationTestRule(instrumentation, Database::class.java)
        }
    )

inline fun <reified Database : RoomDatabase> SharedMigrationTestRule(
    instrumentation: Instrumentation,
    specs: List<AutoMigrationSpec>,
): SharedMigrationTestRule =
    createAndroidClassOrRobolectric(
        androidClassFactory = { androidClass ->
            val constructor = androidClass.getConstructor(
                Instrumentation::class.java,
                Class::class.java,
                List::class.java
            )
            constructor.newInstance(instrumentation, Database::class.java, specs) as SharedMigrationTestRule
        },
        robolectricFactory = {
            RobolectricMigrationTestRule(instrumentation, Database::class.java, specs)
        }
    )

inline fun <reified Database : RoomDatabase> SharedMigrationTestRule(
    instrumentation: Instrumentation,
    specs: List<AutoMigrationSpec>,
    openFactory: SupportSQLiteOpenHelper.Factory,
): SharedMigrationTestRule =
    createAndroidClassOrRobolectric(
        androidClassFactory = { androidClass ->
            val constructor = androidClass.getConstructor(
                Instrumentation::class.java,
                Class::class.java,
                List::class.java,
                SupportSQLiteOpenHelper.Factory::class.java
            )
            constructor.newInstance(instrumentation, Database::class.java, specs, openFactory) as SharedMigrationTestRule
        },
        robolectricFactory = {
            RobolectricMigrationTestRule(instrumentation, Database::class.java, specs, openFactory)
        }
    )

fun createAndroidClassOrRobolectric(
    androidClassFactory: (Class<*>) -> Any,
    robolectricFactory: () -> SharedMigrationTestRule,
): SharedMigrationTestRule {
    val androidClass = getAndroidClass()
    return if (androidClass == null) {
        robolectricFactory()
    } else {
        androidClassFactory(androidClass) as SharedMigrationTestRule
    }
}

@Suppress("SwallowedException")
private fun getAndroidClass() = try {
    Class.forName("org.fnives.test.showcase.android.testutil.AndroidMigrationTestRule")
} catch (classNotFoundException: ClassNotFoundException) {
    null
}
