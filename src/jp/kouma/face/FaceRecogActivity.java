package jp.kouma.face;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class FaceRecogActivity extends TabActivity {
	protected static final String TAG = "FR activity: ";
	/** Called when the activity is first created. */
	TabHost tabHost;
	public String SelectedTab;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	/** TabHost will have Tabs */
	tabHost = (TabHost)findViewById(android.R.id.tabhost);

	/** TabSpec used to create a new tab.
	* By using TabSpec only we can able to setContent to the tab.
	* By using TabSpec setIndicator() we can set name to tab. */

	/** tid1 is firstTabSpec Id. Its used to access outside. */
	TabSpec firstTabSpec = tabHost.newTabSpec("enroll");
	TabSpec secondTabSpec = tabHost.newTabSpec("recognize");

	/** TabSpec setIndicator() is used to set name for the tab. */
	/** TabSpec setContent() is used to set content for a particular tab. */
	firstTabSpec.setIndicator("Enroll").setContent(new Intent(this,FaceEnroll.class));
	secondTabSpec.setIndicator("Recognize").
	setContent(new Intent(this,FaceRecognize.class));

	/** Add tabSpec to the TabHost to display. */
	tabHost.addTab(secondTabSpec);
	tabHost.addTab(firstTabSpec);
	
	getTabWidget().getChildAt(1).setOnClickListener(new OnClickListener() { 
        @Override 
        public void onClick(View v) { 

           // Log.d(TAG,"1: "+getTabHost().getCurrentTabTag());
        	SelectedTab = getTabHost().getCurrentTabTag();
            
        	if (SelectedTab.equals("recognize")) { 
                Log.d(TAG,"2: Recognization tab "+SelectedTab);
                tabHost.setCurrentTabByTag("recognize");
                tabHost.setCurrentTabByTag("enroll");

            } else {
            	Log.d(TAG,"1: Enrollement tab "+SelectedTab);

            	tabHost.setCurrentTabByTag("enroll");
            }
        	
        } 
    });

	
	}
}