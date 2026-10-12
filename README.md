# Personal Finance Goal Planner

A Java Swing application that helps a user decide how much to save each month.
Enter a goal, a target amount and a deadline, and the app calculates the monthly
saving needed, records deposits and tracks progress.

## Problem
People want to save for something (a laptop, a trip) but do not know how much to set aside each month.

## Features
- Add and delete savings goals with a target amount and deadline
- Monthly saving needed, calculated automatically
- Optional interest rate (compounded monthly) for goals kept in a savings account
- Record deposits and track progress as a percentage
- Colour-coded status: Reached, On Track, Behind, At Risk
- What-if projector: "if I save X a month, when will I finish?"
- Summary of total target, total saved and total monthly saving needed
- Data saved in goals.txt

## Java Concepts Used
Abstract class (Goal), inheritance (SavingsGoal, InterestGoal), polymorphism,
encapsulation, ArrayList, java.time (LocalDate, ChronoUnit), file handling,
exception handling, Swing (JTable, JProgressBar).

## Formulas
- Monthly needed (no interest) = (Target - Saved) / months left
- Monthly needed (with interest) = (T - S x (1+i)^n) x i / ((1+i)^n - 1), where i = annual rate / 12
- Progress % = Saved / Target x 100
- Months to finish at m a month = ceil((T - S) / m)

## How to Run
1. Install JDK 17 or newer.
2. Clone: `git clone https://github.com/ihsanmhd344-hash/FinanceGoalPlanner.git`
3. From the project root (the folder that contains `src`):
   ```
   javac -d out src/*.java
   java -cp out Main
   ```

## Note
All amounts in the screenshots are sample data. This is a planning aid, not financial advice.

## Screenshots
See the `screenshots` folder.
