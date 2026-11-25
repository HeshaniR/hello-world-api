# Hello World API

A small Spring Boot service with one endpoint, `GET /hello-world?name=<name>`, that greets
names starting with **A–M** and rejects everything else.

| Request                          | Status | Body                           |
|----------------------------------|--------|--------------------------------|
| `/hello-world?name=alice`        | 200    | `{"message":"Hello Alice"}`    |
| `/hello-world?name=zoe`          | 400    | `{"error":"Invalid Input"}`    |
| `/hello-world` / `?name=`        | 400    | `{"error":"Invalid Input"}`    |

## Prerequisites

- **JDK 21** or newer, on your `PATH` (or `JAVA_HOME` pointing to it). Check with `java -version`.
- Nothing else: Maven is provided by the bundled Maven Wrapper (`mvnw` / `mvnw.cmd`), which
  downloads the right Maven version on first use. The first build needs internet access.

## Tech stack

- Java 21
- Spring Boot 3.5 (Spring Web)
- Maven (via the Maven Wrapper)
- JUnit 5, AssertJ, MockMvc, Mockito

## How to run the application

Run all commands from the project root (the folder containing `pom.xml`).

```bash
./mvnw spring-boot:run          # macOS / Linux / Git Bash
.\mvnw.cmd spring-boot:run      # Windows PowerShell
```

The service starts on port `8080`. Stop it with `Ctrl+C`.

To use a different port:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9090          # macOS / Linux / Git Bash
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=9090"    # Windows PowerShell (quotes required)
```

Or build a jar and run it:

```bash
./mvnw clean package            # macOS / Linux / Git Bash
.\mvnw.cmd clean package        # Windows PowerShell
java -jar target/typeBDigital-1.0.0.jar
java -jar target/typeBDigital-1.0.0.jar --server.port=9090   # different port
```

## How to call the API

```bash
# 200 {"message":"Hello Alice"}
curl -i "http://localhost:8080/hello-world?name=alice"

# 400 {"error":"Invalid Input"}
curl -i "http://localhost:8080/hello-world?name=zoe"
curl -i "http://localhost:8080/hello-world"
```

> **Windows PowerShell 5.1:** `curl` is an alias for `Invoke-WebRequest` there. Use `curl.exe`
> instead, or `Invoke-RestMethod "http://localhost:8080/hello-world?name=alice"`.

You can also open `http://localhost:8080/hello-world?name=alice` in a browser.

Special characters in the name must be URL-encoded. Notably `+` means a space in a query
string: `?name=+alice` is received as `" alice"`; a literal plus is `%2B`.

## How to run the tests

```bash
./mvnw test                     # macOS / Linux / Git Bash
.\mvnw.cmd test                 # Windows PowerShell

# a single test class
./mvnw test -Dtest=GreetingServiceTest
```

## Rules and edge cases

- **Missing or empty**: no `name`, `?name=`, or a name made only of whitespace returns
  `400 Invalid Input`.
- **Whitespace**: leading/trailing whitespace is trimmed before validation (`"  alice "` → `Hello Alice`).
  This includes non-breaking and other Unicode spaces such as U+00A0, U+2007, U+202F and U+3000
  (e.g. `%C2%A0alice` → `Hello Alice`), which often arrive with copy-pasted text.
  Spaces inside the name are kept (`anna maria` → `Hello Anna maria`).
- **English letters only**: the first character must be an ASCII letter `A–M`/`a–m`.
  Names starting with digits, symbols or non-English letters (`1alice`, `_bob`, `Élodie`, `ıvan`)
  return `400 Invalid Input`.
- **Capitalisation**: only the first letter is upper-cased; the rest is kept as given
  (`alice` → `Alice`, `mcDonald` → `McDonald`, `ALICE` → `ALICE`).
- **Characters after the first** are not validated; the rule only concerns the first letter.
  A single value containing a comma (`?name=alice,zoe`) is one name: `Hello Alice,zoe`.
- **Repeated parameter** (`?name=alice&name=zoe`) returns `400 Invalid Input`.
- **Unencoded special characters** such as `^ { } | [ ] \` and the backtick are accepted in the query
  string (configured in `application.properties`), so `?name=^alice` gets the normal JSON
  `400 Invalid Input` instead of the web server's HTML error page.
- **Error responses are always JSON**, even if the client's `Accept` header excludes JSON.
  Successful responses follow normal content negotiation (e.g. `Accept: text/plain` → 406).
- Requests that never reach the endpoint (wrong path, wrong HTTP method) keep Spring Boot's
  default responses (404 / 405).

## Logging

Every rejected request is logged at `WARN` with the method, path and reason, for example:

```
WARN ... GlobalExceptionHandler : Rejected GET /hello-world: "Name must start with a letter between A and M"
```

The query string is not logged, so submitted names (personal data) do not end up in logs.
Clients always receive only the generic `{"error":"Invalid Input"}` body. To quiet these logs,
set `logging.level.com.example.helloworld.controller.GlobalExceptionHandler=ERROR`.
