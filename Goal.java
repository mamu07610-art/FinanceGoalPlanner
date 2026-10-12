import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * Abstract base class for every savings goal.
 * Subclasses decide how the monthly amount is calculated (calcMonthly),
 * so GoalManager can treat all goals the same way (polymorphism).
 */
public abstract class Goal {
    private final String name;
    private final double target;
    private final double initialSaved;      // amount already saved when the goal was created
    private final LocalDate startDate;
    private final LocalDate deadline;
    private final ArrayList<Deposit> deposits = new ArrayList<>();

    protected Goal(String name, double target, double initialSaved,
                   LocalDate startDate, LocalDate deadline) {
        this.name = name;
        this.target = target;
        this.initialSaved = initialSaved;
        this.startDate = startDate;
        this.deadline = deadline;
    }

    /** Monthly amount needed when n months are left. Each subclass has its own formula. */
    protected abstract double calcMonthly(int months);

    /** Short label shown in the table, e.g. "Simple" or "Interest". */
    public abstract String getType();

    /** Annual interest rate in percent (0 for goals without interest). */
    public double getRate() { return 0; }

    // ----- basic getters (encapsulation) -----
    public String getName() { return name; }
    public double getTarget() { return target; }
    public double getInitialSaved() { return initialSaved; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDeadline() { return deadline; }
    public ArrayList<Deposit> getDeposits() { return deposits; }

    public void addDeposit(Deposit d) { deposits.add(d); }

    /** Total saved = amount at the start + all deposits. */
    public double getSaved() {
        double sum = initialSaved;
        for (Deposit d : deposits) sum += d.getAmount();
        return sum;
    }

    /** Amount still missing (never negative). */
    public double remaining() { return Math.max(0, target - getSaved()); }

    public double progressPercent() {
        return Math.min(100, getSaved() / target * 100);
    }

    // ----- time calculations -----
    /** Whole months left until the deadline, rounded up. 0 if the deadline has passed. */
    public int monthsLeft(LocalDate today) {
        if (!deadline.isAfter(today)) return 0;
        long m = ChronoUnit.MONTHS.between(today, deadline);
        if (today.plusMonths(m).isBefore(deadline)) m++;
        return (int) m;
    }
    public int monthsLeft() { return monthsLeft(LocalDate.now()); }

    /** Share of the time between start and deadline that has already passed (0 to 100). */
    public double timeElapsedPercent(LocalDate today) {
        long total = ChronoUnit.DAYS.between(startDate, deadline);
        if (total <= 0) return 100;
        long elapsed = ChronoUnit.DAYS.between(startDate, today);
        double pct = elapsed * 100.0 / total;
        return Math.max(0, Math.min(100, pct));
    }
    public double timeElapsedPercent() { return timeElapsedPercent(LocalDate.now()); }

    // ----- main calculation -----
    /** Monthly saving needed to reach the target by the deadline. */
    public double monthlyNeeded(LocalDate today) {
        if (getSaved() >= target) return 0;
        int n = monthsLeft(today);
        if (n <= 0) return remaining();      // deadline reached: everything is due now
        return calcMonthly(n);
    }
    public double monthlyNeeded() { return monthlyNeeded(LocalDate.now()); }

    /** What-if: months needed if the user saves 'monthly' every month. -1 if monthly <= 0. */
    public int monthsToFinish(double monthly) {
        if (remaining() <= 0) return 0;
        if (monthly <= 0) return -1;
        return (int) Math.ceil(remaining() / monthly);
    }

    /** REACHED, ON TRACK, BEHIND or AT RISK. */
    public String status(LocalDate today) {
        if (getSaved() >= target) return "REACHED";
        if (today.isAfter(deadline)) return "AT RISK";
        double gap = timeElapsedPercent(today) - progressPercent();
        if (gap <= 0) return "ON TRACK";
        if (gap <= 20) return "BEHIND";
        return "AT RISK";
    }
    public String status() { return status(LocalDate.now()); }
}
