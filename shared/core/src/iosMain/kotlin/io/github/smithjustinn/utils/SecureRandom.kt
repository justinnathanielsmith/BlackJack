package io.github.smithjustinn.utils

import kotlinx.cinterop.*
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault

@OptIn(ExperimentalForeignApi::class)
actual fun secureRandomLong(): Long {
    memScoped {
        val randomBytes = alloc<LongVar>()
        SecRandomCopyBytes(kSecRandomDefault, 8.toULong(), randomBytes.ptr)
        return randomBytes.value
    }
}
