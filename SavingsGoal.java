import java.time.LocalDate;

/** A goal kept without interest: monthly = (target - saved) / months. */
public class SavingsGoal extends Goal {

    public SavingsGoal(String name, double target, double initialSaved,
                       LocalDate startDate, LocalDate deadline) {
        super(name, target, initialSaved, startDate, deadline);
    }

    @Override
    protected double calcMonthly(int months) {
        return remaining() / months;
    }

    @Override
    public String getType() { return "Simple"; }
}
