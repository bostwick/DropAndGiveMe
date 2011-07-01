package com.bostwickgarnes.dropandgiveme;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class HighScoreData {
	private static final String TAG = "HighScoreData";
	
	/// Singleton Instance
	private static HighScoreData s_instance;
	
	/// The name of the high scores file
	public static final String HIGH_SCORE_FILE = "highScores.csv";
	
	/// Map from Challenge ID to High Score
	private Map<Integer, Integer> m_highScores = new HashMap<Integer, Integer>();
	
	/// True if the high score data has been loaded.
	private boolean m_isLoaded = false;

	private Context m_context;
	
	/**
	 * Get the singleton instance of the HighScoreData class
	 * @param context
	 * @return
	 */
	public static HighScoreData getInstance(Context context) {
		if ( s_instance == null ) {
			s_instance = new HighScoreData(context.getApplicationContext());
		}
		
		return s_instance;
	}
	
	/**
	 * Instantiate a new HighScoreData
	 * @param context
	 */
	protected HighScoreData(Context context) {
		m_context = context;
		
		load();
	}
	
	/**
	 * Returns the High Score for a Challenge
	 * @param challengeId	The id of the Challenge
	 * @return	The Challenge's highest score, or 0 if there is no high score
	 */
	public int getHighScore(int challengeId) {
		if ( !m_isLoaded ) { 
			load();
		}
		
		Integer score = m_highScores.get(challengeId);
		return (score == null) ? 0 : score.intValue();
	}
	
	/**
	 * Returns true if a Challenge has been attempted and completed by the 
	 * user at least once.
	 * @param challengeId	The id of the Challenge
	 * @return	true if complete, otherwise false
	 */
	public boolean isChallengeComplete(int challengeId) {
		return (getHighScore(challengeId) != 0);
	}
	
	/**
	 * Load the High Score data from storage
	 */
	public void load() {
		// Open the High Score File
		// Read all the high scores into s_highScores
		// close the file
		
		Log.d(TAG, "Loading high scores from " + HIGH_SCORE_FILE);
		
		FileInputStream fis;		
		
		try {
			fis = m_context.openFileInput(HIGH_SCORE_FILE);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			String line;
			
			while ( (line = reader.readLine()) != null ) {
				loadHighScoreLine(line);
			}
			
			fis.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG, "Unable to find highScores.csv: " + e.getMessage());		
		} catch (IOException e) {
			Log.d(TAG, "Error reading from highScores.csv: " + e.getMessage());
		}
		
		m_isLoaded = true;
	}
	
	/**
	 * Save the high score data to storage
	 */
	public void save() {
		Log.d(TAG, "Writing high scores to " + HIGH_SCORE_FILE);
		
		if ( !m_isLoaded ) {
			Log.d(TAG, "No high scores loaded. Aborting.");
			return;
		}

		FileOutputStream fos;		
		
		try {
			fos = m_context.openFileOutput(HIGH_SCORE_FILE, Context.MODE_PRIVATE);
			
			for(Integer challengeId : m_highScores.keySet()) {
				int highScore = getHighScore(challengeId);
				
				fos.write(toOutputBytes(challengeId, highScore));
			}
			
			fos.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG, "Unable to find highScores.csv: " + e.getMessage());			
		} catch (IOException e) {
			Log.d(TAG, "Error writing to highScores.csv: " + e.getMessage());
		}
	}
	/**
	 * 
	 * @param challengeId
	 * @param highScore
	 * @return
	 */
	private static byte[] toOutputBytes(int challengeId, int highScore){
		String outputString = challengeId + "," + highScore + "\n";
		return outputString.getBytes();
	}
	
	/**
	 * 
	 * @param line
	 */
	private void loadHighScoreLine(String line) {
		String[] parts = line.trim().split(",");
		
		if ( parts.length == 2 ) {
			int id = Integer.parseInt(parts[0]);
			int score = Integer.parseInt(parts[1]);
			
			m_highScores.put(id, score);
		} else {
			Log.d(TAG, "Invalid high score line: " + line);
		}
	}

	/**
	 * Saves a high score for a challenge
	 * @param context
	 * @param challengeId	The challenge completed to save the high score for
	 * @param score			The new high score
	 * @param showToast		Should a congratulatory toast be shown?
	 */
	public void setHighScore(int challengeId, int score, boolean showToast) {
		m_highScores.put(challengeId, score);
		save();
		
		if ( showToast ) {
			toastNewHighScore();
		}
	}
	
	/**
	 * Show a Toast congratulating the user on his new high score.
	 * @param context
	 */
	private void toastNewHighScore() {
		CharSequence text = "Congratulations! You got the New High Score!";
		int duration = Toast.LENGTH_SHORT;

		Toast.makeText(m_context, text, duration).show();
	}
}
