package org.fnives.test.showcase.hilt.core.integration.fake

import org.fnives.test.showcase.hilt.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.model.session.Session

class FakeUserDataLocalStorage(override var session: Session? = null) : UserDataLocalStorage
