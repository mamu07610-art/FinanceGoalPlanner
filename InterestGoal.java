import java.time.LocalDate;

/**
 * A goal kept in an account that earns interest, compounded monthly.
 * Money already saved also grows, so the monthly deposit is smaller.
 */
public class InterestGoal extends Goal {
    private final double annualRate;     // in percent, e.g. 6 means 6% a year

    public InterestGoal(String name, double target, double initialSaved,
                        LocalDate startDate, LocalDate deadline, double annualRate) {
        super(name, target, initialSaved, startDate, deadline);
        this.annualRate = annualRate;
    }

    @Override
    public double getRate() { return annualRate; }

    @Override
    public String getType() { return "Interest"; }

    /** monthly = (T - S(1+i)^n) * i / ((1+i)^n - 1), where i is the monthly rate. */
    @Override
    protected double calcMonthly(int months) {
        double i = annualRate / 100.0 / 12.0;
        if (i == 0) return remaining() / months;           // avoid dividing by zero
        double growth = Math.pow(1 + i, months);
        double needed = getTarget() - getSaved() * growth;  // what savings alone will not cover
        if (needed <= 0) return 0;                          // the money already saved will grow enough
        return needed * i / (growth - 1);
    }

    /** n = ln((T + m/i) / (S + m/i)) / ln(1 + i), rounded up. */
    @Override
    public int monthsToFinish(double monthly) {
        if (remaining() <= 0) return 0;
        if (monthly <= 0) return -1;
        double i = annualRate / 100.0 / 12.0;
        if (i == 0) return super.monthsToFinish(monthly);
        double a = monthly / i;
        double n = Math.log((getTarget() + a) / (getSaved() + a)) / Math.log(1 + i);
        return (int) Math.ceil(n - 1e-9);
    }
}
