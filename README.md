# QuizMania

A full-stack, Java Web-based online quiz platform for any topic — timed
quizzes, automatic scoring, detailed performance reports, a moderation workflow, messaging,
reminders and a leaderboard. Built with Java 17, Servlets 6, JSP/JSTL, JDBC and MySQL 8.

## Roles & workflow

- **Admin** — manages user accounts, approves/rejects quiz content submitted by creators,
  configures system-wide settings, and reviews platform-wide performance reports.
- **Quiz Creator** — builds quizzes (title, duration, multiple-choice questions with an
  explanation for each), submits them for approval, reviews/grades submitted attempts with
  written feedback, and messages participants directly.
- **Participant** — browses approved quizzes, takes them under a **server-enforced timer**,
  gets an instant score and a question-by-question report, sets reminders, messages creators,
  and tracks their standing on the leaderboard.

Quiz lifecycle: `DRAFT → PENDING → APPROVED` (visible to participants) `| REJECTED`
(creator can edit and resubmit).

## Highlights

- **Security** — PBKDF2-hashed passwords (no plain text anywhere), CSRF tokens on every POST,
  session fixation protection, role-based access filters, brute-force login throttling.
- **Server-enforced timer** — the countdown is measured by the database clock from
  `started_at`, so editing the browser's JavaScript cannot buy extra time; a late submission
  is recorded without answers.
- **Live dashboards** — stat tiles, bar/line charts and leaderboards built with pure CSS/SVG
  (no external chart library), with dark mode and a fully responsive layout.
- **Background reminders** — a daemon thread (`ScheduledExecutorService`) turns due quiz
  reminders into in-app notifications every 30 seconds.
- **Messaging & notifications** — creators and participants can message each other directly;
  admins get a "System Alerts" feed for registrations, submissions and approvals.
- **Connection pooling** — a small hand-rolled JDBC pool (`DBConnection`) avoids opening a new
  database connection per request.

## Tech stack

Java 17 · Jakarta Servlet 6.0 · JSP + JSTL · JDBC (MySQL Connector/J) · MySQL 8+ ·
Maven · Apache Tomcat 10.1+ (Jakarta EE namespace) · vanilla CSS/JS (no frontend build step).

## Project structure

```
src/main/java/com/quiz/
  model/      Role, QuizStatus, User (abstract) + Admin/QuizCreator/Participant, Quiz<T>,
              Question, QuizResult, Scorable, AnswerDetail, Message, Contact, Notification,
              Reminder, LeaderboardEntry
  dao/        UserDAO, QuizDAO, ResultDAO, MessageDAO, NotificationDAO, ReminderDAO,
              SettingsDAO, StatsDAO (+ CrudDAO<T> interface)
  service/    QuizGrader (pure scoring logic), PerformanceSummary, NotificationService,
              ReminderService
  servlet/    BaseServlet (template method) + admin/, creator/, participant/, common/
  filter/     AuthFilter (login + CSRF + cache headers), RoleFilter (per-area access)
  listener/   AppLifecycleListener (starts the reminder scheduler)
  util/       DBConnection (pool), PasswordUtil (PBKDF2), Validator, AppSettings (cached
              settings), LoginThrottle, UserFactory, SettingDef
  exception/  QuizException, ValidationException
src/main/webapp/
  WEB-INF/views/   JSPs, organised by area (admin/creator/participant/common)
  WEB-INF/jspf/    shared header/footer/answer-list fragments
  css/style.css    design system (tokens, dark mode, responsive)
  js/app.js        toasts, theme toggle, quiz builder, quiz timer, chat, tables, modals
database/
  schema.sql        tables, foreign keys, indexes
  sample-data.sql    demo accounts + sample quizzes/attempts (PBKDF2-hashed passwords)
```

## OOP & language coverage (Review 1 rubric)

| Concept | Where |
|---|---|
| Inheritance | `User` (abstract) → `Admin` / `QuizCreator` / `Participant`; `ValidationException extends QuizException` |
| Polymorphism | `getRoleDescription()` overridden per role; `UserFactory` returns the right subtype at runtime |
| Interfaces | `CrudDAO<T>`, `Scorable` (default methods for percentage/grade) |
| Exception handling | Checked `QuizException`/`ValidationException`, caught centrally in `BaseServlet`, friendly error page |
| Generics | `Quiz<T extends Question>`, `CrudDAO<T>` |
| Collections | `List`, `Map`, `EnumMap`, `TreeSet`, used throughout the DAO/service layer |
| Multithreading & synchronization | `ScheduledExecutorService` daemon thread for reminders; `synchronized` methods in `ReminderService`/`AppSettings`; a lock-free `ConcurrentHashMap` settings cache; a hand-rolled JDBC connection pool using `BlockingQueue`/`AtomicInteger` |
| DB operation classes | One DAO class per entity (`UserDAO`, `QuizDAO`, `ResultDAO`, ...), all using `PreparedStatement` and transactions (`QuizDAO.create`, `ResultDAO.saveSubmission`, `SettingsDAO.saveAll`) |
| JDBC | `DBConnection` (driver + pool), every query parameterised, batched inserts for questions/answers |
| Servlets & web integration | `@WebServlet`-annotated servlets, `Filter`s, `HttpSession`, `RequestDispatcher`, role-based URL protection in `web.xml` |

## Setup

1. Install JDK 17+, Maven 3.6+, MySQL 8+ and Apache Tomcat 10.1+ (needs the **Jakarta**
   namespace — Tomcat 10.1.x, not 9 or 10.0).
2. Create the schema and load sample data:
   ```sh
   mysql -u root -p < database/schema.sql
   mysql -u root -p < database/sample-data.sql
   ```
3. Configure the database connection in `src/main/resources/db.properties`, **or** leave the
   file as-is and set environment variables instead (handy for containers/CI):
   `QUIZ_DB_URL`, `QUIZ_DB_USER`, `QUIZ_DB_PASSWORD`.
4. Build the WAR:
   ```sh
   mvn clean package
   ```
5. Deploy `target/online-quiz-platform.war` to Tomcat 10.1 (copy it into `webapps/`, or run
   `mvn tomcat7:run` if you add the plugin).
6. Open `http://localhost:8080/online-quiz-platform/`.

### Demo accounts (password shown, hashed in the database)

| Role | Email | Password |
|---|---|---|
| Admin | `admin@quiz.com` | `admin123` |
| Quiz Creator | `creator@quiz.com` (also `creator2@quiz.com`) | `creator123` |
| Participant | `participant@quiz.com` (also `diya@`, `rohan@`, `sneha@`, `kabir@quiz.com`) | `participant123` |

Sample data includes 7 quizzes across every status (draft/pending/approved/rejected), 18
graded attempts spread over the last two weeks, sample chat messages, notifications and
reminders, so every dashboard has something to show immediately after setup.

## Notes

- Passwords are hashed with PBKDF2-HMAC-SHA256 (65,536 iterations, random salt per user) —
  never stored or logged in plain text. `PasswordUtil.main(String... passwords)` can print
  hashes for new seed data.
- The system settings page controls the platform name, the public sign-up switch, max
  attempts per quiz, default quiz duration, the pass mark and an announcement banner — all
  editable without a redeploy.
