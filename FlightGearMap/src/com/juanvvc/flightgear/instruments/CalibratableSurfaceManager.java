package com.juanvvc.flightgear.instruments;

import java.io.IOException;
import java.util.ArrayList;

import android.content.SharedPreferences;

import com.juanvvc.flightgear.FGFSConnection;
import com.juanvvc.flightgear.FlightGearMap;
import com.juanvvc.flightgear.myLog;

public class CalibratableSurfaceManager extends Thread {
	private ArrayList<Surface> surfaces;
	private SharedPreferences sp;
	
	public CalibratableSurfaceManager(SharedPreferences sp) {
		this.sp = sp;
		surfaces = new ArrayList<Surface>();
	}
	
	public void register(Surface sc) {
		synchronized(surfaces) {
			this.surfaces.add(sc);
		}
	}
	
	public void empty() {
		synchronized(surfaces) {
			this.surfaces.clear();
		}
	}
	
	public void run() {

		FGFSConnection conn = null;
		boolean cancelled = false;
		int waitPeriod = 5000;
		int port = 9000;
		String fgfsIP = "192.168.1.2";
		
		// read preferences
    	try {
    		waitPeriod = new Integer(sp.getString("update_period", "5000")).intValue();
    		// check limits
    		waitPeriod = Math.max(waitPeriod, 500);
    	} catch (NumberFormatException e) {
    		myLog.w(this, "Config error: wrong update_period=" + sp.getString("update_period", "default") +".");
    		waitPeriod = 5000;
    	}
    	try {
    		port = new Integer(sp.getString("telnet_port", "9000")).intValue();
    		// check limits
    		port = Math.max(port, 1);
    	} catch (ClassCastException e) {
    		myLog.w(this, "Config error: wrong port=" + sp.getString("telnet_port", "default"));
    		port = 9000;
    	}
		fgfsIP = sp.getString("fgfs_ip", "192.168.1.2");
		
		myLog.i(this, "Telnet: " + fgfsIP + ":" + port + " " + waitPeriod + "ms");
		
		try {
			myLog.e(this, "Trying telnet connection to " + fgfsIP + ":" + port);
			conn = new FGFSConnection(fgfsIP, port, FlightGearMap.SOCKET_TIMEOUT);
			myLog.d(this, "Upwards connection ready");
		} catch (IOException e) {
			myLog.w(this, e.toString());
			conn = null;
			return;
		}
		
		while (!cancelled) {
			try{
				myLog.i(this, "Update event");
				synchronized(surfaces) {
					for(Surface cs: surfaces) {
						if (cs.isDirty()) {
							cs.update(conn);
						}
					}
				}
				Thread.sleep(waitPeriod);
				cancelled = conn.isClosed();
			} catch (InterruptedException e) {
				cancelled = true;
			} catch (IOException e) {
				myLog.w(this, myLog.stackToString(e));
			}
		}
		
		try {
			conn.close();
		} catch (IOException e) {
			myLog.w(this, "Error closing connection: " + e.toString());
		}
	}
}
