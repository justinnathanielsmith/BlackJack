package io.github.smithjustinn.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
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
