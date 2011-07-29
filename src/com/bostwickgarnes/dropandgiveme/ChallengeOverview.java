package com.bostwickgarnes.dropandgiveme;

import com.bostwickgarnes.dropandgiveme.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class ChallengeOverview extends Activity {
	private static final String TAG = "ChallengeOverview";
	 
	/**
	 * Required Intent Extra that specifies which challenge to
	 * show the information for.
	 */
	public static final String EXTRA_CHALLENGE_ID = "challenge_id";
	public static final String EXTRA_IS_TRAINING = "isTraining";
	
	private TextView m_title;
	private TextView m_description;
	private TextView m_score;
	private TextView m_titleBar;
	private TextView m_scoreLabel;
	private Button m_friendButton;
	private Button m_goButton;
	
	private ChallengeData m_challengeData;
	private HighScoreData m_highScoreData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dagm_challenge_overview);
		
		m_challengeData = ChallengeData.getInstance(this);
		m_highScoreData = HighScoreData.getInstance(this);
		
		m_title 		= (TextView)findViewById(R.id.dagm_challengeoverview_title);
		m_description	= (TextView)findViewById(R.id.dagm_challengeoverview_description);
		m_score 		= (TextView)findViewById(R.id.dagm_challengeoverview_score);
		m_titleBar 		= (TextView)findViewById(R.id.dagm_challengeoverview_titlebar);
		m_scoreLabel	= (TextView)findViewById(R.id.dagm_challengeoverview_scorelabel);
		m_friendButton	= (Button)findViewById(R.id.dagm_challengeoverview_friend_button);
		m_goButton		= (Button)findViewById(R.id.dagm_challengeoverview_go_button);
		
		showChallengeInfo();
	}
	
	public void showChallengeInfo() {
		Challenge challenge = getChallenge();
		int highScore = m_highScoreData.getHighScore(challenge.getId());
		
		Log.d(TAG, "Received id = " + getChallengeId() + " with isTraining = " + isTraining());
		if( isTraining() ){
			m_titleBar.setText("Training Overview");
			m_score.setVisibility(View.INVISIBLE);
			m_scoreLabel.setVisibility(View.INVISIBLE);
			m_friendButton.setVisibility(View.GONE);
			m_title.setText(challenge.getTitle());
			m_description.setText(challenge.getDescription());
			m_goButton.setText("Start Training!");
		} else {
			m_title.setText(challenge.getTitle());
			m_description.setText(challenge.getDescription());
			m_score.setText(Integer.toString(highScore));
			m_friendButton.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * Get the Challenge from the ID passed to this Activity
	 * @return	The Challenge
	 */
	private Challenge getChallenge() {
		return m_challengeData.getChallenge(getChallengeId());
	}
	
	/**
	 * @return	The Challenge ID passed to this activity as an intent extra
	 */
	public int getChallengeId() {
		return getIntent().getExtras().getInt(EXTRA_CHALLENGE_ID);
	}
	
	/**
	 * @return	Whether or not this is a training challenge
	 */
	public boolean isTraining() {
		return getIntent().getExtras().getBoolean(EXTRA_IS_TRAINING);
	}
	
	/**
	 * OnClick Handler for the "Go" button
	 * @param v
	 */
	public void onGoClicked(View v) {
		Intent play = new Intent(this, PlayChallenge.class);
		play.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		play.putExtra(PlayChallenge.EXTRA_CHALLENGE_ID, getChallengeId());
		play.putExtra(PlayChallenge.EXTRA_IS_TRAINING, isTraining());
		
		startActivity(play);
	}
	
	public void onFriendClicked(View v){
		Intent friendSelection = new Intent(this, FriendSelection.class);
		friendSelection.putExtra(FriendSelection.EXTRA_CHALLENGE_ID, getChallengeId());
		startActivity(friendSelection);
	}
}
