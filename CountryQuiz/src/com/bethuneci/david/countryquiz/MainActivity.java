/* 
 * Name: David Su
 * Date: May 26, 2013
 * Description: Country Quiz application for elementary school students  
 */

package com.bethuneci.david.countryquiz;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

//Main Activity Class
public class MainActivity extends Activity 
{
	private Spinner questionSpinner, questionTypeSpinner, answerSpinner, answerTypeSpinner;
	private TextView pointsTextView;	
	private Resources res;
	private Button startButton;
	private SpinnerHandler handler;
	private String selectedQuestion, selectedQuestionType, selectedAnswer, selectedAnswerType;
	private int totalPoints;
	
	//called when the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //gets all needed resources declared in xml
        res = getResources();        
        questionSpinner = (Spinner)findViewById(R.id.questionSpinner);
        questionTypeSpinner = (Spinner)findViewById(R.id.questionTypeSpinner);
        answerSpinner = (Spinner)findViewById(R.id.answerSpinner);
        answerTypeSpinner = (Spinner)findViewById(R.id.answerTypeSpinner);
        pointsTextView = (TextView)findViewById(R.id.pointsTextView);
        startButton = (Button)findViewById(R.id.startButton);
                
        //initializes spinners
        handler = new SpinnerHandler();
        initSpinner(questionSpinner,R.array.question_array);
        initSpinner(questionTypeSpinner, R.array.question_type_array);
        initSpinner(answerSpinner, R.array.answer_array);
        initSpinner(answerTypeSpinner, R.array.answer_type_array);

        answerSpinner.setSelection(1);
        
        verifySelection();
    }
    
    //receives a spinner object, initializes it, and populates it with items from a string array
    private void initSpinner(Spinner spinner, int strArrId) {    	
    	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, strArrId, android.R.layout.simple_spinner_item);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinner.setAdapter(adapter);  
    	spinner.setOnItemSelectedListener(handler);
    }
    
    //called to check if the user's combination of question, question type, answer, and answer type is valid
    private void verifySelection(){
    	selectedQuestion = questionSpinner.getSelectedItem().toString();
    	selectedQuestionType = questionTypeSpinner.getSelectedItem().toString();
    	selectedAnswer = answerSpinner.getSelectedItem().toString();
    	selectedAnswerType = answerTypeSpinner.getSelectedItem().toString();
    	
    	//if question == answer, disable start button
    	if (selectedQuestion.equals(selectedAnswer)) {
    		pointsTextView.setText(res.getString(R.string.invalid_string));
    		startButton.setEnabled(false);
    		questionTypeSpinner.setEnabled(true);
    	}
    	else {
    		//if question is flag or map, disable question type spinner
    		if (selectedQuestion.equals(res.getStringArray(R.array.question_array)[2]) || selectedQuestion.equals(res.getStringArray(R.array.question_array)[3])) {
        		questionTypeSpinner.setSelection(0);
        		questionTypeSpinner.setEnabled(false);
        	}
    		else {
    			questionTypeSpinner.setEnabled(true);
    		}
    		//calculates the maximum number of points the player can earn per question
    		startButton.setEnabled(true);
    		int questionPoints = Integer.parseInt(selectedQuestion.replaceAll("\\D+",""));
    		int questionTypePoints = Integer.parseInt(selectedQuestionType.replaceAll("\\D+",""));
    		int answerPoints = Integer.parseInt(selectedAnswer.replaceAll("\\D+",""));
    		int answerTypePoints = Integer.parseInt(selectedAnswerType.replaceAll("\\D+",""));
    		totalPoints = questionPoints + questionTypePoints + answerPoints + answerTypePoints;
    		pointsTextView.setText(res.getString(R.string.points_string)+totalPoints);
    	}    	
    }
    
    //called when the user presses the start button (specified using xml "onClick" attribute
    public void start(View view) {
    	Intent intent = new Intent(this, GameActivity.class);    
    	
    	//passes in the user's choices to the Game Activity
    	intent.putExtra("question", selectedQuestion);
    	intent.putExtra("questionType", selectedQuestionType);
    	intent.putExtra("answer", selectedAnswer);
    	intent.putExtra("answerType", selectedAnswerType);
    	intent.putExtra("points", totalPoints);
    	startActivity(intent);
    }
    
    //private inner class used to handle when the user selects a choice on a spinner
    private class SpinnerHandler implements OnItemSelectedListener 
    {
    	//called when a spinner's selection is changed
		@Override
		public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {
			verifySelection();			
		}

		//unimplemented method
		@Override
		public void onNothingSelected(AdapterView<?> parent) {}    	
    }
}




