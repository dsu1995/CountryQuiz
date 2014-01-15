/* 
 * Name: David Su
 * Date: May 26, 2013
 * Description: Country Quiz application for elementary school students 
 */

package com.bethuneci.david.countryquiz;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

//Game Activity class 
public class GameActivity extends Activity 
{		
	private String selectedQuestion, selectedQuestionType, selectedAnswer, selectedAnswerType;	
	private String[] QUESTIONS, QUESTION_TYPES, ANSWERS, ANSWER_TYPES;
	private String[][] DATABASE;
	
	private final int NUM_OF_QUESTIONS = 10;
	
	private Resources res;
	private LayoutInflater inflater;
	
	private TextView questionNumTextView, totalPointsTextView;
	private FrameLayout questionFrameLayout;
	private TableLayout answerTableLayout;
	
	private int questionNum, pointsPerQuestion, currentQuestionPoints, totalPoints;	
	
	private Random rand;	
	private ArrayList<Integer> usedIndices;
	
	private Question question;
	private Answer answer;
		
	//called when the activity starts
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		getActionBar().setDisplayHomeAsUpEnabled(true);		
		
		rand = new Random();		
		usedIndices = new ArrayList<Integer>();
		
		res = getResources();	
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//loads countries database
		String[] tempDatabase = res.getStringArray(R.array.countries);		
		DATABASE = new String[tempDatabase.length][];
		for (int i=0; i < tempDatabase.length; i++) 
			DATABASE[i] = tempDatabase[i].split(" : ");
		
		//loads possible user settings
		QUESTIONS = res.getStringArray(R.array.question_array);
		QUESTION_TYPES = res.getStringArray(R.array.question_type_array);
		ANSWERS = res.getStringArray(R.array.answer_array);
		ANSWER_TYPES = res.getStringArray(R.array.answer_type_array);
		
		//gets user's choices passed from previous activity
		Intent intent = getIntent();		
		selectedQuestion = intent.getStringExtra("question");
		selectedQuestionType = intent.getStringExtra("questionType");
		selectedAnswer = intent.getStringExtra("answer");
		selectedAnswerType = intent.getStringExtra("answerType");
		pointsPerQuestion = intent.getIntExtra("points", 0);
		
		//loads widgets
		totalPointsTextView = (TextView)findViewById(R.id.totalPointsTextView);
		questionNumTextView = (TextView)findViewById(R.id.questionNumberTextView);
		questionFrameLayout = (FrameLayout)findViewById(R.id.questionFrameLayout);
		answerTableLayout = (TableLayout)findViewById(R.id.answerTableLayout);
		
		questionNum = 0;
		totalPoints = 0;
		
		//manages user selections
		
		//Question = country
		if (selectedQuestion.equals(QUESTIONS[0])) {
			//Question Type = text
			if (selectedQuestionType.equals(QUESTION_TYPES[0]))
				question = new TextQuestion(res.getString(R.string.country_string), 0);
			//Question Type = speech
			else 
				question = new SpeechQuestion(res.getString(R.string.country_tts_button_string), 0);
		}	
		//Question = capital city
		else if (selectedQuestion.equals(QUESTIONS[1])) {
			//Question Type = text
			if (selectedQuestionType.equals(QUESTION_TYPES[0]))
				question = new TextQuestion(res.getString(R.string.capital_string), 1);
			//Question Type = speech
			else 
				question = new SpeechQuestion(res.getString(R.string.capital_tts_button_string), 1);
		}
		//Question = flag
		else if (selectedQuestion.equals(QUESTIONS[2])) {
			question = new FlagQuestion();
		}
		//Question = map
		else {
			question = new MapQuestion();
		}
		
		//Answer = country
		if (selectedAnswer.equals(ANSWERS[0])) {
			//Answer Type = text
			if (selectedAnswerType.equals(ANSWER_TYPES[0]))
				answer = new TextAnswer(res.getString(R.string.country_answer_hint_string), 0);	
			//Answer Type = multiple choice
			else 
				answer = new CountryCapitalMCAnswer(0);
		}	
		//Answer = capital city
		else if (selectedAnswer.equals(ANSWERS[1])) {
			//Answer Type = text
			if (selectedAnswerType.equals(ANSWER_TYPES[0]))
				answer = new TextAnswer(res.getString(R.string.capital_answer_hint_string), 1);	
			//Answer Type = multiple choice
			else 
				answer = new CountryCapitalMCAnswer(1);
		}		
		//Answer = Continent
		else {
			//Answer Type = text
			if (selectedAnswerType.equals(ANSWER_TYPES[0]))
				answer = new TextAnswer(res.getString(R.string.continent_answer_hint_string), 2);	
			//Answer Type = multiple choice
			else 
				answer = new ContinentMCAnswer();
		}			
		
		//starts first question
		nextQuestion();
	}
	
	//called to load next question
	private void nextQuestion() {
		questionNum++;
		
		//if user completed quiz
		if (questionNum > NUM_OF_QUESTIONS) {
			//starts the post-quiz report activity
			Intent intent = new Intent(this, SummaryActivity.class);
			intent.putExtra("score", totalPoints);
			startActivity(intent);
			finish();
		} 
		else {
			//sets the question number and number of points on the screen
			questionNumTextView.setText(res.getString(R.string.question_text) +" "+ questionNum);
			totalPointsTextView.setText(res.getString(R.string.score_string) +" "+ totalPoints);
			
			//resets maximum number of points earned for that question
			currentQuestionPoints = pointsPerQuestion;
			
			//chooses random country 
			String[] databaseEntry = randomDatabaseEntry();	
			
			//sets question and answer
			question.setQuestion(databaseEntry);					
			answer.setAnswer(databaseEntry);		
		}
	}
	
	//helper method that returns a random database entry, never repeats
	private String[] randomDatabaseEntry() {
		int index;	
		//repeatedly generates a random index until it finds one that was never used
		do {
			index = rand.nextInt(DATABASE.length);			
		} while (usedIndices.contains(index));		
		//adds index to list of used indices
		usedIndices.add(index);		
		return DATABASE[index];		
	}
	
	//called when the user correctly answers a question
	private void correctAnswer() {
		String dialogStr = res.getString(R.string.correct_answer_string);
		
		//shows a dialog box informing the user that their answer was correct
		showDialog(dialogStr, new DialogInterface.OnClickListener() {
			//when the dialog closes, update the score and start the next question
			@Override
			public void onClick(DialogInterface dialog, int id) {
				totalPoints += currentQuestionPoints;
				nextQuestion();
			}
		});
	}
	
	//called when the user incorrectly answers a question
	private void incorrectAnswer() {			
		//possible score earned for this question halved
		currentQuestionPoints /= 2;
		
		//if the user gets the question wrong too many times
		if (currentQuestionPoints == 0) {
			String dialogStr = res.getString(R.string.incorrect_answer_string) +" "+
					res.getString(R.string.reveal_answer_string)+" "+answer.getAnswer();
			
			//show a dialog informing the user that their answer was incorrect
			//tells them the correct answer			
			showDialog(dialogStr, new DialogInterface.OnClickListener() {
				//starts the next question once they close the dialog box
				@Override				
				public void onClick(DialogInterface dialog, int id) {
					nextQuestion();					
				}			
			});	
		}
		else {
			String dialogStr = res.getString(R.string.incorrect_answer_string);
			
			//informs the user that their answer is incorrect
			showDialog(dialogStr, new DialogInterface.OnClickListener() {
				//resets the current question when the user closes the dialog
				@Override
				public void onClick(DialogInterface dialog, int id) {
					answer.reset();			
				}			
			});				
		}
	}
	
	//helper method that shows a dialog box
	//receives a message to show and an OnClickListener that responds to being clicked
	private void showDialog(String msg, DialogInterface.OnClickListener onClick) {		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg);
		builder.setCancelable(false);
		builder.setPositiveButton(res.getString(R.string.ok_string), onClick);
		builder.create().show();
	}
	
	//called when the skip question button is pressed (specified using xml "onClick" attribute)
	public void skipQuestion(View view) {
		String dialogStr = res.getString(R.string.reveal_answer_string) +" "+ answer.getAnswer();
		
		//reveals the correct answer
		showDialog(dialogStr, new DialogInterface.OnClickListener() {
			//starts the next question
			@Override
			public void onClick(DialogInterface dialog, int id) {
				nextQuestion();	
			}			
		});							
	}		
		
	//called to add the reset button to the options menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);	     
	    
	    menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.restart_string);
	 
	    return true;
	}

	//called when any button from the top menu bar is pressed
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//if user pressed the back button or the reset button, quit the activity
		if (item.getItemId()==android.R.id.home || item.getItemId()==Menu.FIRST){
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//interface class that handles all methods of asking the question
	//all implementing classes show their questions within the questionFrameLayout
	private interface Question 
	{
		//called to set a new question
		public void setQuestion(String[] databaseEntry);
	}
	
	//class for any type of text question (country, capital city)
	//shows a TextView with the question
	private class TextQuestion implements Question 
	{
		private final String QUESTION_STR;
		private final int DATABASE_INDEX;
		private TextView questionTextView;
		
		//constructor that receives a string showing what the given information is, 
		//and an index that tells this class where to read the question from when provided a database entry
		//also initializes the TextView that will be used to display the question
		public TextQuestion(String questionStr, int databaseIndex) {
			QUESTION_STR = questionStr;
			DATABASE_INDEX = databaseIndex;			
			questionTextView = (TextView) inflater.inflate(R.layout.question_textview, questionFrameLayout, false);
			questionFrameLayout.addView(questionTextView);			
		}
		
		//sets a new question provided a database entry
		@Override
		public void setQuestion(String[] databaseEntry) {
			questionTextView.setText(QUESTION_STR +" "+ databaseEntry[DATABASE_INDEX].split("/")[0]);
		}
	}
	
	//class for any type of text-to-speech question (country, capital)
	//displays a button that, when pressed, speaks the question aloud
	private class SpeechQuestion implements Question, TextToSpeech.OnInitListener
	{
		private final int DATABASE_INDEX;
		private Button ttsButton;
		private TextToSpeech tts;
		private String currentQuestion;
		
		//constructor that receives a string to display on the button,
		//and the index from which to read from a database entry
		//also initializes the text-to-speech object and button
		public SpeechQuestion(String questionStr, int databaseIndex) {
			tts = new TextToSpeech(GameActivity.this, this);
			DATABASE_INDEX = databaseIndex;
			ttsButton = (Button) inflater.inflate(R.layout.question_tts_button, questionFrameLayout, false);
			questionFrameLayout.addView(ttsButton);	
			
			ttsButton.setText(questionStr);
			ttsButton.setOnClickListener(new OnClickListener() {
				//called when the user clicks the text-to-speech button
				//speaks the question aloud
				public void onClick(View view){
					tts.speak(currentQuestion.split("/")[0], TextToSpeech.QUEUE_FLUSH, null);
				}
			});
		}
		
		//required method from TextToSpeech.OnInitListener
		//sets the locale to US
		@Override
		public void onInit(int status) {
			 tts.setLanguage(Locale.US);		 
		}		
		
		//sets the next question
		@Override
		public void setQuestion(String[] databaseEntry) {
			currentQuestion = databaseEntry[DATABASE_INDEX];
		}
	}
	
	//class for a flag question
	//displays the flag of the country in the questionFrameLayout
	private class FlagQuestion implements Question
	{
		private final int DATABASE_INDEX = 0;
		private ImageView questionImageView;
		
		//constructor that takes no parameters and initializes the ImageView used to display the flags
		public FlagQuestion() {
			questionImageView = (ImageView)inflater.inflate(R.layout.question_imageview, questionFrameLayout, false);
			questionFrameLayout.addView(questionImageView);	
		}
		
		//called to set a new question (i.e. load a new flag)
		@Override
		public void setQuestion(String[] databaseEntry) {
			AssetManager assets = getAssets();
			InputStream stream;
			try {
				stream = assets.open("flags/"+databaseEntry[DATABASE_INDEX].split("/")[0]+".gif");
				Drawable flag = Drawable.createFromStream(stream, databaseEntry[DATABASE_INDEX]);
		        questionImageView.setImageDrawable(flag);
			} 
			catch (IOException e) {
			}
		}		
	}
	
	//class for displaying a map question
	//displays a Google Map in the questionFrameLayout
	private class MapQuestion implements Question
	{	
		//latitude and longitude are stored in index 3 and 4 of any database entry, respectively
		private final int LAT_INDEX = 3;	
		private final int LNG_INDEX = 4;	
		
		//map objects
		private GoogleMap googleMap;
		private MapFragment mapFragment;
		private FrameLayout frameLayout;
		private Marker marker;
		
		//constructor that takes no parameters and initializes the map
		public MapQuestion() {
			//cannot inflate a fragment into a layout, so instead the fragment is wrapped within another layout in the xml			
			frameLayout = (FrameLayout) inflater.inflate(R.layout.map_fragment_framelayout, questionFrameLayout, false);
			questionFrameLayout.addView(frameLayout);
			mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.questionMapFragment);	
						
			googleMap = mapFragment.getMap();
			//sets map type to satellite to prevent the user from seeing country labels
			googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		}
		
		//sets a new question, and moves the map to the new location
		@Override
		public void setQuestion(String[] databaseEntry) {
			//remove previous marker
			if (marker!=null) {
				marker.remove();
			}	
			
			//find coordinates
			double lat = Double.parseDouble(databaseEntry[LAT_INDEX]);
			double lng = Double.parseDouble(databaseEntry[LNG_INDEX]);			
				
			//moves the camera to the new location, and sets a marker there
			LatLng location = new LatLng(lat, lng);
			googleMap.animateCamera(CameraUpdateFactory.newLatLng(location));
			marker = googleMap.addMarker(new MarkerOptions().position(location));
						
 		}		
	}
	
	//interface class that handles all methods of getting the user's answer
	//all implementing classes receive their answers within the answerTableLayout
	private interface Answer
	{
		//resets the current question (i.e. clears text in EditText in TextAnswer. Left blank in all other implementing classes)
		public void reset();
		
		//sets a new answer provided a database entry
		public void setAnswer(String[] databaseEntry);
		
		//gets the answer string 
		public String getAnswer();
	}
	
	//class used to get a typed-out answer from an EditText
	private class TextAnswer implements Answer 
	{
		private EditText answerEditText;
		private String answerText;
		private final int DATABASE_INDEX;
		
		//constructor that receives the hint the user will see when the EditText is blank,
		//and the index from which to read from the database entry
		//also initializes EditText
		public TextAnswer(String hint, int databaseIndex) {
			DATABASE_INDEX = databaseIndex;			
			
			answerEditText = (EditText) inflater.inflate(R.layout.answer_edittext, answerTableLayout, false);
			answerTableLayout.addView(answerEditText);
			answerEditText.setHint(hint);
			
			answerEditText.setOnKeyListener(new OnKeyListener() {
				//called when the user is finished typing in the EditText
				@Override
			    public boolean onKey(View v, int keyCode, KeyEvent event) {
			        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
			        	//calls the appropriate handler method depending on if the answer is correct or not
			        	if (verifyAnswer()) {
			        		correctAnswer();        		
			        	}
			        	else {
			        		incorrectAnswer();
			        	}	        	
			        	return true;
			        }
			        return false;
			    }
			});		
		}
		
		//clears the EditText and sets focus to it
		@Override
		public void reset() {
			answerEditText.setText("");
			answerEditText.requestFocus();			
		}

		//sets a new answer and also clears the previous answer
		@Override
		public void setAnswer(String[] databaseEntry) {
			answerText = databaseEntry[DATABASE_INDEX];		
			reset();
		}
		
		//returns the correct answer string, properly formatted for the human reader
		@Override
		public String getAnswer(){
			return answerText.replaceAll("/", " OR ");
		}
		
		//called to verify an answer
		private boolean verifyAnswer() {			
			String answer = answerEditText.getText().toString();
			//sets the answer to lowercase
			answer = answer.toLowerCase(Locale.US);        
			//replace all whole word occurences of "st" to "saint"
	        answer = answer.replaceAll("\\bst\\b", "saint");
	        //removes any whole word occurences of "and", "of", and "the"
	        answer = answer.replaceAll("\\b(and|of|the)\\b", "");
	        //removes all non-letters
	        answer = answer.replaceAll("[^a-z]", "");
	        
	        //finds all correct answers  
	        String[] tempCorrectAnswers = answerText.toLowerCase(Locale.US).split("/");
	        //checks all correct answers
	        for (int i=0; i < tempCorrectAnswers.length; i++) {
	        	//similarly, remove all occurences of "and", "of", and "the"
	            tempCorrectAnswers[i] = tempCorrectAnswers[i].replaceAll("\\b(and|of|the)\\b", "");
	            //removes all non-letters
	            tempCorrectAnswers[i] = tempCorrectAnswers[i].replaceAll("[^a-z]", "");
	            //checks if the user's answer is correct
	            if (tempCorrectAnswers[i].equals(answer))
	                return true;
	        }
	        return false;
		}
	}
	
	//class responsible for handlign when answer = "Continent" and answerType="Multiple Choice"
	private class ContinentMCAnswer implements Answer
	{
		private String answerText;
		private final int DATABASE_INDEX = 2;
		
		//constructor with no parameters that initializes all the buttons
		public ContinentMCAnswer() {	
			final String[] CONTINENTS = res.getStringArray(R.array.continents_array);
			
			ButtonListener listener = new ButtonListener();
			
			for (int row = 0; row < 2; row++) {
				for (int col = 0; col < 3; col++) {
					TableRow tableRow = (TableRow)answerTableLayout.getChildAt(row);
					Button button = (Button)inflater.inflate(R.layout.answer_mc_button, tableRow, false);
					tableRow.addView(button);	
					button.setText(CONTINENTS[row*3+col]);
					button.setOnClickListener(listener);
				}
			}
		}		
		
		//unneeded for this class
		@Override
		public void reset() {}

		//sets a new question and re-enable all buttons
		@Override
		public void setAnswer(String[] databaseEntry) {
			answerText = databaseEntry[DATABASE_INDEX];		
			for (int row = 0; row < 2; row++) {
				TableRow tableRow = (TableRow)answerTableLayout.getChildAt(row);
				for (int col = 0; col < 3; col++) {
					tableRow.getChildAt(col).setEnabled(true);
				}
			}
		}

		//returns the correct answer string, properly formatted for the human reader
		@Override
		public String getAnswer() {
			return answerText.replaceAll("/", " OR ");
		}
		
		//private inner class used to handle button presses
		private class ButtonListener implements OnClickListener
		{
			//called when a button is pressed
			@Override
			public void onClick(View button) {
				//disable the button
				button.setEnabled(false);
				//calls the appropriate handler method depending on the correctness of the answer
				if (verifyAnswer((Button)button)) {
					correctAnswer();
				}				
				else {
					incorrectAnswer();
				}
			}		
		}
		
		//verifies correctness of the answer
		private boolean verifyAnswer(Button button) {
			//checks for a match with all possible correct answers
			for (String answer: answerText.split("/")){
				if (button.getText().equals(answer)){
					return true;
				}
			}
			return false;
		}		
	}
	
	//class that is used when the user chooses a multiple choice format and chose answer="country" or "capital"
	private class CountryCapitalMCAnswer implements Answer
	{
		private String answerText;
		private final int DATABASE_INDEX;
		
		//constructor that receives the index to read from a database entry
		//Initializes 9 buttons
		public CountryCapitalMCAnswer(int databaseIndex) {	
			DATABASE_INDEX = databaseIndex;
			
			ButtonListener listener = new ButtonListener();
			
			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 3; col++) {
					TableRow tableRow = (TableRow)answerTableLayout.getChildAt(row);
					Button button = (Button)inflater.inflate(R.layout.answer_mc_button, tableRow, false);
					tableRow.addView(button);	
					button.setOnClickListener(listener);
				}
			}
		}		
		
		//unneeded method
		@Override
		public void reset() {}

		//sets the next answer
		//chooses new random entries as incorrect answer
		@Override
		public void setAnswer(String[] databaseEntry) {
			answerText = databaseEntry[DATABASE_INDEX];	
			
			//generates index of correct answer
			int correctIndex = rand.nextInt(9);
			
			for (int row = 0; row < 3; row++) {
				TableRow tableRow = (TableRow)answerTableLayout.getChildAt(row);
				for (int col = 0; col < 3; col++) {
					Button button = (Button) tableRow.getChildAt(col);					
					button.setEnabled(true);
					//if this is the correct index, use the correct answer
					if (row*3+col == correctIndex) {
						button.setText(answerText.split("/")[0]);
					}
					//otherwise, use a random answer
					else {
						button.setText(randomDatabaseEntry()[DATABASE_INDEX].split("/")[0]);
					}
				}
			}
		}

		//returns a human-readable answer string
		@Override
		public String getAnswer() {
			return answerText.replaceAll("/", " OR ");
		}
		
		//private inner class that listens for button presses
		private class ButtonListener implements OnClickListener
		{
			//called when the user presses a button
			@Override
			public void onClick(View button) {
				//disables button
				button.setEnabled(false);
				//calls the handler method appropriately
				if (verifyAnswer((Button)button)) {
					correctAnswer();
				}				
				else {
					incorrectAnswer();
				}
			}		
		}
		
		//verifies the correctness of the answer
		private boolean verifyAnswer(Button button) {
			for (String answer: answerText.split("/")){
				if (button.getText().equals(answer)){
					return true;
				}
			}
			return false;
		}		
	}
}