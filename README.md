# Flamingo

A small Java testing playground that covers three thin slices of automation
in one place: REST, GraphQL and browser UI. Nothing fancy — just JUnit 5,
RestAssured, Playwright and Allure wired together the way I'd expect to see
them in a real project.

Two Gradle modules: `api-tests/` for HTTP work, `ui-tests/` for Playwright.
The GitHub Actions workflow runs whichever suite you ask for and publishes
the Allure report to Pages.

## Tech stack

| Category | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Build tool | Gradle | 8+ (via wrapper) |
| Test runner | JUnit Jupiter | 5.14 |
| REST | RestAssured | 6.0.1 |
| GraphQL | RestAssured + thin custom client | — |
| UI | Playwright for Java | 1.61.0 |
| Assertions | AssertJ | 3.27.3 |
| Test data | JavaFaker | 1.0.2 |
| Boilerplate | Lombok | 1.18.46 |
| Config | aeonbits.owner | 1.0.12 |
| Reporting | Allure | 2.35.3 (adapter) / 2.44.0 (CLI) |
| Logging | SLF4J | 2.0.18 |
| CI | GitHub Actions | — |

## Layout

```
flamingo/
├── build.gradle              root: Allure, dependencies, shared test config
├── settings.gradle           includes api-tests + ui-tests
├── .github/workflows/
│   └── tests.yml             three test jobs + Allure report on Pages
├── docs/
│   └── api-tests-architecture.md
│
├── api-tests/
│   └── src/
│       ├── main/java/org/flamingo/
│       │   ├── clients/                 HttpSpecs, GraphQLClient
│       │   ├── services/                ApiServices root — RestServices, GraphQLServices
│       │   │   ├── rest/                AuthService, BookingService, BaseRestService
│       │   │   └── graphql/             MovieService
│       │   ├── assertions/              AssertableResponse, AssertableGraphQLResponse
│       │   ├── conditions/              Conditions DSL
│       │   ├── payloads/ + responses/   DTOs
│       │   └── utils/graphql/           reads .gql files from classpath
│       └── test/java/com/flamingo/tests/
│           ├── graphql/                 GraphQL positive/negative
│           └── rest/booking/            CRUD tests
│
└── ui-tests/
    └── src/
        ├── main/java/com/flamingo/ui/
        │   ├── basic/                   PlaywrightContainer, TestObject
        │   ├── dto/, enums/             StudentDto, Gender, Hobbies
        │   └── pages/                   PageHolder, StudentRegistrationPage, SubmittedFormModal
        └── test/java/com/flamingo/ui/
            ├── basic/                   Application, TraceUploader
            ├── assertions/              SubmittedFormAssertions
            └── tests/                   BaseTest + StudentRegistrationFormTests
```

## What you need

- JDK 21+
- Git
- Internet access — the tests hit real services (Restful-booker, Hygraph, demoqa.com)
- ~200 MB free disk for Chromium when Playwright first downloads it

You don't need Gradle installed — `./gradlew` handles that.

## Quick start

```bash
git clone <repo-url>
cd flamingo

./gradlew test                # run everything
./gradlew allureServe         # open a report in the browser
```

## Running tests

Everything at once — `./gradlew test`. Otherwise:

```bash
./gradlew :api-tests:test                                        # only API
./gradlew :ui-tests:test                                          # only UI
./gradlew :api-tests:test --tests "com.flamingo.tests.rest.*"     # REST only
./gradlew :api-tests:test --tests "com.flamingo.tests.graphql.*"  # GraphQL only
```

Single class or method:

```bash
./gradlew :api-tests:test --tests "com.flamingo.tests.graphql.GraphQLPositiveTest"
./gradlew :api-tests:test --tests "com.flamingo.tests.graphql.GraphQLPositiveTest.shouldReturnSingleMovieById"
```

Add `--continue` if you want Gradle to keep running past the first failure —
useful when you're pushing a batch of changes and want to see everything that
breaks in one go.

### By tags

Tests are tagged so you can slice the run:

| Tag | Coverage |
|---|---|
| `graphql` | GraphQL tests |
| `rest` | REST (booking) |
| `positive` / `negative` | happy vs sad paths |
| `ui` | Playwright tests |

Pass a JUnit tag expression via `-Djunit.jupiter.tags`:

```bash
./gradlew :api-tests:test -Djunit.jupiter.tags="positive"
./gradlew :api-tests:test -Djunit.jupiter.tags="graphql & negative"
./gradlew test              -Djunit.jupiter.tags="smoke"
```

The full expression syntax is `A & B`, `A | B`, `!A` and parentheses.

### From GitHub Actions

`.github/workflows/tests.yml` runs on push and PR to `main`, and can also be
triggered manually. In the Actions tab pick **Tests → Run workflow** and you
get two dropdowns:

- **Module** — `all` / `api` / `ui`
- **Tag** — `all` / `smoke` / `booking` / `regression` / `positive` / `graphql` / `rest` / `ui`

`all` in the tag field means no filter — the module's whole suite runs.

The pipeline runs the selected module, uploads `allure-results` from every
job, merges them into a single report and (on `main` or manual dispatch)
deploys it to GitHub Pages.

## Continuous integration

When the workflow finishes:

- **Every job** uploads its `allure-results` as an artifact — you can grab
  them individually from the run's Summary page if you only need one suite.
- **The `allure-report` job** merges all results, generates a full HTML
  report using the Allure CLI (v2.29.0), preserves the last ~20 runs of
  history, and — for `main` and manual dispatch — deploys it to the
  `gh-pages` branch.
- **UI job** additionally uploads `playwright-artifacts` (traces + videos)
  as a separate artifact so you can download them independently of Allure.
- The live report lives at `https://<user>.github.io/<repo>/`. First run
  after enabling Pages takes a minute or two — subsequent updates are near
  instant.

If a test fails on CI and you want to see the browser trace, video and
screenshot — see
[docs/viewing-failed-test-attachments.md](docs/viewing-failed-test-attachments.md).

## Allure report

Locally after a run:

```bash
./gradlew :api-tests:allureServe    # opens a live server
./gradlew :api-tests:allureReport   # dumps static HTML into build/reports/allure-report/
./gradlew allureServe               # aggregated across modules
```

In CI the report is published at `https://<user>.github.io/<repo>/` and the
last ~20 runs are kept for trend graphs.

What ends up attached to each test:

- REST and GraphQL — the raw HTTP request/response, via `AllureRestAssured`.
- UI (on failure) — screenshot, Playwright trace ZIP, video, plus a direct
  link that opens the trace in `trace.playwright.dev`.
- `@Step` methods on services and pages show up as individual steps.
- `@Epic` / `@Feature` / `@Story` group tests in the Behaviours tab.

## Parallel execution

Both modules have a `junit-platform.properties` with parallel execution on:

```properties
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = concurrent
junit.jupiter.execution.parallel.mode.classes.default = concurrent
junit.jupiter.execution.parallel.config.strategy = dynamic
junit.jupiter.execution.parallel.config.dynamic.factor = 1
```

Test instances are per-method (JUnit default), so tests don't share state
through fields.

The API side is straightforward: auth tokens sit in a `ThreadLocal`, HTTP
specs are built per call and the base URL is set once in `@BeforeAll` and
never mutated afterwards.

UI needs a bit more care — one `Playwright` and `Browser` are shared across
the JVM, but each test gets its own `BrowserContext` and `Page` through a
`ThreadLocal`. That way you avoid spawning a Chromium per test but still
keep tests isolated. If you're running on a small CI runner (2 vCPU) and
this feels too aggressive, switch to a fixed pool:

```properties
junit.jupiter.execution.parallel.config.strategy = fixed
junit.jupiter.execution.parallel.config.fixed.parallelism = 2
```

## Configuration

Both modules use [aeonbits.owner](http://owner.aeonbits.org/) to load config
from properties files and let you override anything from the command line.

There are two configs — one per module:

| Module | Interface | Properties file |
|---|---|---|
| API | [`ProjectConfig`](api-tests/src/main/java/org/flamingo/ProjectConfig.java) | [`api-tests/src/main/resources/config.properties`](api-tests/src/main/resources/config.properties) |
| UI  | [`UiProjectConfig`](ui-tests/src/main/java/com/flamingo/ui/UiProjectConfig.java) | [`ui-tests/src/main/resources/ui-config.properties`](ui-tests/src/main/resources/ui-config.properties) |

### Keys you can set

API side:

| Key | Default | What it does |
|---|---|---|
| `baseUrl` | `https://restful-booker.herokuapp.com` | REST base URL |
| `baseGraphQLUrl` | Hygraph CDN URL | GraphQL endpoint |
| `admin.username` | `admin` | login for authenticated REST calls |
| `admin.password` | `password123` | password to match |
| `logging` | `true` | request/response logging in the console |

UI side:

| Key | Default | What it does |
|---|---|---|
| `baseUrl` | `https://demoqa.com/automation-practice-form` | site under test |
| `headless` | `true` | run Chromium without a window |

### How Owner picks a value

In order:

1. Whatever you pass in `ConfigFactory.create(cls, System.getProperties())` —
   that's how `-D` flags win.
2. The files listed in `@Config.Sources`.
3. `@DefaultValue` on the interface method.

### Overriding at runtime

The most common thing you'll do is override with `-D`. Gradle forwards a
short allow-list of keys (`headless`, `baseUrl`, `baseGraphQLUrl`, `logging`,
`env`) from the CLI into the forked test JVM — that's in the `test { ... }`
block of the root `build.gradle`.

```bash
./gradlew :ui-tests:test  -Dheadless=false
./gradlew :api-tests:test -DbaseUrl=https://staging.example.com
./gradlew :api-tests:test -Dlogging=false
./gradlew test            -DbaseUrl=https://staging.example.com -Dheadless=false
```

For a permanent local default just edit the properties file. For a one-off
tweak inside code — `System.setProperty(...)` before you create the config,
though avoid that in parallel tests (it's global state).

### Adding a new key

1. Add the accessor to the Owner interface:
   ```java
   @Key("http.timeout.ms")
   @DefaultValue("10000")
   int httpTimeoutMs();
   ```
2. Put a default in the matching `.properties` file.
3. Add the key to the allow-list inside `test { ... }` in `build.gradle`
   if you want it to be `-D`-overridable:
   ```groovy
   ['headless', 'baseUrl', 'baseGraphQLUrl', 'logging', 'env', 'http.timeout.ms']
       .each { key ->
           String value = System.getProperty(key)
           if (value != null) systemProperty(key, value)
       }
   ```
4. Read it as `config.httpTimeoutMs()`.

### One thing not to do

Don't blindly forward every system property with
`systemProperties = System.getProperties()`. That replaces the map on the
test JVM and wipes properties that Gradle plugins (Allure especially) set
themselves — you'll see it as `FileNotFoundException: allure-results/*.json`.
Explicit allow-list is the safe pattern.

## Where things live

**REST**
- [`BaseRestService`](api-tests/src/main/java/org/flamingo/services/rest/BaseRestService.java) — token in a `ThreadLocal`, common spec setup.
- [`BookingService`](api-tests/src/main/java/org/flamingo/services/rest/BookingService.java) — `/booking` CRUD.
- [`AuthService`](api-tests/src/main/java/org/flamingo/services/rest/AuthService.java) — `/auth` for tokens.

**GraphQL**
- [`GraphQLClient`](api-tests/src/main/java/org/flamingo/clients/GraphQLClient.java) — reads a `.gql` file, posts it.
- [`MovieService`](api-tests/src/main/java/org/flamingo/services/graphql/MovieService.java) — domain wrappers around the queries.
- [`AssertableGraphQLResponse`](api-tests/src/main/java/org/flamingo/assertions/graphql/AssertableGraphQLResponse.java) — `body/asPojo/asList/errorMessages` with an automatic `data.` prefix.
- `.gql` files live in [api-tests/src/test/resources/graphql/](api-tests/src/test/resources/graphql/).

**Composition root**
- [`ApiServices`](api-tests/src/main/java/org/flamingo/services/ApiServices.java) — where `HttpSpecs`, REST services and the GraphQL side are wired together.

**UI**
- [`PlaywrightContainer`](ui-tests/src/main/java/com/flamingo/ui/basic/PlaywrightContainer.java) — shared `Playwright + Browser`, per-thread `BrowserContext + Page`.
- [`PageHolder`](ui-tests/src/main/java/com/flamingo/ui/pages/PageHolder.java) — `page()` as a method (not a cached field) so stale references after `context.close()` can't happen.
- [`StudentRegistrationPage`](ui-tests/src/main/java/com/flamingo/ui/pages/StudentRegistrationPage.java) and [`SubmittedFormModal`](ui-tests/src/main/java/com/flamingo/ui/pages/SubmittedFormModal.java) — the page objects.
- [`BaseTest`](ui-tests/src/test/java/com/flamingo/ui/tests/BaseTest.java) — on failure attaches screenshot/trace/video, only closes the `BrowserContext` (the shared `Browser` stays alive).
- [`TraceUploader`](ui-tests/src/test/java/com/flamingo/ui/basic/TraceUploader.java) — pushes the trace ZIP to `0x0.st` and drops a `trace.playwright.dev` link into the Allure report.

## Writing a new test

Three common shapes come up here — a REST endpoint, a GraphQL query and a UI
scenario. The pattern is the same in all three: pick the right base class,
call into an existing service, and let the assertions and reporting
plumbing do the rest.

### A new REST test

Say you want to test `GET /booking?firstname=John`.

1. If there's no service method yet, add one to
   [`BookingService`](api-tests/src/main/java/org/flamingo/services/rest/BookingService.java):
   ```java
   @Step("Search bookings by first name '{firstName}'")
   public AssertableResponse searchByFirstName(String firstName) {
       return new AssertableResponse(
           setUp().queryParam("firstname", firstName).get(BOOKING)
       );
   }
   ```
   `setUp()` gives you a `RequestSpecification` already primed with the base
   URL, JSON content type, Allure filter and the auth cookie if you're
   logged in. `AssertableResponse` is the fluent wrapper the tests use.

2. Write the test somewhere under
   [`com.flamingo.tests.rest.booking`](api-tests/src/test/java/com/flamingo/tests/rest/booking/),
   extending `BaseApiTests`:
   ```java
   @Tag("rest")
   @Epic("REST API")
   @Feature("Booking")
   @DisplayName("Search bookings")
   class SearchBookingTest extends BaseApiTests {

       @Test
       @DisplayName("returns bookings filtered by first name")
       void shouldReturnBookingsFilteredByFirstName() {
           api.rest().bookingService()
                   .searchByFirstName("John")
                   .shouldHave(Conditions.statusCode(200))
                   .asList(BookingId.class)
                   .forEach(id -> assertThat(id.getBookingId()).isPositive());
       }
   }
   ```

3. If the endpoint needs auth, call `loginAsDefaultUser()` first — the token
   ends up in the `ThreadLocal` that `setUp()` reads.

### A new GraphQL test

1. Drop the query into
   [`api-tests/src/test/resources/graphql/`](api-tests/src/test/resources/graphql/):
   ```graphql
   # graphql/searchMovies.gql
   query SearchMovies($title: String!) {
     movies(where: { title_contains: $title }) {
       id
       title
     }
   }
   ```

2. Add a domain method to
   [`MovieService`](api-tests/src/main/java/org/flamingo/services/graphql/MovieService.java):
   ```java
   private static final String SEARCH_MOVIES = "graphql/searchMovies.gql";

   @Step("Search movies by title '{title}'")
   public List<Movie> searchByTitle(String title) {
       return client.executeGql(SEARCH_MOVIES, Map.of("title", title))
               .then()
               .asList("movies", Movie.class);
   }
   ```
   The path you pass to `asList` / `asPojo` is relative to the `data` node —
   the wrapper adds the `data.` prefix for you.

3. Add the test:
   ```java
   @Tag("graphql")
   @Tag("positive")
   @Epic("GraphQL API")
   @Feature("Movies query")
   class MovieSearchTest extends BaseApiTests {

       @Test
       void shouldReturnMoviesMatchingTitleFragment() {
           List<Movie> movies = api.graphQL().movies().searchByTitle("Jaws");

           assertThat(movies).isNotEmpty();
           assertThat(movies).allMatch(m -> m.getTitle().contains("Jaws"));
       }
   }
   ```

For error paths use `api.graphQL().client().executeGql(...)` directly and
work with `errorMessages()` — that avoids the DTO deserialization.

### A new UI test

1. If you're adding a whole new page, extend
   [`PageHolder`](ui-tests/src/main/java/com/flamingo/ui/pages/PageHolder.java)
   so `page()` gives you the current live Playwright `Page`. Prefer
   `page().getByRole(...)` over CSS selectors — it's more resilient.

2. Wire the page into
   [`Application`](ui-tests/src/test/java/com/flamingo/ui/basic/Application.java)
   so tests can reach it through the `app` field on `BaseTest`.

3. The test itself just extends `BaseTest` — that's where the browser
   lifecycle, artifact collection and Allure hooks live:
   ```java
   @Tag("ui")
   @Epic("Student UI")
   @Feature("Registration form")
   class LoginFormTest extends BaseTest {

       @Test
       void shouldSubmitFormSuccessfully() {
           StudentDto student = StudentDto.builder()
                   .firstName(faker.name().firstName())
                   .lastName(faker.name().lastName())
                   .gender(Gender.OTHER)
                   .mobile(faker.phoneNumber().cellPhone())
                   .build();

           SubmittedFormModal modal = app.getStudentRegistrationPage()
                   .open()
                   .fillStudentData(student)
                   .submit();

           SubmittedFormAssertions.assertThatSubmittedFormMatches(modal, student);
       }
   }
   ```

Screenshot, trace and video are attached automatically when the test fails
— you don't need to touch them from inside the test.

### Little things worth doing

- **Tag every test.** `@Tag("rest")`, `@Tag("graphql")`, `@Tag("positive")` etc.
  Otherwise the workflow's tag filter can't find them.
- **`@DisplayName`** on the class and the method. That's what Allure shows.
- **`@Epic` / `@Feature` / `@Story`** if you want them grouped nicely in
  Allure's Behaviours tab. Not mandatory.
- Domain-level assertions live under `assertions/` (e.g.
  `BookingAssertions`, `SubmittedFormAssertions`). If you find yourself
  copying the same `assertThat` chain across three tests, that's the place
  for it.
- Test data belongs in `testData/` — `BookingTestData.defaultBooking(faker)`
  and friends. Keep the tests focused on the scenario, not on building
  payloads.

## Further reading

[`docs/api-tests-architecture.md`](docs/api-tests-architecture.md) goes deeper
into the API module — the DI graph, auth flow, and a walk-through for adding
a new endpoint with its tests.

## A few things worth knowing

- Faker isn't seeded, so a random `String` from one run won't match the next.
  If you need reproducibility, seed it: `new Faker(new Locale("en"), new Random(seed))`.
- Restful-booker is a public sandbox — expect the occasional 5xx and retry it.
- The Hygraph endpoint is read-only. Mutation tests against it fail with
  "mutation not supported" and that's expected.
- demoqa.com is full of ads and popups. `removeFixedBanners()` and
  Playwright's auto-wait keep the UI tests reliable enough.
