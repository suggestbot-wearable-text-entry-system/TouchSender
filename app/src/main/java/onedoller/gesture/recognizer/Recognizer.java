package onedoller.gesture.recognizer;

import java.util.Vector;

public class Recognizer extends Utility {

	private Vector<RealPoint> sampledPath;

	private int NumTemplates = 16;
	public static double Phi = 0.5 * (-1.0 + Math.sqrt(5.0)); // Golden Ratio
	public static int NumPoints = 64;
	public static double SquareSize = 800.0;
	double HalfDiagonal = 0.5 * Math.sqrt(SquareSize* SquareSize*2);
	double AngleRange = 45.0;
	double AnglePrecision = 2.0;

	private Result result = new Result("",-1,-1);
	
	
	public RealPoint centroid = new RealPoint(0, 0);
	public RealRect boundingBox = new RealRect(0, 0, 0, 0);
	int bounds[] = { 0, 0, 0, 0 };
	
	
	Vector<Template> Templates = new Vector<Template>(NumTemplates);

	public static final int GESTURES_DEFAULT = 1;
	public static final int GESTURES_SIMPLE = 2;
	public static final int GESTURES_CIRCLES = 3;
	public static final int GESTURES_SEGMENT = 4;	

	

	public Recognizer()
	{
		this(GESTURES_SIMPLE);
		sampledPath = new Vector<RealPoint>(1000);
	}

	public Recognizer(int gestureSet)
	{
		switch(gestureSet)
		{
			case GESTURES_DEFAULT:
				loadTemplatesDefault(); break;

			case GESTURES_SIMPLE:
				loadTemplatesSimple();	break;

			case GESTURES_CIRCLES:
				loadTemplatesCircles();	break;
			case GESTURES_SEGMENT:
				loadTemplatesSegment();	break;
		}
		sampledPath = new Vector<RealPoint>(1000);
	}
	
	
	public int size(){
		return sampledPath.size();
	}

	public RealPoint getPointAt(int i) {
		return sampledPath.get(i);
	}

	public double getPointX_At(int i) {
		return sampledPath.get(i).x;
	}

	public double getPointY_At(int i) {
		return sampledPath.get(i).y;
	}

	public void addPoint(double x, double y) {
		sampledPath.add(new RealPoint(x, y));
	}

	public void initialize() {
		sampledPath.clear();
	}


	void loadTemplatesDefault()
	{
		Templates.addElement(loadTemplate("circle CCW", TemplateData.circlePointsCCW));
		Templates.addElement(loadTemplate("check", TemplateData.checkPoints));
		Templates.addElement(loadTemplate("caret CW", TemplateData.caretPointsCW));
		Templates.addElement(loadTemplate("delete", TemplateData.deletePoints));	
		Templates.addElement(loadTemplate("star", TemplateData.starPoints));
		Templates.addElement(loadTemplate("pigTail", TemplateData.pigTailPoints));
	}
	
	void loadTemplatesSimple()
	{
		Templates.addElement(loadTemplate("circle CCW", TemplateData.circlePointsCCW));
		Templates.addElement(loadTemplate("circle CW", TemplateData.circlePointsCW));
		Templates.addElement(loadTemplate("rectangle CCW", TemplateData.rectanglePointsCCW));
		Templates.addElement(loadTemplate("rectangle CW", TemplateData.rectanglePointsCW));
		Templates.addElement(loadTemplate("caret CCW", TemplateData.caretPointsCCW));
		Templates.addElement(loadTemplate("caret CW", TemplateData.caretPointsCW));
	}
	

	void loadTemplatesSegment()
	{
		Templates.addElement(loadTemplate("circle CCW", TemplateData.circlePointsCCW));
		Templates.addElement(loadTemplate("circle CW", TemplateData.circlePointsCW));
		Templates.addElement(loadTemplate("line", TemplateData.linePoints));
		Templates.addElement(loadTemplate("star", TemplateData.starPoints));
		//Templates.addElement(loadTemplate("v", TemplateData.vPoints));
		Templates.addElement(loadTemplate("delete", TemplateData.deletePoints));
		//Templates.addElement(loadTemplate("pigTail", TemplateData.pigTailPoints));
		
	}
	
	void loadTemplatesCircles()
	{
		Templates.addElement(loadTemplate("circle CCW", TemplateData.circlePointsCCW));
		Templates.addElement(loadTemplate("circle CW", TemplateData.circlePointsCW));
	}
	
	Template loadTemplate(String name, int[] array)
	{
		return new Template(name, loadArray(array));
	}
	
	Vector<RealPoint> loadArray(int[] array)
	{
		Vector<RealPoint> v = new Vector<RealPoint>(array.length/2);
		for (int i = 0; i < array.length; i+= 2)
		{
			RealPoint p = new RealPoint(array[i], array[i+1]);
			v.addElement(p);
		}
		
	//	System.out.println(v.size() + " " + array.length);
	
		return v;
	}

	int AddTemplate(String name, Vector<RealPoint> points)
	{
		Templates.addElement(new Template(name, points));
		return Templates.size();
	}
	
	int DeleteUserTemplates()
	{
		for (int i = Templates.size()-NumTemplates; i > 0; i--)
		{
			Templates.removeElementAt(Templates.size()-1);
		}
		
		return Templates.size();
	}

	
	public static Vector<RealPoint> Resample(Vector<RealPoint> points, int n) {
		double I = PathLength(points) / (n - 1); // interval length
		double D = 0.0;

		Vector<RealPoint> srcPts = new Vector<RealPoint>(points.size());
		for (int i = 0; i < points.size(); i++)
			srcPts.addElement(points.elementAt(i));

		Vector<RealPoint> dstPts = new Vector<RealPoint>(n);
		if(srcPts.size() > 0){
			dstPts.addElement(srcPts.elementAt(0)); // assumes that srcPts.size() >
												// 0
	
			for (int i = 1; i < srcPts.size(); i++) {
				RealPoint pt1 = (RealPoint) srcPts.elementAt(i - 1);
				RealPoint pt2 = (RealPoint) srcPts.elementAt(i);
				
				
				double d = RealPoint.Distance(pt1, pt2);
				if ((D + d) >= I) {
					double qx = pt1.x + ((I - D) / d) * (pt2.x - pt1.x);
					double qy = pt1.y + ((I - D) / d) * (pt2.y - pt1.y);
					RealPoint q = new RealPoint(qx, qy);
					dstPts.addElement(q); // append new point 'q'
					srcPts.insertElementAt(q, i); // insert 'q' at position i in
													// points s.t. 'q' will be the
													// next i
					D = 0.0;
				} else {
					D += d;
				}
			}
			// somtimes we fall a rounding-error short of adding the last point, so
			// add it if so
			if (dstPts.size() == n - 1) {
				dstPts.addElement(srcPts.elementAt(srcPts.size() - 1));
			}
		}
		return dstPts;
	}

	public static Vector<RealPoint> RotateToZero(Vector<RealPoint> points)
	{	return RotateToZero(points, null, null);	}

	
	public static Vector<RealPoint> RotateToZero(Vector<RealPoint> points, RealPoint centroid, RealRect boundingBox)
	{
		RealPoint c = Centroid(points);
		RealPoint first = (RealPoint)points.elementAt(0);
		//double theta = Trigonometric.atan2(c.y - first.y, c.x - first.x);
		double theta = Math.atan2(c.y - first.y, c.x - first.x);
		
		if (centroid != null)
			centroid.copy(c);
		
		if (boundingBox != null)
			BoundingBox(points, boundingBox);
		
		return RotateBy(points, -theta);
	}		
	

	public static Vector<RealPoint> ScaleToSquare(Vector<RealPoint> points, double size)
	{
		return ScaleToSquare(points, size, null);
	}				

	public static Vector<RealPoint> ScaleToSquare(Vector<RealPoint> points, double size, RealRect boundingBox)
	{
		RealRect B = BoundingBox(points);
		Vector<RealPoint> newpoints = new Vector<RealPoint>(points.size());
		for (int i = 0; i < points.size(); i++)
		{
			RealPoint p = (RealPoint)points.elementAt(i);
			double qx = p.x * (size / B.Width);
			double qy = p.y * (size / B.Height);
			newpoints.addElement(new RealPoint(qx, qy));
		}
		
		if (boundingBox != null) //this will probably not be used as we are more interested in the pre-rotated bounding box -> see RotateToZero
			boundingBox.copy(B);
		
		return newpoints;
	}			
	
	public static Vector<RealPoint> TranslateToOrigin(Vector<RealPoint> points)
	{
		RealPoint c = Centroid(points);
		Vector<RealPoint> newpoints = new Vector<RealPoint>(points.size());
		for (int i = 0; i < points.size(); i++)
		{
			RealPoint p = (RealPoint)points.elementAt(i);
			double qx = p.x - c.x;
			double qy = p.y - c.y;
			newpoints.addElement(new RealPoint(qx, qy));
		}
		return newpoints;
	}			
	
	

	public static double DistanceAtBestAngle(Vector<RealPoint> points, Template T, double a, double b, double threshold)
	{
	
		double x1 = Phi * a + (1.0 - Phi) * b;
		double f1 = DistanceAtAngle(points, T, x1);
		double x2 = (1.0 - Phi) * a + Phi * b;
		double f2 = DistanceAtAngle(points, T, x2);
		
		while (Math.abs(b - a) > threshold)
		{
			if (f1 < f2)
			{
				b = x2;
				x2 = x1;
				f2 = f1;
				x1 = Phi * a + (1.0 - Phi) * b;
				f1 = DistanceAtAngle(points, T, x1);
			}
			else
			{
				a = x1;
				x1 = x2;
				f1 = f2;
				x2 = (1.0 - Phi) * a + Phi * b;
				f2 = DistanceAtAngle(points, T, x2);
			}
		}
		return Math.min(f1, f2);
	}			

	public static double DistanceAtAngle(Vector<RealPoint> points, Template T, double theta)
	{
		Vector<RealPoint> newpoints = RotateBy(points, theta);
		return PathDistance(newpoints, T.Points);
	}		
	public void Recognize() {
		if(NumPoints > 10){
			sampledPath = Resample(sampledPath, NumPoints);		
			sampledPath = RotateToZero(sampledPath, centroid, boundingBox);
			sampledPath = ScaleToSquare(sampledPath, SquareSize);
			sampledPath = TranslateToOrigin(sampledPath);
		
			bounds[0] = (int)boundingBox.x;
			bounds[1] = (int)boundingBox.y;
			bounds[2] = (int)boundingBox.x + (int)boundingBox.Width;
			bounds[3] = (int)boundingBox.y + (int)boundingBox.Height;
			
			int t = 0;
			
			double b = Double.MAX_VALUE;
			for (int i = 0; i < Templates.size(); i++)
			{
				double d = DistanceAtBestAngle(sampledPath, (Template)Templates.elementAt(i), -AngleRange, AngleRange, AnglePrecision);
				if (d < b)
				{
					b = d;
					t = i;
				}
			}
			double score = 1.0 - (b / HalfDiagonal);
			
			//Log.d("Recognizig Result",Templates.elementAt(t).Name +  "  "+score+"  "+ t);
			
			result.Name = (String)Templates.elementAt(t).Name;
			result.Score = score;
			result.Index = t;
		}

	}
	public Result getResult(){
		return result;
	}

	
	
}
