package onedoller.gesture.recognizer;

import android.graphics.Rect;

public class RealRect extends Object{

	public double x, y, Width, Height;
	public double left,top,right,bottom;
	public double centerX, centerY;
	
	
	public RealRect(double X, double Y, double width, double height) // constructor
	{
		this.x = X;
		this.y = Y;
		this.Width = width;
		this.Height = height;
	}
	public RealRect(double l, double t, double r, double b, int a){
		left = l;
		top = t;
		right = r;
		bottom = b;
		centerX = (l+r)/2;
		centerY = (t+b)/2;
	}
	public void set(double l, double t, double r, double b ){
		left = l;
		top = t;
		right = r;
		bottom = b;
		centerX = (l+r)/2;
		centerY = (t+b)/2;
	}
	
	public void set(Rect r){
		left = r.left;
		right = r.right;
		top = r.top;
		bottom = r.bottom;
		centerX = r.centerX();
		centerY = r.centerY();
	}
	
	public void scale(double scaleFactor){
		left = centerX - (centerX - left)*scaleFactor;
		top = centerY - (centerY - top)*scaleFactor;
		right = centerX + (right - centerX)*scaleFactor;
		bottom = centerY + (bottom - centerY)*scaleFactor;
	}
	
	public void changeCenter(double cX, double cY){
		centerX = cX;
		centerY = cY;
	}
	
	public void moveCenter(double cX, double cY){
		double temp = cX - centerX;
		moveX(temp);
		temp = cY - centerY;
		moveY(temp);
	}
	
	
	
	public void moveX(double dx){
		left += dx;
		right += dx;
		centerX += dx;
	}
	
	public void moveY(double dy){
		top += dy;
		bottom += dy;
		centerY += dy;
	}
	public double centerX(){
		return centerX;
	}
	public double centerY(){
		return centerY;
	}
	
	public void copy(RealRect src)
	{
		x = src.x;
		y = src.y;
		Width = src.Width;
		Height = src.Height;	
		
		left = src.left;
		right = src.right;
		bottom = src.bottom;
		top = src.top;
		centerX = src.centerX();
		centerY = src.centerY();
	}
	
}
