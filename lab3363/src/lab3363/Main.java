package lab3363;

import java.util.*;

public class Main {
    private static final Random random = new Random();
    private static double lastValidSensor3 = 0.0;

    public static void main(String[] args) {
        String logFile = "log.txt";

        for (int i = 1; i <= 10; i++) {
            System.out.println("Cycle " + i);

            double temp = generateTemperature();
            double humidity = generateHumidity();
            double s3_1 = generateSensor3Reading();
            double s3_2 = generateSensor3Reading();
            double s3_3 = generateSensor3Reading();

            System.out.printf("Sensor 1 (Temp): %.2f°C\n", temp);
            System.out.printf("Sensor 2 (Humidity): %.2f%%\n", humidity);
            System.out.printf("Sensor 3.1: %.2f, Sensor 3.2: %.2f, Sensor 3.3: %.2f\n", s3_1, s3_2, s3_3);

            Double majority = majorityVote(s3_1, s3_2, s3_3);

            if (majority != null) {
                System.out.printf("Sensor 3 (Majority Value): %.2f\n", majority);
                lastValidSensor3 = majority;
            } else {
                System.out.printf("No majority found. Using last valid Sensor 3 reading: %.2f\n", lastValidSensor3);
                FileLogger.log(logFile, "Discrepancy in Sensor 3.1-3.3: No majority. Outliers: ALL");
            }

            System.out.println("--------------------------------------------------");
        }
    }

    private static double generateTemperature() {
        return 15 + random.nextDouble() * 10; // 15°C to 25°C
    }

    private static double generateHumidity() {
        return 30 + random.nextDouble() * 40; // 30% to 70%
    }

    private static double generateSensor3Reading() {
        return 100 + random.nextInt(5); // 100 to 104 (simulate closely grouped values)
    }

    private static Double majorityVote(double a, double b, double c) {
        if (equals(a, b)) return a;
        if (equals(a, c)) return a;
        if (equals(b, c)) return b;

        // No majority
        return null;
    }

    private static boolean equals(double x, double y) {
        return Math.abs(x - y) < 0.01; // tolerance for floating point rounding
    }
}
