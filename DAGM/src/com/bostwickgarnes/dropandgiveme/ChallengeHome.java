package com.bostwickgarnes.dropandgiveme;

import java.util.Map;

import com.bostwickgarnes.dropandgiveme.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class ChallengeHome extends ListActivity {
	private static final String TAG = "ChallengeHome";

	ChallengeData m_challengeData;
	
	private OnItemClickListener onChallengeClicked = new OnItemClickListener() {
		@Override
		@SuppressWarnings("unchecked")
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			Map<String, String> item = (Map<String,String>)getListAdapter().getItem(position);

			Log.d(TAG, "onChallengeClicked: position=" + position + ", id=" + id);
			Log.d(TAG, "Item: " + getListAdapter().getItem(position));

			String challengeIdStr = item.get("id");
			
			if ( challengeIdStr != null ) {
				int challengeId = Integer.parseInt(challengeIdStr);
				
				Log.d(TAG, "Challenge with id=" + challengeId);

				Intent overview = new Intent(ChallengeHome.this, ChallengeOverview.class);
				overview.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				overview.putExtra(ChallengeOverview.EXTRA_CHALLENGE_ID, challengeId);
				
				startActivity(overview);
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.dagm_challenge_home);
      
      m_challengeData = ChallengeData.getInstance(this);
            
      setListAdapter(m_challengeData.getChallengeListAdapter());
      getListView().setOnItemClickListener(onChallengeClicked);
	}
}
