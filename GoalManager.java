import java.time.LocalDate;
import java.util.ArrayList;

/** Holds all goals in an ArrayList and calculates totals across them. */
public class GoalManager {
    private final ArrayList<Goal> goals = new ArrayList<>();

    public void add(Goal g) { goals.add(g); }
    public void remove(int index) { goals.remove(index); }
    public Goal get(int index) { return goals.get(index); }
    public int size() { return goals.size(); }
    public ArrayList<Goal> getAll() { return goals; }

    public void setAll(ArrayList<Goal> list) {
        goals.clear();
        goals.addAll(list);
    }

    public double totalTarget() {
        double sum = 0;
        for (Goal g : goals) sum += g.getTarget();
        return sum;
    }

    public double totalSaved() {
        double sum = 0;
        for (Goal g : goals) sum += g.getSaved();
        return sum;
    }

    /** Sum of the monthly amounts needed for all goals (polymorphic call on each goal). */
    public double totalMonthlyNeeded(LocalDate today) {
        double sum = 0;
        for (Goal g : goals) sum += g.monthlyNeeded(today);
        return sum;
    }
}
