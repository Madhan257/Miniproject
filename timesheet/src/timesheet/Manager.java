package timesheet;
import java.util.ArrayList;
import java.util.List;

public class Manager extends Employee {
    private List<Timesheet> pendingTimesheets;

    public Manager(String name) {
        super(name);
        pendingTimesheets = new ArrayList<>();
    }

    public List<Timesheet> getPendingTimesheets() {
        return pendingTimesheets;
    }

    @Override
    public String toString() {
        return "Manager{" +
                "name='" + getName() + '\'' +
                ", pendingTimesheets=" + pendingTimesheets +
                '}';
    }
}