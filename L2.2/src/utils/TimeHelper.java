package utils;

public class TimeHelper {
	public static void sleep(long period){
		if (period <= 0) {
			return;
		}
		try{
			Thread.sleep(period);
		} catch (InterruptedException e) {				
			e.printStackTrace();
		}
	}
	
	public static void sleep(){
		try{
			Thread.sleep(5000);
		} catch (InterruptedException e) {				
			e.printStackTrace();
		}
	}
}
