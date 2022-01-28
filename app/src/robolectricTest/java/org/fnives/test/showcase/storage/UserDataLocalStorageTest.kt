package org.fnives.test.showcase.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.fnives.test.showcase.core.integration.fake.FakeUserDataLocalStorage
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.model.session.Session
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.test.KoinTest
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class UserDataLocalStorageTest(
    private val userDataLocalStorageFactory: () -> UserDataLocalStorage
) : KoinTest {

    private lateinit var userDataLocalStorage: UserDataLocalStorage

    @Before
    fun setup() {
        userDataLocalStorage = userDataLocalStorageFactory.invoke()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    /** GIVEN session value WHEN accessed THEN it's returned **/
    @Test
    fun sessionSetWillStayBeKept() {
        val session = Session(accessToken = "a", refreshToken = "b")
        userDataLocalStorage.session = session

        val actual = userDataLocalStorage.session

        Assert.assertEquals(session, actual)
    }

    /** GIVEN null value WHEN accessed THEN it's null **/
    @Test
    fun sessionSetToNullWillStayNull() {
        userDataLocalStorage.session = Session(accessToken = "a", refreshToken = "b")

        userDataLocalStorage.session = null
        val actual = userDataLocalStorage.session

        Assert.assertEquals(null, actual)
    }

    companion object {

        private fun createFake(): UserDataLocalStorage = FakeUserDataLocalStorage()

        private fun createReal(): UserDataLocalStorage {
            val context = ApplicationProvider.getApplicationContext<Context>()

            return SharedPreferencesManagerImpl.create(context)
        }

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun userDataLocalStorageFactories(): List<() -> UserDataLocalStorage> = listOf(
            ::createFake,
            ::createReal
        )
    }
}