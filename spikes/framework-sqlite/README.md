# Spike: Java server framework with SQLite

Branch: spike/framework-sqlite

## Goal
Decide how FlatFlow's Java backend serves its JSON API: Javalin, or the JDK's
built-in HttpServer. Also confirm that SQLite data survives a server restart.

## Same code, two web layers
- Chore.java, ChoreRepository.java: shared. SQLite access with parameterized queries.
- JavalinApp.java: Version A. Javalin does the routing and the JSON (Jackson). Port 7070.
- JdkApp.java: Version B. JDK HttpServer, JSON written by hand. Port 7071.

## Run it (needs a JDK 17+ and Maven: sudo apt install maven)
    mvn compile exec:java -Dexec.mainClass=JavalinApp
In a second terminal:
    ./try-it.sh 7070
Stop the server (Ctrl+C), start it again, run `curl localhost:7070/api/chores`
and check the chores are still there. Then repeat with `-Dexec.mainClass=JdkApp`
and `./try-it.sh 7071`.

## Findings from running the plain JDK version (before it was packaged)
- GET and POST work, validation returns HTTP 400, other methods return 405.
- Data survives a restart (checked).
- A SQL injection attempt in the title is stored as plain text: the table is not harmed,
  because the queries are parameterized.
- BUG (found by running it, left in on purpose): a title with quotes comes back with extra
  backslashes, because the hand-written JSON parser never unescapes the input and the
  writer then escapes it again. A real JSON library does not have this problem.
- A missing field gives the unhelpful error "Cannot parse null string".

## Javalin vs Plain JDK
| | Javalin | Plain JDK |
|---|---|---|
| Lines in the web layer |15 | 69 |
| Quotes in a title handled correctly? | Yes, Clean "bathroom" came back correctly without any errors | no (see bug) |
| Error message for a missing field |"Assignee is required" | "Cannot parse null string" |
| Anything that failed or was hard |No, for the Javalin app nothing failed and everything ran smoothly with no errors |It required a handwritten JSON parser and writer. It doesn't handle quotes in a title very well, as the parser never escapes quotes so titles with quotes gets corrupted and missing fields return a poor, unhelpful error message.  |
