package os.rental;

public class Truck extends Thread {		
	private static final int TIME_BETWEEN_SITES = 25 ; // in milliseconds
	
	private final int capacity ;
	private int bikes ;
	
	public Truck(int bikes, int capacity) {
		this.bikes = bikes;
		this.capacity = capacity;
	}
	
	public Truck(int capacity) {
		this.bikes = 0;
		this.capacity = capacity;
	}
	
	public int getNbBikes () { return bikes; }
	
	public int getCapacity () { return capacity; }
	// change work function into run function, because we're using the Thread class
	public void run() {
		// circle around sites
		// stops when all users finished their runs
		while (World.getActiveUsers() != 0) {
			for (Stand s : World.getStands()) {
				// set that the truck is working on this stand
				// TODO here, equilibrate the number of bikes on the stand.
				s.balance(this);
				try {
					Thread.sleep(TIME_BETWEEN_SITES);
				} catch (InterruptedException e) {}
			}
		}
	}
}
