package com.shinonometn.koemans.web.spring.context

import com.shinonometn.koemans.web.spring.route.KtorRoute
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.springframework.stereotype.Controller

@Controller
@KtorRoute("/ktor")
class TestKtorRouteController {
    @KtorRoute("/route")
    fun Route.handle() = get {
        call.respond("Installed from @KtorRoute.")
    }
}