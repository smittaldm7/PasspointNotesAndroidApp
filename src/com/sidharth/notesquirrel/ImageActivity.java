package com.sidharth.notesquirrel;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageActivity extends Activity implements PointCollectorListener {

	private static final int POINT_CLOSENESS=50;
	public static final String DEBUGTAG = "SM";
	public static final String PASSPOINTSSAVED = "PasspointsSaved";
	public static final String RESETPASSPOINTS = MainActivity.RESETPASSPOINTS;
	public static final String PREFERENCESFILE = MainActivity.PREFERENCESFILE;
	public static final String PASSPOINT_IMAGE_FILE_PATH = "image file path";

	private PointCollector pointCollector = new PointCollector();
	Database db = new Database(this);
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(DEBUGTAG,"start of ImageActivity.onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);

		//adds touch listener to the screen. function defined in this file i
		addTouchListener();
	
		boolean resetPasspoints=false;
		
		//Start of checking if clicked image is present and if it is setting it as passpoint image
		//explanation: we get the location from the preferenecsFile shared across activities
		SharedPreferences sharedprefs = getSharedPreferences(PREFERENCESFILE,MODE_PRIVATE);
		String imagePath = sharedprefs.getString(PASSPOINT_IMAGE_FILE_PATH,"default");
		Log.d(DEBUGTAG,imagePath);
		if(imagePath!="default")
		{
			Bitmap photo = BitmapFactory.decodeFile(imagePath);
			Log.d(DEBUGTAG,"a");
			if(photo!=null)
			{
				Log.d(DEBUGTAG,"a1");
				ImageView view = (ImageView)findViewById(R.id.passpoint_image);
				Log.d(DEBUGTAG,"a2");
				view.setImageBitmap(photo);
				Log.d(DEBUGTAG,"a3");
				
			}
			else
			{
				Log.d("SM", "unable to read photo");
				//default image saanchi.jpg is used as screen image 
			}
		}
		//end of checking if clicked image is present and if it is setting it as passpoint image
		
		
		//check boolean preference to see if passpoints have been saved by yhte user or not
		//set boolean flag passpointSaved
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		boolean passpointsSaved = prefs.getBoolean(PASSPOINTSSAVED, false);
		
		
		
		//start of check to see if passpoints have asked to be reset by 
		//1.clicking the resetPasspoints button on the mainActivity page
		//2. setting a new passpoint image.
		
		/*
		//Using Shared Preferences across activities
		SharedPreferences sharedprefs = getSharedPreferences(PREFERENCESFILE,MODE_PRIVATE);
		boolean resetPasspoints = sharedprefs.getBoolean(RESETPASSPOINTS,false);
		*/
		///*
		//Using Intent to pass data between activities
		if(getIntent()!=null)
		{
			Bundle extras = getIntent().getExtras();
			if(extras!=null)
			{
				
				Log.d(DEBUGTAG, "resetPasspoints flag set to true based on data received from intent");
				resetPasspoints = extras.getBoolean(RESETPASSPOINTS);
				
				Log.d(DEBUGTAG, "resetPasspoints flag written to this activity's preferences file");
				prefs = getPreferences(MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(RESETPASSPOINTS, true);
				editor.commit();
			}
		}
		//*/
		//end of check to see if passpoints have asked to be reset by clicking the resetPasspoints button on
		//the mainActivity page
							
		
		//start - decide whether to show set passpoints or verify passpoints prompt
		Log.d(DEBUGTAG, "passpointsSaved-"+passpointsSaved);
		Log.d(DEBUGTAG, "resetPasspoints-"+resetPasspoints);
		
		if (passpointsSaved && !resetPasspoints) {
			showVerifyPasspointsPrompt();
			Log.d(DEBUGTAG, "Verify Passpoints Prompt");
		} else {
			showSetPasspointsPrompt();
			Log.d(DEBUGTAG, "Set Passpoints Prompt");
		}
		//end - decide whether to show set passpoints or verify passpoints prompt
	}
	//end of onCreate method
	
	void addTouchListener() {
		ImageView image = (ImageView) findViewById(R.id.passpoint_image);
		image.setOnTouchListener(pointCollector);
		pointCollector.setListener(this);
	}

	void showSetPasspointsPrompt() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setPositiveButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		builder.setTitle("Set PassPoints");
		builder.setMessage("Set 4 points in a sequence as you passpoints. Later use the passpoints to see and edit your notes");
		AlertDialog alert = builder.create();
		alert.show();

	}

	void showVerifyPasspointsPrompt() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setPositiveButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		builder.setTitle("Enter Passpoints");
		builder.setMessage("Enter the 4 Passpoints to login");
		AlertDialog alert = builder.create();
		alert.show();

	}

	public void pointsCollect(List<Point> points) {

		Log.d(DEBUGTAG, "Number of Points" + points.size());

		//start of getting the values of flags 1. passpointsSaved and 2. resetPasspoints from prefrences file
		
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		boolean passpointsSaved = prefs.getBoolean(PASSPOINTSSAVED, false);
		/*
		//Using sharedpreferences accross activities 
		SharedPreferences sharedprefs = getSharedPreferences(PREFERENCESFILE,MODE_PRIVATE);
		boolean resetPasspoints = sharedprefs.getBoolean(RESETPASSPOINTS,false);
		*/
		///*
		//using intent object to pass data
		boolean resetPasspoints = prefs.getBoolean(RESETPASSPOINTS,false);
		//*/
		Log.d(DEBUGTAG,"resetpasspointsflag ->>>"+resetPasspoints);
		
		//end of getting the values of flags 1. passpointsSaved and 2. resetPasspoints from prefrences file
		
		
		//start- decide whether to save fresh passpoints or verify exisintg ones
		if (passpointsSaved && !resetPasspoints) {
			verifyPoints(points);
		} else {
			savePoints(points);
		}
		//end- decide whether to save fresh passpoints or verify exisintg ones

	}
	
	private void verifyPoints(final List<Point> touchedPoints)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("verifying points...");
		final AlertDialog alert = builder.create();
		alert.show();

		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... arg0) {
				List<Point> savedPoints = db.getPoints();
				Boolean pass = true;

				for (int i = 0; i < 4; i++) {
					int xDiff = savedPoints.get(i).x - touchedPoints.get(i).x;
					int yDiff = savedPoints.get(i).y - touchedPoints.get(i).y;
					int distanceSquare = (xDiff*xDiff)+(yDiff*yDiff);
					
					if (distanceSquare<=POINT_CLOSENESS*POINT_CLOSENESS) 
					{

					} else {
						pass = false;
					}
				}
				Log.d(DEBUGTAG, "passpoint verified - "+pass);
				return pass;
			}

			@Override
			protected void onPostExecute(Boolean pass) {
				alert.dismiss();
				pointCollector.clear();
				if(pass)
				{
					Intent i = new Intent(ImageActivity.this,MainActivity.class);
					startActivity(i);
				}
				else
				{
					Toast.makeText(ImageActivity.this, "Access Denied", Toast.LENGTH_SHORT).show();
				}
			}

		};
		task.execute();
	}

	private void savePoints(final List<Point> points)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("storing Points...");
		final AlertDialog alert = builder.create();
		alert.show();

		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				db.storePoints(points);

				SharedPreferences prefs = getPreferences(MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(PASSPOINTSSAVED, true);
				editor.commit();
				/*
				 //using sharedpreferecnes file accross activities
				SharedPreferences sharedprefs = getSharedPreferences(PREFERENCESFILE, MODE_PRIVATE);
				editor = sharedprefs.edit();
				editor.putBoolean(RESETPASSPOINTS, false);
				editor.commit();
				*/
				///*
				//Using intent to pass data
				editor.putBoolean(RESETPASSPOINTS, false);
				editor.commit();
				//*/			
				Log.d(DEBUGTAG, "Passpoints Created");
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				alert.dismiss();
				pointCollector.clear();
				Toast.makeText(ImageActivity.this,"Points Stored",Toast.LENGTH_LONG).show();

			}

		};
		task.execute();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	
}

