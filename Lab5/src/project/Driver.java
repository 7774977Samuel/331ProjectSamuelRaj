package project;

import java.util.Random;
import java.util.Scanner;

public class Driver {

    private static final Random random = new Random();
    private static double lastValidReading = 0.0;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.print("enter max temperature: ");
        double maxTemp = input.nextDouble();

        double temp = getTemperature(maxTemp);
        double humidity = getHumidity();

        System.out.printf("Temp: %.2f°C\n", temp);
        System.out.printf("humidity: %.2f%%\n", humidity);

        double[] sensor3Readings = new double[3];
        for (int i = 0; i < 3; i++) {
            sensor3Readings[i] = getSensor3Reading();
            System.out.printf("Sensor 3.%d: %.2f\n", i + 1, sensor3Readings[i]);
        }

        Double result = checkMajority(sensor3Readings);

        if (result != null) {
            System.out.printf("Sensor 3 (Majority): %.2f\n", result);
            lastValidReading = result;
        } else {
            System.out.printf("No majority — using previous value: %.2f\n", lastValidReading);
            String logMessage = "Disagreement between Sensor 3 replicas: "
                    + String.format("3.1=%.2f, 3.2=%.2f, 3.3=%.2f", 
                      sensor3Readings[0], sensor3Readings[1], sensor3Readings[2]);
            FileLogger.log("log.txt", logMessage);
        }

        input.close();
    }

    private static double getTemperature(double max) {
        return random.nextDouble() * max;
    }

    private static double getHumidity() {
        return 30 + random.nextDouble() * 70;
    }

    private static double getSensor3Reading() {
        return 100 + random.nextInt(5);
    }

    private static Double checkMajority(double[] vals) {
        double a = vals[0], b = vals[1], c = vals[2];
        double epsilon = 0.01;

        if (Math.abs(a - b) < epsilon) return a;
        if (Math.abs(a - c) < epsilon) return a;
        if (Math.abs(b - c) < epsilon) return b;

        return null; // no match
    }
}
