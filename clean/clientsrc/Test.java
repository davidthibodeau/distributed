package clientsrc;

import javax.swing.plaf.SliderUI;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		int numOfClients = Integer.parseInt(args[0]);
		int ratePerSecond = Integer.parseInt(args[1]);
		int experimentLength = Integer.parseInt(args[2]);
		int averageDelay = 0;
		
		
		
		TestClient clients[] = new TestClient[numOfClients];
		Thread threads[] = new Thread[numOfClients];
		
		for (int i = 0; i < numOfClients; ++i){
			clients[i] = new TestClient(ratePerSecond, numOfClients, experimentLength);
			threads[i] = new Thread(clients[i]);
			
		}
		
		for (int i =0; i < numOfClients; ++i){
			threads[i].start();
		}
		
		try {
			Thread.sleep(experimentLength);
		} catch (InterruptedException e) {
			// won't happen
			e.printStackTrace();
		}
		
		for(int i = 0; i <numOfClients; ++i){
			averageDelay = (int) (1f/i * clients[i].averageResponseTime + (double)(i-1f)/i * averageDelay); 
		}
		
		for(int i = 0; i < numOfClients; ++i){
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("Experiment summary: \n Number of Clients: " + numOfClients  );
		System.out.println("Rate of requests: " + ratePerSecond);
		System.out.println("Experiment Length: " + experimentLength/1000);
		System.out.println("Average Delay: " + averageDelay);
	}

}
