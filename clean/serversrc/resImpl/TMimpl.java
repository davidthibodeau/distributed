package serversrc.resImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import LockManager.LockManager;
import serversrc.resInterface.*;

public class TMimpl implements TransactionManager {

	private RMCar rmCar;
	private RMFlight rmFlight;
	private RMHotel rmHotel;
	private RMCustomer rmCustomer;
	private RMHashtable transactionHT;
	// lock is passed to the TM so that the TimeToLive can
	// unlock when aborting an idle transaction
	private LockManager lock;
	private final String folder = "tmmanager/";

	private Crash crashType;

	
	public void updateRMCar(RMCar rm){
		if(rm != null)
			rmCar = rm;
	}

	public void updateRMFlight(RMFlight rm){
		if(rm != null)
			rmFlight = rm;
	}

	public void updateRMHotel(RMHotel rm){
		if(rm != null)
			rmHotel = rm;
	}

	public void updateRMCustomer(RMCustomer rm){
		if(rm != null)
			rmCustomer = rm;
	}


	// Reads a data item
	private Transaction readData(int id) {
		synchronized (transactionHT) {
			return (Transaction) transactionHT.get(id);
		}
	}

	// Writes a data item
	private void writeData(int id, Transaction value) {
		synchronized (transactionHT) {
			transactionHT.put(id, value);
		}
		File file = new File(locationFile(id));
		try {
			if (!file.exists())
				file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(value.extractData());
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Remove the item out of storage
	protected Transaction removeData(int id) {
		Transaction tr;
		synchronized (transactionHT) {
			tr = (Transaction) transactionHT.remove(id);
		}
		File file = new File(locationFile(id));
		file.delete();
		return tr;
	}
	
    private String locationFile(int id) {
		return folder + id + ".tmp";
	}
    
    public boolean boot() {
    	String files;
		File folder = new File(this.folder);
		File[] listOfFiles = folder.listFiles(); 

		for (int i = 0; i < listOfFiles.length; i++)
		{
			TransactionData tr;
			if (listOfFiles[i].isFile()) 
			{
				files = listOfFiles[i].getName();
				if (files.endsWith(".tmp"))
				{
					int id = 0;
					Pattern p = Pattern.compile("[0-9]+");
	    			Matcher m = p.matcher(files);
	    			if (m.find()) {
	    			  id = Integer.valueOf(m.group(1)).intValue();  // The matched substring
	    			} else {
	    				return false;
	    			}
	    			try{
	    				FileInputStream fis = new FileInputStream(files);
	    				ObjectInputStream ois = new ObjectInputStream(fis);

	    				tr = (TransactionData) ois.readObject();
	    				ois.close();
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    				return false;
	    			} catch(ClassNotFoundException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    				return false;
	    			}
	    			cleanupTransaction(tr);
				}
			}
		}
    	return true;
    }
    
    private void cleanupTransaction(TransactionData tr){
    	if(tr.voteResult()){
    		for (RMType type : RMType.values()) {
    			if (tr.isEnlisted(type))
					try {
						getRMfromType(type).commit(tr.getID());
					} catch (RemoteException e) {
						new ReconnectLoop(type, tr.getID(), true);
					} catch (InvalidTransactionException e) {
						//if the transaction is invalid, it means
						//it probably has been handled already
					}
    		}
    	} else {
    		for (RMType type : RMType.values()) {
    			if (tr.isEnlisted(type))
					try {
						getRMfromType(type).abort(tr.getID());
					} catch (RemoteException e) {
						new ReconnectLoop(type, tr.getID(), false);
					} catch (InvalidTransactionException e) {
						//if the transaction is invalid, it means
						//it probably has been handled already
					}
    		}
    	}
    	if (tr.isAutoCommitting())
    		writeData(tr.getID(), new Transaction(tr.getID(), tr.isAutoCommitting()));
    
    	File file = new File(locationFile(tr.getID()));
		file.delete();
    }

	public RMBase getRMfromType(RMType type) {
		switch (type) {
		case CAR:
			return rmCar;
		case HOTEL:
			return rmHotel;
		case FLIGHT:
			return rmFlight;
		case CUSTOMER:
			return rmCustomer;
		}
		return null; // shouldn't happen
	}

	public TMimpl(RMCar Car, RMFlight Flight, RMHotel Hotel,
			RMCustomer Customer, LockManager lock) {
		rmCar = Car;
		rmFlight = Flight;
		rmHotel = Hotel;
		rmCustomer = Customer;
		this.lock = lock;
		transactionHT = new RMHashtable();
	}

	@Override
	public int start() {
		// Generate a globally unique ID for the new transaction
		int tid = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
				String.valueOf( Math.round( Math.random() * 100 + 1 )));
		writeData(tid, new Transaction(tid));
		Trace.info("TM::start() succeeded. Providing transaction id " + tid );
		return tid;
	}

	@Override
	public int start(boolean autocommit) {
		// Generate a globally unique ID for the new transaction

		int tid = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
				String.valueOf( Math.round( Math.random() * 100 + 1 )));
		writeData(tid, new Transaction(tid, autocommit));
		Trace.info("TM::autocommit() succeeded. Providing transaction id " + tid );
		return tid;
	}

	@Override
	public boolean commit(int transactionID) throws InvalidTransactionException, TransactionAbortedException {
		Transaction t = readData(transactionID);
		if (t == null)
			throw new InvalidTransactionException();
		t.timeoutReset();
		if(crashType == Crash.BEFORE_VOTE) System.exit(1);
		for (RMType rm : RMType.values()) {
			if (t.isEnlisted(rm)) {
				PrepareThread th = new PrepareThread(rm, t);
				th.start();
			}
		}
		if(crashType == Crash.BEFORE_REPLIES) System.exit(1);
		if(crashType == Crash.BEFORE_ALL_REPLIES) System.exit(1);
		
		//should be the same because it hasn't started sending commit messages yet. 
		try {
			t.timeoutStart();
			while (!t.isReady()) {
				if (t.isTimedOut()) {
					abort(transactionID);
					throw new TransactionAbortedException(transactionID);
				}
				Thread.sleep(1000);
			}
			Trace.info("TM::commit(" + transactionID + ") all replied.");
			if (t.voteResult(crashType == Crash.BEFORE_DECISION)) {
				//if we crash here, RMs don't know about the vote result. Transaction hasn't been
				//stored to memory either. This transaction should be saved on start as well.
				//Transaction comes back up and either aborts the transaction, or restarts vote request. 
				writeData(t.getID(),t); 
				Trace.info("TM::commit(" + transactionID + ") vote passed.");
				if (crashType == Crash.BEFORE_DECISION_SENT) System.exit(1);
				int crashNumber =(int) Math.random()*RMType.values().length; //used only in crash case
				for (RMType rm : RMType.values()) {
					if (crashType == Crash.BEFORE_ALL_DECISION_SENT && --crashNumber <= 0)
						System.exit(1);
					if (t.isEnlisted(rm))
						try {
							getRMfromType(rm).commit(transactionID);
						} catch (RemoteException e) {
							new ReconnectLoop(rm, transactionID, true);
						}
				}

				if (t.isAutoCommitting())
					t.commit();
				else {
					t.cancelTTL();
					removeData(transactionID);
				}
				if (crashType == Crash.AFTER_DECISIONS) System.exit(1);
			} else {
				Trace.info("TM::commit(" + transactionID
						+ ") vote was rejected.");
				abort(transactionID);
				if (crashType == Crash.AFTER_DECISIONS) System.exit(1);
				throw new TransactionAbortedException(transactionID); 
			}
		} catch (InterruptedException e) {
			//shouldn't happen only system.exit will interrupt the sleep
			//that would be too late to abort. 
			abort(transactionID);
			throw new TransactionAbortedException(transactionID);
		} 

		Trace.info("TM::commit(" + transactionID + ") succeeded.");
		return true;
	}

	@Override
	public void abort(int transactionID) throws InvalidTransactionException {
		Transaction t = readData(transactionID);
		if (t == null)
			throw new InvalidTransactionException();
		int crashNumber = (int) Math.random() * RMType.values().length;
		for (RMType type : RMType.values()) {
			if(crashType==Crash.BEFORE_ALL_DECISION_SENT && --crashNumber <= 0) System.exit(1);
			if (t.isEnlisted(type))
				try {
					getRMfromType(type).abort(transactionID);
				} catch (RemoteException e) {
					new ReconnectLoop(type, transactionID, false);
				}
		}
		if (t.isAutoCommitting())
			t.commit();
		// Does not actually performs commit but clears the enlisted properties
		else {
			t.cancelTTL();
			removeData(transactionID);
		}
		Trace.info("TM::abort(" + transactionID + ") succeeded.");
	}

	@Override
	public void enlist(int transactionID, RMType rm)
			throws InvalidTransactionException {
		Transaction t = readData(transactionID);
		if (t == null)
			throw new InvalidTransactionException();
		try {
			t.enlist(rm);
			getRMfromType(rm).enlist(transactionID);

		} catch (RemoteException e) {
			e.printStackTrace();
		}
		writeData(transactionID,t);
		Trace.info("TM::enlist(" + transactionID + ", " + rm.toString()
				+ ") succeeded.");
	}

	public boolean shutdown() {

		for(Enumeration<Transaction> i = transactionHT.elements(); i.hasMoreElements(); ){
			Transaction tr = i.nextElement();
			try {
				abort(tr.getID());
				lock.UnlockAll(tr.getID());
			} catch (InvalidTransactionException e) {
				// Should not happen
				e.printStackTrace();
			}
		}
		try {
			rmCar.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			rmFlight.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			rmHotel.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			rmCustomer.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Trace.info("TM::shutdown() succeeded.");
		return true;
	}


	public boolean lives(int id) throws InvalidTransactionException{
		Transaction tr = readData(id);
		if (tr == null)
			throw new InvalidTransactionException();
		boolean b = tr.resetTTL();
		Trace.info("TM::lives(" + id + ") succeeded.");
		return b;
	}

	public Crash getCrashType() {
		return crashType;
	}

	public void setCrashType(Crash crashType) {
		this.crashType = crashType;
	}

	private class PrepareThread extends Thread {

		private RMType rm;
		private Transaction tr;

		PrepareThread(RMType tp, Transaction tr){
			rm = tp;
			this.tr = tr;
		}

		public void run() {
			boolean b;
			try {
				b = getRMfromType(rm).prepare(tr.getID());
				if (!tr.isTimedOut())
					tr.prepared(b, rm);
			} catch (RemoteException e){
				if(!tr.isTimedOut())
					tr.prepared(false, rm);
			}
		}
	}

	private class ReconnectLoop {

		private RMType rm;
		private int id;
		private boolean commit; //false means you want to abort
		private Timer timer;
		private final long life = 20; // in seconds
		
		ReconnectLoop(RMType rm, int id, boolean commit) {
			this.rm = rm;
			this.id = id;
			this.commit = commit;
			timer = new Timer();
			timer.schedule(new RemindTask(), life * 1000);
		}

		class RemindTask extends TimerTask {
			public void run() {
				try {
				if(commit)
					getRMfromType(rm).commit(id);
				else
					getRMfromType(rm).abort(id);
				} catch (RemoteException e) {
					Trace.info("TM::ReconnectLoop(" + id + " at " + rm + ") failed.");
					timer.schedule(new RemindTask(), life * 1000);
				} catch (InvalidTransactionException e) {
					Trace.info("TM::ReconnectLoop(" + id + " at " + rm + ") failed.");
					timer.cancel(); // Terminate the timer thread
				}
			}
		}	

	}
	/**
	 * Defines the type of transactions as kept in the hashtable. This class
	 * lives as a subclass to be able to have the TTL as a field and have the
	 * TTL call abort when delay is passed to abort the transaction.
	 */
	public class Transaction extends TransactionData implements Serializable {

		private boolean timeout = false;
		private TimeToLive ttl;
		
		public Transaction (int id){
			super(id);
			ttl = new TimeToLive();
		}

		public Transaction(int id, boolean autocommit) {
			super(id, autocommit);
			// If autocommit is on, then transaction will not expire
			if (!autocommit)
				ttl = new TimeToLive();
		}	

		class TimeToLive {

			private Timer timer;
			private final long life = 120; // in seconds; 2 min.

			TimeToLive() {
				timer = new Timer();
				timer.schedule(new RemindTask(), life * 1000);
			}

			class RemindTask extends TimerTask {
				public void run() {
					try {
						abort(id);
						lock.UnlockAll(id);	
					} catch (InvalidTransactionException e) {
						// Should not happen
						e.printStackTrace();
					}
					timer.cancel(); // Terminate the timer thread
				}
			}	

			void reset(){
				timer.cancel();
				timer = new Timer();
				timer.schedule(new RemindTask(), life * 1000);
			}

			void cancel() {
				timer.cancel();
			}
		}

		// reset does not occur if autocommit since there is nothing to have
		// expire.
		boolean resetTTL() {
			if (!autocommit)
				ttl.reset();
			return !autocommit;
		}

		boolean cancelTTL() {
			if (!autocommit)
				ttl.cancel();
			return true;
		}

		public boolean isTimedOut() {
			return timeout;
		}

		public void timeIsOut() {
			this.timeout = true;
		}

		public void timeoutReset() {
			this.timeout = false;
		}

		public void timeoutStart() {
			new Timeout();
		}

		class Timeout {

			private Timer timer;
			private final long life = 60; // in seconds; 1 min.

			Timeout() {
				timer = new Timer();
				timer.schedule(new RemindTask(), life * 1000);
			}

			class RemindTask extends TimerTask {
				public void run() {
					timeIsOut();
					timer.cancel(); // Terminate the timer thread
				}
			}
		}
		
		public TransactionData extractData(){
			return new TransactionData(id, autocommit, car, hotel, flight, customer);
		}

		
	}
}
