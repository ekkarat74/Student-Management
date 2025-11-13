import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.mindrot.jbcrypt.BCrypt;
import java.util.function.BiConsumer;

@FunctionalInterface
interface TriConsumer<T, U, V> {
    void accept(T t, U u, V v);
}

enum Role { ADMIN, TEACHER, STUDENT, GUEST, UNKNOWN,OFFICER }
enum StudentStatus { ENROLLED, GRADUATED, ON_LEAVE, DROPPED }

enum ClassroomType { 
    NORMAL("ห้องเรียนปกติ"), 
    COMPUTER_LAB("ห้องปฎิบัติการคอมพิวเตอร์");

    private final String displayName;
    ClassroomType(String displayName) { this.displayName = displayName; }
    @Override public String toString() { return displayName; }
}

class Classroom {
    String id, name, teacherId, teacherName, majorId, majorName;
    ClassroomType type;

    public Classroom(String id, String name, String teacherId, String teacherName, String majorId, String majorName, ClassroomType type) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.majorId = majorId;
        this.majorName = majorName;
        this.type = type;
    }

    public Classroom(String id, String name, String teacherId, String teacherName, String majorId, String majorName) {
         this(id, name, teacherId, teacherName, majorId, majorName, ClassroomType.NORMAL);
    }

    @Override 
    public String toString() { 
        if (id == null) return name;
        String typeStr = (type != null) ? " (" + type.toString() + ")" : "";
        return name + typeStr + " [" + (majorName != null ? majorName : "N/A") + "] (Teacher: " + (teacherName != null ? teacherName : "N/A") + ")"; 
    }
}

class Student {
    String id, name, address, phone, email, photoPath, major;
    String classroomId;
    int age, year;
    double gpa;
    StudentStatus status;
    String dateAdded;
    
    String previousSchool;
    String docApplicationPath;
    String docIdCardPath;
    String docTranscriptPath;
    
    public Student(String id, String name, String address, String phone, String email, String photoPath, 
                   int age, double gpa, int year, StudentStatus status, String major, String dateAdded,
                   String previousSchool, String docApplicationPath, String docIdCardPath, String docTranscriptPath,
                   String classroomId) {
        this.id = id; this.name = name; this.address = address; this.phone = phone; this.email = email;
        this.photoPath = photoPath; this.age = age; this.gpa = gpa; this.year = year;
        this.status = status; this.major = major; this.dateAdded = dateAdded;
        this.previousSchool = previousSchool;
        this.docApplicationPath = docApplicationPath;
        this.docIdCardPath = docIdCardPath;
        this.docTranscriptPath = docTranscriptPath;
        this.classroomId = classroomId;
    }
    
    public Student(String id, String name, String address, String phone, String email, String photoPath, 
                   int age, double gpa, int year, StudentStatus status, String major,
                   String previousSchool, String docApplicationPath, String docIdCardPath, String docTranscriptPath) {
        this(id, name, address, phone, email, photoPath, age, gpa, year, status, major, 
             new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
             previousSchool, docApplicationPath, docIdCardPath, docTranscriptPath,
             null);
    }

    public Student(String id, String name, String address, String phone, String email, String photoPath, 
                   int age, double gpa, int year, StudentStatus status, String major) {
        this(id, name, address, phone, email, photoPath, age, gpa, year, status, major, 
             "N/A", "N/A", "N/A", "N/A"); // ⭐️ ตั้งค่า Default
    }
}

class Teacher {
    String id, name, email, office;
    public Teacher(String id, String name, String email, String office) {
        this.id = id; this.name = name; this.email = email; this.office = office;
    }
    @Override public String toString() { return name; }
}

class StudentSchedule {
    String subjectId, subjectName, teacherName;
    String scheduleInfo;
    
    public StudentSchedule(String subjectId, String subjectName, String teacherName, String scheduleInfo) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.teacherName = teacherName;
        this.scheduleInfo = scheduleInfo;
    }
}

class Subject {
    String id, name, majorName, semesterName, teacherName, scheduleInfo;
    int credits;
    String prerequisites;

    public Subject(String id, String name, int credits, String majorName, String semesterName, 
                   String teacherName, String scheduleInfo, String prerequisites) {
        this.id = id;
        this.name = name;
        this.credits = credits;
        this.majorName = majorName;
        this.semesterName = semesterName;
        this.teacherName = teacherName;
        this.scheduleInfo = scheduleInfo;
        this.prerequisites = prerequisites;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s (%d credits)", id, name, credits);
    }
}

class Major {
    String id, name;
    public Major(String id, String name) { this.id = id; this.name = name; }
    @Override public String toString() { return name; }
}

class Semester {
    String id, name;
    public Semester(String id, String name) { this.id = id; this.name = name; }
    @Override public String toString() { return name; }
}

class CourseData {
    String id, name, room, day, time;
    int credits;
    String majorId, semesterId, teacherId;
    public CourseData(String id, String name, int credits, String majorId, String semesterId, 
                      String teacherId, String room, String day, String time) {
        this.id = id; this.name = name; this.credits = credits; this.majorId = majorId;
        this.semesterId = semesterId; this.teacherId = teacherId; this.room = room;
        this.day = day; this.time = time;
    }
}

class LeaveRequest {
    int id;
    String teacherId, teacherName, reason, status, startDate, endDate;

    public LeaveRequest(int id, String teacherId, String teacherName, String reason, String status, String startDate, String endDate) {
        this.id = id;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.reason = reason;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

class EnrollmentRecord {
    int enrollmentId;
    String subjectId;
    String subjectName;
    int credits;
    String grade;
    
    String studentId;
    String studentName;

    public EnrollmentRecord(int enrollmentId, String subjectId, String subjectName, int credits, String grade) {
        this.enrollmentId = enrollmentId;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.credits = credits;
        this.grade = grade;
    }
}

class StudentDisplayRecord {
    String id, name, major, email, phone, dateAdded, homeroomTeacherName;
    String classroomName;
    int year;
    double gpa;
    StudentStatus status;

public StudentDisplayRecord(String id, String name, String major, int year, StudentStatus status, 
                            double gpa, String email, String phone, String dateAdded, 
                            String homeroomTeacherName, String classroomName) {
        this.id = id;
        this.name = name;
        this.major = major;
        this.year = year;
        this.status = status;
        this.gpa = gpa;
        this.email = email;
        this.phone = phone;
        this.dateAdded = dateAdded;
        this.homeroomTeacherName = homeroomTeacherName;
        this.classroomName = classroomName;
    }
}

public class DatabaseManager {

    private static final String DATABASE_FILE_NAME = "university_v7.db"; 
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_FILE_NAME;
    
    private Connection connect() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DATABASE_URL);
        } catch (Exception e) { e.printStackTrace(); }
        return conn;
    }

    public void initDatabase() {
    String createUserTable = "CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password_hash TEXT NOT NULL, role TEXT NOT NULL);";
    
    String createStudentsTable = """
        CREATE TABLE IF NOT EXISTS students (
            student_id TEXT PRIMARY KEY, name TEXT NOT NULL, address TEXT, phone TEXT, email TEXT, 
            photoPath TEXT, age INTEGER, gpa REAL, year INTEGER, status TEXT, major TEXT, dateAdded TEXT,
            previous_school TEXT, doc_application_path TEXT, doc_id_card_path TEXT, doc_transcript_path TEXT,
            classroom_id TEXT, 
            FOREIGN KEY (student_id) REFERENCES users(username),
            FOREIGN KEY (classroom_id) REFERENCES classrooms(classroom_id) ON DELETE SET NULL
        );""";

    String createClassroomsTable = """
        CREATE TABLE IF NOT EXISTS classrooms (
            classroom_id TEXT PRIMARY KEY,
            classroom_name TEXT NOT NULL,
            teacher_id TEXT,
            major_id TEXT,
            classroom_type TEXT,
            FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE SET NULL,
            FOREIGN KEY (major_id) REFERENCES majors(major_id) ON DELETE SET NULL
        );""";
    
    String createTeachersTable = "CREATE TABLE IF NOT EXISTS teachers (teacher_id TEXT PRIMARY KEY, name TEXT NOT NULL, email TEXT, office TEXT, FOREIGN KEY (teacher_id) REFERENCES users(username));";
    
    String createMajorsTable = "CREATE TABLE IF NOT EXISTS majors (major_id TEXT PRIMARY KEY, major_name TEXT NOT NULL);";
    String createSemestersTable = "CREATE TABLE IF NOT EXISTS semesters (semester_id TEXT PRIMARY KEY, semester_name TEXT NOT NULL);";

    String createSubjectsTable = """
        CREATE TABLE IF NOT EXISTS subjects (
            subject_id TEXT PRIMARY KEY, 
            subject_name TEXT NOT NULL, 
            credits INTEGER,
            major_id TEXT,
            semester_id TEXT,
            FOREIGN KEY (major_id) REFERENCES majors(major_id),
            FOREIGN KEY (semester_id) REFERENCES semesters(semester_id)
        );""";
            
    String createPrerequisitesTable = """
        CREATE TABLE IF NOT EXISTS prerequisites (
            subject_id TEXT NOT NULL,
            prerequisite_subject_id TEXT NOT NULL,
            PRIMARY KEY (subject_id, prerequisite_subject_id),
            FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
            FOREIGN KEY (prerequisite_subject_id) REFERENCES subjects(subject_id)
        );""";

    String createEnrollmentsTable = "CREATE TABLE IF NOT EXISTS enrollments (enrollment_id INTEGER PRIMARY KEY AUTOINCREMENT, student_id TEXT NOT NULL, subject_id TEXT NOT NULL, grade TEXT, FOREIGN KEY (student_id) REFERENCES students(student_id), FOREIGN KEY (subject_id) REFERENCES subjects(subject_id));";
    
    String createAssignmentsTable = """
        CREATE TABLE IF NOT EXISTS teaching_assignments (
            assignment_id INTEGER PRIMARY KEY AUTOINCREMENT,
            teacher_id TEXT NOT NULL,
            subject_id TEXT NOT NULL,
            room TEXT,
            schedule_day TEXT,
            schedule_time TEXT,
            FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id),
            FOREIGN KEY (subject_id) REFERENCES subjects(subject_id)
        );""";

    String createAssignmentGradesTable = """
        CREATE TABLE IF NOT EXISTS assignment_grades (
            grade_id INTEGER PRIMARY KEY AUTOINCREMENT,
            enrollment_id INTEGER NOT NULL,
            assignment_name TEXT NOT NULL,
            score REAL DEFAULT 0,
            max_score REAL DEFAULT 100,
            date_recorded TEXT,
            FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE
        );""";
            
    String createLeaveRequestsTable = """
        CREATE TABLE IF NOT EXISTS leave_requests (
            leave_id INTEGER PRIMARY KEY AUTOINCREMENT,
            teacher_id TEXT NOT NULL,
            start_date TEXT NOT NULL,
            end_date TEXT NOT NULL,
            reason TEXT,
            status TEXT NOT NULL DEFAULT 'PENDING', 
            FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id)
        );""";

    String createInvoicesTable = """
        CREATE TABLE IF NOT EXISTS Invoices (
            invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id TEXT NOT NULL,
            semester_id TEXT,
            issue_date TEXT NOT NULL,
            due_date TEXT NOT NULL,
            total_amount REAL NOT NULL,
            status TEXT NOT NULL, 
            FOREIGN KEY (student_id) REFERENCES students (student_id),
            FOREIGN KEY (semester_id) REFERENCES semesters (semester_id)
        );""";
            
    String createInvoiceItemsTable = """
        CREATE TABLE IF NOT EXISTS InvoiceItems (
            item_id INTEGER PRIMARY KEY AUTOINCREMENT,
            invoice_id INTEGER NOT NULL,
            description TEXT NOT NULL,
            amount REAL NOT NULL,
            FOREIGN KEY (invoice_id) REFERENCES Invoices (invoice_id)
        );""";
            
    String createTransactionsTable = """
        CREATE TABLE IF NOT EXISTS Transactions (
            transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
            invoice_id INTEGER NOT NULL,
            student_id TEXT NOT NULL,
            payment_date TEXT NOT NULL,
            amount_paid REAL NOT NULL,
            payment_method TEXT NOT NULL,
            reference_code TEXT,
            FOREIGN KEY (invoice_id) REFERENCES Invoices (invoice_id),
            FOREIGN KEY (student_id) REFERENCES students (student_id)
        );""";
            
    String createFinancialAidTable = """
        CREATE TABLE IF NOT EXISTS FinancialAid (
            aid_id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id TEXT NOT NULL,
            semester_id TEXT,
            aid_type TEXT NOT NULL, 
            description TEXT,
            amount REAL NOT NULL,
            apply_date TEXT NOT NULL,
            FOREIGN KEY (student_id) REFERENCES students (student_id),
            FOREIGN KEY (semester_id) REFERENCES semesters (semester_id)
        );""";
            
    String createActivityLogTable = """
        CREATE TABLE IF NOT EXISTS activity_log (
            log_id INTEGER PRIMARY KEY AUTOINCREMENT,
            log_timestamp TEXT NOT NULL,
            username TEXT NOT NULL,
            action_description TEXT NOT NULL
        );""";

    boolean isNewDatabase = false;

    try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
        stmt.execute(createUserTable);
        stmt.execute(createMajorsTable); 
        stmt.execute(createTeachersTable); 
        stmt.execute(createClassroomsTable); 
        stmt.execute(createStudentsTable); 
        stmt.execute(createSemestersTable); 
        stmt.execute(createSubjectsTable);
        stmt.execute(createPrerequisitesTable); 
        stmt.execute(createEnrollmentsTable);
        stmt.execute(createAssignmentGradesTable);
        stmt.execute(createAssignmentsTable);
        stmt.execute(createLeaveRequestsTable);

        stmt.execute(createInvoicesTable);
        stmt.execute(createInvoiceItemsTable);
        stmt.execute(createTransactionsTable);
        stmt.execute(createFinancialAidTable);
        
        stmt.execute(createActivityLogTable);

        try (ResultSet rs = stmt.executeQuery("SELECT 1 FROM users WHERE username = 'admin'")) {
            if (!rs.next()) {
                isNewDatabase = true;
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    if (isNewDatabase) {
        System.out.println("Database appears to be new. Adding dummy data...");
        addDummyData();
    } else {
        System.out.println("Existing database found. Skipping dummy data.");
    }
    
    System.out.println("✅ Database V6 (with Classroom Types) initialized successfully!"); 
}
    
    public CourseData getSubjectAndAssignmentData(String subjectId) {
        String sql = """
            SELECT 
                s.subject_id, s.subject_name, s.credits,
                s.major_id, s.semester_id,
                ta.teacher_id, ta.room, ta.schedule_day, ta.schedule_time
            FROM subjects s
            LEFT JOIN teaching_assignments ta ON s.subject_id = ta.subject_id
            WHERE s.subject_id = ?
            """;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subjectId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new CourseData(
                    rs.getString("subject_id"),
                    rs.getString("subject_name"),
                    rs.getInt("credits"),
                    rs.getString("major_id"),
                    rs.getString("semester_id"),
                    rs.getString("teacher_id"),
                    rs.getString("room"),
                    rs.getString("schedule_day"),
                    rs.getString("schedule_time")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createStudentWithLogin(Student s, String password) {
        Connection conn = null;
        String sqlUser = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?);";
        String sqlStudent = "INSERT INTO students(student_id, name, address, phone, email, photoPath, age, gpa, year, status, major, dateAdded, previous_school, doc_application_path, doc_id_card_path, doc_transcript_path) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        try {
            conn = connect();
            conn.setAutoCommit(false);

            String hash = BCrypt.hashpw(password, BCrypt.gensalt());
            try (PreparedStatement pstmtUser = conn.prepareStatement(sqlUser)) {
                pstmtUser.setString(1, s.id);
                pstmtUser.setString(2, hash);
                pstmtUser.setString(3, Role.STUDENT.name());
                pstmtUser.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlStudent)) {
                pstmt.setString(1, s.id); 
                pstmt.setString(2, s.name); 
                pstmt.setString(3, s.address);
                pstmt.setString(4, s.phone); 
                pstmt.setString(5, s.email); 
                pstmt.setString(6, s.photoPath);
                pstmt.setInt(7, s.age); 
                pstmt.setDouble(8, s.gpa); 
                pstmt.setInt(9, s.year);
                pstmt.setString(10, s.status.name()); 
                pstmt.setString(11, s.major); 
                pstmt.setString(12, s.dateAdded);
                pstmt.setString(13, s.previousSchool); 
                pstmt.setString(14, s.docApplicationPath);
                pstmt.setString(15, s.docIdCardPath); 
                pstmt.setString(16, s.docTranscriptPath);
                pstmt.executeUpdate();
            }
            
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    public boolean updateCourse(CourseData data) {
        String sqlSubject = "UPDATE subjects SET subject_name = ?, credits = ?, major_id = ?, semester_id = ? WHERE subject_id = ?";
        String sqlAssignment = "UPDATE teaching_assignments SET teacher_id = ?, room = ?, schedule_day = ?, schedule_time = ? WHERE subject_id = ?";
        
        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtSub = conn.prepareStatement(sqlSubject)) {
                pstmtSub.setString(1, data.name);
                pstmtSub.setInt(2, data.credits);
                pstmtSub.setString(3, data.majorId);
                pstmtSub.setString(4, data.semesterId);
                pstmtSub.setString(5, data.id);
                pstmtSub.executeUpdate();
            }
            try (PreparedStatement pstmtAssign = conn.prepareStatement(sqlAssignment)) {
                pstmtAssign.setString(1, data.teacherId);
                pstmtAssign.setString(2, data.room);
                pstmtAssign.setString(3, data.day);
                pstmtAssign.setString(4, data.time);
                pstmtAssign.setString(5, data.id);
                
                int rowsAffected = pstmtAssign.executeUpdate();
                if (rowsAffected == 0) {
                    String sqlInsert = "INSERT INTO teaching_assignments(teacher_id, subject_id, room, schedule_day, schedule_time) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                        pstmtInsert.setString(1, data.teacherId);
                        pstmtInsert.setString(2, data.id);
                        pstmtInsert.setString(3, data.room);
                        pstmtInsert.setString(4, data.day);
                        pstmtInsert.setString(5, data.time);
                        pstmtInsert.executeUpdate();
                    }
                }
            }
            
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean setPrerequisites(String mainSubjectId, List<String> prereqSubjectIds) {
        String sqlDelete = "DELETE FROM prerequisites WHERE subject_id = ?";
        String sqlInsert = "INSERT INTO prerequisites (subject_id, prerequisite_subject_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false); 
            try (PreparedStatement pstmtDelete = conn.prepareStatement(sqlDelete)) {
                pstmtDelete.setString(1, mainSubjectId);
                pstmtDelete.executeUpdate();
            }

            if (prereqSubjectIds != null && !prereqSubjectIds.isEmpty()) {
                try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                    for (String prereqId : prereqSubjectIds) {
                        pstmtInsert.setString(1, mainSubjectId);
                        pstmtInsert.setString(2, prereqId);
                        pstmtInsert.addBatch();
                    }
                    pstmtInsert.executeBatch();
                }
            }
            
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void addDummyData() {
        TriConsumer<String, String, String> insertUser = (user, pass, role) -> {
            String hash = BCrypt.hashpw(pass, BCrypt.gensalt());
            String sql = "INSERT OR IGNORE INTO users (username, password_hash, role) VALUES (?, ?, ?);";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, user); pstmt.setString(2, hash); pstmt.setString(3, role);
                pstmt.executeUpdate();
            } catch (SQLException e) { /* ซ้ำ ไม่เป็นไร */ }
        };
        BiConsumer<String, Object[]> runSql = (sql, params) -> {
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (params != null) { for (int i = 0; i < params.length; i++) { pstmt.setObject(i + 1, params[i]); } }
                pstmt.executeUpdate();
            } catch (SQLException e) { /* ซ้ำ ไม่เป็นไร */ }
        };

        insertUser.accept("admin", "admin123", "ADMIN");

        runSql.accept("INSERT OR IGNORE INTO majors(major_id, major_name) VALUES (?, ?)", 
                        new Object[]{"CS", "Computer Science"});
        runSql.accept("INSERT OR IGNORE INTO majors(major_id, major_name) VALUES (?, ?)", 
                        new Object[]{"TH", "Thai Language Dept."});
        runSql.accept("INSERT OR IGNORE INTO majors(major_id, major_name) VALUES (?, ?)", 
                        new Object[]{"BUS", "Business"});
        runSql.accept("INSERT OR IGNORE INTO majors(major_id, major_name) VALUES (?, ?)", 
                        new Object[]{"ENG", "Engineering"});

        runSql.accept("INSERT OR IGNORE INTO semesters(semester_id, semester_name) VALUES (?, ?)", 
                        new Object[]{"Y1T1", "Year 1, Term 1"});
        runSql.accept("INSERT OR IGNORE INTO semesters(semester_id, semester_name) VALUES (?, ?)", 
                        new Object[]{"Y1T2", "Year 1, Term 2"});
        runSql.accept("INSERT OR IGNORE INTO semesters(semester_id, semester_name) VALUES (?, ?)", 
                        new Object[]{"Y2T1", "Year 2, Term 1"});
        runSql.accept("INSERT OR IGNORE INTO semesters(semester_id, semester_name) VALUES (?, ?)", 
                        new Object[]{"Y2T2", "Year 2, Term 2"});
    }

    public Role checkLogin(String username, String password) {
        String sql = "SELECT password_hash, role FROM users WHERE username = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String roleStr = rs.getString("role");
                if (BCrypt.checkpw(password, storedHash)) {
                    return Role.valueOf(roleStr); 
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Role.UNKNOWN; 
    }
    
    public Student getStudentById(String id) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Student(
                    rs.getString("student_id"), rs.getString("name"), rs.getString("address"),
                    rs.getString("phone"), rs.getString("email"), rs.getString("photoPath"),
                    rs.getInt("age"), rs.getDouble("gpa"), rs.getInt("year"),
                    StudentStatus.valueOf(rs.getString("status")), rs.getString("major"), rs.getString("dateAdded"),
                    rs.getString("previous_school"), rs.getString("doc_application_path"),
                    rs.getString("doc_id_card_path"), rs.getString("doc_transcript_path"),
                    rs.getString("classroom_id")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Teacher getTeacherById(String id) {
        String sql = "SELECT * FROM teachers WHERE teacher_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Teacher(
                    rs.getString("teacher_id"), rs.getString("name"),
                    rs.getString("email"), rs.getString("office")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    
    public ArrayList<StudentSchedule> getStudentSchedule(String studentId) {
        ArrayList<StudentSchedule> schedule = new ArrayList<>();
        String sql = """
            SELECT 
                s.subject_id, s.subject_name, t.name AS teacher_name,
                ta.schedule_day, ta.schedule_time, ta.room
            FROM enrollments e
            JOIN subjects s ON e.subject_id = s.subject_id
            JOIN teaching_assignments ta ON s.subject_id = ta.subject_id
            JOIN teachers t ON ta.teacher_id = t.teacher_id
            WHERE e.student_id = ?
            """;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String scheduleInfo = rs.getString("schedule_day") + " (" + rs.getString("schedule_time") + ") @ " + rs.getString("room");
                schedule.add(new StudentSchedule(
                    rs.getString("subject_id"),
                    rs.getString("subject_name"),
                    rs.getString("teacher_name"),
                    scheduleInfo
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return schedule;
    }
    
    public Teacher getHomeroomTeacher(String studentId) {
        String sql = """
            SELECT t.* FROM teachers t 
            JOIN classrooms c ON t.teacher_id = c.teacher_id
            JOIN students s ON c.classroom_id = s.classroom_id
            WHERE s.student_id = ?
            """;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Teacher(rs.getString("teacher_id"), rs.getString("name"), rs.getString("email"), rs.getString("office"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public ArrayList<Student> getHomeroomStudents(String teacherId) {
        ArrayList<Student> students = new ArrayList<>();
        String sql = """
            SELECT s.* FROM students s
            JOIN classrooms c ON s.classroom_id = c.classroom_id
            WHERE c.teacher_id = ?
            """;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                 students.add(new Student(
                    rs.getString("student_id"), rs.getString("name"), rs.getString("address"),
                    rs.getString("phone"), rs.getString("email"), rs.getString("photoPath"),
                    rs.getInt("age"), rs.getDouble("gpa"), rs.getInt("year"),
                    StudentStatus.valueOf(rs.getString("status")), rs.getString("major"), rs.getString("dateAdded"),
                    rs.getString("previous_school"), rs.getString("doc_application_path"),
                    rs.getString("doc_id_card_path"), rs.getString("doc_transcript_path"),
                    rs.getString("classroom_id") // ⭐️ (เพิ่ม field ที่ 17)
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return students;
    }
    
    public ArrayList<Classroom> getAllClassrooms() {
        ArrayList<Classroom> classrooms = new ArrayList<>();
        String sql = """
            SELECT c.classroom_id, c.classroom_name, c.teacher_id, t.name AS teacher_name, 
                   c.major_id, m.major_name, c.classroom_type
            FROM classrooms c
            LEFT JOIN teachers t ON c.teacher_id = t.teacher_id
            LEFT JOIN majors m ON c.major_id = m.major_id
            ORDER BY c.classroom_name
            """;
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String typeStr = rs.getString("classroom_type");
                ClassroomType type = (typeStr != null) ? ClassroomType.valueOf(typeStr) : ClassroomType.NORMAL; // Default to NORMAL

                classrooms.add(new Classroom(
                    rs.getString("classroom_id"),
                    rs.getString("classroom_name"),
                    rs.getString("teacher_id"),
                    rs.getString("teacher_name"),
                    rs.getString("major_id"),
                    rs.getString("major_name"),
                    type
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classrooms;
    }

    public Classroom getClassroomById(String classroomId) {
        if (classroomId == null || classroomId.isEmpty()) return null;
        String sql = """
            SELECT c.classroom_id, c.classroom_name, c.teacher_id, t.name AS teacher_name, 
                   c.major_id, m.major_name, c.classroom_type
            FROM classrooms c
            LEFT JOIN teachers t ON c.teacher_id = t.teacher_id
            LEFT JOIN majors m ON c.major_id = m.major_id
            WHERE c.classroom_id = ?
            """;
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, classroomId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String typeStr = rs.getString("classroom_type");
                ClassroomType type = (typeStr != null) ? ClassroomType.valueOf(typeStr) : ClassroomType.NORMAL;

                return new Classroom(
                    rs.getString("classroom_id"),
                    rs.getString("classroom_name"),
                    rs.getString("teacher_id"),
                    rs.getString("teacher_name"),
                    rs.getString("major_id"),
                    rs.getString("major_name"),
                    type
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateClassroom(String id, String name, String teacherId, String majorId, ClassroomType type) {
        String sql = "UPDATE classrooms SET classroom_name = ?, teacher_id = ?, major_id = ?, classroom_type = ? WHERE classroom_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, teacherId);
            pstmt.setString(3, majorId);
            pstmt.setString(4, type.name());
            pstmt.setString(5, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteClassroom(String id) {
        String sql = "DELETE FROM classrooms WHERE classroom_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addClassroom(String id, String name, String teacherId, String majorId, ClassroomType type) {
        String sql = "INSERT INTO classrooms(classroom_id, classroom_name, teacher_id, major_id, classroom_type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, teacherId);
            pstmt.setString(4, majorId);
            pstmt.setString(5, type.name());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean createUser(String username, String password, Role role) {
        String checkSql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = connect(); PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
            checkPstmt.setString(1, username);
            if (checkPstmt.executeQuery().next()) {
                System.err.println("Error: Username " + username + " already exists.");
                return false; 
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?);";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hash);
            pstmt.setString(3, role.name());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean addTeacher(Teacher t) {
        String sql = "INSERT INTO teachers(teacher_id, name, email, office) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, t.id);
            pstmt.setString(2, t.name);
            pstmt.setString(3, t.email);
            pstmt.setString(4, t.office);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    public ArrayList<Major> getAllMajors() {
        ArrayList<Major> majors = new ArrayList<>();
        String sql = "SELECT * FROM majors";
        try (Connection conn = connect(); 
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                majors.add(new Major(rs.getString("major_id"), rs.getString("major_name")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return majors;
    }

    public ArrayList<Semester> getAllSemesters() {
        ArrayList<Semester> semesters = new ArrayList<>();
        String sql = "SELECT * FROM semesters";
        try (Connection conn = connect(); 
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                semesters.add(new Semester(rs.getString("semester_id"), rs.getString("semester_name")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return semesters;
    }
    
    public ArrayList<Teacher> getAllTeachers() {
        ArrayList<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT * FROM teachers";
        try (Connection conn = connect(); 
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                teachers.add(new Teacher(rs.getString("teacher_id"), rs.getString("name"), rs.getString("email"), rs.getString("office")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return teachers;
    }
    
    public boolean addCourse(CourseData data) {
        String sqlSubject = "INSERT INTO subjects(subject_id, subject_name, credits, major_id, semester_id) VALUES (?, ?, ?, ?, ?)";
        String sqlAssignment = "INSERT INTO teaching_assignments(teacher_id, subject_id, room, schedule_day, schedule_time) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmtSub = conn.prepareStatement(sqlSubject)) {
                pstmtSub.setString(1, data.id);
                pstmtSub.setString(2, data.name);
                pstmtSub.setInt(3, data.credits);
                pstmtSub.setString(4, data.majorId);
                pstmtSub.setString(5, data.semesterId);
                pstmtSub.executeUpdate();
            }
            
            try (PreparedStatement pstmtAssign = conn.prepareStatement(sqlAssignment)) {
                pstmtAssign.setString(1, data.teacherId);
                pstmtAssign.setString(2, data.id);
                pstmtAssign.setString(3, data.room);
                pstmtAssign.setString(4, data.day);
                pstmtAssign.setString(5, data.time);
                pstmtAssign.executeUpdate();
            }
            
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<Subject> getAllSubjects() {
        ArrayList<Subject> subjects = new ArrayList<>();
        String sql = """
            SELECT 
                s.subject_id, s.subject_name, s.credits,
                m.major_name, sem.semester_name,
                t.name AS teacher_name,
                ta.room, ta.schedule_day, ta.schedule_time,
                (SELECT GROUP_CONCAT(p.prerequisite_subject_id) 
                 FROM prerequisites p 
                 WHERE p.subject_id = s.subject_id) AS prerequisites
            FROM subjects s
            LEFT JOIN majors m ON s.major_id = m.major_id
            LEFT JOIN semesters sem ON s.semester_id = sem.semester_id
            LEFT JOIN teaching_assignments ta ON s.subject_id = ta.subject_id
            LEFT JOIN teachers t ON ta.teacher_id = t.teacher_id
            """;
        
        try (Connection conn = connect(); 
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String schedule = rs.getString("schedule_day") + " (" + rs.getString("schedule_time") + ") @ " + rs.getString("room");
                if (rs.getString("schedule_day") == null) {
                    schedule = "Not Assigned";
                }
                subjects.add(new Subject(
                    rs.getString("subject_id"),
                    rs.getString("subject_name"),
                    rs.getInt("credits"),
                    rs.getString("major_name"),
                    rs.getString("semester_name"),
                    rs.getString("teacher_name"),
                    schedule,
                    rs.getString("prerequisites")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return subjects;
        
    }
    
    public ArrayList<Student> getAllStudents() {
        ArrayList<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(new Student(
                    rs.getString("student_id"), rs.getString("name"), rs.getString("address"),
                    rs.getString("phone"), rs.getString("email"), rs.getString("photoPath"),
                    rs.getInt("age"), rs.getDouble("gpa"), rs.getInt("year"),
                    StudentStatus.valueOf(rs.getString("status")), rs.getString("major"), rs.getString("dateAdded"),
                    rs.getString("previous_school"), rs.getString("doc_application_path"),
                    rs.getString("doc_id_card_path"), rs.getString("doc_transcript_path"),
                    rs.getString("classroom_id")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        
        return students;
    }

    public ArrayList<StudentDisplayRecord> getAllStudentsForDisplay() {
        ArrayList<StudentDisplayRecord> students = new ArrayList<>();
        String sql = """
            SELECT 
                s.student_id, s.name, s.major, s.year, s.status, s.gpa, s.email, s.phone, s.dateAdded,
                t.name AS teacher_name,
                c.classroom_name
            FROM students s
            LEFT JOIN classrooms c ON s.classroom_id = c.classroom_id
            LEFT JOIN teachers t ON c.teacher_id = t.teacher_id
            ORDER BY s.student_id
            """;
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                
                String statusStr = rs.getString("status");
                StudentStatus status = (statusStr != null) ? StudentStatus.valueOf(statusStr) : StudentStatus.ENROLLED;

                students.add(new StudentDisplayRecord(
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("major"),
                    rs.getInt("year"),
                    status,
                    rs.getDouble("gpa"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("dateAdded"),
                    rs.getString("teacher_name"),
                    rs.getString("classroom_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }
    
public boolean addStudent(Student s) {
        String sql = "INSERT INTO students(student_id, name, address, phone, email, photoPath, age, gpa, year, status, major, dateAdded, previous_school, doc_application_path, doc_id_card_path, doc_transcript_path, classroom_id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.id); pstmt.setString(2, s.name); pstmt.setString(3, s.address);
            pstmt.setString(4, s.phone); pstmt.setString(5, s.email); pstmt.setString(6, s.photoPath);
            pstmt.setInt(7, s.age); pstmt.setDouble(8, s.gpa); pstmt.setInt(9, s.year);
            pstmt.setString(10, s.status.name()); pstmt.setString(11, s.major); pstmt.setString(12, s.dateAdded);
            pstmt.setString(13, s.previousSchool); pstmt.setString(14, s.docApplicationPath);
            pstmt.setString(15, s.docIdCardPath); pstmt.setString(16, s.docTranscriptPath);
            pstmt.setString(17, s.classroomId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateStudent(Student s) {
        String sql = "UPDATE students SET name = ?, address = ?, phone = ?, email = ?, photoPath = ?, age = ?, gpa = ?, year = ?, status = ?, major = ?, previous_school = ?, doc_application_path = ?, doc_id_card_path = ?, doc_transcript_path = ?, classroom_id = ? WHERE student_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.name); pstmt.setString(2, s.address); pstmt.setString(3, s.phone);
            pstmt.setString(4, s.email); pstmt.setString(5, s.photoPath); pstmt.setInt(6, s.age);
            pstmt.setDouble(7, s.gpa); pstmt.setInt(8, s.year); pstmt.setString(9, s.status.name());
            pstmt.setString(10, s.major);
            pstmt.setString(11, s.previousSchool); pstmt.setString(12, s.docApplicationPath);
            pstmt.setString(13, s.docIdCardPath); pstmt.setString(14, s.docTranscriptPath);
            pstmt.setString(15, s.classroomId);
            pstmt.setString(16, s.id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteStudent(String id) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean isStudentIdExists(String id) {
        String sql = "SELECT 1 FROM students WHERE student_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); 
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    public int getStudentCount() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
    public void updatePassword(String username, String newPassword) {
        String newHashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newHashedPassword);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating password for " + username + ": " + e.getMessage());
        }
    }
    public String getDatabaseFileName() {
        return DATABASE_FILE_NAME;
    }

    public boolean submitLeaveRequest(String teacherId, String startDate, String endDate, String reason) {
        String sql = "INSERT INTO leave_requests(teacher_id, start_date, end_date, reason) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacherId);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);
            pstmt.setString(4, reason);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<LeaveRequest> getAllLeaveRequests() {
        ArrayList<LeaveRequest> requests = new ArrayList<>();
        String sql = """
            SELECT lr.*, t.name AS teacher_name 
            FROM leave_requests lr
            JOIN teachers t ON lr.teacher_id = t.teacher_id
            ORDER BY lr.leave_id DESC
            """;
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                requests.add(new LeaveRequest(
                    rs.getInt("leave_id"),
                    rs.getString("teacher_id"),
                    rs.getString("teacher_name"),
                    rs.getString("reason"),
                    rs.getString("status"),
                    rs.getString("start_date"),
                    rs.getString("end_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public boolean updateLeaveRequestStatus(int leaveId, String newStatus) {
        String sql = "UPDATE leave_requests SET status = ? WHERE leave_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, leaveId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private double gradeToPoint(String grade) {
        if (grade == null) return 0;
        return switch (grade.toUpperCase()) {
            case "A" -> 4.0;
            case "B+" -> 3.5;
            case "B" -> 3.0;
            case "C+" -> 2.5;
            case "C" -> 2.0;
            case "D+" -> 1.5;
            case "D" -> 1.0;
            case "F" -> 0.0;
            default -> 0.0;
        };
    }

    public ArrayList<EnrollmentRecord> getEnrollmentsForStudent(String studentId) {
        ArrayList<EnrollmentRecord> records = new ArrayList<>();
        String sql = """
            SELECT e.enrollment_id, s.subject_id, s.subject_name, s.credits, e.grade
            FROM enrollments e
            JOIN subjects s ON e.subject_id = s.subject_id
            WHERE e.student_id = ?
            ORDER BY s.subject_id
            """;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                records.add(new EnrollmentRecord(
                    rs.getInt("enrollment_id"),
                    rs.getString("subject_id"),
                    rs.getString("subject_name"),
                    rs.getInt("credits"),
                    rs.getString("grade")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public boolean enrollStudentInCourse(String studentId, String subjectId) {
        String checkSubjectSql = "SELECT 1 FROM subjects WHERE subject_id = ?";
        String checkEnrollSql = "SELECT 1 FROM enrollments WHERE student_id = ? AND subject_id = ?";
        String insertSql = "INSERT INTO enrollments(student_id, subject_id, grade) VALUES (?, ?, 'N/A')";
        
        try (Connection conn = connect()) {
            try (PreparedStatement pstmt = conn.prepareStatement(checkSubjectSql)) {
                pstmt.setString(1, subjectId);
                if (!pstmt.executeQuery().next()) {
                    System.err.println("Enroll Error: Subject ID not found");
                    return false;
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement(checkEnrollSql)) {
                pstmt.setString(1, studentId);
                pstmt.setString(2, subjectId);
                if (pstmt.executeQuery().next()) {
                    System.err.println("Enroll Error: Already enrolled");
                    return false;
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, studentId);
                pstmt.setString(2, subjectId);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateGrade(int enrollmentId, String grade) {
        String sql = "UPDATE enrollments SET grade = ? WHERE enrollment_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, grade);
            pstmt.setInt(2, enrollmentId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double calculateAndUpdatStudentGPA(String studentId) {
        ArrayList<EnrollmentRecord> records = getEnrollmentsForStudent(studentId);
        
        double totalPoints = 0;
        int totalCredits = 0;

        for (EnrollmentRecord record : records) {
            if (record.grade != null && !record.grade.equals("W") && !record.grade.equals("N/A")) {
                double point = gradeToPoint(record.grade);
                totalPoints += (point * record.credits);
                totalCredits += record.credits;
            }
        }

        double gpax = (totalCredits == 0) ? 0.0 : (totalPoints / totalCredits);

        String sql = "UPDATE students SET gpa = ? WHERE student_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, gpax);
            pstmt.setString(2, studentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return gpax;
    }
    public ArrayList<FinanceSummary> getAllStudentFinanceSummary() {
        ArrayList<FinanceSummary> summaries = new ArrayList<>();
        String sql = "SELECT student_id, name FROM students ORDER BY student_id";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String studentId = rs.getString("student_id");
                String studentName = rs.getString("name");
                
                double totalDue = getTotalDueForStudent(conn, studentId);
                double totalPaid = getTotalPaidForStudent(conn, studentId);
                
                summaries.add(new FinanceSummary(studentId, studentName, totalDue, totalPaid));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summaries;
    }

    private double getTotalDueForStudent(Connection conn, String studentId) throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM Invoices WHERE student_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }
    
    private double getTotalPaidForStudent(Connection conn, String studentId) throws SQLException {
        String sql = "SELECT SUM(amount_paid) FROM Transactions WHERE student_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }

    public ArrayList<Invoice> getInvoicesForStudent(String studentId) {
        ArrayList<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM Invoices WHERE student_id = ? ORDER BY issue_date DESC";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                invoices.add(new Invoice(
                    rs.getInt("invoice_id"),
                    rs.getString("student_id"),
                    rs.getString("semester_id"),
                    rs.getString("issue_date"),
                    rs.getString("due_date"),
                    rs.getDouble("total_amount"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    public ArrayList<Transaction> getTransactionsForStudent(String studentId) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM Transactions WHERE student_id = ? ORDER BY payment_date DESC";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getInt("transaction_id"),
                    rs.getInt("invoice_id"),
                    rs.getString("student_id"),
                    rs.getString("payment_date"),
                    rs.getDouble("amount_paid"),
                    rs.getString("payment_method"),
                    rs.getString("reference_code")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public ArrayList<Invoice> getPendingInvoicesForStudent(String studentId) {
        ArrayList<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM Invoices WHERE student_id = ? AND status = 'PENDING' ORDER BY due_date ASC";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                 invoices.add(new Invoice(
                    rs.getInt("invoice_id"), rs.getString("student_id"), rs.getString("semester_id"),
                    rs.getString("issue_date"), rs.getString("due_date"),
                    rs.getDouble("total_amount"), rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    public int generateInvoicesForSemester(String semesterId, double baseFee, String issueDate, String dueDate) {
        String sqlFindStudents = "SELECT student_id FROM students WHERE status = 'ENROLLED'";
        String sqlInsertInvoice = "INSERT INTO Invoices(student_id, semester_id, issue_date, due_date, total_amount, status) VALUES (?, ?, ?, ?, ?, 'PENDING')";
        String sqlInsertItem = "INSERT INTO InvoiceItems(invoice_id, description, amount) VALUES (?, ?, ?)";
        
        int count = 0;
        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlFindStudents)) {
                 
                while (rs.next()) {
                    String studentId = rs.getString("student_id");
                    
                    try (PreparedStatement pstmtInv = conn.prepareStatement(sqlInsertInvoice, Statement.RETURN_GENERATED_KEYS)) {
                        pstmtInv.setString(1, studentId);
                        pstmtInv.setString(2, semesterId);
                        pstmtInv.setString(3, issueDate);
                        pstmtInv.setString(4, dueDate);
                        pstmtInv.setDouble(5, baseFee);
                        pstmtInv.executeUpdate();
                        
                        int invoiceId;
                        try (ResultSet generatedKeys = pstmtInv.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                invoiceId = generatedKeys.getInt(1);
                            } else {
                                throw new SQLException("Creating invoice failed, no ID obtained.");
                            }
                        }
                        
                        try (PreparedStatement pstmtItem = conn.prepareStatement(sqlInsertItem)) {
                            pstmtItem.setInt(1, invoiceId);
                            pstmtItem.setString(2, "Base Tuition Fee - Semester " + semesterId);
                            pstmtItem.setDouble(3, baseFee);
                            pstmtItem.executeUpdate();
                        }
                        count++;
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return -1;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return count;
    }

    public boolean addPayment(Transaction tx) {
        String sqlInsertTx = "INSERT INTO Transactions(invoice_id, student_id, payment_date, amount_paid, payment_method, reference_code) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlCheckInvoice = "SELECT total_amount FROM Invoices WHERE invoice_id = ?";
        String sqlSumPayments = "SELECT SUM(amount_paid) FROM Transactions WHERE invoice_id = ?";
        String sqlUpdateInvoice = "UPDATE Invoices SET status = 'PAID' WHERE invoice_id = ?";

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false); // ⭐️ เริ่ม Transaction

            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertTx)) {
                pstmt.setInt(1, tx.invoiceId);
                pstmt.setString(2, tx.studentId);
                pstmt.setString(3, tx.paymentDate);
                pstmt.setDouble(4, tx.amountPaid);
                pstmt.setString(5, tx.paymentMethod);
                pstmt.setString(6, tx.referenceCode);
                pstmt.executeUpdate();
            }

            // 2. ตรวจสอบว่าจ่ายครบหรือยัง
            double totalDue = 0;
            double totalPaid = 0;
            
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCheckInvoice)) {
                pstmt.setInt(1, tx.invoiceId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) totalDue = rs.getDouble(1);
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sqlSumPayments)) {
                pstmt.setInt(1, tx.invoiceId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) totalPaid = rs.getDouble(1);
            }
            
            // 3. ถ้าครบ, อัปเดตสถานะ Invoice
            if (totalPaid >= totalDue) {
                try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateInvoice)) {
                    pstmt.setInt(1, tx.invoiceId);
                    pstmt.executeUpdate();
                }
            }
            
            conn.commit(); // ⭐️ ยืนยัน Transaction
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean addFinancialAid(FinancialAid aid) {
        String sqlInsertAid = "INSERT INTO FinancialAid(student_id, semester_id, aid_type, description, amount, apply_date) VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertAid)) {
                pstmt.setString(1, aid.studentId);
                pstmt.setString(2, aid.semesterId);
                pstmt.setString(3, aid.aidType);
                pstmt.setString(4, aid.description);
                pstmt.setDouble(5, aid.amount);
                pstmt.setString(6, aid.applyDate);
                pstmt.executeUpdate();
            }

            Transaction tx = new Transaction(
                aid.invoiceId, 
                aid.studentId, 
                aid.amount, 
                aid.aidType,
                aid.description
            );
            
            boolean paymentSuccess = addPaymentInternal(tx, conn); 

            if (paymentSuccess) {
                conn.commit();
                return true;
            } else {
                throw new SQLException("Failed to create internal transaction for financial aid.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private boolean addPaymentInternal(Transaction tx, Connection conn) throws SQLException {
        String sqlInsertTx = "INSERT INTO Transactions(invoice_id, student_id, payment_date, amount_paid, payment_method, reference_code) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlCheckInvoice = "SELECT total_amount FROM Invoices WHERE invoice_id = ?";
        String sqlSumPayments = "SELECT SUM(amount_paid) FROM Transactions WHERE invoice_id = ?";
        String sqlUpdateInvoice = "UPDATE Invoices SET status = 'PAID' WHERE invoice_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertTx)) {
            pstmt.setInt(1, tx.invoiceId);
            pstmt.setString(2, tx.studentId);
            pstmt.setString(3, tx.paymentDate);
            pstmt.setDouble(4, tx.amountPaid);
            pstmt.setString(5, tx.paymentMethod);
            pstmt.setString(6, tx.referenceCode);
            pstmt.executeUpdate();
        }

        double totalDue = 0;
        double totalPaid = 0;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sqlCheckInvoice)) {
            pstmt.setInt(1, tx.invoiceId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) totalDue = rs.getDouble(1);
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sqlSumPayments)) {
            pstmt.setInt(1, tx.invoiceId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) totalPaid = rs.getDouble(1);
        }
        
        if (totalPaid >= totalDue) {
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateInvoice)) {
                pstmt.setInt(1, tx.invoiceId);
                pstmt.executeUpdate();
            }
        }
        return true;
    }

    public Invoice getInvoiceById(int invoiceId) {
        String sql = "SELECT * FROM Invoices WHERE invoice_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Invoice(
                    rs.getInt("invoice_id"), rs.getString("student_id"), rs.getString("semester_id"),
                    rs.getString("issue_date"), rs.getString("due_date"),
                    rs.getDouble("total_amount"), rs.getString("status")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public ArrayList<InvoiceItem> getInvoiceItems(int invoiceId) {
        ArrayList<InvoiceItem> items = new ArrayList<>();
        String sql = "SELECT * FROM InvoiceItems WHERE invoice_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(new InvoiceItem(
                    rs.getInt("item_id"),
                    rs.getString("description"),
                    rs.getDouble("amount")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return items;
    }

    public ArrayList<Transaction> getTransactionsForInvoice(int invoiceId) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM Transactions WHERE invoice_id = ? ORDER BY payment_date ASC";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getInt("transaction_id"), rs.getInt("invoice_id"), rs.getString("student_id"),
                    rs.getString("payment_date"), rs.getDouble("amount_paid"),
                    rs.getString("payment_method"), rs.getString("reference_code")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return transactions;
    }
    public ArrayList<DataPoint> getStudentCountPerMajor() {
        ArrayList<DataPoint> data = new ArrayList<>();
        String sql = "SELECT major, COUNT(*) as count FROM students GROUP BY major ORDER BY count DESC";
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                data.add(new DataPoint(rs.getString("major"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public ArrayList<DataPoint> getStudentStatusCount() {
        ArrayList<DataPoint> data = new ArrayList<>();
        String sql = "SELECT status, COUNT(*) as count FROM students GROUP BY status";
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                data.add(new DataPoint(rs.getString("status"), rs.getInt("count")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    public ArrayList<DataPoint> getMostFailedCourses() {
        ArrayList<DataPoint> data = new ArrayList<>();
        String sql = """
            SELECT s.subject_name, COUNT(*) as fail_count
            FROM enrollments e
            JOIN subjects s ON e.subject_id = s.subject_id
            WHERE e.grade = 'F'
            GROUP BY s.subject_name
            ORDER BY fail_count DESC
            LIMIT 5
            """;
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                data.add(new DataPoint(rs.getString("subject_name"), rs.getInt("fail_count")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public FinancialReport getFinancialSummaryReport() {
        double totalDue = 0;
        double totalPaid = 0;
        int totalTx = 0;

        String sqlDue = "SELECT SUM(total_amount) FROM Invoices";
        String sqlPaid = "SELECT SUM(amount_paid), COUNT(*) FROM Transactions";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            
            try (ResultSet rsDue = stmt.executeQuery(sqlDue)) {
                if (rsDue.next()) {
                    totalDue = rsDue.getDouble(1);
                }
            }
            
            try (ResultSet rsPaid = stmt.executeQuery(sqlPaid)) {
                if (rsPaid.next()) {
                    totalPaid = rsPaid.getDouble(1);
                    totalTx = rsPaid.getInt(2);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return new FinancialReport(totalDue, totalPaid, totalTx);
    }
    public void logActivity(String username, String action) {
        String sql = "INSERT INTO activity_log(log_timestamp, username, action_description) VALUES (?, ?, ?)";
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, timestamp);
            pstmt.setString(2, username);
            pstmt.setString(3, action);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<UserAccount> getAllUserAccounts() {
        ArrayList<UserAccount> users = new ArrayList<>();
        String sql = "SELECT username, role FROM users";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new UserAccount(rs.getString("username"), rs.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public ArrayList<ActivityLog> getActivityLogs() {
        ArrayList<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM activity_log ORDER BY log_id DESC LIMIT 100";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new ActivityLog(
                    rs.getInt("log_id"),
                    rs.getString("log_timestamp"),
                    rs.getString("username"),
                    rs.getString("action_description")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    public boolean resetUserPassword(String username, String newPassword) {
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hash);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserRole(String username, Role newRole) {
        String sql = "UPDATE users SET role = ? WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole.name());
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(String username, Role role) {
        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false); 
            if (role == Role.STUDENT) {
                runSqlInTransaction(conn, "DELETE FROM enrollments WHERE student_id = ?", new Object[]{username});
                runSqlInTransaction(conn, "DELETE FROM students WHERE student_id = ?", new Object[]{username});
            } else if (role == Role.TEACHER) {
                runSqlInTransaction(conn, "DELETE FROM teaching_assignments WHERE teacher_id = ?", new Object[]{username});
                runSqlInTransaction(conn, "DELETE FROM leave_requests WHERE teacher_id = ?", new Object[]{username});
                runSqlInTransaction(conn, "DELETE FROM teachers WHERE teacher_id = ?", new Object[]{username});
            }
            
            runSqlInTransaction(conn, "DELETE FROM users WHERE username = ?", new Object[]{username});

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void runSqlInTransaction(Connection conn, String sql, Object[] params) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
            }
            pstmt.executeUpdate();
        }
    }

public ArrayList<Subject> getSubjectsForTeacher(String teacherId) {
    ArrayList<Subject> subjects = new ArrayList<>();
    String sql = """
        SELECT s.subject_id, s.subject_name, s.credits,
               m.major_name, sem.semester_name,
               t.name AS teacher_name,
               ta.room, ta.schedule_day, ta.schedule_time,
               (SELECT GROUP_CONCAT(p.prerequisite_subject_id) 
                FROM prerequisites p 
                WHERE p.subject_id = s.subject_id) AS prerequisites
        FROM subjects s
        LEFT JOIN majors m ON s.major_id = m.major_id
        LEFT JOIN semesters sem ON s.semester_id = sem.semester_id
        LEFT JOIN teaching_assignments ta ON s.subject_id = ta.subject_id
        LEFT JOIN teachers t ON ta.teacher_id = t.teacher_id
        WHERE ta.teacher_id = ?
        """;
    
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, teacherId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            String schedule = rs.getString("schedule_day") + " (" + rs.getString("schedule_time") + ") @ " + rs.getString("room");
            if (rs.getString("schedule_day") == null) {
                schedule = "Not Assigned";
            }
            subjects.add(new Subject(
                rs.getString("subject_id"),
                rs.getString("subject_name"),
                rs.getInt("credits"),
                rs.getString("major_name"),
                rs.getString("semester_name"),
                rs.getString("teacher_name"),
                schedule,
                rs.getString("prerequisites")
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return subjects;
}

public ArrayList<EnrollmentRecord> getEnrollmentsForSubject(String subjectId) {
        ArrayList<EnrollmentRecord> records = new ArrayList<>();
        String sql = """
            SELECT e.enrollment_id, s.student_id, s.name AS student_name,
                   sub.subject_id, sub.subject_name, sub.credits, e.grade
            FROM enrollments e
            JOIN students s ON e.student_id = s.student_id
            JOIN subjects sub ON e.subject_id = sub.subject_id
            WHERE e.subject_id = ?
            ORDER BY s.name
            """;
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subjectId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                EnrollmentRecord er = new EnrollmentRecord(
                    rs.getInt("enrollment_id"),
                    rs.getString("subject_id"),
                    rs.getString("subject_name"),
                    rs.getInt("credits"),
                    rs.getString("grade")
                );
                er.studentId = rs.getString("student_id");
                er.studentName = rs.getString("student_name");
                records.add(er);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public ArrayList<AssignmentGrade> getAssignmentGradesForEnrollment(int enrollmentId) {
        ArrayList<AssignmentGrade> grades = new ArrayList<>();
        String sql = "SELECT * FROM assignment_grades WHERE enrollment_id = ? ORDER BY date_recorded DESC";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                grades.add(new AssignmentGrade(
                    rs.getInt("grade_id"),
                    rs.getInt("enrollment_id"),
                    rs.getString("assignment_name"),
                    rs.getDouble("score"),
                    rs.getDouble("max_score")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grades;
    }

    public boolean addAssignmentGrade(int enrollmentId, String name, double score, double maxScore) {
        String sql = "INSERT INTO assignment_grades(enrollment_id, assignment_name, score, max_score, date_recorded) VALUES (?, ?, ?, ?, ?)";
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            pstmt.setString(2, name);
            pstmt.setDouble(3, score);
            pstmt.setDouble(4, maxScore);
            pstmt.setString(5, timestamp);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAssignmentGrade(int gradeId, String name, double score, double maxScore) {
        String sql = "UPDATE assignment_grades SET assignment_name = ?, score = ?, max_score = ? WHERE grade_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, score);
            pstmt.setDouble(3, maxScore);
            pstmt.setInt(4, gradeId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAssignmentGrade(int gradeId) {
        String sql = "DELETE FROM assignment_grades WHERE grade_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gradeId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
class DataPoint {
    String label;
    int count;
    double value;

    public DataPoint(String label, int count) {
        this.label = label;
        this.count = count;
    }
    
    public DataPoint(String label, double value) {
        this.label = label;
        this.value = value;
    }
}

class FinancialReport {
    double totalDue;
    double totalPaid;
    double netBalance;
    int totalTransactions;

    public FinancialReport(double totalDue, double totalPaid, int totalTransactions) {
        this.totalDue = totalDue;
        this.totalPaid = totalPaid;
        this.netBalance = totalPaid - totalDue;
        this.totalTransactions = totalTransactions;
    }
}
class UserAccount {
    String username;
    String role;
    String lastLogin;

    public UserAccount(String username, String role) {
        this.username = username;
        this.role = role;
        this.lastLogin = "N/A";
    }
}

class ActivityLog {
    int id;
    String timestamp;
    String username;
    String action;

    public ActivityLog(int id, String timestamp, String username, String action) {
        this.id = id;
        this.timestamp = timestamp;
        this.username = username;
        this.action = action;
    }
}

class AssignmentGrade {
    int gradeId;
    int enrollmentId;
    String assignmentName;
    double score;
    double maxScore;

    public AssignmentGrade(int gradeId, int enrollmentId, String assignmentName, double score, double maxScore) {
        this.gradeId = gradeId;
        this.enrollmentId = enrollmentId;
        this.assignmentName = assignmentName;
        this.score = score;
        this.maxScore = maxScore;
    }

    public AssignmentGrade(int enrollmentId, String assignmentName, double score, double maxScore) {
        this(-1, enrollmentId, assignmentName, score, maxScore);
    }
}