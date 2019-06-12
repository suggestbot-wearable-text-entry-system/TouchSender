package helper;

import android.util.Log;

import onedoller.gesture.recognizer.Recognizer;
import onedoller.gesture.recognizer.Result;

public class SegmentAnalyser extends Object{

	public static final int NO_DRAG = 0;
	public static final int STAR = 1;
	public static final int V = 2;
	public static final int DELETE = 3;
	public static final int PIG_TAIL = 4;
	public static final int LINE = 5;

	public static final int up = 2;
	public static final int down = 6;
	public static final int right = 4;
	public static final int left = 0;
	public static final int up_left = 1;
	public static final int up_right =3;
	public static final int down_left = 7;
	public static final int down_right = 5;
	
	private boolean isStart;
	
	private Recognizer recog;
	 
	public SegmentAnalyser(){
		isStart = false;
		
		recog = new Recognizer(Recognizer.GESTURES_SEGMENT);
		recog.initialize();
	}

	public void addPoint(double x, double y){
		isStart = true;
		recog.addPoint(x, y);
	}
	
	public int analyze(){
		int state = NO_DRAG;
		if(isStart){
			recog.Recognize();
			Result result = recog.getResult();
			recog.initialize();
			Log.d("recog.", result.Name + " : " +result.Score);
			if(result.Score > 0.6){
				if(result.Name.compareTo("line") == 0){
					state = LINE;
				}
			}
			isStart = false;
		}
		return state;
	}
	
}
