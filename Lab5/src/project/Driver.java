package project;

import java.util.Random;
import java.util.Scanner;

/**
 * driver class simulates a fault-tolerant embedded system
 * with three sensors and resilient logging for discrepancies 
 */
public class Driver {

    private static final Random random = new Random();
    private static double lastValidReading = 50.0; // initial fallback value

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Read temperature limit from user
        System.out.print("Enter max temperature: ");
        double maxTemp = input.nextDouble();

        double temp = getTemperature(maxTemp);
        double humidity = getHumidity();

        System.out.printf("\nTemperature: %.2f°C\n", temp);
        System.out.printf("Humidity: %.2f%%\n", humidity);

        // Read Sensor 3 values
        double[] sensor3Readings = new double[3];
        for (int i = 0; i < 3; i++) {
            sensor3Readings[i] = getSensor3Reading();
            System.out.printf("Sensor 3.%d reading: %.2f\n", i + 1, sensor3Readings[i]);
        }

        // Apply majority voter logic
        Double result = checkMajority(sensor3Readings);

        if (result != null) {
            System.out.printf("Sensor 3 (Majority Vote): %.2f\n", result);
            lastValidReading = result;
        } else {
            System.out.printf("No majority — using last valid value: %.2f\n", lastValidReading);
            String logMessage = "Disagreement between Sensor 3 replicas: "
                    + String.format("3.1=%.2f, 3.2=%.2f, 3.3=%.2f",
                    sensor3Readings[0], sensor3Readings[1], sensor3Readings[2]);
            FileLogger.log("log.txt", logMessage);
        }
    }

    // Generates a temperature reading
    public static double getTemperature(double maxTemp) {
        return random.nextDouble() * maxTemp;
    }

    // Generates a humidity reading (0 to 100%)
    public static double getHumidity() {
        return random.nextDouble() * 100.0;
    }

    // Generates a value for a Sensor 3 
    public static double getSensor3Reading() {
        return 45 + random.nextDouble() * 10; // range: 45 to 55
    }

    /*
     * Checks for a majority among three sensor readings.
     * If no majority exists, returns null.
     */
    public static Double checkMajority(double[] values) {
        final double EPSILON = 0.05;

        boolean v01 = Math.abs(values[0] - values[1]) < EPSILON;
        boolean v02 = Math.abs(values[0] - values[2]) < EPSILON;
        boolean v12 = Math.abs(values[1] - values[2]) < EPSILON;

        if (v01 && v02) return values[0]; // all three match
        if (v01) return values[0];        // 0 and 1 match
        if (v02) return values[0];        // 0 and 2 match
        if (v12) return values[1];        // 1 and 2 match

        return null; // all three different
    }
}
