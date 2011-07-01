package com.bostwickgarnes.dropandgiveme;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * Data object for a Round in a Challenge.
 * @see Challenge
 */
public class Round {
	private static final String TAG = "Round";
	
	public static interface ExerciseChangedListener {
		public void onExerciseChanged();
	};
	
	private List<Exercise> exercises = new ArrayList<Exercise>();
	
	private int m_exerciseIdx = 0;
	
	private ExerciseChangedListener m_onExerciseChanged;
	
	public Round() { }
 	
	/**
	 * Copy constructor
	 * @param r		The Round to copy
	 */
	public Round(Round r) {
		for(Exercise e : r.exercises) {
			this.exercises.add(new Exercise(e));
		}
	}
	
	public Round(List<Exercise> exercises) {
		this.exercises = exercises;
	}
	
	public List<Exercise> getExercises() { return this.exercises; }
	public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }
	public void addExercise(Exercise e) { this.exercises.add(e); }
	
	/**
	 * @param n	Which exercise (0-indexed) to get
	 * @return	The nth exercise in this Round
	 */
	public Exercise getExercise(int n) {
		return this.exercises.get(n);
	}
	
	/**
	 * @return	The total number of exercises in this round
	 */
	public int getExerciseCount() {
		return this.exercises.size();
	}
	
	public void setOnExerciseChangedListener(ExerciseChangedListener e) {
		m_onExerciseChanged = e;
	}
	
	private void notifyExerciseChangedListener() {
		if ( m_onExerciseChanged != null ) {
			Log.d(TAG, "notifying exerciseChangedListener");
			m_onExerciseChanged.onExerciseChanged();
		}
	}

	
	public boolean isValid() { 
		return (exercises != null && exercises.size() > 0);
	}
	
	public Exercise getCurrentExercise() {
		if ( m_exerciseIdx >= this.exercises.size() )
			return this.exercises.get(this.exercises.size() - 1);
		else 
			return this.exercises.get(m_exerciseIdx);
	}
	
	public void advanceExercise() {
		Log.i(TAG, "Advancing to next exercise");
		m_exerciseIdx += 1;	
		notifyExerciseChangedListener();
	}
	
	public boolean isFinished() {
		return m_exerciseIdx >= this.exercises.size();
	}
	
	public void tickRep() {
		Log.v(TAG, "tickRep");
		getCurrentExercise().tickRep();
		
		if ( getCurrentExercise().isFinished() ) {
			advanceExercise();
		}
	}
	
	public void tickTime() {
		Log.v(TAG, "tickTime");
		getCurrentExercise().tickTime();
		
		if ( getCurrentExercise().isFinished() ) {
			advanceExercise();
		}
	}
	
	public int getRemainingReps(){
		int reps = 0;
		
		for(Exercise e : this.exercises) {
			reps += e.getReps();
		}
		
		return reps;
	}
	
	public int getRemainingTime() {
		int time = 0;
		
		for(Exercise e : this.exercises) {
			time += e.getTime();
		}
		
		Log.v(TAG, "Time remaining: " + time);
		return time;
	}

}
