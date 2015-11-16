import lejos.nxt.*;
import lejos.nxt.addon.ColorHTSensor;
import lejos.robotics.navigation.DifferentialPilot;
/* Author: Guangpeng Li
 * University of Liverpool
 * Date: 13/11/2014
 * 
 * The purpose of this class is to use the colour sensor to 
 * calibrate the location and heading of the robot.
 */
class Localization implements Runnable {
	/*
	 * The scanned colour ID
	 */
	int color; 
	/*
	 * The localiser for the robot
	 */
	Localizer localizer;
	/*
	 * Instantiate the colour sensor in the sensor port 4
	 */
	ColorHTSensor cs = new ColorHTSensor(SensorPort.S4);
	/*
	 * The snake mover for the robot
	 */
	SnakeMover snake;
	/*
	 * The robot pilot
	 */
	DifferentialPilot dp;
	/*
	 * Check whatever blue paper has scanned
	 */
	boolean bluePdone = false;
	/*
	 * Check whatever green paper has scanned
	 */
	boolean greenPdone = false;
	/*
	 * Constructor	
	 */
	public Localization(Localizer localizer,DifferentialPilot dp) {
		this.localizer = localizer;
		this.dp = dp;
    }
	/*
	 * Constructor to set snake mover
	 */
	public void setSnake(SnakeMover snake){
		this.snake = snake;
	}	
	public void run() {
		while(true){
		//read colour from colour sensor
		color = cs.getColorID();
		locateColors();
		}
		
	}	
	/*
	 * When colour paper is found
	 */
	public void locateColors(){
		//Colour blue and blue paper not scanned
		if(color == 2 && !bluePdone){
			//stop the snakeMover
			snake.stop();
			//blue paper scanned 
			bluePdone = true;
			//do actions in left top corner
			blueCorner();
		}
		//Colour green and it not scanned
		else if(color == 1 && !greenPdone){
			snake.stop();
			greenPdone = true;
			greenPaper();
		}
	}
	/*
	 * Calibrate on blue paper
	 */
	public void blueCorner(){
		while(!snake.getSnakeTurnDone()){}
		//make sure robot is in top corner facing towards front wall
			if(localizer.getHString().equals("FORWARD")){
			//calibrate in x direction
			dp.travel(30);
			dp.travel(-15);
			//calibrate in y direction
			dp.rotate(-90);
			dp.travel(20);
			dp.travel(-10);
			dp.rotate(90);
		}
		//start snakeMover thread
		snake.restart();
	}
	/*
	 * Calibrate on green paper
	 */
	public void greenPaper(){
		while(!snake.getSnakeTurnDone()){}
		//make sure robot is towards the back wall 
		if(localizer.getHString().equals("BACK")){
			//calibrate heading 
			dp.travel(30);
			dp.travel(-15);
		}
		snake.restart();
	}
}