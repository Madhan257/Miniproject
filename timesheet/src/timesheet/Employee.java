package timesheet;
import java.util.ArrayList;
import java.util.List;

public class Employee {
    private int id;
    private String name;
    private List<Timesheet> timesheets;

    public Employee(String name) {
        this.name = name;
        timesheets = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public List<Timesheet> getTimesheets() {
        return timesheets;
    }
}