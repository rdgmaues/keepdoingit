package com.rm.keepdoingit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


public class Questionnaire extends ActionBarActivity {
	private SharedPreferences mPreferences;
	
	private Button saveButton;
	private RadioGroup radioGroup1, radioGroup2, radioGroup3, radioGroup4, radioGroup5;//, radioGroup6;
	private LinearLayout question6;
	private EditText answer6;

	private String answers[] = {"", "", "", "", "", ""};
	private boolean answered[] = {false, false, false, false, false};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.questionnaire);

		//
		mPreferences = getSharedPreferences(KeepDoingItApplication.PREFERENCES_FILE, MODE_PRIVATE);
		
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putLong("lastTime", 0);
		editor.commit();
		//

		ActionBar actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
		actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>App Usage Questionnaire</font>"));
		actionBar.setDisplayHomeAsUpEnabled(true);

		question6 = (LinearLayout)findViewById(R.id.question6);
		question6.setVisibility(View.GONE);
		
		answer6 = (EditText)findViewById(R.id.answer6);
		
		radioGroup1 = (RadioGroup)findViewById(R.id.radioGroup1);
		radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				String answer = ((RadioButton)findViewById(group.getCheckedRadioButtonId())).getText().toString();
				answers[0] = answer;
				answered[0] = true;
			}
		});

		radioGroup2 = (RadioGroup)findViewById(R.id.radioGroup2);
		radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				String answer = ((RadioButton)findViewById(group.getCheckedRadioButtonId())).getText().toString();
				answers[1] = answer;
				answered[1] = true;
			}
		});

		radioGroup3 = (RadioGroup)findViewById(R.id.radioGroup3);
		radioGroup3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				String answer = ((RadioButton)findViewById(group.getCheckedRadioButtonId())).getText().toString();
				answers[2] = answer;
				answered[2] = true;
			}
		});

		radioGroup4 = (RadioGroup)findViewById(R.id.radioGroup4);
		radioGroup4.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				String answer = ((RadioButton)findViewById(group.getCheckedRadioButtonId())).getText().toString();
				answers[3] = answer;
				answered[3] = true;
			}
		});

		radioGroup5 = (RadioGroup)findViewById(R.id.radioGroup5);
		radioGroup5.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				String answer = ((RadioButton)findViewById(group.getCheckedRadioButtonId())).getText().toString();
				answers[4] = answer;
				answered[4] = true;
				
				if (answer.equalsIgnoreCase("I was not expecting to see any particular rule"))
				{
					question6.setVisibility(View.GONE);
					answer6.setText("");
				}
				else
				{
					question6.setVisibility(View.VISIBLE);
				}
			}
		});

/*		radioGroup6 = (RadioGroup)findViewById(R.id.radioGroup6);
		radioGroup6.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				String answer = ((RadioButton)findViewById(group.getCheckedRadioButtonId())).getText().toString();
				answers[5] = answer;
				answered[5] = true;
			}
		});
*/
		
		//

		saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (answered[0] && answered[1] && answered[2] && answered[3] && answered[4])// && answered[5])
				{
					answers[5] = answer6.getText().toString();
					
					Bundle questionnaire = new Bundle();
					questionnaire.putStringArray("answers", answers);
					((KeepDoingItApplication) getApplicationContext()).writeAnswers(questionnaire);

					Toast.makeText(getApplicationContext(), "Thank you for your answers! :)", Toast.LENGTH_LONG).show();

					if (((KeepDoingItApplication) getApplicationContext()).isConnected()) {
						((KeepDoingItApplication) getApplicationContext()).uploadLogFiles();	
					}
					else
					{
						Toast.makeText(getApplicationContext(), "We saved your answers and we will sent them to our server after your phone is connected to the Internet", Toast.LENGTH_LONG).show();
					}
					
					setResult(RESULT_OK);
					//finish();
					final Intent intent = new Intent(getApplicationContext(), MyRulesList.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
				else
				{
					Toast.makeText(getApplicationContext(), "Please answer all the questions before sending your answers" , Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//finish();
			final Intent intent = new Intent(getApplicationContext(), MyRulesList.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}		
	}
	
}

