# Koemans 

[![Test](https://github.com/ShinonomeTN/koemans/actions/workflows/test.yml/badge.svg?branch=master)](https://github.com/ShinonomeTN/koemans/actions/workflows/test.yml)
[![Deployment](https://github.com/ShinonomeTN/koemans/actions/workflows/deploy-shinonometn.yml/badge.svg?branch=master)](https://github.com/ShinonomeTN/koemans/actions/workflows/deploy-shinonometn.yml)
[![Star](https://img.shields.io/github/stars/ShinonomeTN/koemans)](https://github.com/ShinonomeTN/koemans/stargazers)
[![Twitter](https://img.shields.io/badge/Twitter-%40CattenLinger-blue?style=flat&logo=twitter)](https://twitter.com/CattenLinger)
[![Version](https://img.shields.io/github/v/release/ShinonomeTN/koemans?include_prereleases)](https://github.com/ShinonomeTN/koemans/releases)
[![License](https://img.shields.io/github/license/ShinonomeTN/koemans)](https://github.com/ShinonomeTN/koemans/blob/master/LICENSE)

---

Koemans is an util set for us to building Ktor based web applications.

> Now ktor's latest version is 2.x, but Koemans was written for 1.x. We are planning to upgrading the ktor 2.x version.
> This will need some time because it will break our existing projects due to the incompatible between ktor 1.x and 2.x.
> So, ktor upgrade will available since Koemans 2.x, Koemans 1.x will be legacy version.

## Features

- Parameter validator for ktor `Parameters`.
- Sprint Context support for ktor and hocon configuration support for SpringContext.
- `sqlDatabase` support for Exposed.
- paging, filtering and sorting query support for Exposed.
- a simple coroutine based event hub. (not encouraged for use, Spring's `ApplicationEvent` is better.)

## Modules

- koemans-all: all modules, with experimental utils waiting for categorize.
- koemans-event-hub: a little event hub. Not encouraged for use.
- koemans-exposed: Exposed support. Paging, filetering and sorting, query supports.
- koemans-exposed-database: Database support, including a dsl `sqlDatabase` for creating database connection.
- koemans-exposed-database-sqlite3: Sqlite3 support.
- koelans-utils: a set of small utils.
- koemans-web-ktor: currently only a ktor Parameter `Validator` inside.
- koemans-web-spring: SpringContext support.

## Where to get it

For repository url, please see [ShinonomeTN Public Maven Repository](https://github.com/ShinonomeTN/maven-public)

or just pull and install it via `./mvnw install`.

## Usages & Examples

You can just import the hole Koemans:

```groovy
implementation("org.shinonometn:koemans-all:${koemans.version}")
```

or just some modules.

```groovy
// Exposed supports only
implementation("org.shinonometn:koemans-exposed:${koemans.version}")
implementation("org.shinonometn:koemans-exposed-database:${koemans.version}")
implementation("org.shinonometn:koemans-exposed-database-sqlite3:${koemans.version}")
```

### Create a simple application with spring context

```hocon
ktor {
    application {
        modules = [ com.shinonometn.demo.MainAppKt.mainModule ]
    }

    deployment {
        host = "0.0.0.0"
        port = 8098
    }
}
```

```kotlin
package com.shinonometn.demo

open class ApplicationAutoConfiguration() {
    
    // Configure your spring application as usual
    @Bean
    open fun executorService() = Executors.newFixedThreadPool(4)
}

@Service
class HelloService {
    fun hello() {
        println("Hello world!")   
    }
}

fun Application.mainModule() {
    install(SpringContext) {
        annotationDriven(ApplicationAutoConfiguration::class.java) {
            // You can load a hocon property file as source.
            useHoconPropertySource(null, ClassPathResource("application.hocon"))
            
            // ...or use the property of ktor :D
            val hoconConfig = getEnvironmentHoconConfig()
            hoconConfig?.let { useHoconPropertySource("ktor", it) }
        }
    }
    
    routing{
        val helloService = application.springContext.find<HelloService>()
        get("/") {
            helloService.hello()
            call.respondText("Hello, world!")
        }
    }
}

```

### Validating parameters

```kotlin
fun Route.requestRoute() {
    val validator = Validator {
        "username" with isString { it.length <= 32 }
        "password" with isString
        optional("nickname") with isString { it.length <= 32 }
    }
    
    post {
        val parameters = validator.validate(call.receiveParameters())
        
        // Do your fancy logics here :D
        
        //....
    }
}
```

### Query supports 

```kotlin
fun Route.queryRoute() {
    val database = application.sprintContext.find<SqlDatabase>()
    
    val filtering = FilerOptionMapping {
        "username" means { UserTable.colUsername eq it }
        "starDate" means { UserTable.colRegisteredDate greaterEq LocalDatetime.parse(it) }
        "endDate" means { UserTable.colRegisteredDate lesserEq LocalDatetime.parse(it) }
    }
    
    val sorting = SortOptionMapping {
        "registeredDate" associateTo UserTable.colRegisteredDate
        "loginTime" associateTo UserTable.colRegisteredDate defaultOrder SortOrder.DESC
    }
    
    // It will accept request like 
    // '/user?username=foo&starDate=2020-01-01&endDate=2020-01-02&sort=loginTime,ASC'
    get {
        // Currently those shortcut methods are in koemans-all.
        val paging = call.receivePagingRequest()
        val filter = call.receiveFilterOptions(filtering)
        val sort = call.receiveSortOptions(sorting)
        
        val query = database {
            
            val result = UserTable.selectBy(filter).orderBy(sort).pagingBy(paging) {
                it[UserTable.colUsername] 
            } 
            
            call.respond(result)
        }
    }
}
```

### Connect to database

```kotlin
// We only support sqlite3 for now.
val database = sqlDatabase(Sqlite3) {
    inFile("database_name", Paths.get("/path/to/folder"))
    // or you wish an inmemory database
    // inMemory("inMemoryDatabase")
    // We encourage you to use connection pooling when using in-memory Sqlite.
    // We only support HikariPooling currently.
    poolingStrategy = HikariPooling()
}

fun main() {
    // Just do your database operations as usual.
    database {
        SomeTable.selectAll().forEach {
            println(it[SomeTable.colName])
        }
    }
}
```
