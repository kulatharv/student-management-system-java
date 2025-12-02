import java.io.*;
import java.util.*;

/**
 * Simple console Student Management System.
 * Stores students in an ArrayList and persists to students.csv.
 */
class Student {
    private int id;
    private String name;
    private int age;
    private String course;

    public Student(int id, String name, int age, String course) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.course = course;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getCourse() { return course; }

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setCourse(String course) { this.course = course; }

    @Override
    public String toString() {
        return String.format("%d | %s | %d | %s", id, name, age, course);
    }

    public String toCsv() {
        // escape commas in name/course (basic)
        return id + "," + name.replace(",", " ") + "," + age + "," + course.replace(",", " ");
    }

    public static Student fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 4) return null;
        try {
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            int age = Integer.parseInt(parts[2].trim());
            String course = parts[3].trim();
            return new Student(id, name, age, course);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

public class StudentManagement {
    private static final String DATA_FILE = "students.csv";
    private List<Student> students = new ArrayList<>();
    private int nextId = 1;
    private Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        StudentManagement app = new StudentManagement();
        app.loadFromFile();
        app.run();
    }

    private void run() {
        while (true) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": addStudent(); break;
                case "2": updateStudent(); break;
                case "3": deleteStudent(); break;
                case "4": searchByName(); break;
                case "5": listAll(); break;
                case "6": saveToFile(); break;
                case "0": 
                    saveToFile();
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== Student Management ===");
        System.out.println("1. Add Student");
        System.out.println("2. Update Student by ID");
        System.out.println("3. Delete Student by ID");
        System.out.println("4. Search Students by Name");
        System.out.println("5. List All Students");
        System.out.println("6. Save to file now");
        System.out.println("0. Exit (auto-save)");
        System.out.print("Enter choice: ");
    }

    private void addStudent() {
        System.out.print("Enter name: ");
        String name = sc.nextLine().trim();
        int age = readInt("Enter age: ");
        System.out.print("Enter course: ");
        String course = sc.nextLine().trim();
        Student s = new Student(nextId++, name, age, course);
        students.add(s);
        System.out.println("Added: " + s);
    }

    private void updateStudent() {
        int id = readInt("Enter student ID to update: ");
        Student s = findById(id);
        if (s == null) {
            System.out.println("No student with ID " + id);
            return;
        }
        System.out.println("Current: " + s);
        System.out.print("New name (leave blank to keep): ");
        String name = sc.nextLine().trim();
        if (!name.isEmpty()) s.setName(name);
        String ageStr;
        System.out.print("New age (leave blank to keep): ");
        ageStr = sc.nextLine().trim();
        if (!ageStr.isEmpty()) {
            try { s.setAge(Integer.parseInt(ageStr)); }
            catch (NumberFormatException e) { System.out.println("Invalid age, keeping old value."); }
        }
        System.out.print("New course (leave blank to keep): ");
        String course = sc.nextLine().trim();
        if (!course.isEmpty()) s.setCourse(course);
        System.out.println("Updated: " + s);
    }

    private void deleteStudent() {
        int id = readInt("Enter student ID to delete: ");
        Student s = findById(id);
        if (s == null) {
            System.out.println("No student with ID " + id);
            return;
        }
        students.remove(s);
        System.out.println("Deleted student ID " + id);
    }

    private void searchByName() {
        System.out.print("Enter name or substring to search: ");
        String q = sc.nextLine().trim().toLowerCase();
        List<Student> found = new ArrayList<>();
        for (Student s : students) {
            if (s.getName().toLowerCase().contains(q)) found.add(s);
        }
        if (found.isEmpty()) {
            System.out.println("No students found.");
        } else {
            System.out.println("Found:");
            for (Student s : found) System.out.println(s);
        }
    }

    private void listAll() {
        if (students.isEmpty()) {
            System.out.println("No students available.");
            return;
        }
        System.out.println("\nID | Name | Age | Course");
        System.out.println("----------------------------");
        for (Student s : students) System.out.println(s);
    }

    private Student findById(int id) {
        for (Student s : students) if (s.getId() == id) return s;
        return null;
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // Persistence: save/load simple CSV
    private void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (Student s : students) {
                pw.println(s.toCsv());
            }
            System.out.println("Saved " + students.size() + " students to " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            int maxId = 0;
            while ((line = br.readLine()) != null) {
                Student s = Student.fromCsv(line);
                if (s != null) {
                    students.add(s);
                    if (s.getId() > maxId) maxId = s.getId();
                }
            }
            nextId = maxId + 1;
            System.out.println("Loaded " + students.size() + " students from " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Failed to load: " + e.getMessage());
        }
    }
}
