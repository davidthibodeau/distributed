// -------------------------------// adapted from Kevin T. Manley// CSE 593// -------------------------------package serversrc.resImpl;public class Hotel extends ReservableItem{		public Hotel( String location, int count, int price )		{			super( location, count, price );		}		public Hotel(Hotel hotel) {			super((ReservableItem) hotel);		}		@Override		public String getKey()		{			return Hotel.getKey( getLocation() );		}		public static String getKey( String location )		{			String s = "room-" + location  ;			return s.toLowerCase();		}}