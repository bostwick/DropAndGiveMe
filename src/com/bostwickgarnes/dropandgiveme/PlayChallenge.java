package com.bostwickgarnes.dropandgiveme;

import java.util.Locale;
import java.util.Random;

import com.bostwickgarnes.dropandgiveme.R;
import com.bostwickgarnes.dropandgiveme.Exercise.ExerciseType;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PlayChallenge extends Activity implements OnInitListener {
	private static final String TAG = "PlayChallenge";
	
	private static final String WAKELOCK_TAG = "DAGM_WAKELOCK";
	private static final String BUNDLE_CHALLENGEPLAYER = "m_challengePlayer";
	private static final int DIALOG_PUSHUP_CHEATING = 1;
	
	
	/**
	 * Required Intent Extra that specifies which challenge to
	 * show the information for.
	 */
	public static final String EXTRA_CHALLENGE_ID = "challenge_id";

	/**
	 * Intent Extra. Set to "true" if this is a training challenge.
	 */
	public static final String EXTRA_IS_TRAINING = "isTraining";
	
	/**
	 * Intent Extra. Set to "true" if this is a friend challenge.
	 */
	public static final String EXTRA_IS_FRIEND_CHALLENGE = "isFriend";
	
	/**
	 * Intent Extra. Holds the friend's id to challenge.
	 */
	public static final String EXTRA_FRIEND_ID = "friendId";
	
	/**
	 * Intent Extra. Holds the friend's name to challenge
	 */
	public static final String EXTRA_FRIEND_NAME = "friendName";

	private ChallengePlayer m_challengePlayer;
	private PowerManager.WakeLock m_wakeLock = null;
	
	private boolean m_isTraining = false;
	
	private boolean m_playEncouragement = true;
	private boolean m_playSoundEffects = true;
	
	private ChallengeData m_challengeData;
	private SettingsData m_settingsData;
	
	/*****************************
	 * Text to Speech Fields
	 *****************************/
	
	TextToSpeech m_tts;
	
	private int m_motivateTime = 7; // initial time to play motivation
	
    private static final Random RANDOM = new Random();
    
    private static final String[] MOTIVATIONS = {
      "Good job!",
      "Keep it up!",
      "Push it!",
      "Nice work!",
      "Excellent!",
      "Keep going!",
      "Come on, let's go!",
      "You can do better than that!",
      "Push it!",
      "Faster!",
      "You can do it!",
      "Keep up the pace!",
      "You got this!",
      "Don't stop now!",
      "Be aggressive!"
    };

    //////////////////////////////////////////////////// End of Text To Speech Fields.
    
    PlaySounds m_playSounds = new PlaySounds(this, m_playSoundEffects);
	
	/**
	 * OnClick Handler for the target
	 */
	View.OnClickListener onTargetClicked = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			m_challengePlayer.tickRep();
			m_playSounds.play(R.raw.dagm_rep_count);
			showChallenge();
		}
	};
	
	ChallengePlayer.ExerciseChangedListener onExerciseChanged = new ChallengePlayer.ExerciseChangedListener() {
		@Override
		public void onExerciseChanged() {
			Log.d(TAG, "Exercise Changed. Updating Display.");
			updateDisplay();
		}
	};
	
	ChallengePlayer.ChallengeOverListener onChallengeOver = new ChallengePlayer.ChallengeOverListener() {
		@Override
		public void challengeOver() {
			endChallenge(false);
		}
	};
	
	ChallengePlayer.CheatDetectionListener onCheatDetected = new ChallengePlayer.CheatDetectionListener() {
		@Override
		public void cheatDetected(ExerciseType exercise) {
			if ( exercise == ExerciseType.PUSHUP ) {
				showDialog(DIALOG_PUSHUP_CHEATING);
			}
		}
	};
	
	// timer 
	// the amount of time left in the game, initially 180 seconds (3 minutes)
	//private int m_remainingTime = 180;
	
	// the frequency the clock is updated, one second
    private final long mFrequency = 1000; // milliseconds
    private final int TICK_WHAT = 2;
    
    // check if the remaining time is 0, if so, end the game. 
    // if not, update the time and send a new message.
    private Handler mHandler = new Handler() {
    	public void handleMessage(Message m) {
    		m_challengePlayer.tickTime();
    		
    		showChallenge();
    		if(m_challengePlayer.getElapsedTime()==m_motivateTime){
    			if ( !( m_challengePlayer.isExerciseRest()) ) {
    				playMotivation();
    			}
    			// add 7 - 15 seconds to the time to play the next motivation
    			m_motivateTime += (15 - RANDOM.nextInt(7));
    		}
    		
    		if ( m_challengePlayer.isFinished() ){
    			Log.d(TAG, "Game Over - Challenge is won.");
    			endChallenge(true);
    		} else {
	    		sendMessageDelayed(Message.obtain(this, TICK_WHAT), mFrequency);
    		}
    	}
    };
    
    /**
     * Read aloud a random motivation phrase.
     */
    private void playMotivation(){
    	// make sure m_tts is not null
    	if(m_tts == null){return;}
    	
    	// if the setting for playing encouragement is set to false, do not play anything
    	if(!m_playEncouragement) {return;}
    	
    	// Select a random hello.
        int motivationsLength = MOTIVATIONS.length;
        String motivation = MOTIVATIONS[RANDOM.nextInt(motivationsLength)];
        m_tts.speak(motivation,
            TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
            null);
    }
	
	/**
	 * @return	The Challenge ID passed to this activity as an intent extra
	 */
	public int getChallengeId() {
		return getIntent().getExtras().getInt(EXTRA_CHALLENGE_ID);
	}
	
	/**
	 * @return	The Friend ID passed to this activity as an intent extra
	 */
	public String getFriendId() {
		return getIntent().getExtras().getString(EXTRA_FRIEND_ID);
	}
	
	/**
	 * @return	The Friend name passed to this activity as an intent extra
	 */
	public String getFriendName() {
		return getIntent().getExtras().getString(EXTRA_FRIEND_NAME);
	}
	
	/**
	 * @return	Is the current challenge a friend challenge?
	 */
	public Boolean isFriend() {
		return getIntent().getExtras().getBoolean(EXTRA_IS_FRIEND_CHALLENGE);
	}
	
	/**
	 * @return	Whether or not this is a training challenge
	 */
	public boolean isTraining() {
		return getIntent().getExtras().getBoolean(EXTRA_IS_TRAINING);
	}
	
    
	public int getScore() {
		return m_challengePlayer.getScore();
	}
	
	/**
	 * Acquires a wakelock so the screen doesn't turn off during longer Challenges
	 */
	private void acquireWakelock() {
    	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    	m_wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, WAKELOCK_TAG);
    	m_wakeLock.acquire();
	}
	
	/**
	 * Releases the wakelock
	 */
	private void releaseWakelock() {
    	if ( m_wakeLock != null ) {
    		m_wakeLock.release();
    	}
	}
	
	/**
	 * Detects whether or not this is a training challenge and sets 
	 * m_isTraining as appropriate. Called from onCreate.
	 */
	private void detectTrainingChallenge() {
		m_isTraining = getIntent().getExtras().getBoolean(EXTRA_IS_TRAINING);
	}
	

	/**
	 * Get the Challenge from the ID passed to this Activity
	 * @return	The Challenge
	 */
	private Challenge getChallenge() {
		return m_challengeData.getChallenge(getChallengeId());
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.dagm_playchallenge);
      
      m_challengeData = ChallengeData.getInstance(this);
      m_settingsData = new SettingsData(this);
      
      detectTrainingChallenge();
      
      loadSettings();
      
      // this makes it so that the volume buttons on the phone 
      // adjust the media volume instead of the ring volume while
      // in PlayChallenge. This is useful because at the moment we don't have
      // an option to toggle sound.
      setVolumeControlStream(AudioManager.STREAM_MUSIC);
      
      m_challengePlayer = new ChallengePlayer(getChallenge());
      m_challengePlayer.setExerciseChangedListener(onExerciseChanged);
      m_challengePlayer.setChallengeOverListener(onChallengeOver);
      m_challengePlayer.setCheatDetectionListener(onCheatDetected);
      
      m_tts = new TextToSpeech(this,this);
      
      Log.d(TAG, "Starting Challenge " + m_challengePlayer.getTitle());
      
      setTargetHandler();
      
      showChallenge();
      updateDisplay();
      m_playSounds.play(R.raw.dagm_start_bell);
	}
	
    // initialize the text to speech language to US, the only supported locale
    public void onInit(int status) {
    	if (status == TextToSpeech.SUCCESS) {
            int result = m_tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Lanuage data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            }} else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }
    
    // Initialize the settings needed for this activity
    private void loadSettings(){
		m_playEncouragement = m_settingsData.getSettingValue(SettingsData.ENCOURAGEMENT_SETTING, true);
		m_playSoundEffects = m_settingsData.getSettingValue(SettingsData.SOUND_EFFECTS_SETTING, true);
		
		m_playSounds.setEnabled(m_playSoundEffects);
    }
	
	@Override
	public void onResume() {
		super.onResume();
		
		acquireWakelock();
		mHandler.sendMessage(Message.obtain(mHandler, TICK_WHAT));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		releaseWakelock();
		mHandler.removeMessages(TICK_WHAT);
	}
	
	@Override
    public void onDestroy() {
        // Don't forget to shutdown!
        if (m_tts != null) {
            m_tts.stop();
            m_tts.shutdown();
        }

        super.onDestroy();
    }

	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(BUNDLE_CHALLENGEPLAYER, m_challengePlayer.toJson());
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if ( savedInstanceState != null ) {
			String playerJson;
			
			if ( (playerJson = savedInstanceState.getString(BUNDLE_CHALLENGEPLAYER))  != null) {
				m_challengePlayer = ChallengePlayer.fromJson(this, playerJson);
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch(id) {
		case DIALOG_PUSHUP_CHEATING:
			builder.setTitle("Seriously?")
				.setMessage("Just pushing the button doesn't make you stronger. Get down and do your damn pushups.")
				.setCancelable(false)
				.setNeutralButton("Fine.", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						endChallenge(false);
					}
				});
			dialog = builder.create();
			break;
			 	
		default:
			dialog = null;
		}
		
		return dialog;
	}
	
	/**
	 * Set the sets TextView to display the correct number of rounds remaining.
	 * Does not count rest rounds.
	 */
	private void setSets() { 
		TextView setsbox = (TextView)findViewById(R.id.dagm_challenge_sets);
		  
		if (m_challengePlayer.getRoundCount() == 1){
			setsbox.setVisibility(View.GONE);
		} else if (m_challengePlayer.getRemainingRounds() == 0){
			setsbox.setText("Last round, PUSH IT!");
			setsbox.setVisibility(View.VISIBLE);
		} else if (m_challengePlayer.getRemainingRounds() == 1){
			setsbox.setText(m_challengePlayer.getRemainingRounds() + " round left");
			setsbox.setVisibility(View.VISIBLE);
		}
		else {
			setsbox.setText(m_challengePlayer.getRemainingRounds() + " rounds left");
			setsbox.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Ends the Challenge and passes control to ChallengeResult
	 * @param isWin		true if the player won the challenge, 
	 * 					false if the player failed the challenge
	 */
	public void endChallenge(boolean isWin) {
		Intent challengeResult = new Intent(this, ChallengeResult.class);
		challengeResult.putExtra(ChallengeResult.EXTRA_CHALLENGE_ID, getChallengeId());
		challengeResult.putExtra(ChallengeResult.EXTRA_REPS, m_challengePlayer.getTotalReps());
		challengeResult.putExtra(ChallengeResult.EXTRA_TIME, m_challengePlayer.getElapsedTime());
		challengeResult.putExtra(ChallengeResult.EXTRA_SCORE, getScore());
		challengeResult.putExtra(ChallengeResult.EXTRA_IS_WIN, isWin);
		challengeResult.putExtra(ChallengeResult.EXTRA_IS_TRAINING, m_isTraining);
		challengeResult.putExtra(ChallengeResult.EXTRA_FRIEND_ID, getFriendId());
		challengeResult.putExtra(ChallengeResult.EXTRA_FRIEND_NAME, getFriendName());
		challengeResult.putExtra(ChallengeResult.EXTRA_IS_FRIEND_CHALLENGE, isFriend());
		
		// challengeResult.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		
		if ( isWin ) { 
			m_playSounds.play(R.raw.boggle_wordadded);
		} else{
			m_playSounds.play(R.raw.dagm_sad_trombone);
		}
		
		startActivity(challengeResult);
		finish();
	}
	
	private void setTargetHandler() {
		ImageView target = (ImageView)findViewById(R.id.dagm_pushup_target);
		target.setOnClickListener(onTargetClicked);		
	}

	private void setReps(int reps) {
		TextView repsbox = (TextView)findViewById(R.id.dagm_challenge_reps);
		repsbox.setText(m_challengePlayer.getRepsText());
	}

	private void setChallengeTitle(){
		TextView title = (TextView)findViewById(R.id.dagm_challenge_title);
		title.setText(m_challengePlayer.getTitle());
	}
	
	private void setPoints(int points){
		TextView scorebox = (TextView)findViewById(R.id.dagm_challenge_score);
		
		if ( m_isTraining ) {
			scorebox.setText("");
		} else {
			scorebox.setText(points + " pts");
		}
	}
	
	private void setTime(String time){
		TextView timebox = (TextView)findViewById(R.id.dagm_challenge_time);
		timebox.setText(time);
	}
	
	private void setExercise(String exerciseName) {
		TextView exerciseBox = (TextView)findViewById(R.id.dagm_challenge_exercisetype);
		exerciseBox.setText(exerciseName);
	}
	
	private void setTimeLeft() {
		TextView timeLeft = (TextView)findViewById(R.id.dagm_challenge_timeleft);
		String time = formatTime(m_challengePlayer.getExerciseTime());
		
		//Log.d(TAG,"Setting timeLeft to time: " + time);
		timeLeft.setText(time);
	}
	
	/**
	 * Displays the current state of the challenge
	 */
	private void showChallenge() {
		setExercise(m_challengePlayer.getExerciseName());
		setPoints(m_challengePlayer.getScore());
		setTime(getCurrentTime());
		setReps(m_challengePlayer.getReps()); 
		setSets();
		setTimeLeft();
		setSpeed();
	}

	private void setSpeed() {
		TextView speedBox = (TextView)findViewById(R.id.dagm_challenge_speed);
		TextView minSpeedBox = (TextView)findViewById(R.id.dagm_challenge_minspeed);
		int speed = m_challengePlayer.getCurrentExerciseSpeed();
		speedBox.setText("Your Speed: " + speed + " RPM");
		minSpeedBox.setText("Stay above: " + m_challengePlayer.getMinSpeed());
		
	}

	private void updateDisplay() {
		Log.d(TAG, "updateDisplay - exerciseType is " + m_challengePlayer.getExerciseType());
		
		setChallengeTitle();
		
		if ( m_challengePlayer.getExerciseType() == ExerciseType.PUSHUP ) {
			showPushupDisplay();
		} else if ( m_challengePlayer.getExerciseType() == ExerciseType.REST ) {
			showRestDisplay();
		}
	}
	
	private void showPushupDisplay() {
		View setReps = findViewById(R.id.dagm_challenge_setreps_container);
		View timeLeft = findViewById(R.id.dagm_challenge_time_container);
		View pushups = findViewById(R.id.dagm_challenge_pushups_container);
		View rest = findViewById(R.id.dagm_challenge_rest_container);
		View speed = findViewById(R.id.dagm_challenge_speed_container);
		TextView info = (TextView)findViewById(R.id.dagm_pushup_info);
		if(isFriend()){
			info.setText("You are challenging " + getFriendName() + "!" 
					+ " Bring it!");
		}
		
		if(m_challengePlayer.isCurrentExerciseMinSpeed()){
			setReps.setVisibility(View.GONE);
			speed.setVisibility(View.VISIBLE);
			timeLeft.setVisibility(View.GONE);
			pushups.setVisibility(View.VISIBLE);
			rest.setVisibility(View.GONE);
		} else{
			speed.setVisibility(View.GONE);
			setReps.setVisibility(View.VISIBLE);
			timeLeft.setVisibility(View.GONE);
			pushups.setVisibility(View.VISIBLE);
			rest.setVisibility(View.GONE);
		}
	}
	
	private void showRestDisplay() {
		View setReps = findViewById(R.id.dagm_challenge_setreps_container);
		View timeLeft = findViewById(R.id.dagm_challenge_time_container);
		View pushups = findViewById(R.id.dagm_challenge_pushups_container);
		View rest = findViewById(R.id.dagm_challenge_rest_container);
		View speed = findViewById(R.id.dagm_challenge_speed_container);
		
		setReps.setVisibility(View.GONE);
		speed.setVisibility(View.GONE);
		timeLeft.setVisibility(View.VISIBLE);
		pushups.setVisibility(View.GONE);
		rest.setVisibility(View.VISIBLE);
	}
	 
	/**
	 * Formats a time value as MM:SS
	 * @param time	The time value in seconds to format
	 * @return	A string representation of time in MM:SS format
	 */
	private String formatTime(int time) {
		return String.format("%2d:%02d", (time % 3600) / 60, (time % 60));
	}
	
	// update the elapsed time
	private String getCurrentTime() {
		return formatTime(m_challengePlayer.getTime());
	}
}
