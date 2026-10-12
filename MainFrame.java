import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Swing window: goals table, add/delete/deposit form, details, progress bar and what-if box. */
public class MainFrame extends JFrame {
    private final GoalManager manager = new GoalManager();
    private final FileManager fileManager = new FileManager();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Goal", "Type", "Target", "Saved", "Progress", "Monthly needed", "Months left", "Status"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField nameField = new JTextField(12);
    private final JTextField targetField = new JTextField(12);
    private final JTextField savedField = new JTextField("0", 12);
    private final JTextField deadlineField = new JTextField(LocalDate.now().plusMonths(6).toString(), 12);
    private final JTextField rateField = new JTextField("0", 12);
    private final JTextField depositField = new JTextField(12);
    private final JTextField whatIfField = new JTextField(12);

    private final JTextArea details = new JTextArea(8, 30);
    private final JTextArea whatIfResult = new JTextArea(4, 20);
    private final JProgressBar bar = new JProgressBar(0, 100);
    private final JLabel summary = new JLabel(" ");

    public MainFrame() {
        super("Personal Finance Goal Planner");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        manager.setAll(fileManager.load());

        JLabel title = new JLabel("Personal Finance Goal Planner - how much should I save each month?");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.EAST);
        summary.setFont(new Font("Arial", Font.BOLD, 12));
        add(summary, BorderLayout.SOUTH);

        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetails();
        });
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean focus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                setHorizontalAlignment(CENTER);
                comp.setBackground(colourFor(String.valueOf(v)));
                comp.setForeground(Color.BLACK);
                return comp;
            }
        });

        refresh();
        setPreferredSize(new Dimension(1150, 650));
        pack();
        setLocationRelativeTo(null);
    }

    // ---------------------------------------------------------------- layout
    private JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("My goals"));

        details.setEditable(false);
        details.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane detailScroll = new JScrollPane(details);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Selected goal"));
        bar.setStringPainted(true);

        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        bottom.add(detailScroll, BorderLayout.CENTER);
        bottom.add(bar, BorderLayout.SOUTH);

        p.add(tableScroll, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildForm() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        // Add goal
        JPanel add = new JPanel(new GridBagLayout());
        add.setBorder(BorderFactory.createTitledBorder("Add a goal"));
        addRow(add, 0, "Goal name:", nameField);
        addRow(add, 1, "Target amount:", targetField);
        addRow(add, 2, "Saved so far:", savedField);
        addRow(add, 3, "Deadline (yyyy-MM-dd):", deadlineField);
        addRow(add, 4, "Interest % a year (0 = none):", rateField);
        JButton addBtn = new JButton("Add Goal");
        JButton delBtn = new JButton("Delete Selected Goal");
        addBtn.addActionListener(e -> addGoal());
        delBtn.addActionListener(e -> deleteGoal());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        btns.add(addBtn);
        btns.add(delBtn);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        add.add(btns, g);

        // Deposit
        JPanel dep = new JPanel(new GridBagLayout());
        dep.setBorder(BorderFactory.createTitledBorder("Add a deposit to the selected goal"));
        addRow(dep, 0, "Amount:", depositField);
        JButton depBtn = new JButton("Add Deposit");
        depBtn.addActionListener(e -> addDeposit());
        g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 1; g.gridwidth = 2;
        dep.add(depBtn, g);

        // What-if
        JPanel wi = new JPanel(new GridBagLayout());
        wi.setBorder(BorderFactory.createTitledBorder("What if I save this much a month?"));
        addRow(wi, 0, "Monthly amount:", whatIfField);
        JButton wiBtn = new JButton("Simulate");
        wiBtn.addActionListener(e -> whatIf());
        g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 1; g.gridwidth = 2;
        wi.add(wiBtn, g);
        whatIfResult.setEditable(false);
        whatIfResult.setLineWrap(true);
        whatIfResult.setWrapStyleWord(true);
        whatIfResult.setBackground(wi.getBackground());
        g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        wi.add(whatIfResult, g);

        side.add(add);
        side.add(Box.createVerticalStrut(8));
        side.add(dep);
        side.add(Box.createVerticalStrut(8));
        side.add(wi);
        return side;
    }

    private void addRow(JPanel p, int row, String label, JTextField field) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = row;
        p.add(new JLabel(label), g);
        g.gridx = 1;
        p.add(field, g);
    }

    // ---------------------------------------------------------------- actions
    private void addGoal() {
        try {
            String name = nameField.getText().trim().replace('|', ' ');
            if (name.isEmpty()) throw new IllegalArgumentException("Goal name cannot be empty.");

            double target = parseNumber(targetField.getText(), "Target amount");
            if (target <= 0) throw new IllegalArgumentException("Target amount must be greater than 0.");

            double saved = parseNumber(savedField.getText(), "Saved so far");
            if (saved < 0) throw new IllegalArgumentException("Saved amount cannot be negative.");

            String rateText = rateField.getText().trim();
            double rate = rateText.isEmpty() ? 0 : parseNumber(rateText, "Interest rate");
            if (rate < 0 || rate > 100)
                throw new IllegalArgumentException("Interest rate must be between 0 and 100.");

            LocalDate deadline = LocalDate.parse(deadlineField.getText().trim());
            LocalDate today = LocalDate.now();
            if (!deadline.isAfter(today))
                throw new IllegalArgumentException("The deadline must be a future date.");

            Goal goal = (rate > 0)
                    ? new InterestGoal(name, target, saved, today, deadline, rate)
                    : new SavingsGoal(name, target, saved, today, deadline);
            manager.add(goal);
            fileManager.save(manager.getAll());
            nameField.setText("");
            targetField.setText("");
            savedField.setText("0");
            refresh();
            int last = manager.size() - 1;
            table.setRowSelectionInterval(last, last);
        } catch (DateTimeParseException e) {
            error("Enter the deadline as yyyy-MM-dd, for example 2027-03-31.");
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    private void deleteGoal() {
        int row = table.getSelectedRow();
        if (row < 0) { error("Select a goal first."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete the goal \"" + manager.get(row).getName() + "\"?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        manager.remove(row);
        fileManager.save(manager.getAll());
        refresh();
    }

    private void addDeposit() {
        int row = table.getSelectedRow();
        if (row < 0) { error("Select a goal first."); return; }
        try {
            double amount = parseNumber(depositField.getText(), "Deposit amount");
            if (amount <= 0) throw new IllegalArgumentException("Deposit must be greater than 0.");
            manager.get(row).addDeposit(new Deposit(LocalDate.now(), amount));
            fileManager.save(manager.getAll());
            depositField.setText("");
            refresh();
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    private void whatIf() {
        int row = table.getSelectedRow();
        if (row < 0) { error("Select a goal first."); return; }
        try {
            double monthly = parseNumber(whatIfField.getText(), "Monthly amount");
            if (monthly <= 0) throw new IllegalArgumentException("Monthly amount must be greater than 0.");
            Goal g = manager.get(row);
            if (g.remaining() <= 0) {
                whatIfResult.setText("This goal is already reached.");
                return;
            }
            int months = g.monthsToFinish(monthly);
            LocalDate finish = LocalDate.now().plusMonths(months);
            int left = g.monthsLeft();
            String text = "Saving " + money(monthly) + " a month, you finish in " + months
                    + " month(s), around " + finish + ". ";
            if (finish.isAfter(g.getDeadline()))
                text += "That is after your deadline (" + g.getDeadline() + "), so you need to save more.";
            else
                text += "That is within your deadline (" + left + " month(s) left).";
            whatIfResult.setText(text);
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    // ---------------------------------------------------------------- display
    /** Rebuilds the table from the manager and keeps the selected row. */
    private void refresh() {
        int sel = table.getSelectedRow();
        LocalDate today = LocalDate.now();
        model.setRowCount(0);
        for (Goal g : manager.getAll()) {
            model.addRow(new Object[]{
                    g.getName(), g.getType(), money(g.getTarget()), money(g.getSaved()),
                    String.format("%.1f%%", g.progressPercent()),
                    money(g.monthlyNeeded(today)), g.monthsLeft(today), g.status(today)
            });
        }
        if (sel >= 0 && sel < model.getRowCount()) table.setRowSelectionInterval(sel, sel);
        showDetails();
        summary.setText(String.format("Goals: %d   |   Total target: %s   |   Total saved: %s   |   Total needed per month: %s",
                manager.size(), money(manager.totalTarget()), money(manager.totalSaved()),
                money(manager.totalMonthlyNeeded(today))));
    }

    private void showDetails() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= manager.size()) {
            details.setText("Select a goal in the table to see its details.");
            bar.setValue(0);
            bar.setString("");
            return;
        }
        Goal g = manager.get(row);
        LocalDate today = LocalDate.now();
        String status = g.status(today);

        StringBuilder sb = new StringBuilder();
        sb.append(g.getName()).append("  (").append(g.getType());
        if (g.getRate() > 0) sb.append(", ").append(g.getRate()).append("% a year");
        sb.append(")\n");
        sb.append("Target:               ").append(money(g.getTarget())).append("\n");
        sb.append("Saved so far:         ").append(money(g.getSaved())).append("\n");
        sb.append("Still needed:         ").append(money(g.remaining())).append("\n");
        sb.append("Deadline:             ").append(g.getDeadline())
                .append("  (").append(g.monthsLeft(today)).append(" month(s) left)\n");
        sb.append("Monthly saving needed: ").append(money(g.monthlyNeeded(today))).append("\n");
        sb.append(String.format("Progress: %.1f%%   Time used: %.1f%%%n",
                g.progressPercent(), g.timeElapsedPercent(today)));
        sb.append("Deposits recorded:    ").append(g.getDeposits().size()).append("\n");
        sb.append(statusMessage(status));
        details.setText(sb.toString());
        details.setCaretPosition(0);

        bar.setValue((int) Math.round(g.progressPercent()));
        bar.setString(String.format("%.1f%% saved", g.progressPercent()));
        bar.setForeground(colourFor(status).darker());
    }

    private String statusMessage(String status) {
        switch (status) {
            case "REACHED":  return "Status: goal reached. Well done!";
            case "ON TRACK": return "Status: ON TRACK - saving at or ahead of the schedule.";
            case "BEHIND":   return "Status: BEHIND - a little less saved than the time used suggests.";
            default:         return "Status: AT RISK - far behind, or the deadline has passed.";
        }
    }

    private Color colourFor(String status) {
        switch (status) {
            case "REACHED":  return new Color(150, 200, 255);
            case "ON TRACK": return new Color(144, 238, 144);
            case "BEHIND":   return new Color(255, 235, 100);
            default:         return new Color(255, 130, 130);
        }
    }

    // ---------------------------------------------------------------- helpers
    private double parseNumber(String text, String fieldName) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a number.");
        }
    }

    private static String money(double v) { return String.format("Rs. %,.2f", v); }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input error", JOptionPane.ERROR_MESSAGE);
    }
}
