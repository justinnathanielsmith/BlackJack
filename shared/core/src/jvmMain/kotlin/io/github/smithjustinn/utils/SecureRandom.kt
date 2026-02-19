package io.github.smithjustinn.utils

import java.security.SecureRandom

actual fun secureRandomLong(): Long = SecureRandom().nextLong()
