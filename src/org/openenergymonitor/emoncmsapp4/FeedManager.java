package org.openenergymonitor.emoncmsapp4;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;


public class FeedManager {

	TimeCalculator timeCalc;
	Context fileContext;
	ArrayList<Feed> allFeeds;	
	private static final String FILENAME = "feeds.ser";
	private static final long FORTYEIGHTHOURS = 172800000;

	public FeedManager(Context fileContext) {
		allFeeds = new ArrayList<Feed>();
		timeCalc = new TimeCalculator();
		this.fileContext = fileContext;
	}

	public void getFeedData(String url, String sampleTime) {

		String feedList = "";

		Log.i(MainActivity.class.getName(),"getFeedData ---> |" + url + "|");
		try {
			Networking listAllFeeds = new Networking();//"http://sergio-l-marques.dynip.sapo.pt:8080/emoncms/feed/list.json"
			feedList = listAllFeeds.execute(new String[] { "http://"+url+"/feed/list.json" }).get(); 		
			//feedList = listAllFeeds.execute(new String[] { "http://sergio-l-marques.dynip.sapo.pt:8080/emoncms/feed/list.json" }).get();
		} catch (Exception e) {
			e.printStackTrace();
		} 

		try {
			JSONArray jsonArr = new JSONArray(feedList);	//Put JSON string into JSONarray				
			Long timeNow = System.currentTimeMillis() / 1000;

			for(int i =0; i < jsonArr.length(); i++) {
				JSONObject jsonObject;
				
				try {
					jsonObject = jsonArr.getJSONObject(i);
					
					Log.i(MainActivity.class.getName(),"jsonObject.getString(time) ---> : |" + jsonObject.getString("time") + "|"); 
					
					Long timeSinceUpdate = Long.parseLong(jsonObject.getString("time"));

					if(((timeNow - timeSinceUpdate) < FORTYEIGHTHOURS) && (jsonObject.getInt("datatype") != 0)) {	//If it's been less than 48 hours since last update, include in results
						Feed f;					
						
						Log.i(MainActivity.class.getName(), String.format("newFeed?: name->%s", jsonObject.getString("name"))); 
					
						boolean existFeedFlag=false; int j;
						if (jsonObject.getString("name").endsWith("-log")) {
							for(j = 0; j < allFeeds.size(); j++) {
								Log.i(MainActivity.class.getName(), String.format("newFeed exist: name->%s", allFeeds.get(j).getName())); 
								if ( allFeeds.get(j).getName().equalsIgnoreCase(jsonObject.getString("name").substring(0, allFeeds.get(j).getName().length())) ) {
									allFeeds.get(j).setLogId(jsonObject.getInt("id"));
									existFeedFlag=true; break;
								}
							}

							if (existFeedFlag) continue;
							
							f = new Feed();
							f.setLogId(jsonObject.getInt("id")); //Set ID
							f.setName(jsonObject.getString("name").substring(0, jsonObject.getString("name").length()-4)); //Set name
							
						} else {
							for(j = 0; j < allFeeds.size(); j++) {
								if ( allFeeds.get(j).getName().equalsIgnoreCase(jsonObject.getString("name")) ) {
									Log.i(MainActivity.class.getName(), String.format("newFeed-exist: name->%s", allFeeds.get(j).getName())); 
									allFeeds.get(j).setId(jsonObject.getInt("id"));
									existFeedFlag=true; break;
								}
							}

							if (existFeedFlag) continue;

							f = new Feed();
							f.setId(jsonObject.getInt("id")); //Set ID
							f.setName(jsonObject.getString("name")); //Set name
						}

						f.setDatatype(jsonObject.getInt("datatype")); //Set DataType
						f.setTag(jsonObject.getString("tag")); //Set name					
						f.setTime(jsonObject.getLong("time")); //Set time
						f.setValue(jsonObject.getDouble("value")); // Set value
						//f.setDpInterval(jsonObject.getString("dpinterval")); //Set dpInterval
						
						Log.i(MainActivity.class.getName(), String.format("newFeed: id->%d log Id->%d name->%s dataType->%d tag->%s time->%d value->%f",
								f.getId(), f.getLogId(), f.getName(), jsonObject.getInt("datatype"), jsonObject.getString("tag"), jsonObject.getLong("time"), jsonObject.getDouble("value"))); 
						allFeeds.add(f);					 


					} 

				} catch (JSONException e) {
					e.printStackTrace(); 
				}
			}

			
			Long end, start, interval;
			int i, id;
			String historicData;
			long timestampPrev=0; double valuePrev=-1;
			Networking getHistoricData;
			JSONArray graphJsonArr;
			
			interval=Long.parseLong(sampleTime);

			/* Get historic data for all active feeds */
			end = System.currentTimeMillis()/interval*interval;
			
			for(i = 0; i < allFeeds.size(); i++) {
				id = allFeeds.get(i).getId();
				historicData = "";

				//if(allFeeds.get(i).getDatatype() == 1) {
				//	start = timeCalc.pastDate(1);	//If real-time, get data for past 24 hours 
				//}
				//else {
				//	start = timeCalc.pastDate(14);	//If daily, get data for past 14 days
				//}
				//start=start/1000*1000;

				start=end-(8)*(1000*interval.longValue());
				
				//Log.i(MainActivity.class.getName(),"Start :"+start+" / End : "+end+" / ID : "+id); //Log Message
				Log.i(MainActivity.class.getName(),"http://sergio-l-marques.dynip.sapo.pt:8080/emoncms/feed/data.json&id="+id+"&start="+start+"&end="+end+"&interval="+interval.toString()); //Log Message

				timestampPrev=0; valuePrev=-1;
				try {
					getHistoricData = new Networking(); //"http://sergio-l-marques.dynip.sapo.pt:8080/emoncms/feed/data.json&id="+id+"&start="+start+"&end="+end
					historicData = getHistoricData.execute(new String[] { "http://"+url+"/feed/data.json&id="+id+"&start="+start+"&end="+end+"&interval="+interval.toString()}).get(); 		
					graphJsonArr = new JSONArray(historicData);					

					for (int j = 0; j < graphJsonArr.length(); j++) {
						JSONArray arr = graphJsonArr.getJSONArray(j);
						long timestamp = arr.getLong(0);
						double value = arr.getDouble(1);
 
						if (valuePrev==-1) {
							valuePrev=value;
							timestampPrev=timestamp;
						} else {
							allFeeds.get(i).addElement(timestamp, value-valuePrev);
							Log.i(MainActivity.class.getName(),"Timestamp : "+timestamp+" / Value : "+value); //Log Message
							valuePrev=value;
							if (timestampPrev!=0) { 
								allFeeds.get(i).setSamplePeriod(timestamp-timestampPrev);
								timestampPrev=0;
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				} 					
			}

					
					
					
					
					
					
					
					
			/* Get USE TODAY value */
			Calendar c = new GregorianCalendar();
			c.setTime(new Date(System.currentTimeMillis()));
			
			c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			//Log.i(MainActivity.class.getName(),String.format("c.getTimeInMillis------>%d %s", c.getTimeInMillis(), c.getTime().toString())); //Log Message
			
			start=c.getTimeInMillis()/1000*1000;
			end=start+5000;
			interval=Long.valueOf(1);
			
			for(i = 0; i < allFeeds.size(); i++) {
				id = allFeeds.get(i).getId();
				historicData = "";
            
				Log.i(MainActivity.class.getName(),"http://sergio-l-marques.dynip.sapo.pt:8080/emoncms/feed/data.json&id="+id+"&start="+start+"&end="+end+"&interval="+interval.toString()); //Log Message
				try {
					getHistoricData = new Networking(); //"http://sergio-l-marques.dynip.sapo.pt:8080/emoncms/feed/data.json&id="+id+"&start="+start+"&end="+end
					historicData = getHistoricData.execute(new String[] { "http://"+url+"/feed/data.json&id="+id+"&start="+start+"&end="+end+"&interval="+interval.toString()}).get(); 		
					graphJsonArr = new JSONArray(historicData);					
            
					for (int j = 0; j < graphJsonArr.length(); j++) {
						JSONArray arr = graphJsonArr.getJSONArray(j);
						long timestamp = arr.getLong(0);
						double value = arr.getDouble(1);
            
						Log.i(MainActivity.class.getName(),String.format("PowerAtHour0 feedId %d %f %d", allFeeds.get(i).getId(), value, timestamp)); //Log Message
						allFeeds.get(i).powerAtHour0(value);
					}
			
				} catch (Exception e) {
					e.printStackTrace();
				} 					
			}					
	
			
			end = System.currentTimeMillis()/1000*1000;
			start=end-15000;
			interval=Long.valueOf(1);
                   
			for(i = 0; i < allFeeds.size(); i++) {
				id = allFeeds.get(i).getId();
				historicData = "";
            
				//Log.i(MainActivity.class.getName(),"Start :"+start+" / End : "+end+" / ID : "+id); //Log Message
				Log.i(MainActivity.class.getName(),"http://sergio-l-marques.dynip.sapo.pt:8080/emoncms/feed/data.json&id="+id+"&start="+start+"&end="+end+"&interval="+interval.toString()); //Log Message
            
				timestampPrev=0; valuePrev=-1;
				try {
					getHistoricData = new Networking(); //"http://sergio-l-marques.dynip.sapo.pt:8080/emoncms/feed/data.json&id="+id+"&start="+start+"&end="+end
					historicData = getHistoricData.execute(new String[] { "http://"+url+"/feed/data.json&id="+id+"&start="+start+"&end="+end+"&interval="+interval.toString()}).get(); 		
					graphJsonArr = new JSONArray(historicData);					
            
					for (int j = 0; j < graphJsonArr.length(); j++) {
						JSONArray arr = graphJsonArr.getJSONArray(j);
						long timestamp = arr.getLong(0);
						double value = arr.getDouble(1);
            
						Log.i(MainActivity.class.getName(),String.format("PowerAtNow feedId %d %f %d", allFeeds.get(i).getId(), value, timestamp)); //Log Message
						allFeeds.get(i).powerAtNow(value, timestamp);
					}
            
				} catch (Exception e) {
					e.printStackTrace();
				} 					
			}					
					
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void updateLiveFeed(String url) {

		Log.i(MainActivity.class.getName(),"updateLiveFeed : "+url); //Log Message

		String feedList = "";

		try {
			Networking listAllFeeds = new Networking(); //"http://sergio-l-marques.dynip.sapo.pt:8080/emoncms/feed/list.json"
			feedList = listAllFeeds.execute(new String[] { "http://"+url+"/feed/list.json" }).get(); 		
		} catch (Exception e) {
			e.printStackTrace();
		} 

		try {
			JSONArray jsonArr = new JSONArray(feedList);			

			for(int i =0; i < jsonArr.length(); i++) {
				JSONObject jsonObject = jsonArr.getJSONObject(i);				

				/* 
				 * Iterate through feed arrayList. If the JSON object has matching 
				 * id and a new reading, update the value and timestamp.
				 */
				for(int j = 0; j < allFeeds.size(); j++) {											
					if(jsonObject.getInt("id") == allFeeds.get(j).getId() && jsonObject.getDouble("value") != allFeeds.get(j).getValue()) {
						allFeeds.get(j).setValue(jsonObject.getDouble("value"));
						allFeeds.get(j).setTime(jsonObject.getLong("time"));
						break;
					}						
				}															
			} 

		}
		catch (JSONException e) {
			e.printStackTrace();
		}				
	}

	public void updateHistoricData(String url) {

		Log.i(MainActivity.class.getName(),"updateHistoricData : "+url); //Log Message

		/* Get historic data for all active feeds */
		Long end = System.currentTimeMillis();
		Long start;

		for(int i = 0; i < allFeeds.size(); i++) {
			int id = allFeeds.get(i).getId();
			String historicData = "";

			if(allFeeds.get(i).getDatatype() == 1) {
				start = timeCalc.pastDate(1);	//If real-time, get data for past 24 hours 
			}
			else {
				start = timeCalc.pastDate(14);	//If daily, get data for past 14 days
			}

			allFeeds.get(i).clearHistoricFeedData(); //Drop all existing data		
			
			try {
				Networking getHistoricData = new Networking();
				historicData = getHistoricData.execute(new String[] { "http://"+url+"/feed/data.json&id="+id+"&start="+start+"&end="+end}).get(); 		
				JSONArray graphJsonArr = new JSONArray(historicData);					

				for (int j = 0; j < graphJsonArr.length(); j++) {
					JSONArray arr = graphJsonArr.getJSONArray(j);
					long timestamp = arr.getLong(0);
					double value = arr.getDouble(1);
					allFeeds.get(i).addElement(timestamp, value);
				}
				Log.i(MainActivity.class.getName(),"Feed ID : "+id); //Log Message

			} catch (Exception e) {
				e.printStackTrace();
			} 					
		}
	}
	
	public void saveFile() {        

		try {            
			FileOutputStream fos = fileContext.getApplicationContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
			BufferedOutputStream bos = new BufferedOutputStream(fos);  //Added to see if a similar performance increase can be obtained as loadFile()
			ObjectOutputStream os = new ObjectOutputStream(bos);
			os.writeObject(allFeeds);
			os.flush();
			os.close();
			Log.i(MainActivity.class.getName(),"File : "+FILENAME+" serialised successfully."); //Log Message
		}
		catch (Exception e) {
			Log.e(MainActivity.class.getName(),"ERROR : "+e); //Log Message
			e.printStackTrace();
		} 		
	}

	@SuppressWarnings("unchecked")
	public void loadFile() {
		File file = fileContext.getApplicationContext().getFileStreamPath(FILENAME);

		try {			
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream(bis);				
			allFeeds = (ArrayList<Feed>) ois.readObject();			
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
	} 

	public ArrayList<Feed> getFeedArraylist() {
		return allFeeds;
	}



	/* *********************************************** *
	 *			  HELPER + TESTING METHODS
	 * *********************************************** */
	public void doesFileExist() {
		final String FILENAME = "feeds.ser";
		File file = fileContext.getApplicationContext().getFileStreamPath(FILENAME);

		if(file.exists()) {
			Log.e(MainActivity.class.getName(),"FILE EXISTS"); //Log Message
			Log.e(MainActivity.class.getName(),"Bytes : " +file.length()); //Log Message
		}
		else {
			Log.e(MainActivity.class.getName(),"FILE DOESN'T EXIST"); //Log Message
		}

	}

	public void printArraylist() {

		Log.i(MainActivity.class.getName(), String.format("allFeeds ----> %d\n\r", allFeeds.size()));
		for(int i = 0; i < allFeeds.size(); i++) {
			Log.i(MainActivity.class.getName(), allFeeds.get(i).toString());
		}
	}

	public void clearHistoryData() {

		Log.i(MainActivity.class.getName(), String.format("allFeeds ----> %d\n\r", allFeeds.size()));
		for(int i = 0; i < allFeeds.size(); i++) {
			Log.i(MainActivity.class.getName(), allFeeds.get(i).toString());
			allFeeds.get(i).clearHistoricFeedData();
		}
	}

}
