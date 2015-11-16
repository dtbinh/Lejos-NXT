import lejos.robotics.localization.OdometryPoseProvider;
/* Author: Guangpeng Li
 * University of Liverpool
 * Date: 13/11/2014
 * 
 * This class contains the information of the arena, 
 * grid map and the current location of the robot and
 * its direction as well.
 */
public class Localizer {
	/*
	 * The position of the robot
	 */
    private OdometryPoseProvider opp;
    /*
     * The cell x axis location of the robot
     */
	private int x = 0;
	/*
	 * The cell y axis location of the robot
	 */
	private int y = 0;
	/*
	 * Cell size of the grid and the step range for the robot
	 */
	public static final int CELL_SIZE = 35;
	/*
	 * The predicted size for the arena
	 */
	public static final int ARENA_WIDTH = 150;
	public static final int ARENA_HEIGHT = 200;
	/*
	 * Constructor	
	 */
	public Localizer(OdometryPoseProvider opp) {
		this.opp = opp;
	}
	/*
	 * Return the x axis
	 */
	public int getX() {
	    return x;
	}
	/*
	 * Return the y axis
	 */
	public int getY() {
	    return y;
	}
	/*
	 * Return the heading of the robot in degrees
	 */
	public int getH() {
	    return (int)opp.getPose().getHeading();
	}
	/*
	 * Return the full current location information of the robot
	 */
	public Location getLoc() {
	    return new Location(getX(), getY(), getH());
	}
	/*
	 * Return the accurate direction of the robot
	 */
	public String getHString () {
	    if (getH() <= -80 && getH()>=-100)
		    return "LEFT";
		else if ((getH() <= -170 && getH() >= -190) || (getH() >= 170 && getH() <= 190))
		    return "BACK";
		else if (getH() >= 80 && getH() <= 100)
			return "RIGHT";
		else if (getH() >= -10 && getH() <= 10)
			return "FORWARD";
		else
			return "ERROR";
	}
	/*
	 * Update the current location of the robot in the grid map
	 */
	public void movedForward(){
		if (getHString().equals("FORWARD"))
		    x++;
		else if (getHString().equals("BACK"))
		    x--;
		else if (getHString().equals("LEFT"))
		    y--;
	    else if (getHString().equals("RIGHT"))
		    y++;
	}
	
	//Return the location of the cell in front of the robot, from the robots perspective
	public Location getCellInFront () {
        if (getHString().equals("LEFT"))
            return new Location(getX(), getY()-1, getH());
        else if (getHString().equals("RIGHT"))
            return new Location(getX(), getY()+1, getH());
        else if (getHString().equals("FORWARD"))
            return new Location(getX()+1, getY(), getH());
        else
            return new Location(getX()-1, getY(), getH());
    }
    
	//Return the location of the cell left of the robot, from the robots perspective
    public Location getCellToLeft () {
        if (getHString().equals("LEFT"))
            return new Location(getX()-1, getY(), getH());
        else if (getHString().equals("RIGHT"))
            return new Location(getX()+1, getY(), getH());
        else if (getHString().equals("FORWARD"))
            return new Location(getX(), getY()-1, getH());
        else
            return new Location(getX(), getY()+1, getH());
    }
    
	//Return the location of the cell right of the robot, from the robots perspective
    public Location getCellToRight () {
        if (getHString().equals("LEFT"))
            return new Location(getX()+1, getY(), getH());
        else if (getHString().equals("RIGHT"))
            return new Location(getX()-1, getY(), getH());
        else if (getHString().equals("FORWARD"))
            return new Location(getX(), getY()+1, getH());
        else
            return new Location(getX(), getY()-1, getH());
    }
}