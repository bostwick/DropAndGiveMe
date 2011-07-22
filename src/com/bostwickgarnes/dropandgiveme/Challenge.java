package com.bostwickgarnes.dropandgiveme;

import java.util.ArrayList;
import java.util.List;

import com.bostwickgarnes.dropandgiveme.Exercise.ExerciseType;

import android.util.Log;

/**
 * Data object for a Challenge.
 */
public class Challenge {
	private static final String TAG = "Challenge";
	
	private int id;
	private int level;
	private String title;
	private String description;
	private boolean isTraining;
	private List<Round> rounds = new ArrayList<Round>();
	
	private int m_score = 0;
	private int m_roundIdx = 0;
	private boolean m_timeCountsUp = false;
	private ExerciseChangedListener m_onExerciseChanged;
		
	public static interface ExerciseChangedListener {
		public void onExerciseChanged();
	};
	
	private Round.ExerciseChangedListener onExerciseChanged = new Round.ExerciseChangedListener() {
		@Override
		public void onExerciseChanged() {
			Log.d(TAG, "Received ExerciseChanged message");
			notifyExerciseChangedListener();			
		}
	};
	
	public Challenge() { }
	
	public Challenge(int id, int level, String title, String description, List<Round> rounds) {
		this.id = id;
		this.level = level;
		this.title = title;
		this.description = description;
		this.rounds = rounds;
		
		getCurrentRound().setOnExerciseChangedListener(onExerciseChanged);
	}
	
	/**
	 * @return true if this is a valid Challenge that can be loaded,
	 * 			false otherwise
	 */
	public boolean isValid() {
		return (id != 0 && 
				level != 0 && 
				title != null && !title.equals("") &&
				description != null && !description.equals("") &&
				rounds != null);
	}
	
	public int getId() { return this.id; }
	public void setId(int id) { this.id = id; }
	
	public int getLevel() { return this.level; }
	public void setLevel(int level) { this.level = level; }
	
	public boolean isTraining() { return this.isTraining; }
	public void isTraining(boolean isTraining) {
		this.isTraining = isTraining;
	}
	
	public String getTitle() { return this.title; }
	public void setTitle(String title) { this.title = title; }
	
	public String getDescription() { return this.description; }
	public void setDescription(String desc) { this.description = desc; }
	
	public List<Round> getRounds() { return this.rounds; }
	public void setRounds(List<Round> rounds) { this.rounds = rounds; }
	public void addRound(Round r) { this.rounds.add(r); }
	
	/**
	 * @param n		Which round (0-indexed) to return
	 * @return		The nth round of this Challenge
	 */
	public Round getRound(int n) {
		return this.rounds.get(n);
	}
	
	public boolean timeCountsUp() { return m_timeCountsUp; }
	public void timeCountsUp(boolean val) { m_timeCountsUp = val; }
	
	public boolean repsCountUp() { return !m_timeCountsUp; }
	
	public void setOnExerciseChangedListener(ExerciseChangedListener e) {
		m_onExerciseChanged = e;
	}
	
	/**
	 * @return the total number of rounds in the Challenge
	 */
	public int getRoundCount(){
		return rounds.size();
	}
	


	/**
	 * @return The number of reps remaining in the current exercise.
	 */
	public int getCurrentRemainingReps() {
		if ( isFinished() ) { 
			Log.v(TAG, "Already finished.");
			return 0;
		} else {
			return getCurrentRound().getCurrentExercise().getReps();
		}
	}
	
	/**
	 * @return the Round this challenge is currently in
	 */
	protected Round getCurrentRound() {
		// Log.v(TAG, "m_roundIdx = " + m_roundIdx);
		return this.rounds.get(m_roundIdx);
	}
	
	/**
	 * @return	The name of the exercise that is currently being performed
	 */
	public String getExerciseName() {
		if ( isFinished() ) {
			return "Finished";
		} else {
			return getCurrentRound().getCurrentExercise().getName();
		}
	}
	
	public Exercise.ExerciseType getExerciseType() {
		if ( isFinished() ) {
			return ExerciseType.REST;
		} else {
			return getCurrentRound().getCurrentExercise().getType();
		}
	}
	
	private void notifyExerciseChangedListener() {
		if ( m_onExerciseChanged != null ) {
			Log.d(TAG, "notifying exercise changed listener");
			m_onExerciseChanged.onExerciseChanged();
		}
	}
	
	
	/**
	 * @return True if the challenge is complete, false otherwise
	 */
	public boolean isFinished() {
		return m_roundIdx >= rounds.size();
	}
	
	public int getScore(){
		return m_score;
	}
	
}
