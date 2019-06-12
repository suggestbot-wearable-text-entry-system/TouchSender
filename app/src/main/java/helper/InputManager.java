package helper;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;

import hcil.kaist.touchsender.MainActivity;

public class InputManager extends Thread{

	Context c;
	private Handler mHandler;
	private Handler mainHandler;

	private float downX, downY;
	

	static public final int FROM_BASE = 0;
	static public final int FROM_GRAPHICVIEW = 1;

	
	
	
	private String readMessage;


	private SegmentAnalyser SA;

	
	public InputManager(Context context, Handler h){
		c = context;
		initHandler();
		SA = new SegmentAnalyser();

		mainHandler = h;
		
	}

	public void setHandler(Handler h){
		mainHandler = h;
	}
	
	public void vibrate(int msec){
		mainHandler.obtainMessage(MainActivity.MESSAGE_VIBRATE, msec, msec).sendToTarget();
	}
	
	public void initHandler(){
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				switch(msg.what){
				case FROM_BASE:

	                byte[] readBuf = (byte[]) msg.obj;
	        		readMessage = new String(readBuf, 0, msg.arg1);
	                
	        		//Log.d("data", readMessage);

	                String[] str = readMessage.split(" ");

	                try{
	        	        for(int i = 0; i < str.length ; i++){
	        	        	if(str[i] != null && str[i].length() > 0){
		        	        	switch(str[i].charAt(0)){
		        	        	case 'z':
		        	        		Log.d("receive","z");
									mainHandler.obtainMessage(MainActivity.MESSAGE_VIBRATE, 30, 30)
									.sendToTarget();
		        	        		break;
		        	        	case 'y':
		        	        		Log.d("receive","y");
									mainHandler.obtainMessage(MainActivity.MESSAGE_PHASE, 0, -1)
									.sendToTarget();
		        	        		break;

		        	        	case 'x':
									mainHandler.obtainMessage(MainActivity.MESSAGE_END, 0, -1)
									.sendToTarget();
		        	        		Log.d("receive","x");
		        	        		
		        	        		break;
		        	        	}
	        	        	}
	        	        }
	        	        
	                }
	                catch(NumberFormatException e){
	                }
	                
					break;
				case FROM_GRAPHICVIEW:
					MotionEvent event = (MotionEvent)msg.obj;
					switch(event.getAction()){
					case MotionEvent.ACTION_DOWN:
						downX = event.getX();
						downY = event.getY();
						break;
					case MotionEvent.ACTION_MOVE:
						SA.addPoint(event.getX(), event.getY());
						break;
					case MotionEvent.ACTION_UP:

						switch(SA.analyze()){
						case SegmentAnalyser.LINE:
							int tempX = (int)(downX - event.getX());
							int tempY = (int)(downY - event.getY());
							int direction = -1;
							if(Math.abs(tempX) < Math.abs(tempY)){
								if(tempY < 0){
									direction = SegmentAnalyser.down;
								}else{
									direction = SegmentAnalyser.up;
								}
							}else{
								if(tempX < 0){
									direction = SegmentAnalyser.right;
								}else{
									direction = SegmentAnalyser.left;
								}
							}
							mainHandler.obtainMessage(MainActivity.MESSAGE_TOAST2, direction, SegmentAnalyser.LINE).sendToTarget();
							break;
						}
						break;
					}
					break;
				}
			}
		};
	}
	
	public Handler getHandler(){
		return mHandler;
	}
	
	@Override
	public void run(){


	}
	
}
