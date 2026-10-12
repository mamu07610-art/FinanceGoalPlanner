import java.time.LocalDate;

/** One deposit made towards a goal: a date and an amount. */
public class Deposit {
    private final LocalDate date;
    private final double amount;

    public Deposit(LocalDate date, double amount) {
        this.date = date;
        this.amount = amount;
    }

    public LocalDate getDate() { return date; }
    public double getAmount() { return amount; }
}
