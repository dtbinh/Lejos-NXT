/* Author: Guangpeng Li
 * University of Liverpool
 * Date: 13/11/2014
 * 
 * This is a storage to store the current location
 * of the robot in the grid map.
 */
class Location {
    public final int X;
    public final int Y;
	public final double H;
	/*
	 * Constructor	
	 */
	public Location (int X, int Y, double H) {
	    this.X = X;
		this.Y = Y;
		this.H = H;
	}
	// Compare the robot location
	public boolean equals (Location rhs) {
	    return rhs.X == X && rhs.Y == Y && rhs.H == H;
	}
}