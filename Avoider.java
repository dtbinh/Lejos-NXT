/* Author: Guangpeng Li
 * University of Liverpool
 * Date: 13/11/2014
 * 
 * The purpose of this class is to handle any encountered obstacles
 * from the snake mover in the arena. When the obstacle is avoided
 * snake will resume its job.
 */
public class Avoider implements Runnable{
	/*
	 * Retrieve data from the sonar class
	 */
	private SonarMapper sonar;
	/*
	 * Retrieve data from the localizer class
	 */
	private Localizer localizer;
	/*
	 * The movement controller for the robot
	 */
	private MoveController mc;
	/*
	 * Check whatever if the snake mover requires help from turning
	 */
	private boolean turnBlocked = false;
	/*
	 * The direction should be facing to after avoiding the obstacle
	 */
	private String endFacing;
	/*
	 * The robot location when the obstacle is encountered
	 */
	private Location startLocation;
	/*
	 * Check whatever if the robot should go left first
	 * It determine on the algorithm to find left side
	 * or right side has the most unknown cells
	 */
	private boolean goLeft = false;
	/*
	 * Count the steps away from where start location
	 */
	private int stepCounter = 0;
	/*
	 * Constructor	
	 */
	public Avoider(MoveController mc, SonarMapper sonar, Localizer localizer, boolean turnBlocked, String endFacing){
		this.sonar = sonar;
		this.localizer = localizer;
		this.mc = mc;
		this.turnBlocked = turnBlocked;
		this.endFacing = endFacing;
	}
	
	public void run(){		
		startLocation = localizer.getLoc();
		// The robot decides to go to the side that has the most unknown cells
		// to avoid the obstacle
		if (sonar.unknownCellsRightOf(startLocation.Y, localizer.getHString()) < 
				sonar.unknownCellsLeftOf(startLocation.Y, localizer.getHString()))
		    goLeft = true;
	    lookForPath();
	 // If the one side fails then look for the other side
	    if (localizer.getLoc().equals(startLocation)) {
	    	goLeft = !goLeft;
	    	lookForPath();
        }
		
	}
	/*
	 * Execute path searching to avoid the obstacle
	 */
	private void lookForPath() {
		if (turnBlocked){
			turnSupport();
		} else{
			turner(checker());
	        moveBack();
	        if (!goLeft)
	            mc.turnRight();
	        else
	        	mc.turnLeft();
		}
	}
	
	/*
	 * Use the bug algorithm to find a path way
	 * Step counter will count how far away from the line
	 * where encountered the obstacle.
	 */
	private void moveBack(){
    	mc.forward();
    	checkStep();
    	// It stops when the whole map is scanned 
    	// or when the robot has avoided the obstacle
    	while(!sonar.shouldStop()){
    		if(stepCounter == 0)
        		return;
    		// Check whatever the robot should go left or right side of the obstacle
    		if(goLeft ? sonar.rightCellOccupied() : sonar.leftCellOccupied()){
    			if (!goLeft) {
    				if(sonar.frontCellOccupied())
    					mc.turnRight();
    				if(sonar.frontCellOccupied())
    					mc.turnRight();
    			} else {
    				if(sonar.frontCellOccupied())
    					mc.turnLeft();
    				if(sonar.frontCellOccupied())
    					mc.turnLeft();
    			}
    			mc.forward();
    			checkStep();
    		}
    		else{
    			if (!goLeft)
	    			mc.turnLeft();
    			else
    				mc.turnRight();
    			mc.forward();
    			checkStep();
    		}
        }
    }
	
	/*
	 * Top and bottom obstacle avoiding
	 * This method is to support the snake class to move 
	 * back its line when it is turning and encounter obstacles.
	 */
	private void turnSupport(){
		//If the robot needs to face down
		if(endFacing.equals("DOWN")){	
			mc.turnRight();
			mc.forward();
			// Move down until find a way
			while(turnBlocked){
				if(!sonar.leftCellOccupied()){
					mc.turnLeft();
					mc.forward();
					mc.turnRight();
					turnBlocked = false;
				}
				else mc.forward();
			}
		}// if the robot needs to face up
		else{
			mc.turnLeft();
			mc.forward();
			// Move up until find a way
			while(turnBlocked){
				if(!sonar.rightCellOccupied()){
					mc.turnRight();
					mc.forward();
					mc.turnLeft();
					turnBlocked = false;
				}
				else mc.forward();
			}
		}
	}
	
	/*
	** Scan both side of the robot to see
	** if there are any obstacles
	** Left empty = 1
	** Right empty = 2
	** Both blocked = 0
	*/
	private int checker(){
		// Check the right path if blocked
        if(!sonar.rightCellOccupied() && !goLeft)
            return 2;
        else
            // Check the left path if blocked
            if(!sonar.leftCellOccupied())
                return 1;
            else
                // Both path blocked
                return 0;
	}
	
	/*
	** Decide which way the robot rotating to
	*/
	private void turner(int i){
		switch(i){
			// when both path are blocked
			case 0: mc.turnLeft();
					mc.turnLeft();
					mc.forward();
					turner(checker());
					break;
			// when right path is blocked
			case 1: mc.turnLeft();
					break;
			// when left path is blocked
			case 2: mc.turnRight();
					break;
		}
	}
	
	/*
     * Counter the steps away from the obstacle line
     */
    private void checkStep(){
    	// When heading is positive X-axis
    	if(localizer.getHString().equals("RIGHT"))
        	stepCounter++;
        // When heading is negative X-axis
        else if(localizer.getHString().equals("LEFT"))
        	stepCounter--;
    }
}