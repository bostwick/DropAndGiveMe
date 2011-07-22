package com.bostwickgarnes.dropandgiveme;

import java.util.Map;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class TrainingHome extends ListActivity {
	private static final String TAG = "TrainingHome";
	
	ChallengeData m_challengeData;

	private OnItemClickListener onChallengeClicked = new OnItemClickListener() {
		@Override
		@SuppressWarnings("unchecked")
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			Log.d(TAG, "onChallengeClicked: position=" + position + ", id=" + id);
			Log.d(TAG, "Item: " + getListAdapter().getItem(position));

			Map<String, String> item = (Map<String,String>)getListAdapter().getItem(position);
			Log.d(TAG, "Challenge with id=" + item.get("id"));
			
			Intent overview = new Intent(TrainingHome.this, ChallengeOverview.class);
			overview.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			overview.putExtra(ChallengeOverview.EXTRA_CHALLENGE_ID, Integer.parseInt(item.get("id")));
			overview.putExtra(ChallengeOverview.EXTRA_IS_TRAINING, true);
			
			startActivity(overview);
		}		
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.dagm_training_home);
      
      m_challengeData = ChallengeData.getInstance(this);
            
      setListAdapter(m_challengeData.getTrainingListAdapter());
      getListView().setOnItemClickListener(onChallengeClicked);
	}
	

}
