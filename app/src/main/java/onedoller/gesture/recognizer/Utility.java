package onedoller.gesture.recognizer;

import java.util.Enumeration;
import java.util.Vector;

public class Utility extends Thread {

	public static double PathLength(Vector<RealPoint> path) {
		double length = 0;
		for (int i = 1; i < path.size(); i++) {
			// length += Distance((Point) points[i - 1], (Point) points[i]);
			length += RealPoint.Distance((RealPoint) path.elementAt(i - 1),
					(RealPoint) path.elementAt(i));
		}
		return length;
	}

	// computes the 'distance' between two point paths by summing their
	// corresponding point distances.
	// assumes that each path has been resampled to the same number of points at
	// the same distance apart.
	public static double PathDistance(Vector<RealPoint> path1, Vector<RealPoint> path2) {
		double distance = 0;
		for (int i = 0; i < path1.size(); i++) {
			distance += Distance((RealPoint) path1.elementAt(i),
					(RealPoint) path2.elementAt(i));
		}
		return distance / path1.size();
	}

	public static double Distance(RealPoint p1, RealPoint p2) {
		double dx = p2.x - p1.x;
		double dy = p2.y - p1.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	// compute the centroid of the points given
	public static RealPoint Centroid(Vector<RealPoint> points) {
		double xsum = 0.0;
		double ysum = 0.0;

		Enumeration<RealPoint> e = points.elements();

		// foreach (Point p in points)
		while (e.hasMoreElements()) {
			RealPoint p = (RealPoint) e.nextElement();
			xsum += p.x;
			ysum += p.y;
		}
		return new RealPoint(xsum / points.size(), ysum / points.size());
	}

	public static RealRect BoundingBox(Vector<RealPoint> points) {
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;

		Enumeration<RealPoint> e = points.elements();

		// foreach (Point p in points)
		while (e.hasMoreElements()) {
			RealPoint p = (RealPoint) e.nextElement();

			if (p.x < minX)
				minX = p.x;
			if (p.x > maxX)
				maxX = p.x;

			if (p.y < minY)
				minY = p.y;
			if (p.y > maxY)
				maxY = p.y;
		}
		return new RealRect(minX, minY, maxX - minX, maxY - minY);
	}

	public static void BoundingBox(Vector<RealPoint> points, RealRect dst) {
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;

		Enumeration<RealPoint> e = points.elements();

		// foreach (Point p in points)
		while (e.hasMoreElements()) {
			RealPoint p = (RealPoint) e.nextElement();

			if (p.x < minX)
				minX = p.x;
			if (p.x > maxX)
				maxX = p.x;

			if (p.y < minY)
				minY = p.y;
			if (p.y > maxY)
				maxY = p.y;
		}

		dst.x = minX;
		dst.y = minY;
		dst.Width = maxX - minX;
		dst.Height = maxY - minY;
	}

	public static Vector<RealPoint> RotateBy(Vector<RealPoint> points, double theta) {
		return RotateByRadians(points, theta);
	}

	// rotate the points by the given radians about their centroid
	public static Vector<RealPoint> RotateByRadians(Vector<RealPoint> points, double radians) {
		Vector<RealPoint> newPoints = new Vector<RealPoint>(points.size());
		RealPoint c = Centroid(points);

		double _cos = Math.cos(radians);
		double _sin = Math.sin(radians);

		double cx = c.x;
		double cy = c.y;

		for (int i = 0; i < points.size(); i++) {
			RealPoint p = (RealPoint) points.elementAt(i);

			double dx = p.x - cx;
			double dy = p.y - cy;

			newPoints.addElement(new RealPoint(dx * _cos - dy * _sin + cx, dx
					* _sin + dy * _cos + cy));
		}
		return newPoints;
	}
}
