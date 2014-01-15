/* 
 * Name: David Su
 * Date: May 26, 2013
 * Description: Country Quiz application for elementary school students 
 */

package com.bethuneci.david.countryquiz;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

//Post-quiz report class
public class SummaryActivity extends Activity 
{
	//used to convert the user's score to a grade between 1 and 6
	private final int GRADE_CONVERSION_FACTOR = (int)Math.ceil(350/6);
	
	//called when the activity is created
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_summary);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Resources res = getResources();
		
		//get data passed from previous activity
		Intent intent = getIntent();		
		int score = intent.getIntExtra("score", 0);
		
		//sets the summary string displayed to the user
		TextView reportTextView = (TextView)findViewById(R.id.reportTextView);		
		reportTextView.setText(String.format(res.getString(R.string.summary_string), score, score/GRADE_CONVERSION_FACTOR+1));
		
	}

	//called when the user presses the OK button (specified using the xml "onClick" attribute)
	public void done(View view) {
		finish();		
	}
	
	//called when the user presses any button on the top menu bara
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//if they pressed the back button on the top left corner, quit this activity and return to the previous one
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
