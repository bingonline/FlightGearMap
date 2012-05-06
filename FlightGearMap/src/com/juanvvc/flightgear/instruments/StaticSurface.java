package com.juanvvc.flightgear.instruments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.juanvvc.flightgear.PlaneData;

public class StaticSurface extends Surface {
	Matrix m;
	public StaticSurface(String file, float x, float y) {
		super(file, x, y);
		m = null;
	}

	@Override
	public void onDraw(Canvas c, Bitmap b, PlaneData pd) {
		if (m == null) {
			m = new Matrix();
			final float gridSize = parent.getGridSize();
			final float scale = parent.getScale();
			final float col = parent.getCol();
			final float row = parent.getRow();
			m.setTranslate(col * gridSize * scale, row * gridSize * scale);
		}
		c.drawBitmap(b, m, null);
	}
}