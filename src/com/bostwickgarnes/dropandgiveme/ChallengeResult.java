package com.bostwickgarnes.dropandgiveme;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;

import com.bostwickgarnes.dropandgiveme.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ChallengeResult extends Activity {
	private static final String TAG = "ChallengeResult";
	
	private static final String FB_APP_ID 	= "120467011372515";
    private static final String FB_KEY 		= "facebook-session";
	private static final String FB_TOKEN 	= "access_token";
	private static final String FB_EXPIRES 	= "expires_in";
	
	DialogListener m_facebookListener = new DialogListener() {
		@Override
		public void onCancel() {
			Log.d(TAG, "Facebook Login Cancelled");
		} 
 
		@Override
		public void onComplete(Bundle values) {
			Log.d(TAG, "Facebook Login Completed");
			Log.d(TAG, values.toString());
			saveFacebookAccess();
			publishWallPost();
		}

		@Override
		public void onError(DialogError e) {
			Log.d(TAG, "Facebook Login Error: " + e.getLocalizedMessage());
		}

		@Override
		public void onFacebookError(FacebookError e) {
			Log.d(TAG, "Facebook Error: " + e.getLocalizedMessage());
		}		
	};
	
	DialogListener m_facebookLoginAndChallengeListener = new DialogListener() {
		@Override
		public void onCancel() {
			Log.d(TAG, "Facebook Login Cancelled");
		} 
 
		@Override
		public void onComplete(Bundle values) {
			Log.d(TAG, "Facebook Login Completed");
			Log.d(TAG, values.toString());
			saveFacebookAccess();
			publishChallengeToFriend();
		}

		@Override
		public void onError(DialogError e) {
			Log.d(TAG, "Facebook Login Error: " + e.getLocalizedMessage());
		}

		@Override
		public void onFacebookError(FacebookError e) {
			Log.d(TAG, "Facebook Error: " + e.getLocalizedMessage());
		}		
	};

	DialogListener m_fbWallListener = new DialogListener() {
		@Override
		public void onFacebookError(FacebookError e) {
			Log.d(TAG, "Facebook Error: " + e.getLocalizedMessage());
		}
		
		@Override
		public void onError(DialogError e) {
			Log.d(TAG, "Facebook Error: " + e.getLocalizedMessage());			
		}
		
		@Override
		public void onComplete(Bundle values) {
			Log.d(TAG, "Successfully posted to facebook");
			Log.d(TAG, "post_id: " + values.getString("post_id"));
			Toast.makeText(ChallengeResult.this, "Successfully posted to facebook", Toast.LENGTH_SHORT);
		}
		
		@Override
		public void onCancel() {
			Log.d(TAG, "User cancelled wall posting");
		}
	};
	
	AsyncFacebookRunner.RequestListener m_fbSendToFriendListener = new AsyncFacebookRunner.RequestListener() {
		@Override
		public void onComplete(String response, Object state) {
			Log.d(TAG, "Successfully posted to friend's wall");
//			Toast.makeText(ChallengeResult.this, "Challenge successfully sent to " + getFriendName() + ".",
//												Toast.LENGTH_SHORT);			
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Log.d(TAG, "Facebook Error: " + e.getLocalizedMessage());	
			
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			Log.d(TAG, "Facebook Error: " + e.getLocalizedMessage());	
			
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Log.d(TAG, "Facebook Error: " + e.getLocalizedMessage());	
			
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			Log.d(TAG, "Facebook Error: " + e.getLocalizedMessage());	
		}
	};
	
	/**
	 * Key for Intent Extra to pass score along.
	 */
	public static final String EXTRA_REPS = "reps";
	public static final String EXTRA_TIME = "time";
	public static final String EXTRA_SCORE = "score";
	public static final String EXTRA_CHALLENGE_ID = "challenge_id";
	public static final String EXTRA_IS_WIN = "isWin";
	public static final String EXTRA_IS_TRAINING = "isTraining";
	public static final String EXTRA_FRIEND_ID = "friendId";
	public static final String EXTRA_FRIEND_NAME = "friendName";
	public static final String EXTRA_IS_FRIEND_CHALLENGE = "isFriend";
	
	private TextView m_title;
	private TextView m_reps;
	private TextView m_time;
	private TextView m_score;
	private TextView m_highScore;
	private View m_winContainer;
	private View m_failContainer;
	private View m_scoreContainer;
	private TextView m_titleBar;
	private Button m_shareButton;
	private Button m_sendButton;
	private Button m_continueButton;

	private ChallengeData m_challengeData;
	private HighScoreData m_highScoreData;
	private EventData m_eventData;
	private Challenge m_challenge;
	private Facebook m_facebook;
	
	String m_response = "";
	boolean m_challengeSent = false;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dagm_challenge_result);
		
		m_challengeData = ChallengeData.getInstance(this);
		m_highScoreData = HighScoreData.getInstance(this);
		m_eventData		= EventData.getInstance(this);
		
		m_challenge = getChallenge();

		m_title 			= (TextView)findViewById(R.id.dagm_challengeresult_title);
		m_reps				= (TextView)findViewById(R.id.dagm_challengeresult_reps);
		m_time				= (TextView)findViewById(R.id.dagm_challengeresult_time);
		m_score 			= (TextView)findViewById(R.id.dagm_challengeresult_score);
		m_highScore			= (TextView)findViewById(R.id.dagm_challengeresult_highscore);
		m_titleBar			= (TextView)findViewById(R.id.dagm_challengeresult_titlebar);
		m_winContainer  	= findViewById(R.id.dagm_challengeresult_wincontainer);
		m_failContainer  	= findViewById(R.id.dagm_challengeresult_failcontainer);
		m_scoreContainer	= findViewById(R.id.dagm_challengeresult_scorecontainer);
		m_shareButton		= (Button)findViewById(R.id.dagm_challengeresult_share_button);
		m_sendButton		= (Button)findViewById(R.id.dagm_challengeresult_send_button);
		m_continueButton	= (Button)findViewById(R.id.dagm_challengeresult_continue_button);
		
		
		m_facebook = new Facebook(FB_APP_ID);
		
		showChallengeResult();
		updateHighScores();
		logEvent();
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        m_facebook.authorizeCallback(requestCode, resultCode, data);
    }

	
	private void showChallengeResult() {
		if ( isWin() ) {
			if ( isTraining() ) {
				m_failContainer.setVisibility(View.GONE);
				m_winContainer.setVisibility(View.VISIBLE);
				m_scoreContainer.setVisibility(View.GONE);
				
				m_titleBar.setText("Training Result");
				m_title.setText(getChallengeTitle());
				m_reps.setText(Integer.toString(getReps()));
				m_time.setText(formatTime(getTime()));
				m_shareButton.setVisibility(View.GONE);
				m_sendButton.setVisibility(View.GONE);
			} else if ( isFriend() ){
				m_failContainer.setVisibility(View.GONE);
				m_winContainer.setVisibility(View.VISIBLE);
				m_scoreContainer.setVisibility(View.VISIBLE);
				m_shareButton.setVisibility(View.GONE);
				m_sendButton.setVisibility(View.VISIBLE);
				
				m_title.setText("Challenging " + getFriendName() + " to " + getChallengeTitle());
				m_reps.setText(Integer.toString(getReps()));
				m_time.setText(formatTime(getTime()));
				m_score.setText(Integer.toString(getScore()));
				m_highScore.setText(Integer.toString(getHighScore()));	
				m_continueButton.setText("Chicken Out");
			} else {
				m_failContainer.setVisibility(View.GONE);
				m_winContainer.setVisibility(View.VISIBLE);
				m_scoreContainer.setVisibility(View.VISIBLE);
				m_shareButton.setVisibility(View.VISIBLE);
				m_sendButton.setVisibility(View.GONE);
				
				m_title.setText(getChallengeTitle());
				m_reps.setText(Integer.toString(getReps()));
				m_time.setText(formatTime(getTime()));
				m_score.setText(Integer.toString(getScore()));
				m_highScore.setText(Integer.toString(getHighScore()));
			}
		} else {
			m_winContainer.setVisibility(View.GONE);
			m_scoreContainer.setVisibility(View.GONE);
			m_failContainer.setVisibility(View.VISIBLE);
			m_sendButton.setVisibility(View.GONE);
			m_shareButton.setVisibility(View.GONE);
			
			m_title.setText(getChallengeTitle());			
		}
	}

	private void updateHighScores() {
		if( isWin() ) {
			if ( getScore() > getHighScore() ) {
				m_highScoreData.setHighScore(getChallengeId(), getScore(), !isTraining());
			}		
		}
	}
	
	private void logEvent() {
		if ( isTraining() ) {
			m_eventData.logTraining(getChallengeId(), isWin());
		} else {
			m_eventData.logChallenge(getChallengeId(), isWin(), getScore());
		}
	}
	
	public int getReps() {
		return getIntent().getExtras().getInt(EXTRA_REPS);
	}
	
	public int getTime() {
		return getIntent().getExtras().getInt(EXTRA_TIME);
	}
	
	public int getScore() {
		return getIntent().getExtras().getInt(EXTRA_SCORE);
	}
	
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
	 * Get the Challenge from the ID passed to this Activity
	 * @return	The Challenge
	 */
	private Challenge getChallenge() {
		return m_challengeData.getChallenge(getChallengeId());
	}
	
	public boolean isWin() {
		return getIntent().getExtras().getBoolean(EXTRA_IS_WIN);
	}
	
	public boolean isTraining() {
		return getIntent().getExtras().getBoolean(EXTRA_IS_TRAINING);
	}
	
	public String getChallengeTitle(){
		Log.v(TAG, "Challenge is " + m_challenge);
		return m_challenge.getTitle();
	}
	
	public int getHighScore() {
		return m_highScoreData.getHighScore(m_challenge.getId());
	}
	
	/**
	 * OnClick handler for the Share button
	 * @param v
	 */
	public void onShareClicked(View v) {
		Log.d(TAG, "Sharing result");
		
		if ( restoreFacebookAccess() ) {
			Log.d(TAG, "Facebook Session is valid");
			
			publishWallPost();
		} else {
			Log.d(TAG, "Facebook Session is not valid");
			String[] permissions = {"publish_stream"};
			
			m_facebook.authorize(this, permissions, m_facebookListener);
		}
	}
	
	/**
	 * OnClick handler for the Continue button
	 * @param v
	 */
	public void onContinueClicked(View v){
		Intent dagmHome = new Intent(this, DagmHome.class);
		dagmHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(dagmHome);
	}
	
	public void onSendClicked(View v){
		// check if the button has already been clicked
		// if so, do nothing
		if( !m_challengeSent ){
			m_challengeSent = true;
			m_sendButton.setVisibility(View.GONE);
			m_continueButton.setText("Continue");
			Log.d(TAG, "Sending challenge");
			if ( restoreFacebookAccess() ) {
				Log.d(TAG, "Facebook Session is valid");
				
				publishChallengeToFriend();
				
		       if (m_response == null || m_response.equals("") || 
		                m_response.equals("false")) {
		            Log.v("TAG", "Friend response is blank response");
		            Toast.makeText(ChallengeResult.this, "Error Sending Challenge.",
							Toast.LENGTH_SHORT).show();
		        } else{
		            Toast.makeText(ChallengeResult.this, "Challenge successfully sent to " + getFriendName() + ".",
							Toast.LENGTH_SHORT).show();
		        }
				
			} else {
				Log.d(TAG, "Facebook Session is not valid");
				String[] permissions = {"publish_stream"};
				
				m_facebook.authorize(this, permissions, m_facebookLoginAndChallengeListener);
			}
		}
	}
	
	/**
	 * Publish the challenge message to the friend's wall that has been selected.
	 */
	private void publishChallengeToFriend() {
		String description = 
			"I challenge you to " + m_challenge.getTitle() + "!" + 
    		" I finished this challenge with a score of " + getScore() + "."
    		+ " Can you beat that?";
		try{
		
		Bundle params = new Bundle();
		params.putString("message", "");
		params.putString("link", "http://bostwick.github.com/DropAndGiveMe/");
		params.putString("caption", "Mobile Fitness Challenges");
		params.putString("description", description);
		params.putString("name", "You have been challenged! (Drop and Give Me)");
		
		m_response = m_facebook.request(getFriendId() + "/feed", params, "POST");
		
		Log.d("TAG", "Response to friend post is: " + m_response);

		}
		catch (Exception e){
			Log.d("TAG", "Facebook Error: " + e.getLocalizedMessage());
            Toast.makeText(ChallengeResult.this, "Error Sending Challenge.",
					Toast.LENGTH_SHORT);
		}
	}

	/**
	 * Formats a time value as MM:SS
	 * @param time	The time value in seconds to format
	 * @return	A string representation of time in MM:SS format
	 */
	private String formatTime(int time) {
		return String.format("%2d:%02d", (time % 3600) / 60, (time % 60));
	}

	private boolean restoreFacebookAccess() {
	    SharedPreferences savedSession = getSharedPreferences(FB_KEY, Context.MODE_PRIVATE);
	    m_facebook.setAccessToken(savedSession.getString(FB_TOKEN, null));
	    m_facebook.setAccessExpires(savedSession.getLong(FB_EXPIRES, 0));
	    
	    return m_facebook.isSessionValid();
	}
	
    private boolean saveFacebookAccess() {
    	Log.d(TAG, "saving facebook details");
        Editor editor = getSharedPreferences(FB_KEY, Context.MODE_PRIVATE).edit();
        editor.putString(FB_TOKEN, m_facebook.getAccessToken());
        editor.putLong(FB_EXPIRES, m_facebook.getAccessExpires());
        return editor.commit();
    }
    
    private void publishWallPost() {
    	Bundle params = new Bundle();
    	JSONObject attachment = new JSONObject();
    	
    	try {
        	attachment.put("name", "Challenge Complete (Drop and Give Me)");
        	// attachment.put("href", "http://www.yahoo.com/");
        	attachment.put("caption", "Mobile Fitness Challenges");
        	attachment.put("description", 
        			"I completed the challenge " + m_challenge.getTitle() + 
        			" with a score of " + getScore() + ".");
    	} catch(JSONException e) {
    		Log.e(TAG, "JSON Error: " + e.getLocalizedMessage());
    	}
    	
    	params.putString("message", "");
    	params.putString("attachment", attachment.toString());
    	m_facebook.dialog(this, "stream.publish", params, m_fbWallListener);
    }
	
}
