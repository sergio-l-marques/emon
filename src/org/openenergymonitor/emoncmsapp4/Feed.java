package org.openenergymonitor.emoncmsapp4;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Feed implements Serializable {
	
	int id;
	String name;
	int datatype;
	String tag;
	Long time;
	Double value;
	String dpInterval;
	long samplePeriod;
	double powerAtHour0=0;
	double powerAtNow=0;
	double prevPowerNow1=0, prevPowerNow2=0;
	long powerNow1TimeStamp=0;
	long powerNow2TimeStamp=0;
	int logId=-1;
	
	HashMap<Long, Double> historicFeedData;
	
	public Feed() {
		historicFeedData = new HashMap<Long, Double>();
	}
	
	public Feed(int id, String name, int datatype, String tag, Long time,
			Double value, String dpInterval) {
		super();
		this.id = id;
		this.name = name;
		this.datatype = datatype;
		this.tag = tag;
		this.time = time;
		this.value = value;
		this.dpInterval = dpInterval;
		historicFeedData = new HashMap<Long, Double>();
		
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getDatatype() {
		return datatype;
	}
	public void setDatatype(int datatype) {
		this.datatype = datatype;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	public long getSamplePeriod() {
		return samplePeriod;
	}
	public void setSamplePeriod(long samplePeriod) {
		this.samplePeriod = samplePeriod;
	}
	
	public String getDpInterval() {
		return dpInterval;
	}
	
	public void setDpInterval(String dpInterval) {
		this.dpInterval = dpInterval;
	}
	
	/* Adds a key/pair value to the Hash Map */
	public void addElement(Long unixTime, Double feedValue) {
		historicFeedData.put(unixTime, feedValue);
	}
	
	/* Returns the value of a key/pair using Unix Time as key */
	public Double getElement(Long unixTime) {
		Double value = historicFeedData.get(unixTime);
		return value;		
	}
	
	/* Clear all key/pairs from Hash Map */
	public void clearHistoricFeedData() {
		historicFeedData.clear();
	}
	
	public void powerAtHour0(double value) {
		powerAtHour0=value;
	}

	public void powerAtNow(double value, long timeStamp) {
		powerAtNow=value;
		if (prevPowerNow1==0) {
			prevPowerNow1 = value;
			powerNow1TimeStamp=timeStamp;
		} else if (prevPowerNow2==0) {
			prevPowerNow2 = value;
			powerNow2TimeStamp=timeStamp;
		}
	}

	public double getPowerOfDay() {
		return(powerAtNow-powerAtHour0);
	}

	public double getPowerOfNow() {
		Log.i(MainActivity.class.getName(),String.format("PowerAtNow feedId %d %f %f %d %d", id, prevPowerNow1, prevPowerNow2, powerNow1TimeStamp, powerNow2TimeStamp)); //Log Message
		return((prevPowerNow2-prevPowerNow1)*3600000/((powerNow2TimeStamp-powerNow1TimeStamp)/1000));
	}

	public void setLogId(int id) {
		this.logId = id;
	}

	public int getLogId() {
		return logId;
	}
		
	public String toString() {
		String s = "Object : "+id+" / "+name+" / "+tag+" / "+time+" / "+value;
		return s;
	}
	
	public String printHashMap() {
		
		Iterator iterator = historicFeedData.keySet().iterator();
		
		while(iterator.hasNext()) {
			String key = iterator.next().toString();
			String value = historicFeedData.get(key).toString();
			
			System.out.println("Timestamp : "+key+" / Value : "+value);
		}
		
		return null;
	}

	public HashMap<Long, Double> getHistData() {
		
		return historicFeedData;
	}
	
}
