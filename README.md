# MECCA - MEC Campus Community App

Java Swing desktop app (JDBC + SQLite) for campus event management, email/calendar notifications,
KTU activity-point tracking, leaderboards and analytics.

## Requirements
- JDK 17 or newer
- Maven 3.8+
- Git

## Run
```
git clone <repo-url>
cd mecca-app
mvn compile exec:java
```
The first run downloads dependencies (needs internet) and creates `mecca.db` automatically.
Click "Dev: open MainFrame without login" on the stub login screen to see all panels.

## Team rules (short version)
1. Never work on `main`. Branch: `feature/<yourname>-<module>`.
2. Edit only the files you own. Shared files (`schema.sql`, `Constants`, `DatabaseConnection`, `Main`, `pom.xml`) are M1 only.
3. All service methods are `public static`. Keep names and signatures exactly as in the contract.
4. Push every 1-2 hours: `git add .` -> `git commit -m "Mx: message"` -> `git pull --rebase origin main` -> `git push`.
5. Open a Pull Request into `main` at merge windows only. Only M1 merges.
6. Never commit `.db` files or `config.properties`.

## Layout
```
com.mecca.model      data classes
com.mecca.dao        database access (per owner)
com.mecca.service    business logic (static methods)
com.mecca.db         DatabaseConnection (Singleton)
com.mecca.util       Constants, UserFactory, ICSGenerator, seeders
com.mecca.ui.auth / admin / organizer / student   Swing screens
```
