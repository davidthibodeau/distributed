package serversrc.resImpl;

import java.io.Serializable;

import serversrc.resInterface.RMType;

public class TransactionData implements Serializable {
	
	/**
	 * 
	 */
	protected int id;
	protected EnlistedRM car;
	protected EnlistedRM hotel;
	protected EnlistedRM flight;
	protected EnlistedRM customer;
	protected boolean autocommit = false;
	
	public TransactionData(){
		
	}
	
	public TransactionData(int id) {
		this.id = id;
	}
	
	public TransactionData(int id, boolean autocommit) {
		this.id = id;
		this.autocommit = autocommit;
	}
	
	public TransactionData(int id, boolean autocommit, EnlistedRM car, EnlistedRM hotel, EnlistedRM flight, EnlistedRM customer) {
		this.id = id;
		this.car = car;
		this.hotel = hotel;
		this.flight = flight;
		this.customer = customer;
		this.autocommit = autocommit;
	}
	
	public int getID () {
		return id;
	}
	
	private EnlistedRM getEnlistedRMfromType(RMType type) {
		switch (type) {
		case CAR:
			return car;
		case HOTEL:
			return hotel;
		case FLIGHT:
			return flight;
		case CUSTOMER:
			return customer;
		}
		return null; // shouldn't happen
	}
	
	public boolean isReady() {
		for (RMType rm : RMType.values()) {
			if (getEnlistedRMfromType(rm) != null)
				if (!getEnlistedRMfromType(rm).hasReplied())
					return false;
		}
		return true;

	}
	/**
	 * This is used to simulate a crash before receiving all votes.
	 * @param crashType
	 * @return
	 */
	public boolean voteResult(boolean crashDuringVote) {
		for (RMType rm : RMType.values()) {
			
			if (getEnlistedRMfromType(rm) != null)
				if (!getEnlistedRMfromType(rm).hasAccepted())
					return false;
			if(crashDuringVote) System.exit(1); //crashes after first vote (if it was an accept)
		}
		return true;
	}
	/**
	 * gets the result of the vote from each rm
	 * @return
	 */
	public boolean voteResult() {
		
		for (RMType rm : RMType.values()) {
			if (getEnlistedRMfromType(rm) != null)
				if (!getEnlistedRMfromType(rm).hasAccepted())
					return false;
		}
		return true;
	}
	
	/**
	 * Tells whether a particular RM is enlisted
	 */
	public boolean isEnlisted(RMType rm) {
		return (getEnlistedRMfromType(rm) != null);
	}

	/**
	 * Adds RMCar to the list of enlisted RMs
	 */
	public void enlist(RMType rm) {
		switch (rm) {
		case CAR:
			car = new EnlistedRM();
			break;
		case FLIGHT:
			flight = new EnlistedRM();
			break;
		case HOTEL:
			hotel = new EnlistedRM();
			break;
		case CUSTOMER:
			customer = new EnlistedRM();
			break;
		}
	}

	public void prepared(boolean t, RMType rm) {
		if (t) {
			acceptsCommit(rm);
		} else {
			refusesCommit(rm);
		}
	}

	private void acceptsCommit(RMType rm) {
		if (getEnlistedRMfromType(rm) != null)
			getEnlistedRMfromType(rm).accepted();
	}

	private void refusesCommit(RMType rm) {
		if (getEnlistedRMfromType(rm) != null)
			getEnlistedRMfromType(rm).refused();
	}
	
	/**
	 * Will simply reset the enlisted RMs. Is only used for autocommit
	 * transactions.
	 */
	public void commit() {
		car = null;
		flight = null;
		hotel = null;
		customer = null;
	}
	
	public boolean isAutoCommitting(){
		return autocommit;
	}
	
	public class EnlistedRM implements Serializable {

		private boolean repliedPrepared = false;
		private boolean acceptedPrepared = false;

		public void accepted() {
			repliedPrepared = true;
			acceptedPrepared = true;
		}

		public void refused() {
			repliedPrepared = true;
			acceptedPrepared = false;
		}

		public boolean hasReplied() {
			return repliedPrepared;
		}

		public boolean hasAccepted() {
			return acceptedPrepared;
		}
	}
}
