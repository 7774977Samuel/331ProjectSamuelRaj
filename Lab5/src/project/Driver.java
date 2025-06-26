package project;

import java.util.Random;
import java.util.Scanner;

/**
 * driver class simulates a fault-tolerant embedded system
 * with three sensors and resilient logging for discrepancies 
 */
public class Driver {

    private static final Random random =new Random();// random number generator
    private static double lastValidReading= 50.0; // fallback value for sensor 3
    public static void main(String[] args) {
        try (Scanner input = new Scanner(System.in)) {
			// Read temperature limit from user
			System.out.print("Enter max temp: ");
			double max_temp =input.nextDouble();
			// generate temperature and humidity readings
			double temp= getTemperature(max_temp);
			double humidity = getHumidity();
			// display temperature and humidity
			System.out.printf("\nTemp: %.2f°C\n", temp);
			System.out.printf("Humidity: %.2f%%\n",humidity);
		}

        // read Sensor 3 values
        double[] sensor3_readings = new double[3];
        for (int i = 0; i < 3; i++) {
            sensor3_readings[i] = get_sensor3_reading();
            System.out.printf("Sensor 3.%d reading: %.2f\n", i + 1,sensor3_readings[i]);
        }

        // use majority value if available, else log discrepancy
        Double result = checkMajority(sensor3_readings);

        if (result != null) {
            System.out.printf("Sensor 3 (Majority Vote): %.2f\n", result);
            lastValidReading = result;
        } else {
            System.out.printf("No majority — using last valid value: %.2f\n", lastValidReading);
            String logmsg = "Disagreement between Sensor 3 replicas: "
                    + String.format("3.1=%.2f, 3.2=%.2f, 3.3=%.2f",
                    sensor3_readings[0], sensor3_readings[1], sensor3_readings[2]);
            FileLogger.log("log.txt", logmsg);
        }
    }

    private static double get_sensor3_reading() {
    	return 45 + random.nextDouble() * 10;
	
	}

 // generates a temperature reading based on max input
    public static double getTemperature(double maxTemp){
        return random.nextDouble() *maxTemp;
    }

    // generates a humidity reading between 0 and 100 percent
    public static double getHumidity(){
        return random.nextDouble()* 100.0;
    }

  // generates a sensor 3 reading between 45 and 55
    public static double getSensor3_reading() {
        return 45 +random.nextDouble() * 10; // range: 45 to 55
    }
    /*
     * Checks for a majority among three sensor readings.
     * If no majority exists, returns null.
     */
    public static Double checkMajority(double[] values){
        final double EPSILON =0.05;

        boolean v01 =Math.abs(values[0] -values[1])< EPSILON;
        boolean v02= Math.abs(values[0]- values[2]) < EPSILON;
        boolean v12 = Math.abs(values[1] -values[2]) < EPSILON;
        if (v01&& v02) return values[0]; 
        if (v01) return values[0];        
        if(v02) return values[0];        
        if (v12) return values[1];        

        return null; // all three different
    }
}
