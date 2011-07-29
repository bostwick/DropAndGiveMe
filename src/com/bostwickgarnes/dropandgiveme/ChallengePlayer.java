package com.bostwickgarnes.dropandgiveme;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import com.bostwickgarnes.dropandgiveme.Exercise.ExerciseType;

/**
 * Plays through a Challenge, maintaining score, time and rep counts as 
 * required
 */
public class ChallengePlayer {
	private static final String TAG = "ChallengePlayer";
	
	private static final String JSON_SCORE 			= "m_score";
	private static final String JSON_ROUND_IDX 		= "m_roundIdx";
	private static final String JSON_EXERCISE_IDX 	= "m_exerciseIdx";
	private static final String JSON_TOTAL_TIME 	= "m_totalElapsedTime";
	private static final String JSON_NONREST_TIME 	= "m_nonRestElapsedTime";
	private static final String JSON_TOTAL_REPS 	= "m_totalReps";
	private static final String JSON_EXERCISE_TIME 	= "m_exerciseTime";
	private static final String JSON_EXERCISE_REPS 	= "m_exerciseReps";
	private static final String JSON_TIME_DIR 		= "m_timeCountsUp";
	private static final String JSON_ISFINISHED 	= "m_isFinished";
	private static final String JSON_CUR_SPEED 		= "m_currentSpeed";
	private static final String JSON_LAST_SPEED 	= "m_lastRecordedSpeed";
	private static final String JSON_LAST_REP_TIME 	= "m_lastRepTime";
	private static final String JSON_CHALLENGE_ID 	= "m_challenge";
	
	public static interface ExerciseChangedListener {
		public void onExerciseChanged();
	};
	
	public static interface ChallengeOverListener { 
		public void challengeOver();
	}
		
	public static interface CheatDetectionListener {
		public void cheatDetected(ExerciseType exercise);
	}
	
	private int m_score 				= 0;
	private int m_roundIdx 				= 0;
	private int m_exerciseIdx 			= 0;
	private int m_totalElapsedTime 		= 0;
	private int m_nonRestElapsedTime	= 0; //only incremented when not resting
	private int m_totalReps 			= 0;
	private int m_exerciseTime			= 0;
	private int m_exerciseReps			= 0;
	private boolean m_timeCountsUp 		= false;
	private boolean	m_isFinished		= false;
	private double m_currentSpeed		= 0;
	private double m_lastRecordedSpeed	= 0;
	private long m_lastRepTime			= System.currentTimeMillis();
	
	private Challenge m_challenge;
	
	private ExerciseChangedListener m_onExerciseChanged;
	private ChallengeOverListener m_challengeOverListener;
	private CheatDetectionListener m_cheatDetectionListener;

	/**
	 * Return a string representation of ChallengePlayer
	 */
	public String toString() {
		return toJson();
	}
	
	/**
	 * Serializes this ChallengePlayer to a JSON representation. 
	 * @see	fromJson
	 * @return	A json-string representing this ChallengePlayer
	 */
	public String toJson() {
		JSONObject json = new JSONObject();
		
		try {
			json.put(JSON_SCORE, m_score);
			json.put(JSON_ROUND_IDX, m_roundIdx);
			json.put(JSON_EXERCISE_IDX, m_exerciseIdx);
			json.put(JSON_TOTAL_TIME, m_totalElapsedTime);
			json.put(JSON_NONREST_TIME, m_nonRestElapsedTime);
			json.put(JSON_TOTAL_REPS, m_totalReps);
			json.put(JSON_EXERCISE_TIME, m_exerciseTime);
			json.put(JSON_EXERCISE_REPS, m_exerciseReps);
			json.put(JSON_TIME_DIR, m_timeCountsUp);
			json.put(JSON_ISFINISHED, m_isFinished);
			json.put(JSON_CUR_SPEED, m_currentSpeed);
			json.put(JSON_LAST_SPEED, m_lastRecordedSpeed);
			json.put(JSON_LAST_REP_TIME, m_lastRepTime);
			json.put(JSON_CHALLENGE_ID, m_challenge.getId());
		} catch (JSONException e) {
			Log.e(TAG, "Error serializing to JSON: " + e.getMessage());
		}
		
		return json.toString();
	}
	
	/**
	 * De-Serializes a JSON representation of a ChallengePlayer. The returned
	 * ChallengePlayer will not have any listeners attached to it, so be sure to
	 * re-attach any ExerciseChangedListeners or ChallengeOverListeners you need.
	 * @param json	A string containing a json representation of a ChallengePlayer
	 * @return		A new ChallengePlayer object based on the de-serialized values
	 */
	public static ChallengePlayer fromJson(Context context, String json) {
		JSONObject data = null;
		ChallengePlayer player = new ChallengePlayer();
		
		try {
			data = new JSONObject(json);
		} catch(JSONException e) {
			Log.e(TAG, "Error de-serializing json: " + e.getMessage());
			throw new RuntimeException("Error de-serializing json: " + e.getMessage());
		}
		
		try {
			player.m_score 				= data.getInt(JSON_SCORE);
			player.m_roundIdx 			= data.getInt(JSON_ROUND_IDX);
			player.m_exerciseIdx		= data.getInt(JSON_EXERCISE_IDX);
			player.m_totalElapsedTime	= data.getInt(JSON_TOTAL_TIME);
			player.m_nonRestElapsedTime	= data.getInt(JSON_NONREST_TIME);
			player.m_totalReps			= data.getInt(JSON_TOTAL_REPS);
			player.m_exerciseTime		= data.getInt(JSON_EXERCISE_TIME);
			player.m_exerciseReps		= data.getInt(JSON_EXERCISE_REPS);
			player.m_timeCountsUp		= data.getBoolean(JSON_TIME_DIR);
			player.m_isFinished			= data.getBoolean(JSON_ISFINISHED);
			player.m_currentSpeed		= data.getDouble(JSON_CUR_SPEED);
			player.m_lastRecordedSpeed	= data.getDouble(JSON_LAST_SPEED);
			player.m_lastRepTime		= data.getLong(JSON_LAST_REP_TIME);
		} catch (JSONException e) {
			Log.e(TAG, "Error de-serializing json: " + e.getMessage());
			throw new RuntimeException("Error de-serializing json: " + e.getMessage());
		}
		
		try {
			int challengeId = data.getInt(JSON_CHALLENGE_ID);
			player.m_challenge = ChallengeData.getInstance(context).getChallenge(challengeId);
		} catch(JSONException e){
			Log.e(TAG, "Error de-serializing json: " + e.getMessage());
			throw new RuntimeException("Error de-serializing json: " + e.getMessage());
		}
		
		return player;
	}
	
	/**
	 * Create a new ChallengeRunner.
	 * @param c		The Challenge to run.
	 */
	public ChallengePlayer(Challenge c) {
		m_challenge = c;
		firstRound();
		firstExercise();
		
		Log.v(TAG, this.toString());
	}
	
	/**
	 * Private constructor. Used only when de-serializing.
	 */
	private ChallengePlayer() {
		
	}
	
	/**
	 * Listener to be called each time an exercise changes.
	 * @param listener	The ExerciseChangedListener to call
	 */
	public void setExerciseChangedListener(ExerciseChangedListener listener) {
		m_onExerciseChanged = listener;
	}
	
	/**
	 * Sets the listener to be called when the challenge being play is over.
	 * @param listener	A ChallengeOverListener to be called.
	 */
	public void setChallengeOverListener(ChallengeOverListener listener) {
		m_challengeOverListener = listener;
	}
	
	/**
	 * Sets the listener to be called when cheating is detected.
	 * @param listener	A CheatDetectedListener to be called
	 */
	public void setCheatDetectionListener(CheatDetectionListener listener) {
		m_cheatDetectionListener = listener;
	}
	
	/**
	 * Notifies the exercise changed listener that the current exercise has 
	 * been changed
	 */
	protected void notifyExerciseChangedListener() {
		if ( m_onExerciseChanged != null ) {
			m_onExerciseChanged.onExerciseChanged();
		}
	}
	
	/**
	 * Notifies the attached ChallengeOverListener that the challenge has 
	 * finished.
	 */
	protected void notifyChallengeOverListener() {
		if ( m_challengeOverListener != null ) {
			m_challengeOverListener.challengeOver();
		}
	}
	
	/**
	 * Notifies the attached CheatDetectionListener that cheating has been
	 * detected.
	 */
	protected void notifyCheatDetectionListener() {
		if ( m_cheatDetectionListener != null ) {
			m_cheatDetectionListener.cheatDetected(getExerciseType());
		}
	}
	
	/**
	 * @return The speed of the current exercise in RPM
	 */
	public int getMinSpeed(){
		return getCurrentExercise().getSpeed();
	}
	
	/**
	 * @return	The title of the Challenge being played
	 */
	public String getTitle() {
		return m_challenge.getTitle();
	}
	
	/**
	 * @return	The total number of rounds in the challenge
	 */
	public int getRoundCount() {
		return m_challenge.getRoundCount();
	}
	
	/**
	 * @return	Which round we're currently on. 0-indexed.
	 */
	public int getCurrentRound() {
		return m_roundIdx;
	}
	
	/**
	 * @return	The number of rounds remaining in the Challenge after the current round.
	 */
	public int getRemainingRounds() {
		return getRoundCount() - getCurrentRound() - 1;
	}
	
	/**
	 * @return	The number of exercises in the current Round
	 */
	public int getRoundExerciseCount() {
		return m_challenge.getRound(m_roundIdx).getExerciseCount();
	}
	
	/**
	 * @return The movement the user should currently be performing
	 */
	protected Exercise getCurrentExercise() {
		if ( m_exerciseIdx >=  getRoundExerciseCount() ) {
			Log.d(TAG, "Returning default exercise");
			return Exercise.DefaultExercise;
		} else {
			return m_challenge.getRound(m_roundIdx).getExercise(m_exerciseIdx);
		}
	}
	
	/**
	 * @return	The name of the movement the user should currently be performing
	 */
	public String getExerciseName() {
		return getCurrentExercise().getName();
	}
	
	/**
	 * @return	The type of the movement the user should currently be performing
	 */
	public Exercise.ExerciseType getExerciseType() {
		return getCurrentExercise().getType();
	}
	
	/**
	 * Returns either the total number of reps completed, or the reps
	 * remaining in the current round, depending on whether the wrapped
	 * Challenge counts reps up or down
	 * @return 	The significant number of challenge reps 
	 */
	public int getReps() {
		if ( m_challenge.repsCountUp() ) {
			Log.v(TAG, m_totalReps + " reps counted");
			return m_totalReps;
		} else {
			return m_exerciseReps;
		}
	}
	
	/**
	 * Returns the total number of reps completed by the user in the Challenge
	 * @return	Total reps
	 */
	public int getTotalReps() {
		return m_totalReps;
	}
	
	/**
	 * @return	A textual representation suitable for display containing
	 * 			either the total reps completed or the number of reps remaining
	 * 			for the current exercise, as appropriate.
	 */
	public String getRepsText() {
		if ( getCurrentExercise().repsCountUp() ) {
			return (m_exerciseReps + " completed!");
		} else {
			return (m_exerciseReps + " remaining!");
		}
	}
	
	/**
	 * Returns either the amount of total time elapsed if the challenge is 
	 * rep-oriented, or the amount of total challenge time remaining if the
	 * the challenge is time-oriented
	 * @return	The challenge time in seconds
	 */
	public int getTime() {
		if ( m_challenge.timeCountsUp() ) {
			if ( !getCurrentExercise().timeCountsUp() ) {
				return m_exerciseTime;
			} else {
				return m_totalElapsedTime;				
			}
		} else {
			return m_exerciseTime;
		}
	}
	
	/**
	 * Decide if the current exercise is rest.
	 * @return is the exercise rest?
	 */
	public boolean isExerciseRest(){
		ExerciseType type = getExerciseType();
		return type.equals(ExerciseType.REST);
	}
	
	/**
	 * Returns the total elapsed time in the Challenge
	 * @return	time as seconds.
	 */
	public int getElapsedTime() {
		return m_totalElapsedTime;
	}
	
	/**
	 * @return	true if the current round is the final round in the 
	 * 			challenge, otherwise false
	 */
	public boolean isLastRound() {
		return m_roundIdx == (m_challenge.getRoundCount() - 1);
	}
	
	public int getExerciseTime(){
		return m_exerciseTime;
	}
	
	/**
	 * @return	true if the current exercise is the final exericse in the 
	 * 			current round, otherwise false.
	 */
	public boolean isLastExercise() {
		return m_exerciseIdx == (getRoundExerciseCount() - 1);
	}
	
	/**
	 * Returns true if the challenge has finished running.
	 * A challenge is finished when all rounds have been played.
	 * @return
	 */
	public boolean isFinished() {
		return m_isFinished;
	}
	
	/**
	 * @return	true if the current round is finished, otherwise false
	 */
	public boolean isRoundFinished() {
		Round r = m_challenge.getRound(m_roundIdx);
		
		return (m_exerciseIdx >= r.getExerciseCount());
	}
	
	/**
	 * @return	true if the current exercise is finished, otherwise false
	 */
	public boolean isExerciseFinished() {
		Exercise e = getCurrentExercise();
		
		return 	(e.repsCountUp() && m_exerciseTime <= 0) ||
				(e.timeCountsUp() && m_exerciseReps <= 0);
	}
	
	/**
	 * Advances the Challenge to the next round
	 */
	public void advanceRound() {
		if ( isFinished() ) {
			Log.v(TAG, "Already finished. Not advancing round");
		} else if ( isLastRound() ) { 
			m_isFinished = true;
		} else {
			Log.v(TAG, "Advancing round");
			m_roundIdx += 1;
			firstExercise();
		}
	}

	/**
	 * Move to the first Round in a Challenge
	 */
	public void firstRound() {
		m_roundIdx = 0;
	}
	
	/**
	 * Move to the first Exercise in a Round
	 */
	public void firstExercise() {
		m_exerciseIdx = 0;
		
		m_exerciseReps = getCurrentExercise().getReps();
		m_exerciseTime = getCurrentExercise().getTime();
		
		notifyExerciseChangedListener();
	}
	
	/**
	 * Advances the Challenge to the next exercise in the Round
	 */
	public void advanceExercise() {
		if ( isFinished() ) {
			Log.v(TAG, "Already finished. Not advancing exercise");
		} else if ( isLastExercise() ) {
			advanceRound();
		} else {
			Log.v(TAG, "Advancing exercise");
			m_exerciseIdx += 1;
			
			m_exerciseReps = getCurrentExercise().getReps();
			m_exerciseTime = getCurrentExercise().getTime();
			
			notifyExerciseChangedListener();
		}
	}
	
	/**
	 * Returns the score of the challenge
	 * @return
	 */
	public int getScore() {
		return m_score;
	}
	
	private void consistentGameOverCheck() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Update the current speed to be reps/time.
	 */
	public void updateSpeed(){
		long now = System.currentTimeMillis();

		m_lastRecordedSpeed = m_currentSpeed;
		m_currentSpeed = ((1.0 / (now - m_lastRepTime)) * (1000.0 * 60));

		Log.d(TAG,"Speed is: " + m_currentSpeed);
		setLastRepTime();
	}

	/**
	 * Set the time at which the last rep occurred to now.
	 */
	public void setLastRepTime() {
		long now = System.currentTimeMillis();
		m_lastRepTime = now;
	}
	
	/**
	 * @return Is the current challenge a consistent challenge
	 */
	public boolean isExerciseConsistent(){
		return getCurrentExercise().isConsistentSpeed();
	}
	
	/**
	 * update the score to be ((1/speed difference) * 1000) 
	 */
	public void updateConsistentScore(){
		m_score = (int)(Math.round((((1/speedDifference()) * 10))));
	}
	
	/**
	 * Return the absolute value of the difference between the last recorded speed, and the current speed.
	 */
	public double speedDifference(){
		double difference = Math.abs(m_currentSpeed-m_lastRecordedSpeed);
		Log.d(TAG,"Speed Difference is: " + difference);
		return difference;
	}

	/**
	 * Detects whether the user is cheating or not. 
	 * @return	true if the user is cheating, false otherwise
	 */
	protected boolean isCheating() {
		if ( getExerciseType() == ExerciseType.PUSHUP && 
			 getCurrentExerciseSpeed() >= 125 ) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Add one to the number of total reps completed. If the current
	 * exercise is finished, notify the exerciseChangedListener. Advance
	 * the round if it is completed. Called when a rep of the current exercise
	 * is completed.
	 */
	public void tickRep(){
		Log.v(TAG, "ticking rep");
		Log.v(TAG, this.toString());

		if ( isFinished() ) { 
			Log.v(TAG, "Already finished. Not counting rep.");
		} else if ( isRoundFinished() ) {
			advanceRound();
	 	} else if ( isExerciseFinished() ) {
			advanceExercise();
		} else {
			m_totalReps 	+= 1;
			m_exerciseReps 	+= getCurrentExercise().repsCountUp() ? 1 : -1;
			
			// if this is a minimum speed exercise then update the speed and 
			// check if the game is over
			if ( isMinSpeedExercise() ){
				minSpeedGameOverCheck();
			}
			
			// if the exercise is consistent, deal with speed and check if the game is over.
			if ( isExerciseConsistent() ) {
				updateSpeed();
				//updateConsistentScore();
				consistentGameOverCheck();
			}
			
			if ( isCheating() ) notifyCheatDetectionListener();
			updateSpeed();
			updateScore();
		}
	}
	
	/**
	 * Called when a second has passed while playing a challenge.
	 * Updates appropriate time-related variables.
	 */
	public void tickTime() {
		Log.v(TAG, "ticking time");
		Log.v(TAG, this.toString());
		
		if ( isFinished() ) {
			Log.v(TAG, "Already finished. Not counting second");
		} else if ( isRoundFinished() ) {
			advanceRound();
		} else if ( isExerciseFinished() ) {
			advanceExercise();
		} else {
			if ( !isExerciseRest() ) {
				m_nonRestElapsedTime += 1;
			}
			
			m_totalElapsedTime += 1;
			m_exerciseTime -= 1;
			
			// if this is a minimum speed exercise then update the speed and 
			// check if the game is over
			if ( isMinSpeedExercise() ) {
				//updateSpeed();
				minSpeedGameOverCheck();
			}
			if ( isExerciseConsistent() ) {
				consistentGameOverCheck();
			}
			
			if ( isCheating() ) notifyCheatDetectionListener();
			updateScore();
		}
	}
	
	/**
	 * @return Current exercise speed in RPM rounded.
	 */
	public int getCurrentExerciseSpeed(){
		return (int) Math.round(m_currentSpeed);
	}

	/**
	 * 
	 * @return Is the current exercise minimum speed?
	 */
	public boolean isCurrentExerciseMinSpeed(){
		return getCurrentExercise().isMinSpeed();
	}
	
	/**
	 * Is the current speed less than the defined exercise minimum speed? If so,
	 * end the game and display a game over screen.
	 */
	private void minSpeedGameOverCheck() { 
		if ( (m_currentSpeed < getMinSpeed()) && (m_totalReps >= 2)) {
			Log.d(TAG, "Game Over - speed dropped below minumum challenge speed");
			notifyChallengeOverListener();
		}
	}

	/**
	 *  If this exercise has a speed (other than 0) defined, then return true
	 * @return Is this exercise a minimum speed exercise?
	 */
	private boolean isMinSpeedExercise() {
		return getCurrentExercise().isMinSpeed();
	}

	/**
	 * Updates the score based on reps and time.
	 */
	public void updateScore() {
		m_score = Math.round(((float)m_totalReps / (float)m_nonRestElapsedTime) * 1000);
	}
}
