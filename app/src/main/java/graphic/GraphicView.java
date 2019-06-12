package graphic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import hcil.kaist.touchsender.MainActivity;
import hcil.kaist.touchsender.R;
import helper.InputManager;

@SuppressLint("ShowToast")
public class GraphicView extends View {
	private final char TOUCH_STATE_DOWN = 'd';
	private final char TOUCH_STATE_MOVE = 'm';
	private final char TOUCH_STATE_UP = 'u';

	private char touchState;
	private float touchX, touchY;
	private float touchDownX, touchDownY;
	private Paint touchCirclePaint;
	private Paint touchCenterPaint;

	private int xDir, yDir;
	private int dragThreshold; // dragThreshold is used to distinguish a swipe gesture, to distinguish a swipe gesture, we use two params. drag distance and drag speed
	private boolean isMulti;

	private String packet;
	Context c;

	private InputManager inputManager;
	private Handler mainHandler;
	private CountDownTimer cdt;
	private boolean isTouchSendable;
	Drawable background;
	private final double angleFactor = (double) 180/Math.PI;

	private long touchDownTime;
	public GraphicView(Context paramContext) {
		super(paramContext);
	}

	public GraphicView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
	}

	public GraphicView(Context paramContext, AttributeSet paramAttributeSet,
					   int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
	}

	public void initialize(Context context, Handler h) {
		c = context;
		packet = "";
		mainHandler = h;
		inputManager = new InputManager(c, mainHandler);
		touchState = TOUCH_STATE_UP;
		this.setBackgroundColor(Color.DKGRAY);
		touchCirclePaint = new Paint();
		touchCirclePaint.setColor(Color.BLACK);
		touchCirclePaint.setStyle(Paint.Style.STROKE);
		touchCenterPaint = new Paint();
		touchCenterPaint.setColor(Color.WHITE);
		touchCenterPaint.setStyle(Paint.Style.FILL);

		touchCirclePaint.setStrokeWidth(10);


		background = getResources().getDrawable(R.drawable.background);
		background.setBounds(0, 0, 280, 280);


		dragThreshold = 10;
		cdt = new CountDownTimer(50001,50) {
			@Override
			public void onTick(long millisUntilFinished) {
				isTouchSendable = true;
			}

			@Override
			public void onFinish() {
				cdt.start();
			}
		};

		isMulti = false;
	}

	public void drawSelecting(Canvas c) {


	}

	@Override
	public void onDraw(Canvas canvas) {
		//background.draw(canvas);
		switch (touchState) {
			case TOUCH_STATE_DOWN:
				canvas.drawCircle(touchX,touchY,5,touchCenterPaint);
				canvas.drawCircle(touchX,touchY,70,touchCirclePaint);
				break;
			case TOUCH_STATE_MOVE:
				canvas.drawCircle(touchX,touchY,5,touchCenterPaint);
				canvas.drawCircle(touchX,touchY,70,touchCirclePaint);
				break;
		}
	}

	@SuppressLint("NewApi")
	public boolean onTouchEvent(MotionEvent event) {
		touchX = (int) event.getAxisValue(MotionEvent.AXIS_X);
		touchY = (int) event.getAxisValue(MotionEvent.AXIS_Y);
		float tempX = 280- touchY;
		float tempY =touchX;


		switch (event.getAction() & MotionEvent.ACTION_MASK){
			case MotionEvent.ACTION_POINTER_DOWN:
				break;
			case MotionEvent.ACTION_DOWN:
				touchDownTime = System.currentTimeMillis();
				 packet = "x " + (int)(tempX) + " " + (int)((tempY)) + "\r\n";
				mainHandler.obtainMessage(MainActivity.MESSAGE_WRITE_DOWN,packet.getBytes()).sendToTarget();
				isTouchSendable = false;
				cdt.start();
				touchCirclePaint.setColor(Color.CYAN);
				touchState = TOUCH_STATE_DOWN;
				touchDownX = tempX;
				touchDownY = tempY;

				mainHandler.obtainMessage(MainActivity.MESSAGE_VIBRATE, 30, 30).sendToTarget();
				postInvalidate();


				break;
			case MotionEvent.ACTION_MOVE:
				//cdt.cancel();
				if(isTouchSendable) {
					isTouchSendable = false;
					packet = "z " + (int)(tempX) + " " +  (int)((tempY)) + "\r\n";
					mainHandler.obtainMessage(MainActivity.MESSAGE_WRITE_MOVE, packet.getBytes()).sendToTarget();
					touchCirclePaint.setColor(Color.rgb(255, 155, 79));
					touchState = TOUCH_STATE_MOVE;
					postInvalidate();
					cdt.start();
				}

				break;
			case MotionEvent.ACTION_POINTER_UP:
				isMulti = true;
				break;
			case MotionEvent.ACTION_UP:
				cdt.cancel();
				long touchTime = System.currentTimeMillis() - touchDownTime;
				isTouchSendable = false;
				mainHandler.obtainMessage(MainActivity.MESSAGE_VIBRATE, 30, 30).sendToTarget();
				if(!isMulti) {
					xDir = 0;
					yDir = 0;
					int xDir = (int) (touchDownX - tempX);
					int yDir = (int) (touchDownY - tempY);
					int len = (int) Math.sqrt(xDir * xDir + yDir * yDir);
					int speed;
					if (touchTime > 0) {
						speed = (int) (len * 1000 / touchTime);
					} else {
						speed = 0;
					}

					//to detect swiping gesture and its direction.
					Log.d("speed", packet + speed + " " + len + " / " + dragThreshold);
					if (len > dragThreshold && speed > 400) {
						double angle = Math.acos((double) xDir / len) * angleFactor;
						if (yDir < 0) {
							angle = 360 - angle;
						}
						angle += 45;
						int id = (int) (angle / 90);
						if (id > 3) {
							id = 0;
						}
						packet = "s " + id + "\r\n";

						mainHandler.obtainMessage(MainActivity.MESSAGE_WRITE_UP, packet.getBytes()).sendToTarget();

					} else {
						packet = "v " + (0) + " " + (0) + "\r\n";
						mainHandler.obtainMessage(MainActivity.MESSAGE_WRITE_UP, packet.getBytes()).sendToTarget();
					}
					//Log.d("TOUCH", "ACTION_UP");
					touchState = TOUCH_STATE_UP;

					postInvalidate();
				}else{

					packet = "s " + 9 + "\r\n";
					mainHandler.obtainMessage(MainActivity.MESSAGE_WRITE_UP, packet.getBytes()).sendToTarget();
					mainHandler.obtainMessage(MainActivity.MESSAGE_VIBRATE, 30, 30).sendToTarget();
					isTouchSendable = false;
					touchState = TOUCH_STATE_UP;
				}
				isMulti = false;
				break;
		}
		return true;
	}

	public Handler getInputHandler() {
		return inputManager.getHandler();
	}

}
