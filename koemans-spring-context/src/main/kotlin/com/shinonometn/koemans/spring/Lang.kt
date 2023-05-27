package com.shinonometn.koemans.spring

import org.springframework.context.support.GenericApplicationContext

/** Convenience method to refresh and start a spring application context
 * Returns the application context itself */
fun <T : GenericApplicationContext> T.refreshAndStart() : T = apply {
    refresh()
    start()
}