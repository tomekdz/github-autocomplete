# Conventions

These rules apply to every person and every AI assistant that writes code in
this project. `AGENTS.md` and `CLAUDE.md` point here. Keep this file as the only
copy of the rules.

> This document uses ASD-STE100 (Simplified Technical English). Write all
> documents, comments, and commit messages in this project in the same style.

## 1. Product Aim

This project is a native Android autocomplete component for GitHub users and
repositories. The application includes a demo screen, but the main asset is the
GitHub component and its module boundaries.

Develop it as code that a small team can own for a long time:

- Keep public APIs small and stable.
- Keep module responsibilities clear.
- Make changes easy to review.
- Prefer behaviour that tests can prove.
- Record important decisions in the code or documentation that future
  maintainers will read.

## 2. How To Work

### Make assumptions visible

- State important assumptions before you change code.
- If a request has more than one meaning, ask a question.
- If you must guess to continue, name the guess in your answer or pull request.

### Keep changes small

- Write the smallest amount of code that solves the problem.
- Do not add features without a product reason.
- Do not add an abstraction for one caller. Add it when a second caller exists,
  or when the first caller has real complexity that the abstraction removes.
- Change only the files and lines that the task needs.
- Do not refactor adjacent code unless the task needs it.

### Protect team ownership

- Treat public APIs, Gradle modules, dependency versions, and design tokens as
  shared surfaces.
- Explain changes to shared surfaces in the pull request.
- Do not move code between modules without checking the dependency rule that the
  move changes.
- Do not hide behaviour in generated code, global state, or implicit
  side effects.

### Comments and KDoc

- Clear names and a clean structure replace most comments. Use them first.
- If a comment only explains what the code does, remove the comment or improve
  the name.
- Write a comment when the code cannot show the reason. Usual causes are:
  - a workaround for a library or tool defect;
  - a limit of an external API;
  - a decision that looks wrong until you know the cause.
- Keep KDoc on public API in reusable modules. State contracts, ranges, failure
  modes, and threading or lifecycle rules that the signature cannot show.
- Delete a comment when you change the code that it describes.
- Never keep code in a comment. Delete it. Git holds the history.

## 3. Android Practice

Use the current recommended Android practice:

- Kotlin, Jetpack Compose, and Material 3.
- MVVM with one-way data flow. State goes down. Events go up.
- Coroutines and `Flow` for asynchronous work. No callbacks for new async code.
  No `GlobalScope`.
- Hilt for dependency injection in application and integration modules.
  Constructor injection where it is possible.
- Immutable state. Use `data class` and `sealed interface` for UI state.
- Compose functions hold no business state. A caller supplies state and
  callbacks.
- Collect a `Flow` in Compose with `collectAsStateWithLifecycle()`.
- Accessibility is part of done: semantics, content descriptions, keyboard
  support where relevant, and touch targets of 48 dp.

## 4. Project Layout

```
:app                       Application class, Hilt setup, one Activity
:feature-home              Demo screen
:autocomplete:github-ui    Wired GitHub drop-in component
:autocomplete:github-data  GitHub transport, DTOs, merge and sort policy
:autocomplete:github-model GithubResult and FetchStrategy: the types a host names
:autocomplete:ui-compose   GitHub Compose UI
:autocomplete:domain       Pure Kotlin GitHub autocomplete core
:core-ui                   Theme, colour, and type
:core-testing              Hilt runner for instrumented tests
```

### Module Rules

- `:autocomplete:domain` is a pure Kotlin library. It must never import Android,
  Compose, Hilt, Retrofit, OkHttp, Coil, or GitHub transport code.
- `:autocomplete:ui-compose` must never import GitHub transport code.
- `:autocomplete:domain` and `:autocomplete:ui-compose` can depend on
  `:autocomplete:github-model`.
- A feature module may depend on a drop-in component. It must not know transport
  details that the component owns. `:autocomplete:github-ui` therefore takes
  `:autocomplete:github-data` with `implementation`, and gives the host
  `:autocomplete:github-model` with `api`. A public function of the component
  shows a type of the model module, and never a type of the transport module.

These rules are the transport boundary promise. The Gradle module graph must
hold them.

### GitHub Is The Domain

This product finds GitHub users and GitHub repositories. The shared
autocomplete state and UI can therefore use `GithubResult` and GitHub result
words such as `UsersOnly` and `ReposOnly`.

The rule is about transport boundaries:

- No Retrofit, OkHttp, DTO, token, or GitHub URL in `:autocomplete:domain` or
  `:autocomplete:ui-compose`.
- User-visible text lives in string resources.
- Shared string resources must not name tokens or GitHub quotas.

### Dependency Injection Across A Module Boundary

- A library or integration module binds no unqualified framework type.
  `Call.Factory`, `OkHttpClient`, and `Json` are types that the host application
  can also bind. Use a qualifier, for example `@GithubHttpClient Call.Factory`.
- The host supplies credentials. The integration module supplies transport.
  A host application must not name Retrofit, OkHttp, or an interceptor to use the
  component.
- A binding that the host supplies is optional when the component can work
  without it. Use `@BindsOptionalOf`, so a host that has nothing to supply writes
  no code.

### Dependency Scope

Use `api` for a dependency whose types are visible in a public signature. Use
`implementation` for all other dependencies. A consumer of an AAR must compile
against every type that a public function shows.

## 5. Build Rules

- Namespace: `com.example.tdziergwa`.
- `minSdk 24`, `compileSdk 37`, `targetSdk 37`, JVM target 17.
- All build configuration lives in convention plugins in `build-logic`. Do not
  repeat a version or an SDK level in a module build file.
- Convention plugin ids:
  `githubautocomplete.android.application`,
  `githubautocomplete.android.library`,
  `githubautocomplete.kotlin.library`,
  `githubautocomplete.android.compose`,
  `githubautocomplete.android.hilt`.
- Declare every dependency in `gradle/libs.versions.toml`. Do not write a
  coordinate or a version in a module build file.
- Run Spotless before you publish a branch or ask for review.

## 6. Tests

- Unit tests use JUnit 5 (Jupiter). The convention plugins call
  `useJUnitPlatform()`, so a JUnit 4 test in `src/test` does not run.
- Instrumented tests and Compose tests use JUnit 4.
- Use Turbine for `Flow` tests and MockWebServer for transport tests.
- Tests must not call the live GitHub API. Use GitHub-shaped fakes or
  MockWebServer.
- Add or update tests when behaviour changes, when a defect is fixed, or when a
  public contract changes.
- Prefer focused tests near the module that owns the behaviour.

## 7. Commits And Pull Requests

### Write the subject as a Conventional Commit

```
<type>(<optional scope>): <description>
```

- Types: `feat`, `fix`, `docs`, `test`, `refactor`, `perf`, `build`, `ci`,
  `chore`, `revert`.
- The scope is a module or an area, for example `domain`, `github-data`,
  `github-ui`, `ci`.
- Write the description in the imperative and in lower case. Do not put a full
  stop at the end.
- Keep the subject to 72 characters or fewer.
- A change that breaks a public API puts `!` before the colon. The pull request
  description tells the caller what to do.

```
feat(domain): add refresh to the autocomplete state
fix(github-ui): search again when the strategy changes
test(ui-compose): cover the refresh path
refactor(github-data)!: rename the github module to github-data
```

### A commit message is one line

A commit message holds the subject, and nothing else. No body, no list, and no
trailer.

- A change that needs a paragraph is more than one change. Split the commit.
- A change that still needs a paragraph puts the paragraph in the pull request
  description.
- The reason for a line of code belongs in a comment. See section 2.

`git log --oneline` then reads as the list of the work.

### An AI assistant does not sign a commit or a pull request

A person owns every commit and every pull request.

- Do not add a `Co-Authored-By` trailer for an AI assistant.
- Do not add a tool name, a session link, a badge, or a generated-by note to a
  commit message or to a pull request description.
- The person who runs the assistant is the author.

A commit message says what the change does, and not which tool wrote it. A pull
request description says what the change does and why a reviewer can trust it.

## 8. Commands

```bash
./gradlew assembleDebug
./gradlew test
./gradlew :autocomplete:github-data:test
./gradlew :autocomplete:ui-compose:connectedDebugAndroidTest
./gradlew detekt
./gradlew spotlessCheck
./gradlew lint
```

`connectedDebugAndroidTest` and module-specific connected tests need a device or
an emulator.

## 9. Secrets

The GitHub token is optional. It comes from `local.properties`, which
`.gitignore` excludes. Never commit a token. Never write a token into a source
file, a test, or a README.

- Only the debug build type receives the token. A release build compiles an empty
  token, and the component searches unauthenticated.
- The token is private, but it is not secret. A build compiles it into the APK,
  and a person who holds the APK can read it.
- A design that needs a real secret needs a server that holds the secret, or a
  token that belongs to the user.
- Continuous integration needs no token. Every test uses MockWebServer or a fake
  source.

## 10. Review Standard

Before a change merges, check:

- The module dependency rule still holds.
- Public API changes are intentional and documented.
- The behaviour has a focused test.
- Loading, empty, error, and partial success states still make sense.
- Accessibility did not regress.
- No secret, token, or local machine path entered the repository.
