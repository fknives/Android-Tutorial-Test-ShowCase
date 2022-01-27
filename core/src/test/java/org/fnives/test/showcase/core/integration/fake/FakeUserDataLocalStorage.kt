package org.fnives.test.showcase.core.integration.fake

import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.model.session.Session

class FakeUserDataLocalStorage(override var session: Session? = null) : UserDataLocalStorage
