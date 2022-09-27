package org.fnives.test.showcase.hilt.core.storage

import org.fnives.test.showcase.model.session.Session

interface UserDataLocalStorage {
    var session: Session?
}
