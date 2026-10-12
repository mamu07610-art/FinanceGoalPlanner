import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Saves and loads goals in goals.txt.
 * One goal per line: TYPE|name|target|initialSaved|start|deadline|rate|date:amount;date:amount
 */
public class FileManager {
    private static final String FILE = "goals.txt";

    public void save(ArrayList<Goal> goals) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (Goal g : goals) {
                StringBuilder dep = new StringBuilder();
                for (Deposit d : g.getDeposits()) {
                    if (dep.length() > 0) dep.append(';');
                    dep.append(d.getDate()).append(':').append(d.getAmount());
                }
                pw.println(g.getType() + "|" + g.getName() + "|" + g.getTarget() + "|"
                        + g.getInitialSaved() + "|" + g.getStartDate() + "|" + g.getDeadline()
                        + "|" + g.getRate() + "|" + dep);
            }
        } catch (IOException e) {
            System.out.println("Could not save goals: " + e.getMessage());
        }
    }

    /** Returns an empty list if the file is missing. Corrupted lines are skipped. */
    public ArrayList<Goal> load() {
        ArrayList<Goal> list = new ArrayList<>();
        File f = new File(FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    list.add(parseLine(line));
                } catch (Exception e) {
                    System.out.println("Skipped a corrupted line.");
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read goals: " + e.getMessage());
        }
        return list;
    }

    private Goal parseLine(String line) {
        String[] p = line.split("\\|", -1);
        String type = p[0];
        String name = p[1];
        double target = Double.parseDouble(p[2]);
        double saved = Double.parseDouble(p[3]);
        LocalDate start = LocalDate.parse(p[4]);
        LocalDate deadline = LocalDate.parse(p[5]);
        double rate = Double.parseDouble(p[6]);

        Goal g;
        if (type.equals("Interest")) g = new InterestGoal(name, target, saved, start, deadline, rate);
        else g = new SavingsGoal(name, target, saved, start, deadline);

        if (p.length > 7 && !p[7].isEmpty()) {
            for (String item : p[7].split(";")) {
                String[] d = item.split(":");
                g.addDeposit(new Deposit(LocalDate.parse(d[0]), Double.parseDouble(d[1])));
            }
        }
        return g;
    }
}
