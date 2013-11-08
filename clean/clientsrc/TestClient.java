package clientsrc;

import java.rmi.RemoteException;

import serversrc.resImpl.InvalidTransactionException;
import serversrc.resImpl.ServerShutdownException;
import serversrc.resImpl.TransactionAbortedException;



public class TestClient extends client implements Runnable{

	int transactionTime; //time in between transactions (milliseconds)
	int averageResponseTime; //in milliseconds.
	int transactionsCompleted;
	
	TestClient(double requestRate, double numberOfClients){
		transactionTime = (int) (1000*numberOfClients/requestRate);		
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
	
	
	void flightTransaction(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException, InvalidTransactionException, TransactionAbortedException{
		
		rm.addFlight(id, flightNum, flightSeats, flightPrice);
		rm.queryFlight(id, flightNum);
		rm.deleteFlight(id, flightNum);
		rm.addFlight(id, flightNum, flightSeats, flightPrice);
		rm.addFlight(id, flightNum+(int)(5*Math.random()), flightSeats-(int)(10*Math.random()), flightPrice);
		
	}
	
	void transactionSet(int id) throws RemoteException, InvalidTransactionException, TransactionAbortedException{
		rm.addCars(id, String.valueOf(Math.random()), (int)(Math.random()*500), (int)(Math.random()*40));
		
	}
}
