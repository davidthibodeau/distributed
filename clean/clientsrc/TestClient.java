package clientsrc;

import java.rmi.RemoteException;
import java.util.Random;

import serversrc.resImpl.InvalidTransactionException;
import serversrc.resImpl.TransactionAbortedException;

public class TestClient extends client implements Runnable {

	int transactionTime; // time in between transactions (milliseconds)
	int averageResponseTime; // in milliseconds.
	int transactionsCompleted;

	TestClient(double requestRate, double numberOfClients) {
		transactionTime = (int) (1000 * numberOfClients / requestRate);
	}

	@Override
	public void run() {

		int flightNum, flightSeats, flightPrice;
		flightNum = 0;
		try {
			Random r = new Random();
			switch (r.nextInt(2)) {
			// Do operations here

			case (0):
				flightPrice = (int) (Math.random() * 500);// random price
															// between 0 and
															// 500.
				flightSeats = (int) (Math.random() * 250);// random number of
															// seats to be
															// added.

				int tid = rm.start();
				// Do operations here
				flightTransaction(tid, flightNum, flightSeats, flightPrice);
				rm.commit(tid);
				break;
			case (1):

				break;

			default:
				break;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void flightTransaction(int id, int flightNum, int flightSeats,
			int flightPrice) throws RemoteException,
			InvalidTransactionException, TransactionAbortedException {

		rm.addFlight(id, flightNum, flightSeats, flightPrice);
		rm.queryFlight(id, flightNum);
		rm.deleteFlight(id, flightNum);
		rm.addFlight(id, flightNum, flightSeats, flightPrice);
		rm.addFlight(id, flightNum + (int) (5 * Math.random()), flightSeats
				- (int) (10 * Math.random()), flightPrice);

	}

	void transactionSet(int id) throws RemoteException,
			InvalidTransactionException, TransactionAbortedException {
		rm.addCars(id, String.valueOf(Math.random()),
				(int) (Math.random() * 500), (int) (Math.random() * 40));

	}
}
