package com.bostwickgarnes.dropandgiveme;

import com.bostwickgarnes.dropandgiveme.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class FriendConfirm extends Activity {
	 
	/**
	 * Required Intent Extra that specifies which challenge to
	 * show the information for.
	 */
	public static final String EXTRA_CHALLENGE_ID = "challengeId";
	public static final String EXTRA_FRIEND_ID = "friendId";
	public static final String EXTRA_FRIEND_NAME = "friendName";
	
	private TextView m_friendTitle;
	private TextView m_challengeTitle;
	
	private ChallengeData m_challengeData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dagm_friend_confirm);
		
		m_challengeData = ChallengeData.getInstance(this);
		
		m_friendTitle		= (TextView)findViewById(R.id.dagm_friendconfirm_title);
		m_challengeTitle	= (TextView)findViewById(R.id.dagm_friendconfirm_description);
		
		showChallengeInfo();
	}
	
	public void showChallengeInfo() {
		Challenge challenge = getChallenge();
		
		m_friendTitle.setText("You are about to challenge " + getFriendName() + " to:");
		m_challengeTitle.setText(challenge.getTitle());
		
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
	 * OnClick Handler for the "Go" button
	 * @param v
	 */
	public void onGoClicked(View v) {
		Intent play = new Intent(this, PlayChallenge.class);
		play.putExtra(PlayChallenge.EXTRA_CHALLENGE_ID, getChallengeId());
		play.putExtra(PlayChallenge.EXTRA_FRIEND_ID, getFriendId());
		play.putExtra(PlayChallenge.EXTRA_FRIEND_NAME, getFriendName());
		play.putExtra(PlayChallenge.EXTRA_IS_FRIEND_CHALLENGE, true);
		play.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(play);
	}
}
