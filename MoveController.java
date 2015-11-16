import lejos.robotics.navigation.DifferentialPilot;
/* Author: Guangpeng Li
 * University of Liverpool
 * Date: 13/11/2014
 * 
 * This class contains possible movement and rotation methods
 * of the robot. 
 */
class MoveController {
	/*
	 * The pilot for the robot
	 */
    private DifferentialPilot dp;
    /*
     * The sonar mapper
     */
    private SonarMapper sonar;
    /*
     * The localiser for the robot
     */
	public Localizer l;
	/*
	 * Constructor	
	 */ 
    public MoveController(DifferentialPilot dp, SonarMapper sonar, Localizer l){
        this.dp = dp;
        this.sonar = sonar;
        this.l = l;
    }
    /*
     * The robot move forward one step
     * and also scan the unknown cells 
     */
    public void forward(){
        dp.travel(Localizer.CELL_SIZE);
		l.movedForward();
		sonar.scan();
    }
    /*
     *The robot turns left 90 degrees 
     */
    public void turnLeft(){
        dp.rotate(-90);
        sonar.turnedLeft();
    }
    /*
     * The robot turn right 90 degrees 
     */
    public void turnRight(){
        dp.rotate(90);
        sonar.turnedRight();
    }
}