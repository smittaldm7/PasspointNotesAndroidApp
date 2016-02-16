package com.sidharth.notesquirrel;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String DEBUGTAG = ImageActivity.DEBUGTAG;
	public static final String TEXTFILE = "notesquirrel.txt";
	public static final String FILESAVED = "FileSaved";
	public static final String RESETPASSPOINTS = "ResetPasspoints";
	public static final String PREFERENCESFILE = "preferences";
	public static final int TAKE_PHOTO=0;
	public static final String PASSPOINT_IMAGE_FILE_PATH = "image file path";
	public static final int SELECT_FROM_GALLERY=1;


	File imageFile;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		addSaveButtonListener();
		addLockButtonListener();
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		boolean fileSaved = prefs.getBoolean(FILESAVED,false);
		if(fileSaved)
		{
			loadSavedFile();
		}
		ActionBar bar = getActionBar();
		bar.hide();
		//bar.show();
		bar.setDisplayShowHomeEnabled(false);
		bar.setDisplayShowTitleEnabled(false);
		
	}

	
	
	@Override
	protected void onPause() {
		super.onPause();
		saveText();
	}



	private void addLockButtonListener() {
		Button lockButton = (Button) findViewById(R.id.lock);
		lockButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(MainActivity.this,ImageActivity.class);
				startActivity(i);
				
			}});
		
	}

	void loadSavedFile() {
		FileInputStream fis;
		EditText editText = (EditText) findViewById(R.id.text);
		try {
			fis = openFileInput(TEXTFILE);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new DataInputStream(fis)));
			String line;
			while ((line = reader.readLine()) != null) {
				editText.append(line);
				editText.append("\n");
			}
			fis.close();
		} catch (FileNotFoundException e) {
			Log.d(DEBUGTAG, "Unable to read file");
		} catch (IOException e) {
			Log.d(DEBUGTAG, "Unable to read file");
		}
	}

	void addSaveButtonListener() {
		Button saveButton = (Button) findViewById(R.id.save);

		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.d(DEBUGTAG, "Save Button Clicked");
				saveText();			
			}
		});
	}

	void saveText()
	{
		EditText editText = (EditText) findViewById(R.id.text);
		String text = editText.getText().toString();
		try {
			
			FileOutputStream fos = openFileOutput(TEXTFILE,	Context.MODE_PRIVATE);
			fos.write(text.getBytes());
			fos.close();
			//writing preferences
			SharedPreferences prefs = getPreferences(MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(FILESAVED, true);
			editor.commit();
			Toast.makeText(MainActivity.this,getString(R.string.toast_saved),Toast.LENGTH_LONG).show();
		} catch (Exception e)
		{
			Log.d(DEBUGTAG, "Unable to write to file or write preferences");
			Toast.makeText(MainActivity.this,getString(R.string.toast_not_saved),Toast.LENGTH_LONG).show();

		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		
		switch (id) 
		{
			case R.id.action_settings:
				Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.reset_passpoints:
				resetPasspoints();
				return true;
			case R.id.change_passpoint_image:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				View v = getLayoutInflater().inflate(R.layout.replace_image,null);
				builder.setTitle(R.string.change_passpoint_image);
				builder.setView(v);
				
				final AlertDialog dlg = builder.create();
				dlg.show();
				
				Button takePhoto = (Button)dlg.findViewById(R.id.take_photo);
				Button selectFromGallery = (Button)dlg.findViewById(R.id.select_from_gallery);
				
				takePhoto.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						takePhoto();
					}
				});
				
				selectFromGallery.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						dlg.dismiss();
						selectFromGallery();
					}
				});
			default:
				return super.onOptionsItemSelected(item);

		}

	}
	
	void takePhoto()
	{
		Log.d(DEBUGTAG,"start of takePhoto()");
		File PicturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		imageFile = new File(PicturesDirectory,"passpoint_image");
		Intent i1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		Log.d("SM",Uri.fromFile(imageFile).toString());
		i1.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(imageFile));
		startActivityForResult(i1, TAKE_PHOTO);
	}
	
	void selectFromGallery()
	{
		Log.d(DEBUGTAG,"start of selectFromGallery()");
		Intent i = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i,SELECT_FROM_GALLERY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode)
		{
		case TAKE_PHOTO:
			setNewImage(imageFile.getAbsolutePath());
			
			resetPasspoints();
			break;
			
		case SELECT_FROM_GALLERY:
			
			String columns[] = {MediaStore.Images.Media.DATA};
			for(String c:columns)
			{
				Log.d(DEBUGTAG,c); // There is only 1 column which prints _data
			}
			Uri ImageUri = intent.getData();//gallery URI - content://media/external/images/media/49160
			Log.d(DEBUGTAG,ImageUri.toString());
			
			Cursor cursor = getContentResolver().query(ImageUri,columns,null,null,null);
			//take argument of gallery URI and columns which must be returned to the cursor resultSet
			cursor.moveToFirst();
			
			//get the string which stores the proper android URI of the image.
			//String imagePath=cursor.getString(0);//also works
			int columnIndex = cursor.getColumnIndex(columns[0]);
			String imagePath=cursor.getString(columnIndex);

			cursor.close();
			
			//convert String to URI
			//Uri image = Uri.parse(imagePath);//proper URI of image  - /storage/emulated/0/DCIM/Camera/IMG_20150908_163708196.jpg
			
			Log.d(DEBUGTAG,imagePath);
						
			setNewImage(imagePath);
			resetPasspoints();
			
			break;
			
		default:super.onActivityResult(requestCode, resultCode, intent);
		}
	}
	void setNewImage(String imagePath)
	{
		SharedPreferences prefs = getSharedPreferences(PREFERENCESFILE, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PASSPOINT_IMAGE_FILE_PATH, imagePath);
		editor.commit();
	}
	void resetPasspoints()
	{
		/*
		//using shared preferences file across activities
		SharedPreferences prefs = getSharedPreferences(PREFERENCESFILE, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(RESETPASSPOINTS, true);
		editor.commit();
		*/
		Intent i = new Intent(this,ImageActivity.class);
		///*
		//using intent to pass data
		i.putExtra(RESETPASSPOINTS, true);
		//*/
		startActivity(i);
	}
	
}
