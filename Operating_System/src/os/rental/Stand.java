package os.rental;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Stand {
	private int id ;
	private int availableBikes ;
	private int capacity ;
	// to directly get the half capacity without doing the calculations every time
	private int halfCapacity;
	// to tell that the truck is here
	private boolean truckWorking = false; 
	
	/* Reentrant Lock for each stand because some of the stands are more visited than
	 * other and with synchronized we are too limited for this problem 
	 * reentrant lock use the FIFO system */
	private final ReentrantLock rlock = new ReentrantLock(true); //never use static keyword on these or you'll smash your head like me
	
	// Conditions for the lock
	private final Condition isAvailable = rlock.newCondition(); // condition that a bike is available
	private final Condition hasFreeSlot = rlock.newCondition(); // condition that the stand is not full

	public Stand (int id, int capacity, int availableBikes) {
		this.id = id;
		this.capacity = capacity;
		this.availableBikes = availableBikes;
		this.halfCapacity = capacity / 2;
	}
	
	public int getId() {
		return id;
	}
	
	public int getAvailableBikes() {
		return availableBikes;
	}

	public int getFreeSlots () {
		return capacity - availableBikes ;
	}

	public int getCapacity() {
		return capacity;
	}
	
	public int getHalfCapacity() {
		return halfCapacity;
	}

	public void getBike () {
		// lock the reentrant lock to enter critical section
		rlock.lock();
		try {
			// need to use while to make sure the condition is met
			// make sure that the truck is not working in the stand
			// and that there is a bike available
			while (availableBikes == 0 || truckWorking) {
				// wait for the condition to be met (waiting for wakeup signal)
					isAvailable.await();
			}
			availableBikes = availableBikes - 1;
			// waking up waiting users now there is a free slot
			hasFreeSlot.signal();
		} catch (InterruptedException e) {} 
		finally {
			//unlock the reentrant lock to leave critical section
			rlock.unlock();
		}
	}

	public void getBikes(int nbBikes) {
		availableBikes = availableBikes - nbBikes;
	}

	public void returnBike() {
		rlock.lock();
		try {
			// make sure that the truck is not working in the stand
			// and that the stand is not full
			while (getFreeSlots() == 0|| truckWorking) {
				hasFreeSlot.await();
			}
			availableBikes = availableBikes + 1;
			// waking up waiting users now that a bike is available
			isAvailable.signal();
		} catch (InterruptedException e) {}
		finally {
			rlock.unlock();
		}
	}

	public void returnBikes(int nbBikes) {	
		availableBikes = availableBikes + nbBikes ;
	}
	
	public void balance(Truck truck) {
		rlock.lock();
		/* Operations on the bikes need to be in the critical par
		 * or there will be data race or deadlock (i was doing the
		 * balance calculation outside the lock and it was not fun to debug
		 * */
		try {
			truckWorking = true;
			int truckBikes = truck.getNbBikes();
			int truckCapacity = truck.getCapacity();
			// check if we need to balance something
			if (availableBikes > halfCapacity && truckBikes < truckCapacity) {
				int toBalance = Math.min(availableBikes - halfCapacity, truckCapacity - truckBikes);
				getBikes(toBalance);
				System.out.println("Truck: " + toBalance + " bikes picked");
				//System.out.println("Stand: " + id + " has " + availableBikes + " bikes");
			}
			else if (availableBikes < halfCapacity && truckBikes > 0) {
				int toBalance = Math.min(halfCapacity - availableBikes, truckBikes);
				returnBikes(toBalance);
				System.out.println("Truck: " + toBalance + " bikes returned");
				//System.out.println("Stand: " + id + " has " + availableBikes + " bikes");
			}
			// sending signal to all waiting users
			isAvailable.signalAll();
			hasFreeSlot.signalAll();
		} 
		finally {
			truckWorking = false;
			rlock.unlock();
		}
		
	}
}
