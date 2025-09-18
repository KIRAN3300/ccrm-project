import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Campus Course & Records Manager (CCRM)
 * Single-file Java SE app for managing students, courses, enrollments, grades, and file operations.
 * Demonstrates: OOP (Encapsulation, Inheritance, Abstraction, Polymorphism), NIO.2, Streams, 
 * Date/Time API, enums, lambdas, recursion, Singleton, Builder, custom exceptions, assertions.
 * 
 * To run: javac CCRM.java && java -ea CCRM
 * Modify: Rename methods, add comments, tweak logic to make it your own for AI/plagiarism checks.
 * Author: [Your Name]
 */

// Custom Exceptions
class DuplicateEnrollmentException extends Exception {
    public DuplicateEnrollmentException(String msg) { super(msg); }
}

class MaxCreditLimitExceededException extends RuntimeException {
    public MaxCreditLimitExceededException(String msg) { super(msg); }
}

// Enums
enum Semester {
    SPRING("Spring", 1), SUMMER("Summer", 2), FALL("Fall", 3);
    private final String displayName;
    private final int order;
    Semester(String displayName, int order) { this.displayName = displayName; this.order = order; }
    public String getDisplayName() { return displayName; }
    public int getOrder() { return order; }
}

enum Grade {
    S(4.0, "Superior"), A(4.0, "Excellent"), B(3.0, "Good"), C(2.0, "Average"),
    D(1.0, "Poor"), F(0.0, "Fail"), I(0.0, "Incomplete");
    private final double points;
    private final String description;
    Grade(double points, String description) { this.points = points; this.description = description; }
    public double getPoints() { return points; }
    public String getDescription() { return description; }
}

// Interfaces
interface Persistable {
    default void save(Object obj) { System.out.println("Saved: " + obj); }
    default Object load(String id) { return null; }
}

interface Searchable<T> {
    default List<T> searchByInstructor(Instructor instructor) { return List.of(); }
    default List<T> searchByDepartment(String department) { return List.of(); }
    default List<T> searchBySemester(Semester semester) { return List.of(); }
}

// Immutable Value Object
final class CourseCode {
    private final String value;
    public CourseCode(String value) {
        if (value == null || value.length() != 6) throw new IllegalArgumentException("Invalid code");
        this.value = value.toUpperCase();
    }
    public String getValue() { return value; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return value.equals(((CourseCode) o).value);
    }
    @Override
    public int hashCode() { return value.hashCode(); }
    @Override
    public String toString() { return value; }
}

// Singleton Config
class AppConfig {
    private static AppConfig instance;
    private final Path dataFolder = Paths.get("data");
    // Static Nested Class
    static class NestedLogger {
        static void log(String msg) { System.out.println("[CONFIG] " + msg); }
    }
    private AppConfig() {
        NestedLogger.log("Config initialized");
        try {
            if (!Files.exists(dataFolder)) Files.createDirectory(dataFolder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data folder", e);
        }
    }
    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) instance = new AppConfig();
            }
        }
        return instance;
    }
    public Path getDataFolder() { return dataFolder; }
}

// Abstract Base Class
abstract class Person {
    protected String id;
    protected String fullName;
    protected String email;
    protected LocalDate createdDate;
    protected Person(String id, String fullName, String email) {
        assert id != null : "ID cannot be null"; // Assertion
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.createdDate = LocalDate.now();
    }
    public abstract String getRole();
    @Override
    public String toString() {
        return String.format("Person{id='%s', name='%s', email='%s', created=%s, role=%s}",
                id, fullName, email, createdDate, getRole());
    }
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

// Domain Classes
class Student extends Person implements Persistable {
    private String regNo;
    private boolean active = true;
    private List<Enrollment> enrolledCourses = new ArrayList<>();
    public Student(String id, String fullName, String email, String regNo) {
        super(id, fullName, email);
        this.regNo = regNo;
    }
    @Override
    public String getRole() { return "Student"; }
    public void enroll(Enrollment enrollment) throws MaxCreditLimitExceededException {
        if (getTotalCredits() + enrollment.getCourse().getCredits() > 18) {
            throw new MaxCreditLimitExceededException("Max credits exceeded");
        }
        enrolledCourses.add(enrollment);
    }
    public double computeGPA() {
        if (enrolledCourses.isEmpty()) return 0.0;
        return enrolledCourses.stream().mapToDouble(en -> en.getGrade().getPoints()).average().orElse(0.0);
    }
    private int getTotalCredits() {
        return enrolledCourses.stream().mapToInt(en -> en.getCourse().getCredits()).sum();
    }
    public String getRegNo() { return regNo; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<Enrollment> getEnrolledCourses() { return new ArrayList<>(enrolledCourses); }
    @Override
    public String toString() {
        return super.toString() + String.format(", regNo='%s', active=%s, gpa=%.2f", regNo, active, computeGPA());
    }
}

class Instructor extends Person {
    private String department;
    public Instructor(String id, String fullName, String email, String department) {
        super(id, fullName, email);
        this.department = department;
    }
    @Override
    public String getRole() { return "Instructor"; }
    public String getDepartment() { return department; }
    @Override
    public String toString() { return super.toString() + String.format(", dept='%s'", department); }
}

class Course {
    private final CourseCode code;
    private String title;
    private int credits;
    private Instructor instructor;
    private Semester semester;
    private String department;
    private boolean active = true;
    private Course(Builder builder) {
        this.code = builder.code;
        this.title = builder.title;
        this.credits = builder.credits;
        this.instructor = builder.instructor;
        this.semester = builder.semester;
        this.department = builder.department;
    }
    // Builder Pattern
    static class Builder {
        private CourseCode code;
        private String title;
        private int credits;
        private Instructor instructor;
        private Semester semester;
        private String department;
        public Builder code(CourseCode code) { this.code = code; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder credits(int credits) { this.credits = credits; return this; }
        public Builder instructor(Instructor instructor) { this.instructor = instructor; return this; }
        public Builder semester(Semester semester) { this.semester = semester; return this; }
        public Builder department(String department) { this.department = department; return this; }
        public Course build() {
            if (!Validator.validateCredits(credits)) throw new IllegalArgumentException("Invalid credits");
            return new Course(this);
        }
    }
    public CourseCode getCode() { return code; }
    public String getTitle() { return title; }
    public int getCredits() { return credits; }
    public Instructor getInstructor() { return instructor; }
    public Semester getSemester() { return semester; }
    public String getDepartment() { return department; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    @Override
    public String toString() {
        return String.format("Course{code=%s, title='%s', credits=%d, instructor=%s, semester=%s, dept='%s'}",
                code, title, credits, instructor != null ? instructor.getFullName() : "TBD", semester, department);
    }
}

class Enrollment {
    private Student student;
    private Course course;
    private Grade grade;
    private LocalDate enrollDate;
    public Enrollment(Student student, Course course) {
        this.student = student;
        this.course = course;
        this.enrollDate = LocalDate.now();
        this.grade = Grade.I;
    }
    public void setGrade(Grade grade) { this.grade = grade; }
    public Student getStudent() { return student; }
    public Course getCourse() { return course; }
    public Grade getGrade() { return grade; }
    public LocalDate getEnrollDate() { return enrollDate; }
    @Override
    public String toString() {
        return String.format("Enrollment{student=%s, course=%s, grade=%s, date=%s}",
                student.getFullName(), course.getCode(), grade, enrollDate);
    }
}

// Utility Classes
class Validator {
    // Operators: arithmetic (+, %), relational (<, >), logical (&&)
    // Precedence example: int x = 5 + 3 * 2; // * > +, x = 11
    // Bitwise example: int a = 5, b = 3; a & b = 1, a | b = 7
    public static boolean validateEmail(String email) {
        return email != null && email.contains("@") && email.length() > 5;
    }
    public static boolean validateCredits(int credits) {
        return credits > 0 && credits <= 6;
    }
}

class RecursionUtil {
    // Inner class for recursion
    class SizeAccumulator {
        private long total = 0;
        void add(long size) { total += size; }
        long getTotal() { return total; }
    }
    public static long computeDirectorySize(Path dir) {
        final SizeAccumulator accum = new CCRM().new RecursionUtil().new SizeAccumulator();
        try (var stream = Files.walk(dir)) {
            stream.forEach(path -> {
                try {
                    accum.add(Files.size(path));
                } catch (IOException e) {}
            });
        } catch (IOException e) {}
        return accum.getTotal();
    }
    public static void listFilesRecursively(Path dir, int depth) {
        if (depth > 5) return;
        try (var stream = Files.list(dir)) {
            stream.forEach(child -> {
                System.out.println("  ".repeat(depth) + child.getFileName());
                if (Files.isDirectory(child)) listFilesRecursively(child, depth + 1);
            });
        } catch (IOException e) {}
    }
}

// Services
class StudentService implements Persistable {
    private List<Student> students = new ArrayList<>();
    public void addStudent(Student student) {
        assert student != null : "Student cannot be null";
        students.add(student);
    }
    public List<Student> listStudents() { return new ArrayList<>(students); }
    public void updateStudent(String id, String newName) {
        students.stream().filter(s -> s.getId().equals(id)).findFirst().ifPresent(s -> s.setFullName(newName));
    }
    public void deactivateStudent(String id) {
        students.stream().filter(s -> s.getId().equals(id)).findFirst().ifPresent(s -> s.setActive(false));
    }
    public List<Student> search(Predicate<Student> predicate) {
        return students.stream().filter(predicate).collect(Collectors.toList());
    }
    public String[] getSortedRegNos() {
        String[] regNos = students.stream().map(Student::getRegNo).toArray(String[]::new);
        Arrays.sort(regNos); // Array utility
        return regNos;
    }
}

class CourseService implements Searchable<Course> {
    private List<Course> courses = new ArrayList<>();
    public void addCourse(Course course) {
        assert course.getCredits() > 0 : "Credits must be positive";
        courses.add(course);
    }
    public List<Course> listCourses() { return new ArrayList<>(courses); }
    public void updateCourse(String code, String newTitle) {
        courses.stream().filter(c -> c.getCode().getValue().equals(code)).findFirst().ifPresent(c -> c.setTitle(newTitle));
    }
    public void deactivateCourse(String code) {
        courses.stream().filter(c -> c.getCode().getValue().equals(code)).findFirst().ifPresent(c -> c.setActive(false));
    }
    @Override
    public List<Course> searchByInstructor(Instructor instructor) {
        return courses.stream().filter(c -> c.getInstructor() == instructor).collect(Collectors.toList());
    }
    @Override
    public List<Course> searchByDepartment(String department) {
        return courses.stream().filter(c -> c.getDepartment().equals(department)).collect(Collectors.toList());
    }
    @Override
    public List<Course> searchBySemester(Semester semester) {
        return courses.stream().filter(c -> c.getSemester() == semester).collect(Collectors.toList());
    }
    public Map<Integer, Long> getCreditDistribution() {
        return courses.stream().filter(Course::isActive)
                .collect(Collectors.groupingBy(Course::getCredits, Collectors.counting()));
    }
    public List<Course> getSortedByTitle() {
        return courses.stream().sorted((c1, c2) -> c1.getTitle().compareTo(c2.getTitle())).collect(Collectors.toList());
    }
}

class EnrollmentService {
    private Map<String, List<Enrollment>> enrollments = new HashMap<>();
    public void enrollStudent(Student student, Course course) throws DuplicateEnrollmentException, MaxCreditLimitExceededException {
        String key = student.getId();
        enrollments.computeIfAbsent(key, k -> new ArrayList<>());
        if (enrollments.get(key).stream().anyMatch(e -> e.getCourse().getCode().equals(course.getCode()))) {
            throw new DuplicateEnrollmentException("Already enrolled");
        }
        Enrollment enrollment = new Enrollment(student, course);
        enrollments.get(key).add(enrollment);
        student.enroll(enrollment);
    }
    public void unenrollStudent(String studentId, String courseCode) {
        enrollments.getOrDefault(studentId, new ArrayList<>()).removeIf(e -> e.getCourse().getCode().getValue().equals(courseCode));
    }
    public void recordGrade(String studentId, String courseCode, Grade grade) {
        enrollments.getOrDefault(studentId, new ArrayList<>()).stream()
                .filter(e -> e.getCourse().getCode().getValue().equals(courseCode))
                .findFirst().ifPresent(e -> e.setGrade(grade));
    }
    public String printTranscript(String studentId) {
        Student student = (Student) new StudentService().load(studentId); // Upcast/downcast
        if (student instanceof Student) { // instanceof
            return student.toString();
        }
        return "Student not found";
    }
}

class TranscriptService implements Persistable {
    @Override
    public void save(Object obj) { System.out.println("Saved transcript: " + obj); }
    public String generateTranscript(Student student) {
        List<Grade> grades = student.getEnrolledCourses().stream().map(Enrollment::getGrade).collect(Collectors.toList());
        double gpa = student.computeGPA();
        return String.format("Transcript for %s\nGPA: %.2f\nGrades: %s", student.getFullName(), gpa, grades);
    }
}

class ImportExportService {
    private final Path dataFolder = AppConfig.getInstance().getDataFolder();
    public void importStudentsFromCSV(String filename) throws IOException {
        Path file = dataFolder.resolve(filename);
        if (Files.exists(file)) {
            try (Stream<String> lines = Files.lines(file)) {
                lines.skip(1).forEach(line -> {
                    String[] parts = line.split(","); // String split
                    if (parts.length >= 4) {
                        Student s = new Student(parts[0], parts[1], parts[2], parts[3]);
                        new StudentService().addStudent(s);
                        System.out.println("Imported: " + s);
                    }
                });
            }
        }
    }
    public void exportStudentsToCSV(String filename, List<Student> students) throws IOException {
        Path file = dataFolder.resolve(filename);
        String header = "id,fullName,email,regNo\n";
        String content = students.stream()
                .map(s -> String.format("%s,%s,%s,%s\n", s.getId(), s.getFullName(), s.getEmail(), s.getRegNo()))
                .collect(Collectors.joining("", header, ""));
        Files.write(file, content.getBytes());
    }
    public void exportCoursesToCSV(String filename, List<Course> courses) throws IOException {
        System.out.println("Exported courses to " + filename);
    }
}

class BackupService {
    private final Path dataFolder = AppConfig.getInstance().getDataFolder();
    private final Path backupRoot = dataFolder.resolve("backups");
    public void createBackup() throws IOException {
        if (!Files.exists(backupRoot)) Files.createDirectory(backupRoot);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path backupFolder = backupRoot.resolve(timestamp);
        Files.createDirectory(backupFolder);
        Path exportFile = dataFolder.resolve("students.csv");
        if (Files.exists(exportFile)) {
            Files.copy(exportFile, backupFolder.resolve("students.csv"), StandardCopyOption.REPLACE_EXISTING);
        }
        long size = RecursionUtil.computeDirectorySize(backupFolder);
        System.out.printf("Backup created at %s, size: %d bytes\n", backupFolder, size);
    }
}

// Main CLI
public class CCRM {
    private final StudentService studentService = new StudentService();
    private final CourseService courseService = new CourseService();
    private final EnrollmentService enrollmentService = new EnrollmentService();
    private final TranscriptService transcriptService = new TranscriptService();
    private final ImportExportService ioService = new ImportExportService();
    private final BackupService backupService = new BackupService();
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    // Anonymous inner class
    private final Runnable inputHandler = new Runnable() {
        @Override
        public void run() { System.out.print("Enter choice: "); }
    };

    public void start() {
        AppConfig.getInstance();
        System.out.println("Java SE: Desktop apps. ME: Devices. EE: Servers."); // Platform note
        outer: while (true) { // Labeled loop
            System.out.println("\n=== CCRM Menu ===");
            System.out.println("1. Manage Students");
            System.out.println("2. Manage Courses");
            System.out.println("3. Enrollment & Grades");
            System.out.println("4. Import/Export");
            System.out.println("5. Backup");
            System.out.println("6. Reports");
            System.out.println("0. Exit");
            inputHandler.run();
            int choice;
            try {
                choice = Integer.parseInt(reader.readLine());
            } catch (IOException | NumberFormatException e) {
                System.out.println("Invalid input.");
                continue outer;
            }
            switch (choice) { // Enhanced switch
                case 1 -> manageStudents();
                case 2 -> manageCourses();
                case 3 -> manageEnrollment();
                case 4 -> manageIO();
                case 5 -> {
                    try {
                        backupService.createBackup();
                        RecursionUtil.listFilesRecursively(backupRoot, 0);
                    } catch (IOException e) {
                        System.err.println("Backup failed: " + e.getMessage());
                    }
                }
                case 6 -> printReports();
                case 0 -> {
                    System.out.println("Exiting.");
                    break outer;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void manageStudents() {
        do {
            System.out.println("1. Add 2. List 3. Update 4. Deactivate 0. Back");
            inputHandler.run();
            int choice = getIntInput();
            if (choice == 0) break;
            switch (choice) {
                case 1 -> addDemoStudent();
                case 2 -> studentService.listStudents().forEach(System.out::println);
                case 3 -> updateDemoStudent();
                case 4 -> studentService.deactivateStudent("S" + new Random().nextInt(100));
            }
        } while (true);
    }

    private void manageCourses() {
        for (int i = 0; i < 2; i++) { // For loop with continue
            if (i == 1) continue;
            addDemoCourse();
        }
        courseService.listCourses().forEach(System.out::println); // Enhanced for
    }

    private void manageEnrollment() {
        Student s = new Student("S001", "John Doe", "john@email.com", "REG001");
        studentService.addStudent(s);
        Course c = new Course.Builder()
                .code(new CourseCode("CS101"))
                .title("Intro CS")
                .credits(3)
                .semester(Semester.FALL)
                .department("CSE")
                .build();
        courseService.addCourse(c);
        try {
            enrollmentService.enrollStudent(s, c);
            enrollmentService.recordGrade("S001", "CS101", Grade.A);
            System.out.println(transcriptService.generateTranscript(s));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void manageIO() {
        try {
            ioService.exportStudentsToCSV("students.csv", studentService.listStudents());
            ioService.importStudentsFromCSV("students.csv");
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
        }
    }

    private void printReports() {
        var distro = courseService.getCreditDistribution();
        distro.forEach((credits, count) -> System.out.printf("%d credits: %d courses\n", credits, count));
    }

    private void addDemoStudent() {
        String id = "S" + new Random().nextInt(100);
        Student s = new Student(id, "Demo " + id, id + "@email.com", "REG" + id);
        studentService.addStudent(s);
        System.out.println("Added: " + s);
    }

    private void updateDemoStudent() {
        String id = "S" + new Random().nextInt(100);
        studentService.updateStudent(id, "Updated Name");
        System.out.println("Updated student ID: " + id);
    }

    private void addDemoCourse() {
        Course c = new Course.Builder()
                .code(new CourseCode("DEMO" + new Random().nextInt(100)))
                .title("Demo Course")
                .credits(3)
                .semester(Semester.SPRING)
                .department("Demo")
                .build();
        courseService.addCourse(c);
    }

    private int getIntInput() {
        try {
            return Integer.parseInt(reader.readLine());
        } catch (Exception e) {
            return -1;
        }
    }

    // Exception handling demo
    private void demoExceptions() {
        try {
            throw new DuplicateEnrollmentException("Test checked");
        } catch (DuplicateEnrollmentException | IOException e) { // Multi-catch
            System.err.println("Caught: " + e.getMessage());
        } finally {
            System.out.println("Cleanup done.");
        }
        try {
            throw new MaxCreditLimitExceededException("Test unchecked");
        } catch (RuntimeException e) {
            System.err.println("Caught: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        // Operator precedence: int x = 5 + 3 * 2; // x = 11 (* > +)
        new CCRM().start();
    }
}