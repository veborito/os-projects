/* * Operating Systems - Universite de Neuchatel
 * 
 * Assignment #3: an introduction to threads and synchronization in Java
 * 
 * Do not forget to indicate with comments inside the code the 
 * modifications you have made and what problems they fix or 
 * prevent, with references to the questions of the subject (Q1, Q2, etc.)
 */

package os.kitchen;

/**
 * Objects of class Stock represent a set of food. Food is not effectively stored,
 * only a counter is used to represent how much food is available.
 * 
 * It could be possible to use a more realistic queue (FIFO) for the Stock representation.
 * This is left as an exercise for home work. *
 */
class Stock {
	/**
	 * Amount of food
	 */
    private int nbFood;
    /**
     * Name of the stock
     */
    private String name;
    
    /**
     * Max amount of food
     */
    private int maxFood;
    
    /**
     * Creates a new Stock object
     * @param name its name
     * @param nbFood initial number of food
     */
    public Stock(String name, int nbFood, int maxFood) {
        this.nbFood = nbFood;
        this.name = name;		
        this.maxFood = maxFood;
    }

    /**
     * Adds food
     */
    // Q-2 adding synchronized keyword to solve the concurrency problem
    public synchronized void put() {
    	// Q-7 waiting until the food is out of the stock
    	while (nbFood == maxFood) {
    		try {
				wait();
			} catch (InterruptedException e) {}
    	}
        nbFood++;
        // Q-5 using notify to wake up the waiting thread
//        notify();
        //Q-7 using notifyAll to remove deadlock 
        notifyAll();
        // Q-3 get the stock evolution of the final stock
        System.out.println(Thread.currentThread().getName() 
        		+ ": stock \"" + name + "\" contains " + nbFood + " food.");
    }
    
    /**
     * Removes (takes) food
     */
    // Q-2 adding synchronized keyword to solve the concurrency problem
    public synchronized void get() {
    	// Q-5 waiting until some food is available
    	while (nbFood == 0) {
			try {
				wait();
			} catch (InterruptedException e) {}
    	}
        nbFood--;
        // Q-7 notifying all the waiting thread to avoid deadlock.
        // PS: it works also if one of the notifyAll() is replaced
        // with notify() but it's a little slower :D
        notifyAll();
        // Q-3 get the stock evolution of the initial stock
        System.out.println(Thread.currentThread().getName()
        		+ ": stock \"" + name + "\" contains " + nbFood + " food.");
    }

    /**
     * Display the stock status
     */
    public void display() {
        System.out.println("The stock " + name + " contains " + nbFood + " food.");
    }

    /** 
     * "Unit test" for class Stock
     * @param args not used
     */
    static public void main(String[] args) {
        Stock stock = new Stock("test", 5, 1);
        stock.put();
        stock.display();
        stock.get();
        stock.display();
    }
}
