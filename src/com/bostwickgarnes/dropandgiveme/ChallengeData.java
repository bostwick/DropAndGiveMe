package com.bostwickgarnes.dropandgiveme;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.bostwickgarnes.dropandgiveme.R;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

public class ChallengeData {
	public static final String TAG = "ChallengeData";
	
	private static final String FILE_CHALLENGES_XML = "challenges.xml";
	private static final String FILE_TRAINING_XML = "training.xml";
	
	private static final String[] s_fromBindings = 
		{"id", "title", "score"};
	private static final int[] s_toBindings = 
		{R.id.dagm_challenge_item_id, 
		 R.id.dagm_challenge_item_title,
		 R.id.dagm_challenge_item_score};
	
	private static final String[] s_lockedLevelFromBindings = {"text"};
	private static final int[] s_lockedLevelToBindings = {R.id.dagm_lockedlevel_item_text};
	
	public static ChallengeData s_instance = null;
	
	private final Context m_context;
	
	private Map<Integer, Challenge> m_id2challenge = new HashMap<Integer, Challenge>();
	private Map<Integer, List<Challenge>> m_level2challenges = new HashMap<Integer, List<Challenge>>();
	
	private int m_minChallengeLevel = 0;
	private int m_minTrainingLevel 	= 100;
	private int m_maxChallengeLevel = 99;
	private int m_maxTrainingLevel 	= 200;
	private boolean m_isChallengesLoaded = false;
	private boolean m_isTrainingLoaded = false;
	
	private HighScoreData m_highScoreData;
	
	/**
	 * Private constructor for singleton 
	 * @param context
	 */
	protected ChallengeData(Context context){
		Log.d(TAG, "Instantiating NewChallengeData");
		
		m_context = context;
		m_highScoreData = HighScoreData.getInstance(context);
		
		load();
	}
	
	/**
	 * Get the singleton instance of ChallengeData
	 * @param context
	 * @return	ChallengeData instance
	 */
	public static ChallengeData getInstance(Context context){
		if ( s_instance == null ) {
			s_instance = new ChallengeData(context.getApplicationContext());
		}
		
		return s_instance;
	}
	
	/**
	 * Returns the challenge with the given id.
	 * @param id	The id of the Challenge to get
	 * @return		A the challenge with id 
	 */
	public Challenge getChallenge(int id) {
		if ( !m_isChallengesLoaded && !m_isTrainingLoaded ) {
			load();
		}
		
		Log.i(TAG, "Returning challenge with id=" + id);
		return m_id2challenge.get(id);
	}
	
	/**
	 * Returns the highest unfinished challenge or null if all challenges have
	 * been finished
	 * @return
	 */
	public Challenge getNextChallenge() {
		int level = getChallengeLevel();
		
		for(Challenge challenge : m_level2challenges.get(level)) {
			if ( !m_highScoreData.isChallengeComplete(challenge.getId())) {
				return challenge;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the highest unfinished training challenge, or null if all 
	 * training challenges have been finished
	 * @return
	 */
	public Challenge getNextTraining() {
		int level = getTrainingLevel();
		
		for(Challenge challenge : m_level2challenges.get(level)) {
			if ( !m_highScoreData.isChallengeComplete(challenge.getId()) ) {
				return challenge;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the level the user is currently on. The user's current level 
	 * is the highest unlocked they have achieved. A level becomes unlocked
	 * once the user has attempted all the Challenges in the previous level
	 * at least once. Level 1 is always unlocked.
	 * @return	The current user level
	 */
	public int getChallengeLevel() {
		int level = m_minChallengeLevel;
		
		for ( ; level <= m_maxChallengeLevel; level += 1) {
			if ( !isLevelComplete(level) ) {
				break;
			}
		}
		
		return level;
	}
	
	/**
	 * @return	The highest completed training level
	 */
	public int getTrainingLevel() {
		int level = m_minTrainingLevel;
		
		for ( ; level <= m_maxTrainingLevel; level += 1) {
			if ( !isLevelComplete(level) ) {
				break;
			}
		}
		
		return level;
	}
	
	/**
	 * @return	The highest level achievable in the challenges
	 */
	public int getMaxChallengeLevel() {
		return m_maxChallengeLevel;
	}
	
	/**
	 * @return	The highest level achievable in training
	 */
	public int getMaxTrainingLevel(){
		return m_maxTrainingLevel;
	}
	
	/**
	 * Returns true if all of the challenges in a level have been attempted
	 * and completed by the user at least once.
	 * @param level		The Challenge Level
	 * @return			true if the level is complete, otherwise false
	 */
	public boolean isLevelComplete(int level) {
		for(Challenge challenge : m_level2challenges.get(level)) {
			if ( !m_highScoreData.isChallengeComplete(challenge.getId()) ) {
				return false;
			}
		}
		
		return true;
	}
	

	/**
	 * Returns the Challenges associated with a given level
	 * @param level		The level whose challenges you want
	 * @return			A List of the Challenges on that level
	 */
	public List<Challenge> getChallengesForLevel(int level) {
		return m_level2challenges.get(level);
	}
	
	/**
	 * Get a list adapter displaying all the challenges in a given level
	 * @param level		The level to adapt
	 * @return			A ListAdapter
	 */
	protected ListAdapter getLevelAdapter(int level, boolean isTraining) {
		return new SimpleAdapter(m_context, 
				levelMap(level, isTraining), 
				R.layout.dagm_challenge_item, 
				s_fromBindings, s_toBindings);
	}
	
	/**
	 * Get a list adapter to display a locked level
	 * @param level		The level to adapt
	 * @return			A ListAdapter
	 */
	protected ListAdapter getLockedLevelAdapter(int level) {
		return new SimpleAdapter(m_context, 
				lockedLevelMap(level),
				R.layout.dagm_lockedlevel_item,
				s_lockedLevelFromBindings, s_lockedLevelToBindings);
	}
	
	
	/**
	 * Get a list Adapter that displays all unlocked training challenges
	 */
	public ListAdapter getTrainingListAdapter() {
		SeparatedListAdapter adapter = new SeparatedListAdapter(m_context);
		
		for( int level = m_minTrainingLevel; level <= m_maxTrainingLevel; level += 1 ) {
			int week = level - 100;
			String levelStr = "Week " + week;
			
			if ( level == m_minTrainingLevel || isLevelComplete(level - 1) ) {
				adapter.addSection(levelStr, getLevelAdapter(level, true));
			} else {
				// Show "Complete the previous level to unlock Level <next>!"
				break;
			}
		}
		
		return adapter;
	}

	/**
	 * Get a List Adapter that displays all unlocked challenges
	 */
	public ListAdapter getChallengeListAdapter() {
		SeparatedListAdapter adapter = new SeparatedListAdapter(m_context);
		
		Log.d(TAG, "minChallengeLevel = " + m_minChallengeLevel + ", maxChallengeLevel = " + m_maxChallengeLevel);
		
		for( int level = m_minChallengeLevel; level <= m_maxChallengeLevel; level += 1 ) {
			String levelStr = "Level " + level;
			
			if ( level == m_minChallengeLevel || isLevelComplete(level - 1) ) {
				adapter.addSection(levelStr, getLevelAdapter(level, false));
			} else {
				// Show "Complete the previous level to unlock Level <next>!"
				adapter.addSection(levelStr, getLockedLevelAdapter(level));
				break;
			}
		}
		
		return adapter;
	}
	
	/**
	 * @return List<Map> suitable for being passed to a SimpleAdapter 
	 */
	private List<Map<String, String>> levelMap(int level, boolean isTraining) {
		List<Map<String, String>> datamap = new ArrayList<Map<String,String>>();

		Log.d(TAG, "Level " + level + " has " + m_level2challenges.get(level).size() + " challenges");
		
		for(Challenge challenge : m_level2challenges.get(level)) {
			addChallengeToAdapterMap(datamap, challenge, isTraining);
		}
		
		return datamap;
	}

	/**
	 * 
	 * @param level
	 * @return
	 */
	protected List<Map<String, String>> lockedLevelMap(int level) {
		List<Map<String, String>> datamap = new ArrayList<Map<String,String>>();
		Map<String,String> rowVals = new HashMap<String, String>();

		rowVals.put("text", "Finish Level " + (level - 1) + " to unlock Level " + level);
		
		datamap.add(rowVals);
		
		return datamap;
	}

	
	/**
	 * Given a datamap that is being constructed for passing to a SimpleAdapter, 
	 * add a given challenge to the datamap.
	 * @param datamap		The datamap to modify
	 * @param challenge		The challenge to append to the datamap
	 */
	private void addChallengeToAdapterMap
	(List<Map<String, String>> datamap, Challenge challenge, boolean isTraining) 
	{
		Map<String,String> rowVals = new HashMap<String, String>();
		int highScore = m_highScoreData.getHighScore(challenge.getId());
		
		rowVals.put("id", Integer.toString(challenge.getId()));
		rowVals.put("title", challenge.getTitle());

		if ( highScore > 0 && !isTraining) {
			rowVals.put("score", Integer.toString(highScore));
		} else {
			rowVals.put("score", "");
		}
		
		datamap.add(rowVals);
	}
	
	/**
	 * Load data from the Challenge and Training assets into memory
	 */
	public void load() {
		Log.d(TAG, "Loading challenges and training");
		m_id2challenge.clear();
		m_level2challenges.clear();
		
		loadChallenges();
		loadTraining();
	}
	
	/**
	 * Loads the challenge data into memory from challenges.xml asset.
	 * @param context
	 */
	public void loadChallenges() {
		if ( m_isChallengesLoaded ) {
			Log.d(TAG, "Challenges were already loaded");
			return;
		}
		
		AssetManager assetManager = m_context.getAssets();
		ChallengeXmlHandler handler = new ChallengeXmlHandler();
		
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			InputStream stream = assetManager.open(FILE_CHALLENGES_XML);
			
			parser.parse(stream, handler, "");
		}
		catch(IOException e) {
			Log.e(TAG, "Unable to open asset challenges.xml");
			Log.e(TAG, "Caught exception " + e.getMessage());
			Log.e(TAG, e.getStackTrace().toString());
			throw new RuntimeException(e.getMessage());
		}
		catch(Exception e) {
			Log.e(TAG, "Caught exception " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		
		for(Challenge challenge : handler.getChallenges()) {
			addChallengeToMaps(challenge);
		}
		
		m_minChallengeLevel = handler.getMinLevel();
		m_maxChallengeLevel = handler.getMaxLevel();
		
		m_isChallengesLoaded = true;
		Log.i(TAG, "Loaded " + handler.getChallenges().size() + " challenges");
	}
	
	/**
	 * Loads the challenge data into memory from training.xml asset.
	 * @param context
	 */
	public void loadTraining() {
		if ( m_isTrainingLoaded ) {
			Log.d(TAG, "Challenges were already loaded");
			return;
		}
		
		AssetManager assetManager = m_context.getAssets();
		ChallengeXmlHandler handler = new ChallengeXmlHandler();
		
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			InputStream stream = assetManager.open(FILE_TRAINING_XML);
			
			parser.parse(stream, handler, "");
		}
		catch(IOException e) {
			Log.e(TAG, "Unable to open asset training.xml");
			Log.e(TAG, "Caught exception " + e.getMessage());
			Log.e(TAG, e.getStackTrace().toString());
			throw new RuntimeException(e.getMessage());
		}
		catch(Exception e) {
			Log.e(TAG, "Caught exception " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		
		for(Challenge challenge : handler.getChallenges()) {
			addChallengeToMaps(challenge);
		}
		
		m_minTrainingLevel = handler.getMinLevel();
		m_maxTrainingLevel = handler.getMaxLevel();
		
		m_isTrainingLoaded = true;
		Log.i(TAG, "Loaded " + handler.getChallenges().size() + " training challenges");
	}
	
	/**
	 * Adds a challenge to the loaded data maps
	 * @param challenge	The challenge to add
	 */
	private void addChallengeToMaps(Challenge challenge) {
		int id 		= challenge.getId();
		int level 	= challenge.getLevel();
		
		Log.d(TAG, "Add challenge " + challenge.getTitle() + " with id = " + id + " and level = " + level);
		
		m_id2challenge.put(id, challenge);

		if ( m_level2challenges.get(level) == null ) {
			m_level2challenges.put(level, new ArrayList<Challenge>());
		}
		
		m_level2challenges.get(level).add(challenge);
	}
	
}
