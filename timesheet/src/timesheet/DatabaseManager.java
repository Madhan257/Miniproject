package timesheet;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:mysql://127.0.0.1:3306/emsystem";
    private static final String DATABASE_USERNAME = "root";
    private static final String DATABASE_PASSWORD = "madhan25";
    private static Connection connection;

    public static void connect() throws SQLException {
        connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
        createTablesIfNotExist();
    }

    public static void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    private static void createTablesIfNotExist() throws SQLException {
        String createEmployeesTableQuery = "CREATE TABLE IF NOT EXISTS employees (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "name TEXT" +
                ");";

        String createTimesheetsTableQuery = "CREATE TABLE IF NOT EXISTS timesheets (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "employee_id INTEGER," +
                "date DATE," +
                "hours_worked DOUBLE," +
                "status VARCHAR(10)," +
                "FOREIGN KEY (employee_id) REFERENCES employees(id)" +
                ");";

        connection.createStatement().execute(createEmployeesTableQuery);
        connection.createStatement().execute(createTimesheetsTableQuery);
    }

    public static int insertEmployee(Employee employee) throws SQLException {
        String insertQuery = "INSERT INTO employees (name) VALUES (?);";
        PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, employee.getName());
        statement.executeUpdate();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        }

        return -1;
    }

    public static void insertTimesheet(int employeeId, Timesheet timesheet) throws SQLException {
        String insertQuery = "INSERT INTO timesheets (employee_id, date, hours_worked, status) VALUES (?, ?, ?, ?);";
        PreparedStatement statement = connection.prepareStatement(insertQuery);
        statement.setInt(1, employeeId);
        statement.setDate(2, new java.sql.Date(timesheet.getDate().getTime()));
        statement.setDouble(3, timesheet.getHoursWorked());
        statement.setString(4, timesheet.getStatus());
        statement.executeUpdate();
    }

    public static Map<Integer, Employee> getAllEmployees() throws SQLException {
        Map<Integer, Employee> employees = new HashMap<>();
        String selectQuery = "SELECT * FROM employees;";
        ResultSet resultSet = connection.createStatement().executeQuery(selectQuery);

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            Employee employee = new Employee(name);
            employee.setId(id);
            employees.put(id, employee);
        }

        return employees;
    }

    public static List<Timesheet> getTimesheetsForEmployee(int employeeId) throws SQLException {
        List<Timesheet> timesheets = new ArrayList<>();
        String selectQuery = "SELECT * FROM timesheets WHERE employee_id = ?;";
        PreparedStatement statement = connection.prepareStatement(selectQuery);
        statement.setInt(1, employeeId);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            Date date = resultSet.getDate("date");
            double hoursWorked = resultSet.getDouble("hours_worked");
            String status = resultSet.getString("status");
            Timesheet timesheet = new Timesheet(date, hoursWorked);
            timesheet.setId(id);
            timesheet.setEmployeeId(employeeId);
            timesheet.setStatus(status);
            timesheets.add(timesheet);
        }

        return timesheets;
    }

    public static List<Timesheet> getPendingTimesheets() throws SQLException {
        List<Timesheet> pendingTimesheets = new ArrayList<>();
        String selectQuery = "SELECT * FROM timesheets WHERE status IS NULL;";
        ResultSet resultSet = connection.createStatement().executeQuery(selectQuery);

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int employeeId = resultSet.getInt("employee_id");
            Date date = resultSet.getDate("date");
            double hoursWorked = resultSet.getDouble("hours_worked");
            Timesheet timesheet = new Timesheet(date, hoursWorked);
            timesheet.setId(id);
            timesheet.setEmployeeId(employeeId);
            pendingTimesheets.add(timesheet);
        }

        return pendingTimesheets;
    }

    public static Timesheet getTimesheetById(int timesheetId) throws SQLException {
        String selectQuery = "SELECT * FROM timesheets WHERE id = ?;";
        PreparedStatement statement = connection.prepareStatement(selectQuery);
        statement.setInt(1, timesheetId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            int id = resultSet.getInt("id");
            int employeeId = resultSet.getInt("employee_id");
            Date date = resultSet.getDate("date");
            double hoursWorked = resultSet.getDouble("hours_worked");
            String status = resultSet.getString("status");
            Timesheet timesheet = new Timesheet(date, hoursWorked);
            timesheet.setId(id);
            timesheet.setEmployeeId(employeeId);
            timesheet.setStatus(status);
            return timesheet;
        }

        return null;
    }

    public static void updateTimesheetStatus(int timesheetId, String status) throws SQLException {
        String updateQuery = "UPDATE timesheets SET status = ? WHERE id = ?;";
        PreparedStatement statement = connection.prepareStatement(updateQuery);
        statement.setString(1, status);
        statement.setInt(2, timesheetId);
        statement.executeUpdate();
    }

}