# Campus Course & Records Manager (CCRM)

## Project Overview
A single-file console-based Java SE application for managing students, courses, enrollments, grades, and file operations. Built with OOP principles (Encapsulation, Inheritance, Abstraction, Polymorphism), NIO.2, Streams, Date/Time API, enums, lambdas, recursion, design patterns (Singleton & Builder), custom exceptions, and assertions.

This project meets all functional requirements (student/course management, enrollment/grading, file import/export/backup) and technical requirements from the project statement.

## How to Run
1. Ensure JDK 17+ is installed (steps below).
2. Download `CCRM.java` from this repo.
3. In a terminal/Command Prompt, navigate to the folder with `CCRM.java`.
4. Compile: `javac CCRM.java`
5. Run: `java -ea CCRM` (the `-ea` enables assertions).
6. The app creates a `data/` folder automatically for files/backups.

JDK version: 17 or later.

## Evolution of Java (Short Timeline)
- 1995: Java 1.0 released by Sun Microsystems.
- 2000: Introduction of J2ME, J2SE, J2EE.
- 2004: Java 5 adds generics.
- 2014: Java 8 introduces lambdas and Streams.
- 2017: Java 9 adds modules.
- 2023: Java 21 with ongoing updates.

## Java ME vs SE vs EE Comparison
| Edition | Purpose | Examples |
|---------|---------|----------|
| ME (Micro) | Embedded and mobile devices | Games on old phones |
| SE (Standard) | Desktop and general-purpose apps | This CCRM app |
| EE (Enterprise) | Server-side and large-scale apps | Web applications with Spring |

## Java Architecture: JDK, JRE, JVM
- JVM (Java Virtual Machine)**: Executes compiled Java bytecode, handles memory and garbage collection.
- JRE (Java Runtime Environment)**: Includes JVM + core libraries to run Java apps.
- JDK (Java Development Kit)**: Includes JRE + tools like javac (compiler) for developing apps.
Interaction: Write code → Compile with JDK (javac) → Bytecode → Run with JRE/JVM.

## Install & Configure Java on Windows (Steps + Screenshot)
1. Download JDK 17+ from [oracle.com/java](https://www.oracle.com/java/technologies/downloads/).
2. Run the installer and note the install path (e.g., C:\Program Files\Java\jdk-17).
3. Add to PATH: Right-click This PC > Properties > Advanced system settings > Environment Variables > Edit "Path" > Add `%JAVA_HOME%\bin` (set JAVA_HOME to your install path first).
4. Verify: Open Command Prompt, type `java -version`.

## Using Eclipse IDE: New Project Creation, Run Configs (Steps + Screenshots)
1. Download Eclipse from [eclipse.org](https://www.eclipse.org/downloads/).
2. Launch Eclipse and create a workspace.
3. File > New > Java Project > Name: CCRM > Finish.
4. Right-click src > New > Class > Name: CCRM > Paste code > Save.
5. Run: Right-click CCRM.java > Run As > Java Application.

[Screenshot: screenshots/eclipse-setup.png]

[Screenshot: screenshots/program-running.png] (shows the CLI menu)

## Functional and Technical Demonstrations (Mapping Table)
| Syllabus Topic | Location in Code |
|---------------|------------------|
| Primitive variables, objects, operators (arithmetic, relational, logical, bitwise), precedence | Validator class comments and methods |
| Decision structures (if/else, switch) | CCRM.start() method (switch menu) |
| Loops (while/do-while/for/enhanced-for), break/continue/labeled jump | CCRM.start() (while with labeled outer), manageStudents() (do-while), manageCourses() (for with continue) |
| Arrays and Arrays class (sort/search) | StudentService.getSortedRegNos() |
| Strings & methods (substring, split, etc.) | ImportExportService.importStudentsFromCSV() (split) |
| Encapsulation | Person class (private fields + getters/setters) |
| Inheritance & super | Student/Instructor extend Person |
| Abstraction | Person abstract class with abstract getRole() |
| Polymorphism | toString() overrides, TranscriptService.generateTranscript() |
| Access levels (private/protected/default/public) | Used in Person and subclasses |
| Immutability | CourseCode class (final fields, defensive copying) |
| Top-level & nested classes | AppConfig.NestedLogger (static nested), RecursionUtil.SizeAccumulator (inner) |
| Interfaces & default methods | Persistable, Searchable<T> (with defaults) |
| Functional interfaces & lambdas | CourseService.getSortedByTitle() (comparator lambda) |
| Anonymous inner classes | CCRM.inputHandler |
| Enums with constructors/fields | Semester, Grade |
| Upcast/downcast & instanceof | EnrollmentService.printTranscript() |
| Overriding/overloading | Person.toString() overrides |
| Design patterns: Singleton & Builder | AppConfig (Singleton), Course.Builder |
| Exceptions (checked/unchecked, try/catch/finally/multi-catch/throw/throws) | Custom: DuplicateEnrollmentException (checked), MaxCreditLimitExceededException (unchecked); Demo in CCRM.demoExceptions() |
| Assertions | Person constructor, StudentService.addStudent() (enable with -ea) |
| File I/O (NIO.2 + Streams) | ImportExportService (Files.lines, Streams), BackupService (Files.copy, Path) |
| Date/Time API | LocalDate in Person/Enrollment, LocalDateTime in BackupService |
| Recursion | RecursionUtil.computeDirectorySize() and listFilesRecursively() |

## Notes on Enabling Assertions
Run with `java -ea CCRM` to enable assertions for invariants (e.g., non-null checks).

## Acknowledgements
Inspired by general AI guidance for structure; core implementation and modifications by kiran. No other references used.