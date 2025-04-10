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
 * Objects instances of Kitchen represent a kitchen with initially two stoves and 
 * two stocks: initial stock of 16 food and empty final stock. Stoves are used to
 * prepare from the former to the latter.
 */
class Kitchen {
	/**
	 * Stock of food to prepare
	 */
	// Q-7
	Stock stockInput = new Stock("input", 10_000, 10_000);
//    Stock stockInput = new Stock("input", 16); 
    //Q-2 changing production capacity to 100 millions
    //Stock stockInput = new Stock("input", 100_000_000);
     //Q-4 intermediate Stock
//	Stock stockIntermediate = new Stock("intermediate", 0)
	// Q-7 max 1 el in the intermediate stock
    Stock stockIntermediate = new Stock("intermediate", 0, 1);
    
    /**
     * Stock of final (prepared) food
     */
//    Stock stockOutput = new Stock("output", 0);
    // Q-7
    Stock stockOutput = new Stock("output", 0, 10_000);
    /**
     * Stoves for the preparations
     */
    // Q-4 making stove 1 prepare food and stock it in an intermediate stock
    //Stove stove1 = new Stove(stockInput, stockIntermediate, 16); 
    // Q-2 50 millions per stove
//    Stove stove1 = new Stove(stockInput, stockOutput, 50_000_000); 
//    Stove stove2 = new Stove(stockInput, stockOutput, 50_000_000);
    // Q-1 ,Q-3
    //Stove stove1 = new Stove(stockInput, stockOutput, 8); 
//  Stove stove2 = new Stove(stockInput, stockOutput, 8);
    // Q-4, Q-6 making stove 1 take prepared food from intermediate and put it in final stock.
//    Stove stove1 = new Stove(stockInput, stockIntermediate, 16);
    //Q-6 sharing workload
//    Stove stove2 = new Stove(stockIntermediate, stockOutput, 8);
    // Q-6  adding  an additional stove to share work with stove 2
//    Stove stove3 = new Stove(stockIntermediate, stockOutput, 8);
//    // Q-7
    Stove stove1 = new Stove(stockInput, stockIntermediate, 5_000);
    Stove stove2 = new Stove(stockInput, stockIntermediate, 5_000);
    Stove stove3 = new Stove(stockIntermediate, stockOutput, 5_000);
    Stove stove4 = new Stove(stockIntermediate, stockOutput, 5_000);
    
    /**
     * Main entry point: proceed to operate the kitchen work of preparation
     */
    public void work() {
    	System.out.println("Starting kitchen work ...");
    	long initialTime = System.currentTimeMillis();
    	// Q-1 starting the threads
   		stove3.start();
   		stove4.start();
   		/* Q-1 delaying the start of the second stove 
   		 * this partially solve the problem of concurrency, data race.
   		 * But slows down the program. */
   		//try { Thread.sleep(10); } catch (InterruptedException e) {}
   		// Q-5 launching stove 1 last.
   		stove1.start();
   		stove2.start();
   		try {
   			// Q-1 joining threads at the end
			stove1.join();
			stove2.join();
			stove3.join();
			stove4.join();
		} catch (Exception e) {}
   		/* Q-3 we can't draw any conclusion about the scheduler
   		 * the order is almost different at each run
   		*/
   		stockInput.display();
   		stockOutput.display();
   		System.out.println("... done ("+((double)(System.currentTimeMillis() - initialTime)/1000)+" second(s))");
    }
    
    /**
     * Entry point for the whole program
     * @param args not used
     */
    public static void main(String[] args) {
    	new Kitchen().work();
    }
}
