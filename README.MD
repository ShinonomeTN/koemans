# Koemans

Koemans is an util set for us for building Ktor based web applications.

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

Currently, Koemans is not available on public maven repositories. But you can use it through our private repository.

Gradle:
```groovy
repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = "https://nexus.shinonometn.com/repository/maven-public/"
    }
}
```

Maven:
```xml
<repository>
  <id>shinonometn-public</id>
  <name>ShinonomeTN Public Maven Repository</name>
  <url>https://nexus.shinonometn.com/repository/maven-public/</url>
</repository>
```

or just pull and install it via `mvn`.

## Usages & Examples

You can just import the hole Koemans:

```groovy
implementation("org.shinonometn:koemans-all:1.1-SNAPSHOT")
```

or jus some modules.

```groovy
// Exposed supports only
implementation("org.shinonometn:koemans-exposed:1.1-SNAPSHOT")
implementation("org.shinonometn:koemans-exposed-database:1.1-SNAPSHOT")
implementation("org.shinonometn:koemans-exposed-database-sqlite3:1.1-SNAPSHOT")
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