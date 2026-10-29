import java.util.Scanner;

/*
 * Cricket NRR & DLS Predictor
 *
 * Formulas used:
 *   1) Overs lost      = minutes lost / minutes per over   (rounded down)
 *   2) New overs       = original overs - overs lost
 *   3) Resource lost   = R(at stoppage) - R(at restart)
 *   4) Team 2 resource = 100 - resource lost
 *   5) Par score       = Team 1 score x (Team 2 resource / Team 1 resource)
 *   6) Target          = par score (rounded down) + 1
 *   7) NRR             = (runs scored / overs faced) - (runs conceded / overs bowled)
 */
public class dls_system{

    static final double ODI_MIN_PER_OVER = 4.2;   // 60 / 14.28
    static final double T20_MIN_PER_OVER = 4.25;  // 60 / 14.11
    static final int ODI_MIN_OVERS = 20;
    static final int T20_MIN_OVERS = 5;
    static final double G50 = 245.0;

    // ODI resource table: ODI_TABLE[50 - oversLeft][wicketsLost]
    static final double[][] ODI_TABLE = {
        {100.0, 93.4, 85.1, 74.9, 62.7, 49.0, 34.9, 22.0, 11.9, 4.7},  // 50
        { 99.1, 92.6, 84.5, 74.4, 62.5, 48.9, 34.9, 22.0, 11.9, 4.7},  // 49
        { 98.1, 91.7, 83.8, 74.0, 62.2, 48.8, 34.9, 22.0, 11.9, 4.7},  // 48
        { 97.1, 90.9, 83.2, 73.5, 61.9, 48.6, 34.9, 22.0, 11.9, 4.7},  // 47
        { 96.1, 90.0, 82.5, 73.0, 61.6, 48.5, 34.9, 22.0, 11.9, 4.7},  // 46
        { 95.0, 89.1, 81.8, 72.5, 61.3, 48.4, 34.8, 22.0, 11.9, 4.7},  // 45
        { 93.9, 88.2, 81.0, 72.0, 61.0, 48.3, 34.8, 22.0, 11.9, 4.7},  // 44
        { 92.8, 87.3, 80.3, 71.4, 60.7, 48.1, 34.7, 22.0, 11.9, 4.7},  // 43
        { 91.7, 86.3, 79.5, 70.9, 60.3, 47.9, 34.7, 22.0, 11.9, 4.7},  // 42
        { 90.5, 85.3, 78.7, 70.3, 59.9, 47.8, 34.6, 22.0, 11.9, 4.7},  // 41
        { 89.3, 84.2, 77.8, 69.6, 59.5, 47.6, 34.6, 22.0, 11.9, 4.7},  // 40
        { 88.0, 83.1, 76.9, 69.0, 59.1, 47.4, 34.5, 22.0, 11.9, 4.7},  // 39
        { 86.7, 82.0, 76.0, 68.3, 58.7, 47.1, 34.5, 21.9, 11.9, 4.7},  // 38
        { 85.4, 80.9, 75.0, 67.6, 58.2, 46.9, 34.4, 21.9, 11.9, 4.7},  // 37
        { 84.1, 79.7, 74.1, 66.8, 57.7, 46.6, 34.3, 21.9, 11.9, 4.7},  // 36
        { 82.7, 78.5, 73.0, 66.0, 57.2, 46.4, 34.2, 21.9, 11.9, 4.7},  // 35
        { 81.3, 77.2, 72.0, 65.2, 56.6, 46.1, 34.1, 21.9, 11.9, 4.7},  // 34
        { 79.8, 75.9, 70.9, 64.4, 56.0, 45.8, 34.0, 21.9, 11.9, 4.7},  // 33
        { 78.3, 74.6, 69.7, 63.5, 55.4, 45.4, 33.9, 21.9, 11.9, 4.7},  // 32
        { 76.7, 73.2, 68.6, 62.5, 54.8, 45.1, 33.7, 21.9, 11.9, 4.7},  // 31
        { 75.1, 71.8, 67.3, 61.6, 54.1, 44.7, 33.6, 21.8, 11.9, 4.7},  // 30
        { 73.5, 70.3, 66.1, 60.5, 53.4, 44.2, 33.4, 21.8, 11.9, 4.7},  // 29
        { 71.8, 68.8, 64.8, 59.5, 52.6, 43.8, 33.2, 21.8, 11.9, 4.7},  // 28
        { 70.1, 67.2, 63.4, 58.4, 51.8, 43.3, 33.0, 21.7, 11.9, 4.7},  // 27
        { 68.3, 65.6, 62.0, 57.2, 50.9, 42.8, 32.8, 21.7, 11.9, 4.7},  // 26
        { 66.5, 63.9, 60.5, 56.0, 50.0, 42.2, 32.6, 21.6, 11.9, 4.7},  // 25
        { 64.6, 62.2, 59.0, 54.7, 49.0, 41.6, 32.3, 21.6, 11.9, 4.7},  // 24
        { 62.7, 60.4, 57.4, 53.4, 48.0, 40.9, 32.0, 21.5, 11.9, 4.7},  // 23
        { 60.7, 58.6, 55.8, 52.0, 47.0, 40.2, 31.6, 21.4, 11.9, 4.7},  // 22
        { 58.7, 56.7, 54.1, 50.6, 45.8, 39.4, 31.2, 21.3, 11.9, 4.7},  // 21
        { 56.6, 54.8, 52.4, 49.1, 44.6, 38.6, 30.8, 21.2, 11.9, 4.7},  // 20
        { 54.4, 52.8, 50.5, 47.5, 43.4, 37.7, 30.3, 21.1, 11.9, 4.7},  // 19
        { 52.2, 50.7, 48.6, 45.9, 42.0, 36.8, 29.8, 20.9, 11.9, 4.7},  // 18
        { 49.9, 48.5, 46.7, 44.1, 40.6, 35.8, 29.2, 20.7, 11.9, 4.7},  // 17
        { 47.6, 46.3, 44.7, 42.3, 39.1, 34.7, 28.5, 20.5, 11.8, 4.7},  // 16
        { 45.2, 44.1, 42.6, 40.5, 37.6, 33.5, 27.8, 20.2, 11.8, 4.7},  // 15
        { 42.7, 41.7, 40.4, 38.5, 35.9, 32.2, 27.0, 19.9, 11.8, 4.7},  // 14
        { 40.2, 39.3, 38.1, 36.5, 34.2, 30.8, 26.1, 19.5, 11.7, 4.7},  // 13
        { 37.6, 36.8, 35.8, 34.3, 32.3, 29.4, 25.1, 19.0, 11.6, 4.7},  // 12
        { 34.9, 34.2, 33.4, 32.1, 30.4, 27.8, 24.0, 18.5, 11.5, 4.7},  // 11
        { 32.1, 31.6, 30.8, 29.8, 28.3, 26.1, 22.8, 17.9, 11.4, 4.7},  // 10
        { 29.3, 28.9, 28.2, 27.4, 26.1, 24.2, 21.4, 17.1, 11.2, 4.7},  // 9
        { 26.4, 26.0, 25.5, 24.8, 23.8, 22.3, 19.9, 16.2, 10.9, 4.7},  // 8
        { 23.4, 23.1, 22.7, 22.2, 21.4, 20.1, 18.2, 15.2, 10.5, 4.7},  // 7
        { 20.3, 20.1, 19.8, 19.4, 18.8, 17.8, 16.4, 13.9, 10.1, 4.6},  // 6
        { 17.2, 17.0, 16.8, 16.5, 16.1, 15.4, 14.3, 12.5,  9.4, 4.6},  // 5
        { 13.9, 13.8, 13.7, 13.5, 13.2, 12.7, 12.0, 10.7,  8.4, 4.5},  // 4
        { 10.6, 10.5, 10.4, 10.3, 10.2,  9.9,  9.5,  8.7,  7.2, 4.2},  // 3
        {  7.2,  7.1,  7.1,  7.0,  7.0,  6.8,  6.6,  6.2,  5.5, 3.7},  // 2
        {  3.6,  3.6,  3.6,  3.6,  3.6,  3.5,  3.5,  3.4,  3.2, 2.5},  // 1
        {  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, 0.0}   // 0
    };

    // T20 resource table: T20_TABLE[20 - oversLeft][wicketsLost]
    static final double[][] T20_TABLE = {
        {100.0, 96.9, 93.0, 87.9, 81.3, 72.2, 59.9, 44.8, 29.7, 17.6},  // 20
        { 95.6, 90.9, 87.7, 83.0, 76.9, 68.3, 56.5, 42.0, 27.2, 15.3},  // 19
        { 91.7, 86.7, 82.9, 78.7, 73.2, 65.4, 54.2, 40.2, 25.7, 13.9},  // 18
        { 87.7, 82.3, 78.9, 73.8, 69.7, 62.8, 52.2, 38.7, 24.6, 12.8},  // 17
        { 83.5, 78.2, 75.3, 70.5, 66.4, 60.2, 50.3, 37.4, 23.5, 12.0},  // 16
        { 79.2, 74.3, 70.9, 66.9, 62.6, 57.4, 48.4, 36.2, 22.7, 11.2},  // 15
        { 75.1, 70.7, 67.3, 63.7, 59.3, 54.6, 46.4, 35.0, 21.8, 10.5},  // 14
        { 71.5, 67.4, 63.6, 60.3, 56.2, 51.5, 44.3, 33.8, 21.0,  9.8},  // 13
        { 68.3, 63.7, 60.2, 56.8, 52.9, 47.5, 41.9, 32.6, 20.2,  9.1},  // 12
        { 65.0, 59.9, 56.6, 53.3, 49.7, 43.9, 39.3, 31.3, 19.4,  8.5},  // 11
        { 61.3, 56.0, 52.6, 50.1, 46.0, 40.8, 36.1, 30.0, 18.6,  7.9},  // 10
        { 57.9, 52.3, 47.9, 46.1, 42.5, 37.8, 33.1, 28.3, 17.7,  7.2},  // 9
        { 54.0, 48.3, 44.3, 41.7, 38.9, 34.9, 30.2, 26.1, 16.7,  6.6},  // 8
        { 49.3, 44.2, 40.2, 37.4, 35.4, 32.1, 27.2, 23.4, 15.7,  5.9},  // 7
        { 41.7, 38.5, 35.7, 33.0, 31.7, 29.0, 24.2, 20.0, 14.5,  5.2},  // 6
        { 36.2, 33.4, 31.0, 28.6, 27.3, 25.5, 21.5, 17.0, 12.2,  4.4},  // 5
        { 30.8, 28.0, 26.1, 24.1, 22.4, 20.7, 18.3, 14.2, 10.0,  3.5},  // 4
        { 25.4, 22.8, 21.1, 19.4, 17.7, 16.5, 14.4, 11.6,  7.9,  2.5},  // 3
        { 19.7, 17.2, 15.5, 14.1, 12.7, 11.9, 10.6,  9.3,  6.2,  1.6},  // 2
        { 13.7, 11.3,  9.7,  8.5,  7.3,  6.7,  6.0,  5.2,  4.2,  0.9}   // 1
    };

    static Scanner sc = new Scanner(System.in);

    // Resource % for a given number of overs left and wickets lost
    static double resource(int matchOvers, int oversLeft, int wicketsLost) {
        if (oversLeft <= 0 || wicketsLost >= 10) {
            return 0.0;
        }
        if (matchOvers == 50) {
            return ODI_TABLE[50 - oversLeft][wicketsLost];
        }
        return T20_TABLE[20 - oversLeft][wicketsLost];
    }

    // Par score for the full innings
    static double parScore(double s, double r1, double r2) {
        if (r2 < r1) {
            return s * r2 / r1;
        } else if (r2 == r1) {
            return s;
        } else {
            return s + G50 * (r2 - r1) / 100.0;
        }
    }

   
    static int readInt(String label, int min, int max) {
        while (true) {
            System.out.print(label + " [" + min + " to " + max + "]: ");
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= min && v <= max) {
                    return v;
                }
            } catch (NumberFormatException e) {
                // fall through to the message below
            }
            System.out.println("  Invalid value. Please enter a number from " + min + " to " + max + ".");
        }
    }

    
    static boolean readYesNo(String label) {
        while (true) {
            System.out.print(label + " [y/n]: ");
            String line = sc.nextLine().trim().toLowerCase();
            if (line.equals("y")) return true;
            if (line.equals("n")) return false;
            System.out.println("  Invalid value. Please type y or n.");
        }
    }

    
    static int readMatchType() {
        while (true) {
            System.out.print("Match type [20 or 50 overs]: ");
            String line = sc.nextLine().trim();
            if (line.equals("20")) return 20;
            if (line.equals("50")) return 50;
            System.out.println("  Invalid value. Please enter 20 or 50.");
        }
    }

    

    // over reduction. Returns the new number of overs for Team 2.
    static int newOvers(int matchOvers, int minutesLost, double minPerOver) {
        int oversLost = (int) Math.floor(minutesLost / minPerOver + 1e-9);
        System.out.println();
        System.out.println("--- Over reduction ---");
        System.out.printf("Overs lost       : %d / %.2f = %d%n", minutesLost, minPerOver, oversLost);
        System.out.println("Team 2 new overs : " + matchOvers + " - " + oversLost + " = " + (matchOvers - oversLost));
        return matchOvers - oversLost;
    }

    // new target after play restarts
    static void showNewTarget(int matchOvers, int s, int score2, int w2, int o2, int newOvers) {
        double rStop = resource(matchOvers, matchOvers - o2, w2);     // before rain
        double rRestart = resource(matchOvers, newOvers - o2, w2);    // after restart
        double resourceLost = rStop - rRestart;
        double r1 = 100.0;
        double r2 = 100.0 - resourceLost;

        double par = parScore(s, r1, r2);
        int target = (int) Math.floor(par + 1e-9) + 1;
        int needed = target - score2;
        int oversLeft = newOvers - o2;

        System.out.println();
        System.out.println("--- DLS ---");
        System.out.printf("Resource at stoppage : %.1f%%%n", rStop);
        System.out.printf("Resource at restart  : %.1f%%%n", rRestart);
        System.out.printf("Resource lost        : %.1f%%%n", resourceLost);
        System.out.printf("Team 1 resource (R1) : %.1f%%%n", r1);
        System.out.printf("Team 2 resource (R2) : %.1f%%%n", r2);
        System.out.printf("Par score            : %.2f%n", par);

        System.out.println();
        System.out.println("*****************************************");
        System.out.println(" NEW TARGET : " + target + " runs in " + newOvers + " overs");
        if (needed > 0) {
            System.out.println(" Team 2 need " + needed + " more runs in " + oversLeft + " overs");
            System.out.printf(" Required run rate : %.2f%n", (double) needed / oversLeft);
        } else {
            System.out.println(" Team 2 has already reached the target.");
        }
        System.out.println("*****************************************");
    }

    //  result when play does NOT restart (par score at the stoppage)
    static void showResult(int matchOvers, int s, int score2, int w2, int o2, int minOvers) {
        if (o2 < minOvers) {
            System.out.println("Team 2 played only " + o2 + " overs (minimum " + minOvers + ").");
            System.out.println("RESULT: NO RESULT");
            return;
        }

        double rStop = resource(matchOvers, matchOvers - o2, w2);
        double resourceUsed = 100.0 - rStop;
        double par = s * resourceUsed / 100.0;
        int parInt = (int) Math.floor(par + 1e-9);

        System.out.println("--- Par score ---");
        System.out.printf("Resource used : 100 - %.1f = %.1f%%%n", rStop, resourceUsed);
        System.out.printf("Par score     : %d x %.1f / 100 = %.2f (%d)%n", s, resourceUsed, par, parInt);
        System.out.println("Team 2 score  : " + score2);

        System.out.println();
        System.out.println("*****************************************");
        if (score2 > parInt) {
            System.out.println(" RESULT: Team 2 WINS (DLS)");
        } else if (score2 == parInt) {
            System.out.println(" RESULT: MATCH TIED");
        } else {
            System.out.println(" RESULT: Team 1 WINS by " + (parInt - score2) + " runs (DLS)");
        }
        System.out.println("*****************************************");
    }

    // Main flow 

    static void runDLS() {
        System.out.println();
        System.out.println("===== DLS PREDICTOR =====");

        int matchOvers = readMatchType();
        double minPerOver = (matchOvers == 50) ? ODI_MIN_PER_OVER : T20_MIN_PER_OVER;
        int minOvers = (matchOvers == 50) ? ODI_MIN_OVERS : T20_MIN_OVERS;

        System.out.println();
        System.out.println("--- Team 1 (first innings) ---");
        int s = readInt("Score", 0, 600);
        readInt("Wickets lost", 0, 10);
        readInt("Overs played", 1, matchOvers);

        System.out.println();
        System.out.println("--- Team 2 (second innings) ---");
        int score2 = readInt("Score when rain stopped play", 0, 600);
        int w2 = readInt("Wickets lost", 0, 9);
        int o2 = readInt("Overs played when rain stopped play", 0, matchOvers - 1);

        System.out.println();
        if (readYesNo("Did play start again?")) {
            // Longest rain break that still leaves Team 2 with enough overs
            int maxOversLost = matchOvers - Math.max(minOvers, o2 + 1);
            int maxMinutes = (int) Math.ceil((maxOversLost + 1) * minPerOver) - 1;

            int minutes = readInt("Minutes of play lost due to rain", 0, maxMinutes);
            int overs = newOvers(matchOvers, minutes, minPerOver);
            showNewTarget(matchOvers, s, score2, w2, o2, overs);
        } else {
            showResult(matchOvers, s, score2, w2, o2, minOvers);
        }
    }

    // Net Run Rate

    // Asks for one team's runs and overs, returns {runs, overs as decimal}
    static double[] readInnings(String team, int quota) {
        System.out.println();
        System.out.println("--- " + team + " ---");
        int runs = readInt("Runs scored", 0, 600);
        boolean allOut = readYesNo("Was the team all out?");

        double overs;
        if (allOut) {
            overs = quota;                       // all out: full quota of overs is counted
            System.out.println("  Overs counted: " + quota + " (full quota)");
        } else {
            int whole = readInt("Overs faced", 1, quota);
            int balls = (whole == quota) ? 0 : readInt("Extra balls in the last over", 0, 5);
            overs = whole + balls / 6.0;         // 18 overs 3 balls = 18.5
        }
        return new double[] { runs, overs };
    }

    static void runNRR() {
        System.out.println();
        System.out.println("===== NET RUN RATE =====");
        int quota = readMatchType();

        double[] a = readInnings("Your team", quota);
        double[] b = readInnings("Opposition", quota);

        double rateFor = a[0] / a[1];
        double rateAgainst = b[0] / b[1];
        double nrr = rateFor - rateAgainst;

        System.out.println();
        System.out.printf("Run rate scored   : %.0f / %.2f = %.3f%n", a[0], a[1], rateFor);
        System.out.printf("Run rate conceded : %.0f / %.2f = %.3f%n", b[0], b[1], rateAgainst);
        System.out.println();
        System.out.println("*****************************************");
        System.out.printf(" NET RUN RATE : %.3f - %.3f = %+.3f%n", rateFor, rateAgainst, nrr);
        System.out.println("*****************************************");
    }

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("  Cricket NRR & DLS Predictor");
        System.out.println("=====================================");

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("1) DLS & Over Reduction   2) Net Run Rate   3) Exit");
            int choice = readInt("Choose an option", 1, 3);
            if (choice == 1) {
                runDLS();
            } else if (choice == 2) {
                runNRR();
            } else {
                running = false;
            }
        }
        System.out.println("Goodbye!");
    }
}