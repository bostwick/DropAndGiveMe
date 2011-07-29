package com.bostwickgarnes.dropandgiveme;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.bostwickgarnes.dropandgiveme.R;
import com.bostwickgarnes.dropandgiveme.DagmEvent.EventType;

import android.content.Context;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;


public class EventData {
	public static final String TAG = "EventData";
	
	/// Name of the file to store activity data in
	private static final String EVENT_FILE = "events.csv";
	
	
	private static final String[] s_fromBindings = {"text", "date"};
	private static final int[] s_toBindings = 
		{R.id.dagm_event_item_text,
		 R.id.dagm_event_item_date};
	
	// Singleton Instance
	private static EventData s_instance = null;
	
	private final Context m_context;
	private ChallengeData m_challengeData;
	
	/// Map from a Date to the activities that occurred on that date
	private List<DagmEvent> m_eventList = new ArrayList<DagmEvent>();
	// private Map<Date, List<DagmEvent>> m_date2activity = new HashMap<Date, List<DagmEvent>>();
	
	/// True if the activity data has been loaded
	private boolean m_isLoaded = false;
	
	/**
	 * Protected constructor for singleton
	 * @param context
	 */
	protected EventData(Context context) {
		Log.d(TAG, "Instantiating ActivityData");
		m_context = context;
		m_challengeData = ChallengeData.getInstance(context);
		
		load();
	}
	
	/**
	 * Get the singleton instance of ActivityData
	 * @param context
	 * @return	ActivityData instance
	 */
	public static EventData getInstance(Context context) {
		if ( s_instance == null ) {
			s_instance = new EventData(context.getApplicationContext());
		}
		
		return s_instance;
	}
	
	/**
	 * Logs when a player completes a Challenge 
	 * @param id		The id of the Challenge completed
	 * @param isWin		Whether the user won or lost the challenge
	 */
	public void logChallenge(int id,  boolean isWin, int score) {
		Log.i(TAG, "Logging Challenge Event id=" + id + ", isWin=" + isWin);
		m_eventList.add(0, DagmEvent.challenge(id, isWin, score));
		save();
	}
	
	/**
	 * Log when a player completes a training challenge
	 * @param id		The id of the training challenge
	 * @param isWin		Whether the user successfully completed it.
	 */
	public void logTraining(int id, boolean isWin) {
		Log.i(TAG, "Logging Training Event id=" + id + ", isWin=" + isWin);
		m_eventList.add(0, DagmEvent.training(id, isWin, 1));
		save();
	}
	
	/**
	 * Get a ListAdapter representing the Activity Data
	 * @return	A ListAdapter
	 */
	public ListAdapter getListAdapter() {
		return new SimpleAdapter(m_context, eventMap(),
				R.layout.dagm_event_item,
				s_fromBindings, s_toBindings);
	}
	
	public List<Map<String, String>> eventMap() {
		List<Map<String, String>> datamap = new ArrayList<Map<String,String>>();

		for(DagmEvent event : m_eventList) {
			Map<String,String> adapterMap = new HashMap<String, String>();
			
			if ( m_challengeData.getChallenge(event.id) != null ) {			
				adapterMap.put("text", formatEventText(event));
				adapterMap.put("date", Utils.timeAgoInWords(event.time) + " ago");
				
				datamap.add(adapterMap);
			}
		}
		
		return datamap;
	}
	
	/**
	 * Formats event into a human-readable description of what happened.
	 * @param event
	 * @return
	 */
	protected String formatEventText(DagmEvent event) {
		Challenge challenge = m_challengeData.getChallenge(event.id);
		StringBuilder sb = new StringBuilder();
		
		if ( event.isWin && event.type == EventType.CHALLENGE ) {
			sb.append("You completed the challenge ")
				.append(challenge.getTitle())
				.append(" with a score of ").append(event.score);
		} else if ( !event.isWin && event.type == EventType.CHALLENGE ) {
			sb.append("You failed the challenge ")
				.append(challenge.getTitle());
		} else if ( event.isWin && event.type == EventType.TRAINING ) {
			sb.append("You completed the training exercise ")
				.append(challenge.getTitle());
		} else if ( !event.isWin && event.type == EventType.TRAINING ) {
			sb.append("You failed the training exercise ")
			.append(challenge.getTitle());
		}
		
		return sb.toString();
	}
		
	protected void loadEventLine(String line) {
		String[] parts = line.trim().split(",");
		
		if ( parts.length == 5) {
			String type 	= parts[0];
			Date time 		= Utils.parseDate(parts[1]);
			int id 			= Integer.parseInt(parts[2]);
			boolean isWin 	= Boolean.parseBoolean(parts[3]);
			int score		= Integer.parseInt(parts[4]);
			
			if ( type.equalsIgnoreCase("challenge") ) {
				m_eventList.add(
						DagmEvent.challenge(time, id, isWin, score));
			} else if ( type.equalsIgnoreCase("training") ) {
				m_eventList.add(
						DagmEvent.training(time, id, isWin, score));
			}
			
		} else {
			Log.w(TAG, "Invalid line in " + EVENT_FILE);
			Log.w(TAG, "Line: " + line);
		}
	}
	
	/**
	 * Loads the activity data from EVENT_FILE into memory 
	 */
	public void load() {
		Log.d(TAG, "Loading event data from " + EVENT_FILE);
		
		FileInputStream fis;
		
		try {
			fis = m_context.openFileInput(EVENT_FILE);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			String line;
			
			while ( (line = reader.readLine()) != null ) {
				loadEventLine(line);
			}
			
			fis.close();
		} catch(FileNotFoundException e) {
			Log.e(TAG, "Unable to find file " + EVENT_FILE);
			Log.e(TAG, "Exception: " + e.getLocalizedMessage());
		} catch(IOException e) {
			Log.e(TAG, "Error reading from file " + EVENT_FILE);
			Log.e(TAG, "Exception: " + e.getLocalizedMessage());
		} 
		
		m_isLoaded = true;
	}
	
	/**
	 * Save event data to EVENT_FILE
	 */
	public void save() {
		Log.d(TAG, "Writing event data to " + EVENT_FILE);
		
		if ( !m_isLoaded ) {
			Log.d(TAG, "No event data is loaded. Aborting.");
			return;
		}
		
		FileOutputStream fos;
		
		try {
			fos = m_context.openFileOutput(EVENT_FILE, Context.MODE_PRIVATE);
			
			for(DagmEvent event : m_eventList) {
				fos.write(event.toOutputBytes());
			}
			
			fos.close();
		} catch(FileNotFoundException e) {
			Log.w(TAG, "Unable to find event file " + EVENT_FILE);
			Log.w(TAG, "Exception: " + e.getLocalizedMessage());
		} catch(IOException e) {
			Log.w(TAG, "Unable to write to event file " + EVENT_FILE);
			Log.w(TAG, "Exception: " + e.getLocalizedMessage());
		}
	}
}
