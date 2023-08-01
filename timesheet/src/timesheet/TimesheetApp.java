package timesheet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TimesheetApp {
    private static Scanner scanner = new Scanner(System.in);
    private static Map<Integer, Employee> employees = new HashMap<>();
    private static int employeeIdCounter = 1;

    public static void main(String[] args) {
        try {
            DatabaseManager.connect();
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database.");
            e.printStackTrace();
            return;
        }

        displayLoginScreen();
    }

    private static void displayLoginScreen() {
        System.out.println("Welcome to Timesheet App");
        System.out.println("-------------------------");
        System.out.println("1. Employee Login");
        System.out.println("2. Manager (View Employee Details and Approve Timesheets)");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        switch (choice) {
            case 1:
                employeeLogin();
                break;
            case 2:
                managerLogin();
                break;
            case 3:
                System.out.println("Goodbye!");
                scanner.close();
                try {
                    DatabaseManager.disconnect();
                } catch (SQLException e) {
                    System.out.println("Failed to disconnect from the database.");
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                displayLoginScreen();
        }
    }

    private static void employeeLogin() {
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        Employee employee = new Employee(name);
        employee.setId(employeeIdCounter);
        employeeIdCounter++;
        employees.put(employee.getId(), employee);

        displayEmployeeMenu(employee);
    }

    private static void displayEmployeeMenu(Employee employee) {
        System.out.println("Hello, " + employee.getName() + " (Employee ID: " + employee.getId() + ")");
        System.out.println("-----------------------------------------------");
        System.out.println("1. Enter Timesheet");
        System.out.println("2. Logout");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        switch (choice) {
            case 1:
                enterTimesheet(employee);
                break;
            case 2:
                displayLoginScreen();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                displayEmployeeMenu(employee);
        }
    }
    
    private static void enterTimesheet(Employee employee) {
        System.out.println("Enter Timesheet for " + employee.getName());
        System.out.println("---------------------------------");
        System.out.print("Enter date (yyyy-MM-dd): ");
        String dateString = scanner.nextLine();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            System.out.println("Invalid date format. Please try again.");
            enterTimesheet(employee);
            return;
        }

        System.out.print("Enter hours worked: ");
        double hoursWorked = scanner.nextDouble();
        scanner.nextLine(); // Consume the newline character

        Timesheet timesheet = new Timesheet(date, hoursWorked);
        employee.getTimesheets().add(timesheet);

        try {
            // Insert the employee into the database and get the generated employeeId
            int employeeId = DatabaseManager.insertEmployee(employee);

            // Insert the timesheet for the employee into the database
            DatabaseManager.insertTimesheet(employeeId, timesheet);

            System.out.println("Timesheet entry added successfully!");
        } catch (SQLException e) {
            System.out.println("Failed to insert timesheet into the database.");
            e.printStackTrace();
        }

        displayEmployeeMenu(employee);
    }

    private static void managerLogin() {
        System.out.println("View Employee Details and Approve Timesheets");
        System.out.println("------------------------------------------");
        System.out.println("Enter the Employee ID to view details and approve timesheets: ");
        int employeeId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        Employee employee = employees.get(employeeId);
        if (employee == null) {
            System.out.println("Employee with ID " + employeeId + " not found.");
            displayLoginScreen();
            return;
        }

        try {
            // Display employee details
            System.out.println("Employee ID: " + employee.getId());
            System.out.println("Employee Name: " + employee.getName());
            System.out.println("Timesheets:");
            displayTimesheets(employeeId);

            // Approve timesheets
            System.out.println("Approve timesheets for " + employee.getName() + " (Y/N)?");
            String approvalChoice = scanner.nextLine();
            if (approvalChoice.equalsIgnoreCase("Y")) {
                approveTimesheets(employeeId);
            } else if (approvalChoice.equalsIgnoreCase("N")) {
                rejectTimesheets(employeeId); // Add this option to reject timesheets
            }

        } catch (SQLException e) {
            System.out.println("Error fetching employee details and timesheets.");
            e.printStackTrace();
        }

        displayLoginScreen();
    }

    private static void displayTimesheets(int employeeId) throws SQLException {
        List<Timesheet> timesheets = DatabaseManager.getTimesheetsForEmployee(employeeId);

        if (timesheets.isEmpty()) {
            System.out.println("No timesheets found for this employee.");
        } else {
            for (Timesheet timesheet : timesheets) {
                System.out.println("Date: " + timesheet.getDate() + ", Hours Worked: " + timesheet.getHoursWorked() +
                        ", Status: " + timesheet.getStatus());
            }
        }
    }

    private static void approveTimesheets(int employeeId) throws SQLException {
        List<Timesheet> timesheets = DatabaseManager.getTimesheetsForEmployee(employeeId);

        if (timesheets.isEmpty()) {
            System.out.println("No timesheets found for this employee.");
            return;
        }

        System.out.println("Enter Timesheet ID(s) to approve (separated by commas): ");
        String input = scanner.nextLine();
        String[] timesheetIds = input.split(",");

        for (String timesheetIdStr : timesheetIds) {
            int timesheetId = Integer.parseInt(timesheetIdStr.trim());
            Timesheet timesheet = findTimesheetById(timesheets, timesheetId);

            if (timesheet == null) {
                System.out.println("Timesheet with ID " + timesheetId + " not found.");
            } else {
                DatabaseManager.updateTimesheetStatus(timesheetId, "Approved");
                timesheet.setStatus("Approved");
                System.out.println("Timesheet with ID " + timesheetId + " has been approved.");
            }
        }
    }

    private static Timesheet findTimesheetById(List<Timesheet> timesheets, int timesheetId) {
        for (Timesheet timesheet : timesheets) {
            if (timesheet.getId() == timesheetId) {
                return timesheet;
            }
        }
        return null;
    }
    private static void rejectTimesheets(int employeeId) throws SQLException {
        List<Timesheet> timesheets = DatabaseManager.getTimesheetsForEmployee(employeeId);

        if (timesheets.isEmpty()) {
            System.out.println("No timesheets found for this employee.");
            return;
        }

        System.out.println("Enter Timesheet ID(s) to reject (separated by commas): ");
        String input = scanner.nextLine();
        String[] timesheetIds = input.split(",");

        for (String timesheetIdStr : timesheetIds) {
            int timesheetId = Integer.parseInt(timesheetIdStr.trim());
            Timesheet timesheet = findTimesheetById(timesheets, timesheetId);

            if (timesheet == null) {
                System.out.println("Timesheet with ID " + timesheetId + " not found.");
            } else {
                DatabaseManager.updateTimesheetStatus(timesheetId, "Rejected");
                timesheet.setStatus("Rejected");
                System.out.println("Timesheet with ID " + timesheetId + " has been rejected.");
            }
        }
    }

}