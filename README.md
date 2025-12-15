# Chess App

Java 8 compatible Spring Boot (2.7.x) chess platform featuring live play, training puzzles, and AI opponents rendered with Thymeleaf and WebSockets.

## Project Overview

Chess App delivers a browser-based experience that combines competitive play, interactive tactics training, and AI-assisted practice. Players can launch human-vs-human games with random color assignment, queue up matches against an AI opponent, or dive into the puzzle gallery to solve curated tactics. Power users can even build their own puzzles with validation and rating ranges, making it easy to expand the training catalog. Real-time boards update through WebSockets, and every match result is summarized with clear end-game reasoning.

## Prerequisites

- Java Development Kit 8 (1.8.0_202 or newer)
- Maven 3.6+
- Git
- Eclipse IDE for Enterprise Java Developers (2023-09 or newer)

Confirm your toolchain:

```bash
java -version
mvn -version
git --version
```

## Project Setup

### Clone the repository

```bash
git clone https://github.com/vishwaskarale83/ChessProject.git
cd ChessProject
```

### First-time Maven build

```bash
mvn clean install
```

This resolves dependencies and compiles against Java 8.

## Running the Application

### From Eclipse

1. Launch Eclipse and ensure Java 8 is the active JRE under Window → Preferences → Java → Installed JREs.
2. Import the project via File → Import → Maven → Existing Maven Projects and select the `ChessProject` directory containing pom.xml.
3. After the workspace build completes, right-click src/main/java/com/vishwask/ChessApplication.java in Package Explorer.
4. Choose Run As → Spring Boot App (or Run As → Java Application if the Spring Boot option is unavailable).
5. Watch the Console view for “Started ChessApplication” and browse to http://localhost:8081.


### From the terminal

```bash
mvn spring-boot:run
```

The application serves the UI at http://localhost:8081. Stop with Ctrl+C.

## Database Notes

- The default profile uses an on-disk H2 database at jdbc:h2:file:./chessdb. Only one JVM process should access it at a time.
- To start fresh, stop the app and delete chessdb.mv.db (and chessdb.trace.db if present); the schema will regenerate on next launch.
- For stateless development or test runs, switch spring.datasource.url in [src/main/resources/application.properties](src/main/resources/application.properties) to jdbc:h2:mem:chessdb;DB_CLOSE_DELAY=-1 and restart.

## Running Tests

Stop any running instance that might hold the H2 file lock, then execute:

```bash
mvn test
```

## Key Features

- **Live Versus Mode:** Host or join two-player games with random color assignment, synchronized clocks, and automatic result summaries when the match ends.
- **AI Practice Mode:** Challenge the built-in computer opponent from either side, using difficulty labels to find the right learning pace.
- **Puzzle Library:** Browse curated tactics with rating bands, descriptive hints, and instant validation feedback as you step through candidate moves.
- **Puzzle Builder:** Create and publish custom puzzles by defining board states, turns, and solutions; validation protects against illegal or ambiguous setups.
- **Real-Time Updates:** WebSocket sessions broadcast every move, clock update, and system message so all participants see the board change instantly.
- **Secure Accounts & Storage:** Spring Security handles registration and login with BCrypt hashing, while Spring Data JPA persists games, puzzles, and solves in H2.

## Troubleshooting

- **Port in use:** Adjust server.port in [src/main/resources/application.properties](src/main/resources/application.properties) or stop competing services.
- **H2 version mismatch:** Delete regenerated chessdb files when switching between framework versions.
- **Gradle errors in Eclipse:** Remove any accidental Gradle project import and re-import strictly as Maven.