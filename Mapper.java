import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.nxt.*;
import lejos.nxt.comm.*;
import java.lang.Thread;
/* Author: Guangpeng Li
 * University of Liverpool
 * Date: 13/11/2014
 * 
 * The Main class for this project. The strategy for this robot to scan
 * the arena is to sneak around the arena and find all possible unknown 
 * cells until all cells are found. All unreachable cells will be
 * counted in the map as well. The mapping for the robot is occupancy
 * grid map.
 * 
 * Video Clip: *link to youtube*
 */
class Mapper {
    public static void main (String[] args) {
    		// Connect to the remote console 
	    RConsole.open();
		LCD.clear();
			// Instantiate the pilot for the robot
        DifferentialPilot dp = new DifferentialPilot(3.22, 21, Motor.A, Motor.B);
        	// Instantiate the position provider for the robot
        OdometryPoseProvider opp = new OdometryPoseProvider(dp);
        	// Instantiate the localizer for the robot
        Localizer localizer = new Localizer(opp);
        	// Instantiate the localization for the robot
        Localization localization = new Localization(localizer, dp);
        	// Instantiate the soanr mapper for the robot
        SonarMapper sonar = new SonarMapper(localizer);
        	// Instantiate the movement controller for the robot
        MoveController mover = new MoveController(dp, sonar, localizer);
        	// Set the motor speed
		dp.setTravelSpeed(30);
        dp.setRotateSpeed(30);
        Motor.C.setSpeed(900);
        sonar.scan();
        	// Instantiate a thread for localization
        Thread localizationThread = new Thread(localization);
        localizationThread.start();
        	// The robot will not stop until the arena is fully explored
        while(!sonar.shouldStop()) {
        		// Instantiate snake mover
            SnakeMover snake = new SnakeMover(localizer, sonar, mover);
            Thread snakeMoverThread = new Thread(snake);
            localization.setSnake(snake);
            snakeMoverThread.start();
            while(snakeMoverThread.isAlive()) {}
				// Check if the robot should stop
            if (sonar.shouldStop())
            	break;
            	// Instantiate the obstacle avoider
			Avoider avoider = new Avoider(mover, sonar, localizer, snake.getTurnBlocked(), snake.getEndFacing());
            Thread avoidThread = new Thread(avoider);
            avoidThread.start();
            while (avoidThread.isAlive()) {}
        }
			// Reset the motor C
		Motor.C.rotateTo(0);
			// Send the map data to the console
		sonar.sendMapViaBluetooth();
			// Close the console
		RConsole.close();
    }
}