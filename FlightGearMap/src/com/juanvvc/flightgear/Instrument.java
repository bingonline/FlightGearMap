package com.juanvvc.flightgear;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

/** A generic instrument.
 * This class manages the list of resources that instruments need, and
 * specifically two generic hands, and takes care of scaling images.
 * @author juanvi
 */
public abstract class Instrument {
	/** Position of the instrument in the canvas. Unscaled. */
	float x;
	/** Position of the instrument in the canvas. Unscaled. */
	float y;
	/** Scale all positions with this value! */
	float scale;
	/** The context of the application. */
	Context context;
	/** Name of the images to load. Use names in the AssetManager.
	 * Two names are automatically added in the constructor: hand1 and hand2,
	 * and they are at positions 0 and 1.
	 * Then, your fist image will be at position 2,  your second
	 * image at position 3 and so on.
	 */
	ArrayList<String> imgFiles;
	/** The unscaled Bitmaps of your instruments. */
	ArrayList<Bitmap> imgs;
	/** The scaled bitmaps of your instruments. */
	ArrayList<Bitmap> imgsScaled;
	
	/** We know that instruments are squares 512x512.
	 * This is a convenience constant: for me, it is more
	 * confortable to think in "half size" instruments.
	 */
	public static final int SEMICLOCKSIZE = 256;
	/** Hands are vertical bitmaps and the axis is at HAND_CENTERX, HAND_CENTERY. */
	public static final int HAND_CENTERX = 20;
	/** Hands are vertical bitmaps and the axis is at HAND_CENTERX, HAND_CENTERY. */
	public static final int HAND_CENTERY = 200;
	/** Position of the large hand in the internal ArrayLists. */
	static final int HAND1 = 0;
	/** Position of the small hand in the internal ArrayLists. */
	static final int HAND2 = 1;
	/** If true, the instrument is ready to be drawn.
	 * An instrument is ready when its bitmaps are loaded.
	 */
	boolean ready = false;
	
	/** Constructor. Call this constructor always from your extended classes!
	 * @param x The x position of the instrument
	 * @param y The y position of the instrument
	 * @param c A reference to the context of the application
	 */
	public Instrument(float x, float y, Context c) {
		this.x = x;
		this.y = y;
		context = c;
		scale = 1; // we begin unscaled
		imgFiles = new ArrayList<String>();
		imgs = new ArrayList<Bitmap>();
		imgsScaled = new ArrayList<Bitmap>();
		// automatically added
		imgFiles.add("hand1.png");
		imgFiles.add("hand2.png");
		ready = false;
	}
	
	/** Loads the images in imgFiles.
	 * @throws Exception If the images cannot be loaded.
	 */
	public void loadImages() throws Exception {
		AssetManager mng = context.getAssets();
		for(String f: this.imgFiles) {
			imgs.add(BitmapFactory.decodeStream(mng.open(f)));
		}
		setScale(scale);
		
		ready = true;
	}
	
	/** Sets the scale of the instrument to fill the screen.
	 * @param s The scale of the instrument. 1=original size (sides of 2xSEMICLOCKSIZE)
	 */
	public void setScale(float s) {
		scale = s;
		Matrix matrix = new Matrix();
		matrix.setScale(scale,  scale);
		for(Bitmap b: this.imgsScaled) {
			b.recycle();
		}
		imgsScaled.clear();
		for(Bitmap b: this.imgs) {
			imgsScaled.add(Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true));
		}
	}
	
	/** Draw the instrument on the canvas.
	 * 
	 * @param c The current Canvas
	 * @param pd The current value of the plane information
	 */
	public abstract void onDraw(Canvas c, PlaneData pd);
}

class Altimeter extends Instrument {
	public Altimeter(float x, float y, Context c) {
		super(x, y, c);
		// load the background of the altimeter, in addition to the hands
		// remember: the background is gong to be at position 2
		this.imgFiles.add("alt1.png");
	}

	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(x * scale, y * scale);
		// remember: the background is gong to be at position 2, since hands are at 0 and 1
		c.drawBitmap(imgsScaled.get(2), matrix, null);

		double alt2Angle = ((pd.getAltitude() / 1000) * 360 / 10);
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - HAND_CENTERX) * scale, (y + SEMICLOCKSIZE - HAND_CENTERY) * scale);
		matrix.postRotate((float) alt2Angle, (x + SEMICLOCKSIZE ) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(imgsScaled.get(HAND2), matrix, null);
		double alt1Angle = ((pd.getAltitude() % 1000) * 360 / 1000);
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - HAND_CENTERX) * scale, (y + SEMICLOCKSIZE - HAND_CENTERY) * scale);
		matrix.postRotate((float) alt1Angle, (x + SEMICLOCKSIZE ) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(imgsScaled.get(HAND1), matrix, null);			
	}
}

class Attitude extends Instrument {
	public Attitude(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("ati0.png");
		this.imgFiles.add("ati1.png");
		this.imgFiles.add("ati2.png");
		this.imgFiles.add("ati3.png");
	}
	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(2), matrix, null);
		
		Bitmap ati1 = imgs.get(3);
		Bitmap ati1Scaled = imgsScaled.get(3);
		Bitmap ati2 = imgs.get(4);
		Bitmap ati2Scaled = imgsScaled.get(4);
		
		
		// draw pitch
		matrix.reset();
		// translate 23 pixels each 5 degrees
		matrix.postTranslate((x + SEMICLOCKSIZE - ati1.getWidth() / 2) * scale, ( y + SEMICLOCKSIZE - ati1.getHeight() / 2 + pd.getPitch() / 5 * 23) * scale);
		matrix.postRotate(-pd.getRoll(), (x + SEMICLOCKSIZE) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(ati1Scaled, matrix, null);
		// draw roll
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - ati2.getWidth() / 2) * scale, (y + SEMICLOCKSIZE - ati2.getHeight() / 2) * scale);
		matrix.postRotate(-pd.getRoll(), (x + SEMICLOCKSIZE) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(ati2Scaled, matrix, null);
		
		matrix.reset();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(5), matrix, null);
	}
}

class Speed extends Instrument {
	public Speed(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("speed.png");
	}
	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(2), matrix, null);
		
		// climb speed
		// 40kts = 30º, 200kts = 320º
		double speedAngle = (pd.getSpeed() - 40) * 290 / 160  + 30;
		if (speedAngle < 0) {
			speedAngle = 0;
		} else if (speedAngle > 330) {
			speedAngle = 330;
		}
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - HAND_CENTERX) * scale, (y + SEMICLOCKSIZE - HAND_CENTERY) * scale);
		matrix.postRotate((float) speedAngle, (x + SEMICLOCKSIZE) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(imgsScaled.get(HAND1), matrix, null);
	}
}

class RPM extends Instrument {
	public RPM(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("rpm.png");
	}
	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(2), matrix, null);
		
		// rpm
		double rpmAngle = ((pd.getRPM() / 100) * HAND_CENTERY / 25) - (35 * 90) / 25;
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - HAND_CENTERX) * scale, (y + SEMICLOCKSIZE - HAND_CENTERY) * scale);
		matrix.postRotate((float) rpmAngle, (x + SEMICLOCKSIZE) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(imgsScaled.get(HAND1), matrix, null);
	}
}

class ClimbRate extends Instrument {
	public ClimbRate(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("climb.png");
	}
	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(2), matrix, null);
		
		// climb speed
		double climbAngle = (pd.getRate() * 180 / 2000) - 90;
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - HAND_CENTERX) * scale, (y + SEMICLOCKSIZE - HAND_CENTERY) * scale);
		matrix.postRotate((float) climbAngle, (x + SEMICLOCKSIZE) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(imgsScaled.get(HAND1), matrix, null);
	}
}

class TurnSlip extends Instrument {
	public TurnSlip(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("trn0.png");
		this.imgFiles.add("trn1.png");
		this.imgFiles.add("slip.png");
		this.imgFiles.add("turn.png");
	}
	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(2), matrix, null);
		
		Bitmap slip = imgs.get(4);
		Bitmap slipScaled = imgsScaled.get(4);
		
		// draw slip skid
		matrix.reset();
		// (0.7*SEMICLOSEWIDTH is calibrated with on screen instruments with FG sim)
		matrix.setTranslate((x + (1 - 0.7f * pd.getSlipSkid()) * SEMICLOCKSIZE - slip.getWidth() / 2 ) * scale, (y + 1.3f * SEMICLOCKSIZE - slip.getHeight() / 2) * scale);
		c.drawBitmap(slipScaled, matrix, null);
		
		matrix.reset();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(3), matrix, null);
		
		Bitmap turn = imgs.get(5);
		Bitmap turnScaled = imgsScaled.get(5);
		
		// turn rate
		// 20º means a turn rate = 1 turn each 2 minuts
		double turnAngle = (pd.getTurnRate() * 20);
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - turn.getWidth() / 2) * scale, (y + SEMICLOCKSIZE - turn.getHeight() / 2) * scale);
		matrix.postRotate((float) turnAngle, (x + SEMICLOCKSIZE) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(turnScaled, matrix, null);
	}
}