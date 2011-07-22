package com.bostwickgarnes.dropandgiveme;

import android.util.Log;

/**
 * Data object for an Exercise in a Challenge Round.
 * @see Round
 */
public class Exercise {
	private static final String TAG = "Exercise";
	
	public static enum ExerciseType {
		PUSHUP, REST, FINISHED
	};
	
	public static Exercise DefaultExercise = 
		new Exercise(ExerciseType.FINISHED, 0, 0, 0, false);
	
	// To be used if time, reps or speed was not specified.
	public static final int NOT_GIVEN = -1;
		
	private ExerciseType type;			// What type of exercise is this?
	private int time;					// in seconds
	private int reps;					// number of reps to complete
	private int speed = 0;				// in reps/second, default 0
	private boolean consistentSpeed;	// true if speed should remain consistent
	
	private boolean m_repsCountUp = true;
	private boolean m_timeCountsUp = true;
	
	public Exercise(ExerciseType type) {
		this.type = type;
	}
	
	/**
	 * Copy constructor
	 * @param e		The Exercise to copy
	 */
	public Exercise(Exercise e) {
		this.type = e.type;
		this.time = e.time;
		this.reps = e.reps;
		this.speed = e.speed;
		this.consistentSpeed = e.consistentSpeed;
		
		this.m_repsCountUp = e.m_repsCountUp;
		this.m_timeCountsUp = e.m_timeCountsUp;
	}
	
	public static Exercise ofType(String type) {
		if ( type.equalsIgnoreCase("pushup") ) {
			return new Exercise(ExerciseType.PUSHUP);
		} else if ( type.equalsIgnoreCase("rest") ) {
			return new Exercise(ExerciseType.REST);
		} else {
			throw new RuntimeException("Invalid exercise type: " + type);
		}
	}
	
	public Exercise(ExerciseType type, int time, int reps, int speed, boolean isConsistentSpeed) {
		this.type = type;
		this.time = time;
		this.reps = reps;
		this.speed = speed;
		this.consistentSpeed = isConsistentSpeed;
	}
	
	public ExerciseType getType() { return this.type; }
	public void setType(ExerciseType type) { this.type = type; }
	
	public String getName() {
		switch (this.type) {
		case PUSHUP: return "Pushups";
		case REST: return "Rest";
		case FINISHED: return "Finished";
		default: return "Unknown";
		}
	}
	
	public int getTime() { return this.time; }
	public void setTime(int time) { this.time = time; }
	
	public int getReps() { return this.reps; }
	public void setReps(int reps) { this.reps = reps; }
	
	public int getSpeed() { return this.speed; }
	public void setSpeed(int speed) { this.speed = speed; }
	
	public boolean isConsistentSpeed() { return this.consistentSpeed; }
	public void isConsistentSpeed(boolean val) { this.consistentSpeed = val; }
	
	public void decideDirections() {
		m_repsCountUp 	= (this.reps == 0);
		m_timeCountsUp 	= (this.time == 0);
	}
	
	public boolean repsCountUp() {
		return m_repsCountUp;
	}
	
	public boolean timeCountsUp() {
		return m_timeCountsUp;
	}
	
	public void tickTime(){
		Log.v(TAG, "m_timeCountsUp = " + m_timeCountsUp);
		
		if ( m_timeCountsUp )
			this.time += 1;
		else
			this.time -= 1;
	}
	
	public void tickRep(){
		if ( m_repsCountUp )
			this.reps += 1;
		else
			this.reps -= 1;
	}

	public boolean isFinished() {
		return (!m_repsCountUp && this.reps == 0) ||
			   (!m_timeCountsUp && this.time == 0);
	}
	
	/**
	 * @return if the speed is not 0, then one has been specified
	 * and this is a minimum speed challenge
	 */
	public boolean isMinSpeed(){
		return !(this.speed==0);
	}
}
