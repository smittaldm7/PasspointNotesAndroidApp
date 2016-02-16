package com.sidharth.notesquirrel;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class PointCollector implements OnTouchListener
{

	
		private static final String DEBUGTAG = "SM";
		List<Point> points = new ArrayList<Point>();
		private PointCollectorListener listener;
		
		public void setListener(PointCollectorListener listener) {
			this.listener = listener;
		}
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int x= Math.round(event.getX());
			int y= Math.round(event.getY());
			String message = String.format("coordinates (%d, %d) ",x,y);
			Log.d(DEBUGTAG,message);
			points.add(new Point(x,y));
			if(points.size()==4)
			{
				listener.pointsCollect(points);
			}
			return false;
		}
		public void clear()
		{
			points.clear();
		}
}
