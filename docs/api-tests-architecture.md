# API tests — architecture

Deep-dive into the `api-tests` module. If the README tells you *how* to run
tests and add a new one, this doc tells you *why* things are wired the way
they are, and where the important seams live.

## Table of contents

1. [Bird's-eye view](#birds-eye-view)
2. [Package layout](#package-layout)
3. [Configuration](#configuration)
4. [HTTP layer — `HttpSpecs`](#http-layer--httpspecs)
5. [Composition root — `ApiServices`](#composition-root--apiservices)
6. [REST stack](#rest-stack)
7. [GraphQL stack](#graphql-stack)
8. [Authorization — the whole cycle](#authorization--the-whole-cycle)
9. [Assertions and conditions](#assertions-and-conditions)
10. [Test infrastructure](#test-infrastructure)
11. [Cleanup for created resources](#cleanup-for-created-resources)
12. [Allure reporting](#allure-reporting)
13. [Parallel execution](#parallel-execution)
14. [Adding a new endpoint or test](#adding-a-new-endpoint-or-test)

---

## Bird's-eye view

The module has two independent stacks under a shared root:

```
                       ApiServices
                     ┌──────┴──────┐
                     ▼             ▼
                RestServices   GraphQLServices
                     │             │
       ┌─────────────┼──────┐      ├──────────┐
       ▼             ▼      ▼      ▼          ▼
  AuthService  BookingService …  GraphQLClient  MovieService
       │             │             │
       └───► BaseRestService       │
                     │             │
                     ▼             ▼
                  HttpSpecs  ← (shared) → HttpSpecs
```

`HttpSpecs` is the one place that knows how to build a base
`RequestSpecification` — content type, Allure filter, optional
request/response logging. Both stacks get it via constructor.

`main/` holds production infrastructure — configs, services, DTOs, clients.
`test/` holds tests, test-data factories and domain assertions.

## Package layout

```
api-tests/src/
├── main/java/org/flamingo/
│   ├── ProjectConfig.java                aeonbits.owner interface
│   ├── clients/
│   │   ├── HttpSpecs.java                RequestSpecification factory
│   │   ├── GraphQLClient.java            POST + JSON to a GraphQL endpoint
│   │   └── GraphQLQuery.java             {query, variables} body DTO
│   ├── services/
│   │   ├── ApiServices.java              composition root, DI
│   │   ├── RestServices.java             container for REST services
│   │   ├── GraphQLServices.java          container for the GraphQL client + services
│   │   ├── rest/
│   │   │   ├── BaseRestService.java      base spec + ThreadLocal auth
│   │   │   ├── AuthService.java          /auth
│   │   │   └── BookingService.java       /booking CRUD
│   │   └── graphql/
│   │       └── MovieService.java         domain wrappers around .gql queries
│   ├── assertions/
│   │   ├── rest/AssertableResponse.java  wraps Response, .shouldHave / .asPojo
│   │   └── graphql/
│   │       ├── GraphQLResponse.java             entry point .then()
│   │       └── AssertableGraphQLResponse.java   body/asPojo/asList/errorMessages
│   ├── conditions/
│   │   ├── Condition.java                interface with check(Response)
│   │   ├── Conditions.java               factory (statusCode, bodyField)
│   │   ├── StatusCodeCondition.java
│   │   └── BodyFieldCondition.java
│   ├── payloads/                         request DTOs (Auth, Booking)
│   ├── responses/                        response DTOs (Auth, Booking, Movies)
│   └── utils/
│       ├── graphql/GqlUtils.java         read .gql files from classpath
│       └── rest/BookingCleanupRegistry.java   (legacy static registry — unused, see Cleanup section)
├── main/resources/
│   └── config.properties                 baseUrl, baseGraphQLUrl, admin creds
└── test/
    ├── java/com/flamingo/tests/
    │   ├── BaseApiTests.java             @BeforeAll setup, login helpers, runtimeState field
    │   ├── RuntimeState.java             per-class tracker of test-created resources
    │   ├── assertions/                   domain assertions (Booking, Movie)
    │   ├── testdata/                     BookingTestData, MovieTestData
    │   ├── graphql/                      GraphQLPositiveTest, GraphQLNegativeTest
    │   └── rest/booking/
    │       ├── BookingBaseTest.java      PER_CLASS base + @AfterAll cleanup via RuntimeState
    │       └── *BookingTest.java         CRUD scenarios
    └── resources/
        ├── junit-platform.properties     parallel exec settings
        └── graphql/                      *.gql query files
```

## Configuration

Loaded by [aeonbits.owner](http://owner.aeonbits.org/) — the
`ProjectConfig` interface plus `config.properties`.

```java
@Config.Sources({"classpath:config.properties"})
public interface ProjectConfig extends Config {
    @Key("baseUrl")            String baseUrl();
    @Key("baseGraphQLUrl")     String baseGraphQLUrl();
    @Key("admin.username")     @DefaultValue("admin")       String adminUsername();
    @Key("admin.password")     @DefaultValue("password123") String adminPassword();
    @DefaultValue("true")      boolean logging();
}
```

Created once with `ConfigFactory.create(ProjectConfig.class, System.getProperties())`
— so `-D` flags override values from the properties file. Priority:

1. `imports` argument (system properties).
2. Files in `@Config.Sources`.
3. `@DefaultValue`.

## HTTP layer — `HttpSpecs`

The one utility that knows about RestAssured filters and defaults:

```java
public final class HttpSpecs {
    private final boolean logHttp;

    public RequestSpecification jsonSpec() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .filters(filters());
    }

    private List<Filter> filters() {
        List<Filter> filters = new ArrayList<>();
        filters.add(new AllureRestAssured());
        if (logHttp) {
            filters.add(new RequestLoggingFilter());
            filters.add(new ResponseLoggingFilter());
        }
        return filters;
    }
}
```

Three deliberate choices:

- **Per-call spec.** `jsonSpec()` returns a new `RequestSpecification` on
  every call. RestAssured specs accumulate state (path params, headers) —
  reusing one across calls is a source of subtle bugs.
- **Explicit `logHttp` in the constructor.** No hidden `ConfigFactory`
  lookups inside — the caller decides.
- **Allure always on.** Without it there are no HTTP attachments in the
  report.

One instance per JVM, injected everywhere from `ApiServices`.

## Composition root — `ApiServices`

Where the graph is assembled:

```java
public ApiServices(String graphQLUrl) {
    var httpSpecs = new HttpSpecs(loadConfig().logging());
    this.rest = new RestServices(httpSpecs);
    this.graphQL = new GraphQLServices(graphQLUrl, httpSpecs);
}
```

Tests reach services through fluent getters (Lombok `@Accessors(fluent=true)`):

```java
api.rest().authService().login(...)
api.rest().bookingService().createBooking(...)
api.graphQL().client().executeGql(...)
api.graphQL().movies().getPage(10, 0)
```

The instance is recreated in `BaseApiTests.@BeforeEach` — one per test.

## REST stack

### `BaseRestService`

Abstract base. Two responsibilities (candidate for a split — see TODO):

1. Build the base spec + attach auth: `setUp()` → `httpSpecs.jsonSpec()` and
   adds `Cookie: token=...` if the ThreadLocal has a token.
2. Manage the ThreadLocal-scoped token (`ACCESS_TOKEN`).

```java
protected RequestSpecification setUp() {
    return auth(httpSpecs.jsonSpec());
}

protected RequestSpecification auth(RequestSpecification spec) {
    String token = ACCESS_TOKEN.get();
    return (token == null || token.isBlank())
            ? spec
            : spec.header(AUTH_HEADER_NAME, token);
}
```

### `AuthService`

One method — `login(AuthPayload)` — posts to `/auth`, returns
`AssertableResponse` (which the caller deserializes to `AuthResponse`).

### `BookingService`

`/booking` CRUD:

| Method | HTTP | Endpoint | Auth |
|---|---|---|---|
| `createBooking(payload)` | POST | `/booking` | no |
| `getBookingById(id)` | GET | `/booking/{id}` | no |
| `updateBookingById(id, payload, token)` | PUT | `/booking/{id}` | explicit `token` param via `setUpWithAuth(token)` |
| `deleteBookingById(id, token)` | DELETE | `/booking/{id}` | explicit `token` param via `setUpWithAuth(token)` |

Update/delete take the token explicitly rather than reading it from the
ThreadLocal. That makes it obvious at the call site which credentials
are being used — helpful for tests that want to log in as one user and
try to delete another user's booking.

## GraphQL stack

### `GraphQLClient`

Thin transport — reads a `.gql` file, wraps it into a `GraphQLQuery`
(`{query, variables}`), posts it:

```java
public GraphQLResponse executeGql(String name, Object variables) {
    var query = readGql(name, variables);
    return new GraphQLResponse(runQuery(query));
}

private Response runQuery(GraphQLQuery query) {
    return httpSpecs.jsonSpec().body(query).post(url);
}
```

### `MovieService`

Domain wrapper over `GraphQLClient`. One method — one query file:

| Method | .gql file | Purpose |
|---|---|---|
| `getPage(first, skip)` | `moviesPage.gql` | pagination |
| `getById(id)` | `movieById.gql` | single movie |
| `getWithPublisher(id)` | `movieWithPublisher.gql` | fragment + nested types |
| `create(title)` | `createMovie.gql` | mutation (CDN endpoint is read-only, expect an error) |

Files live in `api-tests/src/test/resources/graphql/`.

### `GqlUtils.readGql`

Reads the file by name via classpath. Throws `IllegalStateException` if the
file isn't found and `UncheckedIOException` on I/O failures.

### `AssertableGraphQLResponse`

Wraps a `Response` with four tools:

```java
response.body("movie", nullValue());       // Hamcrest against data.movie
response.asPojo("movie", Movie.class);     // extract data.movie as POJO
response.asList("movies", Movie.class);    // extract data.movies as List<Movie>
response.errorMessages();                  // List<String> from errors[].message
response.statusCode();                     // int
```

The `body/asPojo/asList` methods **auto-prefix the JSON path with `data.`**,
mirroring the GraphQL response shape (`{data: {...}, errors: [...],
extensions: {...}}`). For error assertions there's a separate
`errorMessages()` method that doesn't add the prefix.

## Authorization — the whole cycle

Auth applies to the REST side only. The GraphQL endpoint is a public
Hygraph CDN.

### Mechanics

The token lives in `ThreadLocal<String>` inside `BaseRestService`. That
gives us:

- Parallel-safe execution — each thread carries its own token.
- Automatic propagation into every `setUp()` call.

### Header format

Restful-booker expects the token in the `Cookie` header:

```
Cookie: token=abc123def456...
```

`BaseRestService.formatAuthToken()` normalizes the value — if it doesn't
already start with `token=`, that prefix is added.

### Life cycle

```
Test start
   │
   ▼
@BeforeEach — new ApiServices()               (no auth yet)
   │
   ▼
loginAsDefaultUser() inside the test
   │
   ├─ authService.login(payload)              POST /auth → {token: "..."}
   ├─ extract token
   └─ authService.setAuthToken(token)          store in ThreadLocal (with token= prefix)
   │
   ▼
calls that require auth:
   bookingService.updateBookingById(id, payload, token)
   bookingService.deleteBookingById(id, token)
   │
   └─ setUp() → auth(spec) → adds Cookie from ThreadLocal
   │
   ▼
@AfterEach — clearAccessToken()
   │
   └─ ACCESS_TOKEN.remove()                   hygiene for the next test
```

### Which endpoints need auth

| Endpoint | Method | Auth | Source of truth |
|---|---|---|---|
| POST `/auth` | `AuthService.login` | no | issues the token |
| POST `/booking` | `BookingService.createBooking` | no | Restful-booker allows anonymous |
| GET `/booking/{id}` | `BookingService.getBookingById` | no | public read |
| PUT `/booking/{id}` | `BookingService.updateBookingById` | **yes** | ThreadLocal via `setUp()` |
| DELETE `/booking/{id}` | `BookingService.deleteBookingById` | **yes** | ThreadLocal via `setUp()` |

### Token API in `BaseRestService`

```java
setAuthToken(String token)   // validates non-blank, stores in ThreadLocal
getAccessToken()             // returns the raw token without the token= prefix
clearAuthToken()             // removes from ThreadLocal — called in @AfterEach
```

### Helper on `BaseApiTests`

Simplest way to log in inside a test:

```java
protected String loginAsDefaultUser() {
    return loginAs(config.adminUsername(), config.adminPassword());
}
```

## Assertions and conditions

### REST — `AssertableResponse` + `Conditions`

Fluent DSL built around a small `Condition` interface:

```java
booking = bookingService.createBooking(payload)
        .shouldHave(Conditions.statusCode(200))
        .asPojo(BookingResponse.class);
```

- **`Condition`** — interface with `void check(Response)`.
- **`Conditions`** — factory (`statusCode(int)`, `bodyField(path, matcher)`).
- **`AssertableResponse.shouldHave(condition)`** — chainable.
- **`AssertableResponse.asPojo(Class)`** — Jackson-deserializes the whole body.
- **`AssertableResponse.response()`** — raw `Response` for edge cases.

Plus **domain assertions** under `test/`:

```java
BookingAssertions.assertThatBookingMatches(actualBooking, expectedPayload);
MovieAssertions.assertMovieHasCorrectSchema(movie);
```

Softly-grouped assertions (`assertSoftly`) inside — you see all field
mismatches at once, not just the first one.

### GraphQL — `AssertableGraphQLResponse`

Hybrid API — you can go the Hamcrest way:

```java
graphQLClient.executeGql("graphql/movieById.gql", Map.of("id", "..."))
    .then()
    .body("movie.title", equalTo("Jaws"));
```

Or the extract-and-assert way:

```java
graphQLClient.executeGql("graphql/movieById.gql", Map.of("id", "..."))
    .then()
    .asPojo("movie", Movie.class);
```

## Test infrastructure

### `BaseApiTests`

The parent of every test:

```java
@BeforeAll
static void setUp() {
    RestAssured.baseURI = config.baseUrl();
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
}

@BeforeEach
void setUpEach() {
    this.api = new ApiServices(config.baseGraphQLUrl());
}

@AfterEach
void tearDown() {
    clearAccessToken();
}
```

Fresh `ApiServices` per test method, no state leaks between tests except
`RestAssured.baseURI` which is set once and never mutated.

### `BookingTestData` / `MovieTestData`

Precondition helpers under `com.flamingo.tests.testdata`:

```java
BookingPayload payload = BookingTestData.defaultBooking(faker);
String movieId       = MovieTestData.existingMovieIdWithPoster(movies);
```

Dates use `LocalDate.now().plusMonths(...)` so tests don't rot when
hard-coded dates fall into the past.

### `junit-platform.properties`

Parallel execution config — see the [Parallel execution](#parallel-execution)
section.

## Cleanup for created resources

Two moving pieces, both on the test side:

- **`RuntimeState`** — an instance-field holder on `BaseApiTests`. Wraps a
  `CopyOnWriteArrayList<Integer>` of booking ids created during a single
  test class run. Thread-safe, so parallel methods can add safely.

  ```java
  @Getter
  public class RuntimeState {
      private final List<Integer> bookings = new CopyOnWriteArrayList<>();
      public void addBooking(int bookingId) { bookings.add(bookingId); }
      public void clear() { bookings.clear(); }
  }
  ```

- **`BookingBaseTest`** — `PER_CLASS` lifecycle base for booking tests.
  Exposes a `createdBooking(payload)` helper that both creates the booking
  and pushes its id into `runtimeState`. In `@AfterAll` it iterates the
  snapshot and deletes each id.

  ```java
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  public abstract class BookingBaseTest extends BaseApiTests {

      public BookingResponse createdBooking(BookingPayload payload) {
          var created = api.rest().bookingService()
                  .createBooking(payload)
                  .shouldHave(Conditions.statusCode(200))
                  .shouldHave(Conditions.bodyField("bookingid", notNullValue()))
                  .asPojo(BookingResponse.class);
          runtimeState.addBooking(created.getBookingId());
          return created;
      }

      @AfterAll
      void cleanUpTestData() {
          List<Integer> bookings = runtimeState.getBookings();
          bookings.forEach(id ->
              api.rest().bookingService().deleteBookingById(id, loginAsDefaultUser())
          );
      }
  }
  ```

Because `@AfterAll` runs after `@AfterEach` has already cleared the
`ACCESS_TOKEN` ThreadLocal, cleanup performs a fresh `loginAsDefaultUser()`
before it starts deleting.

The `PER_CLASS` lifecycle matters — with one instance per class,
`runtimeState` is shared across every method in that class, and `@AfterAll`
can be non-static.

> Note: `org.flamingo.utils.rest.BookingCleanupRegistry` is an older
> process-wide static registry that predated `RuntimeState`. It's not wired
> anywhere and can be removed.

## Allure reporting

### Wiring

Configured in the root `build.gradle`:

```gradle
allure {
    version = "2.44.0"                    // CLI for report rendering
    adapter {
        aspectjWeaver = true              // required for @Step
        frameworks { junit5 { enabled = true; adapterVersion = "2.35.3" } }
    }
}
```

### What gets attached automatically

- **HTTP request/response** for every call — via `AllureRestAssured` filter
  in `HttpSpecs.filters()`.
- **`@Step` annotations** on all `MovieService`, `BookingService`,
  `AuthService` methods. AspectJ weaving inserts step tracking around them.
- **`@DisplayName`** — used as the visible test name.
- **`@Epic` / `@Feature` / `@Story`** — build the tree in the Behaviours tab.
- **`@Severity`** — filter by criticality.

### How to generate

```bash
./gradlew :api-tests:test                     # results → build/allure-results/
./gradlew :api-tests:allureServe              # opens locally
./gradlew :api-tests:allureReport             # static build/reports/allure-report/
./gradlew allureServe                         # aggregated across modules
```

## Parallel execution

`api-tests/src/test/resources/junit-platform.properties`:

```properties
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = concurrent           # methods
junit.jupiter.execution.parallel.mode.classes.default = concurrent   # classes
junit.jupiter.execution.parallel.config.strategy = dynamic
junit.jupiter.execution.parallel.config.dynamic.factor = 1           # threads = CPUs × 1
```

### What's already thread-safe

- `HttpSpecs.jsonSpec()` — per-call spec, no shared state.
- `BaseRestService.ACCESS_TOKEN` — `ThreadLocal`, each thread has its own.
- Allure `@Step` — uses a ThreadLocal context internally.
- `RestAssured.baseURI` — set once in `@BeforeAll`, never mutated.
- `Faker` — instance field on `BaseApiTests`, JUnit creates a new instance
  per method.

### When you'd need `@Execution(SAME_THREAD)`

- Tests that mutate `RestAssured.baseURI` or another global.
- Tests that measure timing.
- Tests that hold an exclusive resource (port, file).

## Adding a new endpoint or test

### Adding a new REST endpoint to `BookingService`

1. Method on the service with `@Step`.
2. `setUp()` to get the base spec (with auth if needed).
3. Wrap the response in `AssertableResponse`.

```java
@Step("Get all bookings")
public AssertableResponse getAllBookings() {
    return new AssertableResponse(
        setUp().when().get(BOOKING)
    );
}
```

### Adding a new GraphQL query

1. Create the `.gql` file under `src/test/resources/graphql/`.
2. Add a path constant on `MovieService`.
3. Add a method with `@Step`.

```java
private static final String SEARCH_MOVIES = "graphql/searchMovies.gql";

@Step("Search movies by title contains '{query}'")
public List<Movie> searchByTitle(String query) {
    return client.executeGql(SEARCH_MOVIES, Map.of("q", query))
            .then().asList("movies", Movie.class);
}
```

### Adding a new test class

1. Extend `BaseApiTests`.
2. Add `@Epic`, `@Feature`, `@DisplayName` at the class level.
3. Use `api.rest().xxxService()` or `api.graphQL().movies()` as entry points.
4. For preconditions — static helpers under `testdata/`.
5. For domain-level assertions — static methods under `assertions/`.
6. For booking tests specifically — extend `BookingBaseTest` instead of
   `BaseApiTests`; call `createdBooking(payload)` so cleanup is automatic.
