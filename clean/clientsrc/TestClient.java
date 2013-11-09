package clientsrc;

import java.rmi.RemoteException;

import serversrc.resImpl.InvalidTransactionException;
import serversrc.resImpl.ServerShutdownException;
import serversrc.resImpl.TransactionAbortedException;



public class TestClient extends client implements Runnable{

	int transactionTime; //in milliseconds.
	int averageResponseTime; //in milliseconds.
	int lengthOfExperiment;
	int startTime;
	int stopTime;
	
	TestClient(int requestRate, int numberOfClients, int lengthOfExperiment){
		transactionTime = (int) (1000f*numberOfClients/requestRate);		
		this.lengthOfExperiment = lengthOfExperiment;
	}
	

	@Override
	public void run() {
		
		int flightNum, flightSeats, flightPrice;
		flightNum = 0;
		
		try{
			flightPrice = (int) (Math.random()*500);//random price between 0 and 500.
			flightSeats = (int) (Math.random()*250);//random number of seats to be added. 
			int tid = rm.start();
			
			flightTransaction(tid,flightNum,flightSeats,flightPrice);
			
			//Do operations here
			rm.commit(tid);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	
	
	void flightTransaction(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException, InvalidTransactionException, TransactionAbortedException, ServerShutdownException{
		rm.addFlight(id, flightNum, flightSeats, flightPrice);
		rm.queryFlight(id, flightNum);
		rm.deleteFlight(id, flightNum);
		rm.addFlight(id, flightNum, flightSeats, flightPrice);
		
	}
}
