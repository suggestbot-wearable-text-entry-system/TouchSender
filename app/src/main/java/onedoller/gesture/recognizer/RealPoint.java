package onedoller.gesture.recognizer;


public class RealPoint extends Object{
	public double x;
	public double y;
	
	public void copy(RealPoint p){
		x = p.x;
		y = p.y;
	}
	public RealPoint(RealPoint p){
		x = p.x;
		y = p.y;
	}
	public RealPoint(double x_val, double y_val){
		x = x_val;
		y = y_val;		
	}
	

	public static double Distance(RealPoint p1, RealPoint p2)
	{
		double dx = p2.x - p1.x;
		double dy = p2.y - p1.y;
		return Math.sqrt(dx * dx + dy * dy);
	}
	
}