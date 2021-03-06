package com.sidharth.notesquirrel;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;

public class Database extends SQLiteOpenHelper {

	public static String POINTS_TABLE = "POINTS";
	public static String COL_ID = "ID";
	public static String COL_X = "X";
	public static String COL_Y = "Y";
	
	public Database(Context context) {
		super(context, "notes.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = String
				.format("create table %s (%s INTEGER PRIMARY KEY, X INTEGER NOT NULL, Y INTEGER NOT NULL)",
						POINTS_TABLE,COL_ID,COL_X,COL_Y);
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public void storePoints(List<Point> points) {
		SQLiteDatabase db = getWritableDatabase();

		db.delete(POINTS_TABLE, null, null);

		ContentValues values = new ContentValues();

		int i = 0;
		for (Point point : points) {
			values.put(COL_ID, i);
			values.put(COL_X, point.x);
			values.put(COL_Y, point.y);
			db.insert(POINTS_TABLE, null, values);
			i++;
		}

		db.close();
	}
	
	public List<Point> getPoints()
	{
		List<Point> points= new ArrayList<Point>();
		SQLiteDatabase db = getReadableDatabase();
		
		String sql = String.format("select %s,%s from %s", COL_X,COL_Y,POINTS_TABLE);
		Cursor cursor = db.rawQuery(sql, null);
		while(cursor.moveToNext())
		{
			int x = cursor.getInt(0);
			int y = cursor.getInt(1);
			points.add(new Point(x,y));
		}
		db.close();
		return points;
	}
}
