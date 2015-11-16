/* Author: Guangpeng Li
 * University of Liverpool
 * Date: 13/11/2014
 * 
 * This class contain the snake movement for the robot.
 * It is used to explore the arena and once it encounter 
 * obstacles then Avoider class will take over the job.
 * This will wait until Avoider is done.
 */
public class SnakeMover implements Runnable{
    /*
     * Movement controller for the robot
     */
    MoveController mc;
    /*
     * The location of the robot
     */
    Localizer localizer;
    /*
     * The sonar mapper
     */
    SonarMapper map;
    /*
     * Determine whatever the robot should stop for calibration
     */
    private boolean stopForCalibration = false;
    /*
     * Determine whatever the snake movement is finish
     */
    private boolean snakeTurnDone = false;
    /*
     * Determine whatever the snake turn has blocked
     * by obstacles
     */
    private boolean turnBlocked = false;
    /*
     * The end facing of the robot should be facing to
     * after avoiding the obstacle
     */
    private String endFacing;
    
    /*
	 * Constructor	
	 */
    public SnakeMover(Localizer localizer, SonarMapper map, MoveController mc) {
        this.localizer = localizer;
        this.map = map;
        this.mc = mc;
    }
    
    public void run() {
    	while (!map.shouldStop()) {
    	   // Pause for calibration when blue paper is found
    	   while (stopForCalibration) {} 
    	   snakeTurnDone = false;
    	   // Turn around when reach to the top of the arena
           if (localizer.getX() == map.MAP_XAXIS-1 && localizer.getHString().equals("FORWARD")) {
        	   mc.turnRight();
        	   // Check if the robot can turn
               if (map.frontCellOccupied()) {
                   turnBlocked = true;
                   endFacing = "DOWN";
                   return;
               } else {
                   mc.forward();
                   mc.turnRight();
               }
           } 
           // Turn around when reach to the bottom of the arena
           else if (localizer.getX() == 0 && localizer.getHString().equals("BACK")) {
               mc.turnLeft();
               // Check if the robot can turn
               if (map.frontCellOccupied()) {
                   turnBlocked = true;
                   endFacing = "UP";
                   return;
               } else {
                   mc.forward();
                   mc.turnLeft();
               }
            }
            if (!map.frontCellOccupied())
                mc.forward();
            else
                return;
            //snakeMover's current move is over
            snakeTurnDone = true; 
       }
    }
    /*
     * Let avoider to know if turn is blocked
     */
    public boolean getTurnBlocked(){
    	return turnBlocked;
    }
    /*
     * Let avoider to know the end facing after avoiding
     */
    public String getEndFacing(){
    	return endFacing;
    }
    /*
     * Let localisation know the job is done
     */
    public boolean getSnakeTurnDone(){
    	return snakeTurnDone;
    }
    
    //pause snakeMover thread for localization
    public void stop() {
    	stopForCalibration = true;
    }
    
    //start snakeMover thread
    public void restart() {
    	stopForCalibration = false;
    }
}