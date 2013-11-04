package serversrc.resImpl;

import java.io.Serializable;

public class Transaction implements Serializable {

	private boolean carEnlisted = false;
	private boolean hotelEnlisted = false;
	private boolean flightEnlisted = false;
	private boolean customerEnlisted = false;
	private int id;
	
	public Transaction (int id){
		this.id = id;
	}
	
	public int getID (){
		return id;
	}
	
	/**
	 * Tells whether RMCar is enlisted
	 */
	public boolean isCarEnlisted() {
		return carEnlisted;
	}
	
	/**
	 * Adds RMCar to the list of enlisted RMs
	 */
	public void enlistCar() {
		this.carEnlisted = true;
	}

	/**
	 * Tells whether RMHotel is enlisted
	 */
	public boolean isHotelEnlisted() {
		return hotelEnlisted;
	}

	/**
	 * Adds RMHotel to the list of enlisted RMs
	 */
	public void enlistHotel() {
		this.hotelEnlisted = true;
	}

	/**
	 * Tells whether RMFlight is enlisted
	 */
	public boolean isFlightEnlisted() {
		return flightEnlisted;
	}

	/**
	 * Adds RMFlight to the list of enlisted RMs
	 */
	public void enlistFlight() {
		this.flightEnlisted = true;
	}

	/**
	 * Tells whether RMCustomer is enlisted
	 */
	public boolean isCustomerEnlisted() {
		return customerEnlisted;
	}

	/**
	 * Adds RMCustomer to the list of enlisted RMs
	 */
	public void enlistCustomer() {
		this.customerEnlisted = true;
	}

}
