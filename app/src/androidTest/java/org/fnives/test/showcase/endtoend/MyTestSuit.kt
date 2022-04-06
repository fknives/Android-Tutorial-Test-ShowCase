package org.fnives.test.showcase.endtoend

import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.Suite

@Ignore("Example test Suite")
@RunWith(Suite::class)
@Suite.SuiteClasses(
    LoginLogoutEndToEndTest::class,
)
class MyTestSuit
