package com.bostwickgarnes.dropandgiveme;

import java.util.Map;

import com.bostwickgarnes.dropandgiveme.R;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class DagmHome extends TabActivity {
	private static final String TAG = "DagmHome";
	
	private final int challengeList = R.id.dagm_home_challengelist;
	private final int trainingList	= R.id.dagm_home_traininglist;
	private final int activityList	= R.id.dagm_home_activitylist;
	private final int activityContainer = R.id.dagm_home_activitycontainer;
	private final int settingsContainer = R.id.dagm_home_settingscontainer;
	 
	private TabHost m_tabHost;
	private ChallengeData m_challengeData;
	private EventData m_eventData;
	private SettingsData m_settingsData;
	
	private OnItemClickListener onChallengeClicked = new OnItemClickListener() {
		@Override
		@SuppressWarnings("unchecked")
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			ListView listView = (ListView)parent;
			ListAdapter adapter = listView.getAdapter();
			
			Map<String, String> item = (Map<String,String>)adapter.getItem(position);

			Log.d(TAG, "onChallengeClicked: position=" + position + ", id=" + id);
			Log.d(TAG, "Item: " + adapter.getItem(position));

			String challengeIdStr = item.get("id");
			
			if ( challengeIdStr != null ) {
				int challengeId = Integer.parseInt(challengeIdStr);
				
				Log.d(TAG, "Challenge with id=" + challengeId);

				Intent overview = new Intent(DagmHome.this, ChallengeOverview.class);
				overview.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				overview.putExtra(ChallengeOverview.EXTRA_CHALLENGE_ID, challengeId);
				
				startActivity(overview);
			}
		}
	};

	private OnItemClickListener onTrainingClicked = new OnItemClickListener() {
		@Override
		@SuppressWarnings("unchecked")
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			ListView listView = (ListView)parent;
			ListAdapter adapter = listView.getAdapter();
			
			Map<String, String> item = (Map<String,String>)adapter.getItem(position);

			Log.d(TAG, "onChallengeClicked: position=" + position + ", id=" + id);
			Log.d(TAG, "Item: " + adapter.getItem(position));

			String challengeIdStr = item.get("id");
			
			if ( challengeIdStr != null ) {
				int challengeId = Integer.parseInt(challengeIdStr);
				
				Log.d(TAG, "Challenge with id=" + challengeId);

				Intent overview = new Intent(DagmHome.this, ChallengeOverview.class);
				overview.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				overview.putExtra(ChallengeOverview.EXTRA_CHALLENGE_ID, challengeId);
				overview.putExtra(ChallengeOverview.EXTRA_IS_TRAINING, true);
				
				startActivity(overview);
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dagm_home);
		  
		m_challengeData = ChallengeData.getInstance(this);
		m_eventData		= EventData.getInstance(this);
		m_settingsData 	= new SettingsData(this);
		  
		initializeTabHost();
		initSettings();
		populateLists();
		showNextChallenges();
	}
	
	protected void initializeTabHost() {
		m_tabHost = getTabHost();
		m_tabHost.addTab(m_tabHost.newTabSpec("challengelist")
				.setIndicator("Challenges", getResources().getDrawable(R.drawable.dagm_trophy))
				.setContent(challengeList));
		m_tabHost.addTab(m_tabHost.newTabSpec("traininglist")
				.setIndicator("Training", getResources().getDrawable(R.drawable.dagm_star))
				.setContent(trainingList));
		m_tabHost.addTab(m_tabHost.newTabSpec("activitylist")
				.setIndicator("Activity", getResources().getDrawable(R.drawable.dagm_check))
				.setContent(activityContainer));
		m_tabHost.addTab(m_tabHost.newTabSpec("settings")
				.setIndicator("Settings", getResources().getDrawable(R.drawable.dagm_wrench))
				.setContent(settingsContainer));
	}
	
	/**
	 * Get the settings values from shared preferences, set the checkboxes with the values,
	 * and register listeners for change on the checkboxes
	 */
	protected void initSettings(){
		boolean encouragement = m_settingsData.getSettingValue(SettingsData.ENCOURAGEMENT_SETTING, true);
		boolean soundEffects = m_settingsData.getSettingValue(SettingsData.SOUND_EFFECTS_SETTING, true);

		initSettingCheckbox(R.id.dagm_settings_encouraging, encouragement, SettingsData.ENCOURAGEMENT_SETTING);
		initSettingCheckbox(R.id.dagm_settings_sound_effects, soundEffects, SettingsData.SOUND_EFFECTS_SETTING);
	}
	
	/**
	 * This listener will save the preference with the key given whenever the checkbox is changed.
	 */
	private class SettingsOnCheckedChangeListener implements OnCheckedChangeListener {

		private String m_sharedPrefsKey;
		
		public SettingsOnCheckedChangeListener(String sharedPrefsKeyString){
			m_sharedPrefsKey = sharedPrefsKeyString;
		}
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			m_settingsData.putSettingValue(m_sharedPrefsKey, isChecked);
		}
		
	}
	
	/**
	 * Set the checkbox checked to the given value, and create a listener that will save the value
	 * of the checkbox when it is changed
	 * @param id The id of the checkbox to set
	 * @param value The value to set the checkbox to
	 * @param sharedPrefsKey The key of the shared preference to get
	 */
	protected void initSettingCheckbox(int id, boolean value, String sharedPrefsKey){
		
		CheckBox checkBox = (CheckBox)findViewById(id);

		checkBox.setChecked(value);
		
		checkBox.setOnCheckedChangeListener(new SettingsOnCheckedChangeListener(sharedPrefsKey));
	}
	
	protected void populateLists() {
		ListView challengeListView = (ListView)findViewById(challengeList);
		ListView trainingListView = (ListView)findViewById(trainingList);
		ListView eventListView = (ListView)findViewById(activityList);
		
		challengeListView.setAdapter(
				m_challengeData.getChallengeListAdapter());
		challengeListView.setOnItemClickListener(onChallengeClicked);
		
		trainingListView.setAdapter(
				m_challengeData.getTrainingListAdapter());
		trainingListView.setOnItemClickListener(onChallengeClicked);
		trainingListView.setOnItemClickListener(onTrainingClicked);
		
		eventListView.setAdapter(
				m_eventData.getListAdapter());
	}
	
	protected void showNextChallenges() {
		TextView nextChallengeView 	= (TextView)findViewById(R.id.dagm_event_nextchallenge);
		TextView nextTrainingView	= (TextView)findViewById(R.id.dagm_event_nexttraining);
		Challenge nextChallenge = m_challengeData.getNextChallenge();
		Challenge nextTraining 	= m_challengeData.getNextTraining();
		
		if ( nextChallenge == null ) {
			nextChallengeView.setText("All challenges have been finished! You Rock!");
		} else {
			nextChallengeView.setText(nextChallenge.getTitle());
		}
		
		if ( nextTraining == null ) {
			nextTrainingView.setText("All Training has been finished. You must be strong!");
		} else {
			nextTrainingView.setText(nextTraining.getTitle());
		}
	}
	
	public void showChallengeHome(View v) {
		Intent challengeHome = new Intent(this, ChallengeHome.class);
		startActivity(challengeHome);
	}
	
	public void showTrainingHome(View v) {
		Intent trainingHome = new Intent(this, TrainingHome.class);
		startActivity(trainingHome);
	}

}
