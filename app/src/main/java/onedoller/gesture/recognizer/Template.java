package onedoller.gesture.recognizer;

import java.util.Vector;


public class Template extends Object
{
	public String Name;
	public Vector<RealPoint> Points;
	
	Template(String name, Vector<RealPoint> points) 
	{
		this.Name = name;
		
		this.Points = Recognizer.Resample(points, Recognizer.NumPoints);
		this.Points = Recognizer.RotateToZero(this.Points);
		this.Points = Recognizer.ScaleToSquare(this.Points, Recognizer.SquareSize);
		this.Points = Recognizer.TranslateToOrigin(this.Points);		
	}
}