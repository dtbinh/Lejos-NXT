import javax.microedition.lcdui.Graphics;
import lejos.nxt.*;
import lejos.nxt.comm.*;
/* Author: Guangpeng Li
 * University of Liverpool
 * Date: 13/11/2014
 * 
 * This class contains the algorithm and methods to scan the 
 * grid map. It also store the occupancy grid map for the arena and all 
 * information relate to the occupancy grid map.
 */
class SonarMapper {
	/*
	 * The size for the occupancy grid map
	 */
	public final int MAP_YAXIS = (int)Math.ceil(Localizer.ARENA_WIDTH/Localizer.CELL_SIZE);
    public final int MAP_XAXIS = (int)Math.ceil(Localizer.ARENA_HEIGHT/Localizer.CELL_SIZE);
    /*
     * The left distance for the sonar scanner
     */
    private float leftDistance = 1;
    /*
     * The forward distance for the sonar scanner
     */
    private float forwardDistance = 1;
    /*
     * The right distance for the sonar scanner
     */
    private float rightDistance = 1;
    /*
     * The storage for the occupancy grid map
     */
    private byte[][] occupancyGrid = new byte[MAP_YAXIS][MAP_XAXIS];
    /*
     * The reach able grids in the occupancy grid map
     * 0 is unknown
	 * 1 is reachable
     * 2 is unreachable
     */
	private byte[][] reachability = new byte[MAP_YAXIS][MAP_XAXIS];
    /*
     * The scanning distance for the sonar scanner	
     */
	private static int DISTANCE_FROM_SENSOR_TO_FAR_EDGE_OF_CELL = Localizer.CELL_SIZE + 5;
    /*
     * The graphic sketcher for the LCD
     */
	Graphics g = new Graphics();
	/*
	 * The robot localizer
	 */
    Localizer localizer;
    /*
     * Instantiate the ultrasonic sensor in the sensor  port 2
     */
    UltrasonicSensor sonar = new UltrasonicSensor(SensorPort.S2);
    /*
	 * Constructor	
	 */
    public SonarMapper(Localizer localizer) {
        this.localizer = localizer;
        resetMap();
    }
    
	//Returns true when all cells which can be scanned have been scanned
    public boolean shouldStop () {
	    //Count unscanned cells
        int unknownCells = 0;
        for (int y = 0; y < MAP_YAXIS; y++)
        	for (int x = 0; x < MAP_XAXIS; x++)
        		if (occupancyGrid[y][x] == -1)
        			unknownCells++;
        //Count occupied cells
        int occupiedCells = 0;
        for (int y = 0; y < MAP_YAXIS; y++)
        	for (int x = 0; x < MAP_XAXIS; x++)
        		if (occupancyGrid[y][x] == 1)
        			occupiedCells++;
        
        return unknownCells+occupiedCells == countUnreachableCells();
    }
    //Count the number of unreachable cells in the arena
	public int countUnreachableCells() {
		int unreachableCells = 0;
    	for (int y = 0; y < MAP_YAXIS; y++)
        	for (int x = 0; x < MAP_XAXIS; x++)
        		if (reachability[y][x] == 2 || reachability[y][x] == 0)
        			unreachableCells++;
    	
    	return unreachableCells;
	}
	
	//A cell is unreachable if the robot cannot occupy it, either due
	//to it being obstructed or there being no path to reach it from (0,0).
	//The following method works by taking each cell and trying to find a
	//route to an unoccupied cell, and checking the cell itself is not obstructed.
	//For the purposes of this method unscanned cells are assumed to be unobstructed,
	//and as such the unreachable cells are recalculated each time the robot scans.
    private void calcUnreachableCells() {
    	for (int y = 0; y < MAP_YAXIS; y++)
        	for (int x = 0; x < MAP_XAXIS; x++)
        		reachability[y][x] = 0;
				
    	reachability[0][0] = 1; //The (0,0) is always reachable from (0,0)
		
    	for (int round = 0; round < MAP_YAXIS*MAP_XAXIS; round++) {
    		for (int y = 0; y < MAP_YAXIS; y++) {
            	for (int x = 0; x < MAP_XAXIS; x++) {
            		if (reachability[y][x] == 0) {
	            		boolean upReachable = false;
	            		boolean downReachable = false;
	            		boolean leftReachable = false;
	            		boolean rightReachable = false;
	            		boolean known = false; //If known is true then we are utterly certain about the reachability of this cell
	            		
						//Check whether the cell above is reachable
	            		if (cellInGrid(x+1, y)) {
	                    	if (occupancyGrid[y][x+1] == 0 || reachability[y][x+1] == 1) {
	                    		reachability[y][x] = 1;
	                    		known = true;
	                    	} else if (occupancyGrid[y][x+1] == 1 || reachability[y][x+1] == 2) {
	                    		upReachable = false;
	                    	}
	            		}
	                    
						//Check whether the cell to the left is reachable
	                    if (cellInGrid(x, y-1)) {
	                    	if (occupancyGrid[y-1][x] == 0 || reachability[y-1][x] == 1) {
	                    		reachability[y][x] = 1;
	                    		known = true;
	                    	} else if (occupancyGrid[y-1][x] == 1 || reachability[y-1][x] == 2) {
	                    		downReachable = false;
	                    	}
	                    }
	                    
						//Check whether the cell to the right is reachable
	                    if (cellInGrid(x, y+1)) {
	                    	if (occupancyGrid[y+1][x] == 0 || reachability[y+1][x] == 1) {
	                    		reachability[y][x] = 1;
	                    		known = true;
	                    	} else if (occupancyGrid[y+1][x] == 1 || reachability[y+1][x] == 2) {
	                    		rightReachable = false;
	                    	}
	                    }
						
	                    //Check whether the cell below is reachable
	                    if (cellInGrid(x-1, y)) {
	                    	if (occupancyGrid[y][x-1] == 0 || reachability[y][x-1] == 1) {
	                    		reachability[y][x] = 1;
	                    		known = true;
	                    	} else if (occupancyGrid[y][x-1] == 1 || reachability[y][x-1] == 2) {
	                    		leftReachable = false;
	                    	}
	                    }
	                    
	                    if (!known) {
	                    	if (leftReachable || rightReachable || upReachable || downReachable)
	                    		reachability[y][x] = 1;
	                    	if (!leftReachable && !rightReachable && !upReachable && !downReachable)
	                    		reachability[y][x] = 2;
	                    }
	                    
	                    if (occupancyGrid[y][x] == 1) //Occupied cells are unreachable
	                    	reachability[y][x] = 2;
            		}
    			}
    		}
    	}
    }
    
	//Count the number of unscanned cells in columns left of Y, from the robots perspective
    public int unknownCellsLeftOf (int Y, String heading) {
    	if (heading.equals("BACK"))
    		return unknownCellsRightOf(Y, "FORWARD");
    	
        int unknownCells = 0;
        for (int y = 0; y < Y; y++)
        	for (int x = 0; x < MAP_XAXIS; x++)
        		if (occupancyGrid[y][x] == -1)
        			unknownCells++;
        return unknownCells;
    }
    
	//Count the number of unscanned cells in columns right of Y, from the robots perspective
    public int unknownCellsRightOf (int Y, String heading) {
    	if (heading.equals("BACK"))
    		return unknownCellsLeftOf(Y, "FORWARD");
    	
        int unknownCells = 0;
        for (int y = Y+1; y < MAP_YAXIS; y++)
        	for (int x = 0; x < MAP_XAXIS; x++)
        		if (occupancyGrid[y][x] == -1)
        			unknownCells++;
        return unknownCells;
    }

	//Initialize the occupancyGrid to everything being unknown
    private void resetMap () {
        for (int x = 0; x < occupancyGrid.length; x++)
            for (int y = 0; y < occupancyGrid[0].length; y++)
                occupancyGrid[x][y] = -1;
        occupancyGrid[0][0] = 0;
    }
    
	//Display the map the LCD screen
    public void printMap(){
    	LCD.setAutoRefresh(true);
    	g.clear();
    	arrow(localizer.getY()*10, (MAP_XAXIS - localizer.getX())*10); //Show the location and heading of the robot
    	g.drawRect(0, MAP_XAXIS*10, 10, 10);
    	for(int x = 0; x < MAP_XAXIS; x++)
    		for(int y = 0; y < MAP_YAXIS; y++) {
    				if(occupancyGrid[y][x] == 0)
    					g.drawRect(y * 10, (MAP_XAXIS - x)*10, 10, 10);	
    				else if(occupancyGrid[y][x] == 1)
    					g.fillRect(y * 10, (MAP_XAXIS - x)*10, 10, 10);
    		}
    	LCD.drawInt(localizer.getX(), 10, 0);
    	LCD.drawInt(localizer.getY(), 13, 0);
    }
    
	//Draw an arrow at (x, y) in LCD showing the heading of the robot
    private void arrow(int x, int y){
    	int x1 = x + 5;
    	int y1 = y + 5;
    	int x2 = x + 10;
    	int y2 = y + 10;
    	int x3 = x + 9;
    	int y3 = y + 9;
    	int x4 = x + 4;
    	// When the robot is heading forward
    	if(localizer.getHString().equals("FORWARD")){
	    	g.drawLine(x, y1, x1, y); 	// ->/\
			g.drawLine(x1, y, x2, y1);  // /\<-
			g.drawLine(x, y1, x3, y1); 	// --
    	}
    	// When the robot is heading backward
    	else if(localizer.getHString().equals("BACK")){
    		g.drawLine(x, y1, x1, y2); 	// ->\/
			g.drawLine(x1, y2, x2, y1); // \/<-
			g.drawLine(x, y1, x3, y1); 	// --
    	}
    	// When the robot is heading left
    	else if(localizer.getHString().equals("LEFT")){
    		g.drawLine(x1, y, x, y1); 	// <up
			g.drawLine(x, y1, x1, y2);  // <low
			g.drawLine(x1, y, x1, y3); 	// |
    	}
    	// When the robot is heading right
    	else if(localizer.getHString().equals("RIGHT")){
    		g.drawLine(x4, y, x3, y1); 	// up>
			g.drawLine(x3, y1, x1, y3); // low>
			g.drawLine(x4, y, x4, y3);  // |
    	}
    }
    
	//Scan those adjacent cells which haven't been scanned
    public void scan() {
    	printMap();
        scanFront();
        printMap();
        scanRight();
        printMap();
        scanLeft();
        printMap();
		calcUnreachableCells();
    }

	//Check whether a given coordinate is in the grid
    public boolean cellInGrid (int x, int y) {
    	return x >= 0 && y >= 0 && x < MAP_XAXIS && y < MAP_YAXIS;
    }
    
	//Check whether the cell in front of the robot (from its perspective) is occupied
    public boolean frontCellOccupied () {
        Location l = localizer.getCellInFront();
        return cellOccupied(l.X, l.Y);
    }
    
	//Check whether the cell left of the robot (from its perspective) is occupied
    public boolean leftCellOccupied () {
        Location l = localizer.getCellToLeft();
        return cellOccupied(l.X, l.Y);
    }
    
	//Check whether the cell in right of the robot (from its perspective) is occupied
    public boolean rightCellOccupied () {
        Location l = localizer.getCellToRight();
        return cellOccupied(l.X, l.Y);
    }
    
	//Check whether (x,y) is occupied
    public boolean cellOccupied (int x, int y) {
        if (cellInGrid(x, y))
            return occupancyGrid[y][x] == 1;
        return true;
    }
    
	// Scan the fron of the robot
    private float scanFront(){
    	Location l = localizer.getCellInFront();
    	if (!cellInGrid(l.X, l.Y))
    		return 10;
    	if (occupancyGrid[l.Y][l.X] != -1)
    		return 10;
        Motor.C.rotateTo(0);
    	forwardDistance = sonar.getDistance();
		//update the map
		if (cellInGrid(l.X,l.Y))
            occupancyGrid[l.Y][l.X] = forwardDistance < 
                DISTANCE_FROM_SENSOR_TO_FAR_EDGE_OF_CELL ? (byte)1 : (byte)0;
        return forwardDistance;
    }
    
    // Scan the right side of the robot
    private float scanRight () {
    	Location l = localizer.getCellToRight();
    	if (!cellInGrid(l.X, l.Y))
    		return 10;
    	if (occupancyGrid[l.Y][l.X] != -1)
    		return 10;
    	Motor.C.rotateTo(650);
    	rightDistance = sonar.getDistance();
		Motor.C.rotateTo(0);
		//update the map
		if (cellInGrid(l.X,l.Y))
            occupancyGrid[l.Y][l.X] = rightDistance < 
                DISTANCE_FROM_SENSOR_TO_FAR_EDGE_OF_CELL ? (byte)1 : (byte)0;
		return rightDistance;
    }
    
    // Scan the left side of the robot
    private float scanLeft () {
    	Location l = localizer.getCellToLeft();
    	if (!cellInGrid(l.X, l.Y))
    		return 10;
    	if (occupancyGrid[l.Y][l.X] != -1)
    		return 10;
        Motor.C.rotateTo(-650);
    	leftDistance = sonar.getDistance();
		Motor.C.rotateTo(0);
		//update the map
		if (cellInGrid(l.X,l.Y))
            occupancyGrid[l.Y][l.X] = leftDistance < 
                DISTANCE_FROM_SENSOR_TO_FAR_EDGE_OF_CELL ? (byte)1 : (byte)0;
		return leftDistance;
    }
    
	//Called by move controller so SonarMapper can update the results of getLeftDistance &c
    public void turnedLeft () {
        float ol = leftDistance;
        float of = forwardDistance;
        forwardDistance = ol;
        rightDistance = of;
    }
    
	//See above	
    public void turnedRight () {
        float of = forwardDistance;
        float or = rightDistance;
        forwardDistance = or;
        leftDistance = of;
    }
    
	//Get the distance to the first surface to the left
    public float getLeftDistance() {
        return leftDistance;
    }
    
	//Get the distance to the first surface to the right
    public float getRightDistance() {
        return rightDistance;
    }
    
	//Get the distance to the first surface ahead
    public float getForwardDistance() {
        return forwardDistance;
    }
	
	//A # represents an occupied cell
	//An _ represents an unoccupied cell
	//A ? represents a cell which cannot be scanned
    public void sendMapViaBluetooth () {
		while(!RConsole.isOpen()) {}
		for (int x = MAP_XAXIS-1; x >= 0 ; x--) {
		    for (int y = 0; y < MAP_YAXIS; y++) {
		        if (occupancyGrid[y][x] == 1)
				    RConsole.print("#");
				else if (occupancyGrid[y][x] == 0)
				    RConsole.print("_");
				else if (occupancyGrid[y][x] == -1)
				    RConsole.print("?");
			}
			RConsole.print("\n");
	    }
	}
}
