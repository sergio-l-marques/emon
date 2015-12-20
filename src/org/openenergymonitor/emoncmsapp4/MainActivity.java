package org.openenergymonitor.emoncmsapp4;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.openenergymonitor.emoncmsapp4.ViewAndUpdatePreferencesActivity;
import org.openenergymonitor.emoncmsapp4.FeedIntentService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	final String FILENAME = "feeds.ser";
	static private String sampleTime=null;

	SectionsPagerAdapter mSectionsPagerAdapter=null;

	ViewPager mViewPager;
	SharedPreferences prefs;
	EditText apiKeyET; 
	EditText serverUrlET;
	Button apiKeyDoneButton; 
	private ArrayList<Feed> feedsArr;
	private ResponseReceiver receiver;
	String serviceResponse;

	private static boolean canvasDrawFlag; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		

		canvasDrawFlag=false;
		
		/*Register Broadcast Receiver*/
		IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new ResponseReceiver();
		registerReceiver(receiver, filter);

		//prefs = this.getSharedPreferences("AppPreferences", 0); //Create shared preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String apiKey = prefs.getString("api_key", null);  //Create an API Key pair value
		String serverUrl = prefs.getString("server_url", null);
		
		Log.i(MainActivity.class.getName(), "API Key : "+apiKey); //Log Message
		Log.i(MainActivity.class.getName(), "Server URL : "+serverUrl); //Log Message
		
    	//if (apiKey == null) {
        //    intent = new Intent(this, ViewAndUpdatePreferencesActivity.class);
        //    //this.startActivityForResult(intent, 1);
    	//} else {
        //	startup(serverUrl, apiKey);
    	//}
		
		if (sampleTime==null) sampleTime="30";
		Log.i(MainActivity.class.getName(), "Sampling Time: "+sampleTime); //Log Message

		startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
		
		//if(apiKey == null) {			//If an API key isn't set - run set API key activity to enter one
		//	Log.i(MainActivity.class.getName(), "API Key not set"); //Log Message
		//	setContentView(R.layout.set_api_key);
		//	apiKeyET = (EditText) findViewById(R.id.apiKeyEditText);
		//	serverUrlET = (EditText) findViewById(R.id.serverUrlEditText);
		//	apiKeyDoneButton = (Button) findViewById(R.id.apiKeyDoneButton);
		//	setButtonOnClickListeners();
		//}
		//else { 
		//	startup(false, serverUrl, apiKey);
		//}		





		//FeedManager fm = new FeedManager(this);
        //
		//fm.getFeedData(serverUrl, apiKey);
		//fm.saveFile();
		//fm.doesFileExist();
		//fm.loadFile();		
		//fm.printArraylist();
		//fm.updateLiveFeed();
		//fm.printArraylist();
	}

	@Override
	public void onDestroy() {
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	public void onResume() {
        super.onResume();

        Log.i(MainActivity.class.getName(), "onResume: invalidate");
        if (canvasDrawFlag==true) mViewPager.postInvalidate();
	}
	
	//@Override
	//public boolean onCreateOptionsMenu(Menu menu) {
	//	// Inflate the menu; this adds items to the action bar if it is present.
	//	getMenuInflater().inflate(R.menu.main, menu);
	//	return true;
	//}
    //
    //
	///* Set listener action for when the done button is pressed */
	//private void setButtonOnClickListeners() {
	//	apiKeyDoneButton.setOnClickListener(new OnClickListener() {
	//		@Override
	//		public void onClick(View v) {
	//			Editor editor = prefs.edit();  //Make shared preferences editable			
	//			editor.putString("api_key", apiKeyET.getText().toString()); //Set key-pair value for api_key to user entered string
    //
    //
	//			String serverUrl = serverUrlET.getText().toString();
	//			if(serverUrl.matches("")) {
	//				editor.putString("server_url", "http://emoncms.org"); //If no server specified, use emoncms.org
	//				Log.i(MainActivity.class.getName(), "In the else statement"); //Log Message
	//			}
	//			else {
	//				editor.putString("server_url", serverUrlET.getText().toString()); //Set key-pair value for server_url to user entered string
	//				Log.i(MainActivity.class.getName(), "In the if statement"); //Log Message				
	//			}
    //
	//			editor.commit(); //Commit changes
	//			Log.i(MainActivity.class.getName(), "API Key : "+prefs.getString("api_key", null)); //Log Message
	//			Log.i(MainActivity.class.getName(), "Server URL : "+prefs.getString("server_url", null)); //Log Message
    //
	//			startup(true, prefs.getString("server_url", null), prefs.getString("api_key", null));  //Run startup method
	//		}			
	//	});
    //
	//}
;
	Intent msgIntent;
	private void startup(String url, String apiKey, String sampleTime) {
		setContentView(R.layout.activity_main);

		//if(doesFileExist()==false) {
			String temp = "all";
			msgIntent = new Intent(this, FeedIntentService.class);        
			msgIntent.putExtra(FeedIntentService.PARAM_IN_MSG, temp);
			msgIntent.putExtra(FeedIntentService.PARAM_IN_KEY, apiKey);
			msgIntent.putExtra(FeedIntentService.PARAM_IN_URL, url);
			msgIntent.putExtra(FeedIntentService.PARAM_IN_SAMPLE_TIME, sampleTime);
			startService(msgIntent);
		//	
		//} else {
		//	loadFile();
		//	createGUI();
		//}
	}

	public boolean doesFileExist() {
		File file = getFileStreamPath(FILENAME);

		return file.exists();
		
		//if(file.exists()) {
		//	Log.e(MainActivity.class.getName(),"FILE EXISTS"); //Log Message
		//	Log.e(MainActivity.class.getName(),"Bytes : " +file.length()); //Log Message
		//}
		//else {
		//	Log.e(MainActivity.class.getName(),"FILE DOESN'T EXIST"); //Log Message
		//}
	}
	
	@SuppressWarnings("unchecked")
	public void loadFile() {
		File file = getFileStreamPath(FILENAME);

		try {			
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream(bis);
			feedsArr = (ArrayList<Feed>) ois.readObject();
			ois.close();			
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Log.i(MainActivity.class.getName(), "Loading file: " + String.format("%d", feedsArr.size())); //Log Message
		//int i; for (i=0;i<feedsArr.size();i++) Log.i(MainActivity.class.getName(), String.format("loadFile: %s %d", feedsArr.get(i).getName(), feedsArr.get(i).getSamplePeriod())); //Log Message feedsArr.get(i).clearHistoricFeedData();

	} 
	
	public class ResponseReceiver extends BroadcastReceiver {
		public static final String ACTION_RESP = "org.openenergymonitor.intent.action.MESSAGE_PROCESSED";
		@Override
		public void onReceive(Context context, Intent intent) {                      
			serviceResponse = intent.getStringExtra(FeedIntentService.PARAM_OUT_MSG);
			Log.i(MainActivity.class.getName(), "Response from Service : "+serviceResponse); //Log Message

			if(serviceResponse.equals("all")) {
				loadFile();			
				createGUI();	
			}
			
		}

	}

	public void createGUI() {
		
		//if (mSectionsPagerAdapter==null) {
			mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mSectionsPagerAdapter);
			
	    	canvasDrawFlag=true;

		//} else {
			//mViewPager.getAdapter().notifyDataSetChanged();
			//Log.i(MainActivity.class.getName(), "mViewPager.getAdapter().notifyDataSetChanged()"); //Log Message
			
			//mViewPager.invalidate();
			//Log.i(MainActivity.class.getName(), "mViewPager.invalidate()"); //Log Message
			
		//}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			//Log.i(MainActivity.class.getName(), String.format("getItem")); //Log Message

			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			Double feedValue = feedsArr.get(position).getValue();						
			args.putDouble(DummySectionFragment.ARG_SECTION_NUMBER, feedValue);
			
			args.putSerializable(DummySectionFragment.ARG_SECTION_FEED, feedsArr.get(position));
			
			
			//Bundle feedObject = new Bundle();
			//feedObject.putSerializable("FeedObject",feedsArr.get(position));
			//args.putBundle("FeedObject", feedObject);
						
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			//Log.i(MainActivity.class.getName(), String.format("getCount %d", feedsArr.size())); //Log Message
			// Show 3 total pages.
			return feedsArr.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			//Log.i(MainActivity.class.getName(), String.format("getPageTitle %d", position)); //Log Message

			Locale l = Locale.getDefault();
			String feedName = feedsArr.get(position).getName();
			return feedName.toUpperCase();
		}
		
		@Override
		public int getItemPosition(Object object) {
			//Log.i(MainActivity.class.getName(), "getItemPosition: POSITION_NONE"); //Log Message
			
		    return POSITION_NONE;
		}
		
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		public static final String ARG_SECTION_FEED = "section_feed";
		
		//private static View rootView;
		
        public DummySectionFragment() {
		}

        //@Override
        //public void onCreate (Bundle savedInstanceState) {
    	//	Log.i(MainActivity.class.getName(), "DummySectionFragment: onCreate"); //Log Message
        //	super.onCreate(savedInstanceState);
        //}
        //
        //@Override
        //public void onPause () {
        //	Log.i(MainActivity.class.getName(), "DummySectionFragment: onPause"); //Log Message
        //	super.onPause();
        //}
        //
        //@Override
        //public void onStop () {
        //	Log.i(MainActivity.class.getName(), "DummySectionFragment: onStop"); //Log Message
        //	super.onStop();
        //}
        //
        //@Override
        //public void onDestroyView () {
        //	Log.i(MainActivity.class.getName(), "DummySectionFragment: onDestroyView"); //Log Message
        //	super.onDestroyView();
        //}
        //
        //@Override
        //public void onDestroy () {
    	//	Log.i(MainActivity.class.getName(), "DummySectionFragment: onDestroy"); //Log Message
        //	super.onDestroy();
        //}
        //
        //@Override
        //public void onDetach () {
        //	Log.i(MainActivity.class.getName(), "DummySectionFragment: onDetach"); //Log Message
        //	super.onDetach();
        //}
        
        
        
        @Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

    		View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			//TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
			//dummyTextView.setText(Double.toString(getArguments().getDouble(
			//		ARG_SECTION_NUMBER)));
			

			final Feed feed=(Feed)getArguments().getSerializable(ARG_SECTION_FEED);
			//dummyTextView.setText( Double.toString(getArguments().getDouble(ARG_SECTION_NUMBER)) + "---" + Double.toString(feed.getValue()) );

			//RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.container);
			//relativeLayout.addView(new Rectangle(getActivity()));
			LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.graph1);
			linearLayout.addView(new drawGraphCanvas(getActivity(), feed));

			linearLayout.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
		    		String url = String.format("http://sergio-l-marques.dynip.sapo.pt:8080/emoncms/vis/realtime?feedid=%d&embed=1&apikey=ce7e86151469bb8e48454fa933d9833b", feed.getLogId());
		    		Intent i = new Intent(Intent.ACTION_VIEW);
		    		i.setData(Uri.parse(url));
		    		startActivity(i);    		
		            
					return false;
				}
			});
			
				
		   /* linearLayout.setOnTouchListener(new OnTouchListener() {

			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			        	
			            switch (event.getAction()) {
			            case MotionEvent.ACTION_MOVE: 
			                break;
			            case MotionEvent.ACTION_UP:
			            case MotionEvent.ACTION_CANCEL:
			                break;
			            case MotionEvent.ACTION_DOWN:
				            Log.i(null, String.format("SMARQUES-TOUCH EVENT %s", feed.getId())); // handle your fragment number here
				            break;
			            }

			            return false;
			        }
			    });*/
    		
			
			//rootView.addView(new Rectangle(getActivity()));
			return rootView;
		}
		
        //@Override
        //public View getView() {
        //	return this.rootView;
        //}
        
	    //@Override
	    //public void onAttach(Activity activity) {
    	//	Log.i(MainActivity.class.getName(), "DummySectionFragment: onAttach"); //Log Message
	    //    super.onAttach(activity);
	    //}
		
		
		
	    private class drawGraphCanvas extends View{
		    Paint paint = new Paint();
		    Feed myFeed;

		    public drawGraphCanvas(Context context, Feed feed) {
		        super(context);
		        myFeed=feed;
		    }
		    @Override
		    public void onDraw(Canvas canvas) {
		        //paint.setColor(Color.GREEN);
		        //Rect rect = new Rect(20, 56, 200, 112);
		        //canvas.drawRect(rect, paint );

		    	if (myFeed==null) return;
		        // Typeface definitions
		        Typeface robotoNormal = Typeface.create("Roboto",Typeface.NORMAL);
		        Typeface robotoBold = Typeface.create("Roboto",Typeface.BOLD);
                
		        // Clear background
		        paint.setColor(Color.parseColor("#222222"));
		        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
                
		        // View needs to be responsive to different screen width's and heights
		        // and whether the display is in portrait or landscape mode
		        //-------------------------------------------------------------------------
                
		        int screenWidth = getWidth();
		        int screenHeight = getHeight();
                
		        float scale = 1.0f;
                
		        float left = 0;
		        float top = 0;
		        float graphWidth = 0;
		        float graphHeight = 0;
		        float bottomGraphOffset=0;
                
		        if (screenWidth<screenHeight) {
		            // Portrait mode:
		            scale = screenWidth / 720.0f;
                
		            left = 30*scale;
		            graphWidth = screenWidth - 60*scale;
                
		            //graphHeight = 0.6f * screenHeight;
		            graphHeight = 0.5f * screenHeight;
		            bottomGraphOffset=35*scale;//0.1f * screenHeight;
		            top = screenHeight - graphHeight-bottomGraphOffset-(30*scale);
		            
                
		        } else {
		            // Landscape mode:
		            scale = screenHeight / 720.0f;
                
		            left = screenWidth * 0.35f;
		            graphWidth = screenWidth * 0.65f - 30*scale;
                
		            top = (90*scale);
		            bottomGraphOffset=35*scale;//0.1f * screenHeight;
		            graphHeight = screenHeight - bottomGraphOffset - (120*scale);
		        }
                
		        // Power now text and value's
		        //-------------------------------------------------------------------------
                
		        // Grey horizontal line at the top
		        paint.setColor(Color.parseColor("#333333"));
		        canvas.drawLine(30*scale, 60*scale, screenWidth-30*scale, 60*scale, paint);
                
		        // My Electric text
		        paint.setTypeface(robotoBold);
		        paint.setColor(Color.parseColor("#aaaaaa"));
		        paint.setTextSize((int)35*scale);
		        canvas.drawText("POWER NOW:", 30*scale, (int)120*scale, paint);
                
		        // Power value text
		        paint.setColor(Color.parseColor("#0699fa"));
		        paint.setTextSize(160*scale);        
		        canvas.drawText(String.format("%.0f", myFeed.getPowerOfNow())+"W", 30*scale, 260*scale, paint);
                
		        // kwh text
		        paint.setTypeface(robotoNormal);
		        paint.setTextSize((int)35*scale);
		        canvas.drawText(String.format("USE TODAY: %f kWh", myFeed.getPowerOfDay()), 30*scale, (int)320*scale, paint);
                
		        // Start of graph drawing:
		        //-------------------------------------------------------------------------
                
		        // Margin and inner dimensions
		        float margin = 12 * scale;
		        float innerWidth = graphWidth - 2*margin;
		        float innerHeight = graphHeight - 2*margin;
                
		        // Draw Axes
		        paint.setColor(Color.rgb(6,153,250));
		        canvas.drawLine(left, top, left, top+graphHeight, paint);
		        canvas.drawLine(left, top+graphHeight, left+graphWidth, top+graphHeight, paint);
                
		        // Draw kWh label top-left
		        //paint.setTextSize(35*scale);
		        
		        //String kwhLabel;
		        //canvas.drawText(String.format("kWh/%d", myFeed.getSamplePeriod()), left+10, top+30*scale, paint);
		        //canvas.drawText("kWh", left+10, top+30*scale, paint);
                
		        // Auto detect xmin, xmax, ymin, ymax
		        long xmin = 0;
		        long xmax = 0;
		        float ymin = 0;
		        float ymax = 0;
		        boolean s = false;
                
		        
		        Iterator keySetIterator = myFeed.getHistData().keySet().iterator();//data.keySet().iterator();
		        while( keySetIterator.hasNext() ){
		            //Integer time = keySetIterator.next();
		            Long time = Long.parseLong(keySetIterator.next().toString());
		            float value = myFeed.getElement(time).floatValue();
		            
		            if (!s) {
		                xmin = time.longValue();
		                xmax = time.longValue();
		                ymin = value;
		                ymax = value;
		                s = true;
		            }
                
		            if (value>ymax) ymax = value;
		            if (value<ymin) ymin = value;
		            if (time.longValue()>xmax) xmax = time.longValue();
		            if (time.longValue()<xmin) xmin = time.longValue();
		        }
		        
		        float r = (ymax - ymin);
		        ymax = (ymax - (r / 2f)) + (r/1.5f);
		        // Fixed min y
		        ymin = 0;
		     
		        // Draw kWh label top-left
		        paint.setTextSize(35*scale);
                
		        if (ymax>1) canvas.drawText("kWh", left+10, top+30*scale, paint);
		        else canvas.drawText("Wh", left+10, top+30*scale, paint);
		        
		        {
			        String xLabel;
			        Rect textBounds = new Rect();
			        int sampleTime=(int)myFeed.getSamplePeriod()/1000;
			        
			        if      (sampleTime<       60) xLabel=String.format("%d seconds", sampleTime         );
			        else if (sampleTime==      60) xLabel=String.format("%d minute" , sampleTime/60      );
			        else if (sampleTime<    60*60) xLabel=String.format("%d minutes", sampleTime/60      );
			        else if (sampleTime==   60*60) xLabel=String.format("%d hour"   , sampleTime/60/60   );
			        else if (sampleTime< 60*60*24) xLabel=String.format("%d hours"  , sampleTime/60/60   );
			        else if (sampleTime==60*60*24) xLabel=String.format("%d day"    , sampleTime/60/60/24);
			        else                           xLabel=String.format("%d day(s)" , sampleTime/60/60/24);
			        
			        paint.getTextBounds(xLabel, 0, xLabel.length(), textBounds);
			        Log.i(MainActivity.class.getName(),String.format("%d %d %d", sampleTime, textBounds.left, textBounds.right)); //Log Message
			        canvas.drawText(xLabel, left+graphWidth/2-(textBounds.right-textBounds.left)/2+10, top+graphHeight+35*scale, paint);
		        	
		        }
		        
		        //float barWidth = 3600*20;
		        //float barWidth = 30000;//3600*20;
		        long barWidth=myFeed.getSamplePeriod();
		        xmin -= barWidth /2;
		        xmax += barWidth /2;

		        float barWidthpx = ((float)barWidth / (xmax - xmin)) * innerWidth * 0.8f;
                
		        // kWh labels on each bar
		        paint.setTextAlign(Align.CENTER);
		        paint.setTextSize(35*scale);
                
		        keySetIterator = myFeed.getHistData().keySet().iterator();//keySetIterator = data.keySet().iterator();
                
		        while(keySetIterator.hasNext()){
                
		    		//Integer time = keySetIterator.next();
		            Long time = Long.parseLong(keySetIterator.next().toString());
		            float value = myFeed.getElement(time).floatValue();

		            //Log.i(MainActivity.class.getName(), "time: "+time.toString()+" value: "+myFeed.getElement(time).toString()); //Log Message
                
		            float px = (float)((time.longValue()-xmin) * innerWidth / (xmax-xmin)) ;
		            float py = ((value - ymin) / (ymax - ymin)) * (innerHeight*0.9f);
                
		            float barLeft = left + margin + px - barWidthpx/2;
		            float barBottom = top + margin + innerHeight;
                
		            float barTop = barBottom - py;
		            float barRight = barLeft + barWidthpx;
                
		            paint.setColor(Color.rgb(6,153,250));
		            canvas.drawRect(barLeft,barTop,barRight,barBottom,paint);
                
		            // Draw kwh label text
		            if (py>38*scale) {
		              paint.setColor(Color.parseColor("#ccccff"));
		              int offset = (int)(45*scale);
		              if (ymax>1) canvas.drawText(String.format("%.0f", value), left+margin+px, barTop + offset, paint);
		              else canvas.drawText(String.format("%.0f", value*1000), left+margin+px, barTop + offset, paint);
		            }
		        }
		    }
		}	
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //super.recreate();
        //if(data.getExtras().containsKey("widthInfo")){
        //    width.setText(data.getStringExtra("widthInfo"));
        //}
        //if(data.getExtras().containsKey("heightInfo")){
        //    height.setText(data.getStringExtra("heightInfo"));
        //}
    }
	
	Intent intent;
	public boolean onOptionsItemSelected(MenuItem item) {
		File file;
		
    	switch (item.getItemId()) {
        case R.id.action_settings:
            intent = new Intent(this, ViewAndUpdatePreferencesActivity.class);
            this.startActivityForResult(intent, 1);
            return true;
        case R.id.st5sec :  
        	sampleTime="5";
        	startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
        	return true;
        case R.id.st30sec:
        	sampleTime="30";
        	startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
        	return true;
        case R.id.st1min:
        	sampleTime="60";
        	startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
        	return true;
        case R.id.st5min:
        	sampleTime="300";
        	startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
        	return true;
        case R.id.st15min:
        	sampleTime="900";
        	startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
        	return true;
        case R.id.st30min:
        	sampleTime="1800";
        	startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
        	return true;
        case R.id.st1hour:
        	sampleTime="3600";
        	startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
        	return true;
        case R.id.st3hour:
        	sampleTime="10800";
        	startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
        	return true;
        case R.id.st6hour:
        	sampleTime="21600";
        	startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
        	return true;
        case R.id.st8hour:
        	sampleTime="28800";
        	startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
        	return true;
        case R.id.st12hour:
        	sampleTime="43200";
        	startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
        	return true;
        case R.id.st1day:
        	sampleTime="86400";
        	startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
        	return true;
        case R.id.st30day:
        	sampleTime="2592000";
        	startup("sergio-l-marques.dynip.sapo.pt:8080/emoncms", "ce7e86151469bb8e48454fa933d9833b", sampleTime);
        	return true;
        case R.id.reset:
    		file = getFileStreamPath(FILENAME);
    		if (file.exists()) file.delete();
        	super.recreate();
        	return true;
        default:
          return super.onOptionsItemSelected(item);
        }
    } 
	
}
