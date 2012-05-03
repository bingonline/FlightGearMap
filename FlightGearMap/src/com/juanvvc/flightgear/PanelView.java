package com.juanvvc.flightgear;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Shows a small panel on the screen. This panel resize controls to the
 * available space.
 * 
 * @author juanvi
 * 
 */
public class PanelView extends View {

	/** Specifies the distribution type. */
	// TODO: change this to a enum
	public class Distribution {
		/** A 2x3 panel with simple instruments. */
		public static final int SIMPLE_VERTICAL_PANEL = 0;
		/** 6x1 panel with simple instruments. */
		public static final int HORIZONTAL_PANEL = 1;
		/** A 3x2 panel with simple instruments. */
		public static final int SIMPLE_HORIZONTAL_PANEL = 2;
		/** Show only the map. PanelView is not used. */
		public static final int ONLY_MAP = 3;
		/** Show a complete Cessna-172 instrument panel. */
		public static final int C172_INSTRUMENTS = 4;
		/** Show a complete Cessna-172 comm panel. */
		public static final int C172_COMM = 5;
	};

	/** Scaled to be applied to all sizes on screen. */
	private float scale = 0;
	/** Constant TAG to be used during development. */
	private static final String TAG = "PanelView";
	/** Plane data. */
	private PlaneData lastPlaneData = new PlaneData();
	/** The available instruments. */
	private ArrayList<Instrument> instruments;
	/** Number of columns in the panel. */
	private int cols;
	/** Number of rows in the panel. */
	private int rows;
	/** identifier of the current distribution. */
	private int distribution;

	/* Constructors */
	public PanelView(Context context) {
		super(context);
	}

	public PanelView(Context context, AttributeSet attrs) {
		super(context, attrs);

		instruments = new ArrayList<Instrument>();

		// Check the instrument distribution that was declared in the XML
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PanelView);
		int distribution = 0;
		for (int i = 0; i < a.getIndexCount(); i++) {
			switch (a.getIndex(i)) {
			case R.styleable.PanelView_distribution:
				distribution = a.getInt(i, 0);
				break;
			default:
			}
		}

		setDistribution(distribution);
	}
	
	/**
	 * @return The name of the most suitable image set.
	 */
	private String selectImageSet() {
		float minSize = Math.min(getWidth() * 1.0f / cols , getHeight() * 1.0f / rows);
		if (minSize > 400) {
			//return "high"; // not available to save space
			return "medium";
		} else if (minSize > 200) {
			return "medium";
		} else {
			return "low";
		}
	}

	/**
	 * Sets the distribution of this panel.
	 * 
	 * @param The
	 *            distribution to use
	 */
	public void setDistribution(int distribution) {
		
		myLog.v(TAG, "Loading distribution: " + distribution);

		Instrument.getBitmapProvider(this.getContext()).recycle();
		instruments.clear();

		Context context = getContext();
		switch (distribution) {
		case Distribution.SIMPLE_VERTICAL_PANEL:
			cols = 2;
			rows = 3;
			instruments.add(new Attitude(0, 0, context));
			instruments.add(new TurnSlip(1, 0, context));
			instruments.add(new OneHandInstrument(0, 1, context,new String[] {"speed.png", "hand1.png"}, 1, PlaneData.SPEED, 20, 200, 256, 256, 0, 0, 200, 320, -1, -1));
			instruments.add(new OneHandInstrument(1, 1, context,new String[] {"rpm.png", "hand1.png"}, 1, PlaneData.RPM, 20, 200, 256, 256, 0, -120, 3500, 120, -1, -1));
			instruments.add(new Altimeter(0, 2, context));
			instruments.add(new OneHandInstrument(1, 2, context,new String[] {"climb.png", "hand1.png"}, 1, PlaneData.CLIMB_RATE, 20, 200, 256, 256, -2000, -270, 2000, 90, -1, -1));

			break;
		case Distribution.SIMPLE_HORIZONTAL_PANEL:
			cols = 3;
			rows = 2;
			instruments.add(new OneHandInstrument(0, 0, context,new String[] {"speed.png", "hand1.png"}, 1, PlaneData.SPEED, 20, 200, 256, 256, 0, 0, 200, 320, -1, -1));
			instruments.add(new Attitude(1, 0, context));
			instruments.add(new Altimeter(2, 0, context));
			instruments.add(new TurnSlip(0, 1, context));
			instruments.add(new OneHandInstrument(1, 1, context,new String[] {"rpm.png", "hand1.png"}, 1, PlaneData.RPM, 20, 200, 256, 256, 0, -120, 3500, 120, -1, -1));
			instruments.add(new OneHandInstrument(2, 1, context,new String[] {"climb.png", "hand1.png"}, 1, PlaneData.CLIMB_RATE, 20, 200, 256, 256, -2000, -270, 2000, 90, -1, -1));

			break;
		case Distribution.HORIZONTAL_PANEL:
			cols = 6;
			rows = 1;
			instruments.add(new Attitude(0, 0, context));
			instruments.add(new TurnSlip(1, 0, context));
			instruments.add(new OneHandInstrument(2, 0, context,new String[] {"speed.png", "hand1.png"}, 1, PlaneData.SPEED, 20, 200, 256, 256, 0, 0, 200, 320, -1, -1));
			instruments.add(new OneHandInstrument(3, 0, context,new String[] {"rpm.png", "hand1.png"}, 1, PlaneData.RPM, 20, 200, 256, 256, 0, -120, 3500, 120, -1, -1));
			instruments.add(new Altimeter(4, 0, context));
			instruments.add(new OneHandInstrument(5, 0, context,new String[] {"climb.png", "hand1.png"}, 1, PlaneData.CLIMB_RATE, 20, 200, 256, 256, -2000, -270, 2000, 90, -1, -1));
			break;
		case Distribution.C172_INSTRUMENTS:
			cols = 5;
			rows = 3;
			instruments.add(new OneHandInstrument(1, 0, context,new String[] {"speed.png", "hand1.png"}, 1, PlaneData.SPEED, 20, 200, 256, 256, 0, 0, 200, 320, -1, -1));
			instruments.add(new Attitude(2, 0, context));
			instruments.add(new Altimeter(3, 0, context));
			instruments.add(new Nav(4, 0, context));
			instruments.add(new TurnSlip(1, 1, context));
			instruments.add(new OneHandInstrument(2, 1, context,new String[] {"hdg1.png", "hdg2.png"}, -1, -1, 20, 200, 256, 256, 0, 0, 0, 0, 0, PlaneData.HEADING));
			instruments.add(new OneHandInstrument(3, 1, context,new String[] {"climb.png", "hand1.png"}, 1, PlaneData.CLIMB_RATE, 20, 200, 256, 256, -2000, -270, 2000, 90, -1, -1));
			instruments.add(new Nav(4, 1, context));

			instruments.add(new OneHandInstrument(1, 2, context,new String[] {"rpm.png", "hand1.png"}, 1, PlaneData.RPM, 20, 200, 256, 256, 0, -120, 3500, 120, -1, -1));
			
			instruments.add(new Fuel(0.2f, 2, context));
			instruments.add(new Oil(0.2f, 1, context));
		default: // this includes Distribution.NO_MAP
		}
		
		
		// load the instruments. This could be in a different thread, but IN MY
		// DEVICES, loading does not take long
		for (Instrument i : instruments) {
			try {
				i.loadImages(this.selectImageSet());
			} catch (OutOfMemoryError e) {
				// if out of memory, try forcing the low quality version
				try {
					i.loadImages(BitmapProvider.LOW_QUALITY);
				} catch (Exception e2) {
					myLog.e(TAG, "Cannot load instruments: " + myLog.stackToString(e2));
				}
			} catch (Exception e) {
				myLog.e(TAG, "Cannot load instrument: " + myLog.stackToString(e));
			}
		}
		this.rescaleInstruments();
	
		this.distribution = distribution;
	}
	
	/** @return The currently displayed distribution. */
	public int getDistribution() {
		return this.distribution;
	}

	/**
	 * Rescale bitmaps. Call after a new size is detected and at the beginning
	 * of the execution.
	 */
	private void rescaleInstruments() {
		if (getWidth() > 0) {
			// scale to match the available size. All instrumewnts should be
			// visible.
			scale = Math.min(
					1.0f * getWidth() / (cols * Instrument.getGridSize()),
					1.0f * getHeight()/ (rows * Instrument.getGridSize()));
			myLog.d(TAG, "Scale: " + scale);

			// prevent spurious scales
			// if (Math.abs(scale - 1) < 0.1) {
			// scale = 1;
			// }

			Instrument.getBitmapProvider(this.getContext()).setScale(scale);
			for (Instrument i: instruments) {
				i.setScale(scale);
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// reload and rescale images
		setDistribution(distribution);
	}

	/**
	 * @param pd
	 *            The last received PlaneData
	 */
	public void setPlaneData(PlaneData pd) {
		this.lastPlaneData = pd;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(distribution == Distribution.ONLY_MAP) {
			return;
		}
		
		super.onDraw(canvas);

		for (Instrument i : instruments) {
			try {
				i.onDraw(canvas, lastPlaneData);
			} catch(Exception e) {
				myLog.e(TAG, myLog.stackToString(e));
			}
		}
	}
}