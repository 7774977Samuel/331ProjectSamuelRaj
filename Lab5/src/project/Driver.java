package project;


import java.util.Random;

class FileLogger{
	
	final int MAX_BACKUP = 5;
	
}


public class Driver {
	
	public static final Random random = new Random();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// ask the user for max temp value
		// store it in a variabe
		System.out.println("the random temperature is "+generateTemperature(37.1));
		// temp done
		
		// create humidity variable and call generateHumidity 0-100
		
		// 3 sensor array
		double [] three_sensors = new double[3];
		for (int i=0; i<3 ; i++) {
			three_sensors[i] = generateThirdSensor();
			System.out.println("Sensor ID: "+ i +" Reads: "+ three_sensors[i]);
		}
	}
	
	
	public static double MajorityVoter(double [] three_sensors) {
		
		final double epsilon = 0.0001;
		// compare all three inputs 
		// store boolean flags for each comparison
		
		// if 2 flags are true, then return one of them
		// if flag 01 is true, log the faulty reading with its index (eg: 2 is faulty)
		// else give the timestamp data
	}
	
	private static double generateThirdSensor() {
		// TODO Auto-generated method stub
		return random.nextDouble() * 100;
	}

	public static double generateTemperature(double maxTemp) {
		return random.nextDouble() * maxTemp;
	}

}