package com.rm.keepdoingit;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class RuleDetails extends ActionBarActivity {//implements UndoBarController.UndoListener {
	Bundle event;
	ArrayList<Bundle> conditions;
	ArrayList<Bundle> actions;
	//
	ViewGroup eventVG;
	ViewGroup conditionsVG;
	ViewGroup actionsVG;
	//
	Button addEvent;
	Button addCondition;
	Button addAction; 
	//
	boolean newRule;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rule_details);

		getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
		getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Selected rule</font>"));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		////
		newRule = this.getIntent().getBooleanExtra("new", true);

		////
		Bundle rule = this.getIntent().getBundleExtra("rule");
		event = rule.getBundle("event");
		conditions = rule.getParcelableArrayList("conditions");
		actions = rule.getParcelableArrayList("actions");

		////
		ViewGroup ruleDetailsVG = (ViewGroup) this.findViewById(R.id.rule_details);
		eventVG = (ViewGroup) ruleDetailsVG.findViewById(R.id.event);
		conditionsVG = (ViewGroup) ruleDetailsVG.findViewById(R.id.conditions);
		actionsVG = (ViewGroup) ruleDetailsVG.findViewById(R.id.actions);

		////
		addEvent = (Button) ruleDetailsVG.findViewById(R.id.add_event);
		addCondition = (Button) ruleDetailsVG.findViewById(R.id.add_condition);
		addAction = (Button) ruleDetailsVG.findViewById(R.id.add_action);

		OnClickListener addEventListener = new OnClickListener() {
			public void onClick(View v){
				startActivityForResult(0);
			}
		};
		addEvent.setOnClickListener(addEventListener);

		OnClickListener addConditionListener = new OnClickListener() {
			public void onClick(View v){
				startActivityForResult(1);
			}
		};
		addCondition.setOnClickListener(addConditionListener);

		OnClickListener addActionListener = new OnClickListener() {
			public void onClick(View v){
				startActivityForResult(2);
			}
		};
		addAction.setOnClickListener(addActionListener);

		////
		if (rule != null) {
			//
			generateEventRow();

			if (!event.isEmpty()) addEvent.setVisibility(View.GONE);
			else addEvent.setVisibility(View.VISIBLE);

			//
			for (int j=0; j<conditions.size(); j++) {
				generateConditionRow(j);
			}

			if (conditions.size()>2) addCondition.setVisibility(View.GONE);
			else addCondition.setVisibility(View.VISIBLE);

			//
			for (int j=0; j<actions.size(); j++) {
				generateActionRow(j);
			}

			if (actions.size()>2) addAction.setVisibility(View.GONE);
			else {
				if (actions.isEmpty()) addAction.setText("Add an action (required)");
				else addAction.setText("Add an action");
				addAction.setVisibility(View.VISIBLE);
			}
		}
	}


	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK)
		{
			Bundle option = data.getBundleExtra("option");
			int position = data.getIntExtra("position", 0);
			int order = 1;

			String operation = "switch";

			switch (requestCode)
			{
			case 0:
				if (event.isEmpty())
				{
					operation = "add";
				}

				event = option;
				generateEventRow();
				addEvent.setVisibility(View.GONE);
				break;
			case 1:
				if (position < conditions.size())
				{
					conditions.set(position, option);
					generateConditionRow(position);	
				}
				else
				{	
					operation = "add";
					conditions.add(option);
					generateConditionRow(position);
					if (conditions.size()>2)
					{
						addCondition.setVisibility(View.GONE);
					}
				}

				order += position + 1;
				break;
			case 2:
				if (position < actions.size())
				{
					actions.set(position, option);
					generateActionRow(position);	
				}
				else 
				{
					operation = "add";
					actions.add(option);
					generateActionRow(position);
					if (actions.size()>2)
					{
						addAction.setVisibility(View.GONE);
					}
				}

				order += position + 1 + conditions.size();
				break;
			default:
				break;
			}

			logEditAction(operation, requestCode, option, order);
		}
	}


	protected void logEditAction(String operation, int role, Bundle option, int order) {
		//log when the user edit a new rule
		if (newRule)
		{
			Bundle rule = new Bundle();
			rule.putBundle("event", event);
			rule.putParcelableArrayList("conditions", conditions);
			rule.putParcelableArrayList("actions", actions);

			Bundle action = new Bundle();
			action.putInt("actionType", KeepDoingItApplication.ACTION_EDIT);
			action.putBundle("rule", rule);
			action.putString("operation", operation);
			action.putInt("role", role);
			action.putInt("type", option.getInt("type"));
			action.putString("value", option.getString("value"));
			action.putString("extra_value", option.getString("extra_value"));
			action.putInt("order", order);

			((KeepDoingItApplication) getApplicationContext()).writeLog(action);
		}
	}


	private void startActivityForResult(int role)
	{
		int position = 0;

		switch (role)
		{
		case 1:
			position = conditions.size();
			break;
		case 2:
			position = actions.size();
			break;
		}

		startActivityForResult(role, new Bundle(), position);
	}


	private void startActivityForResult(int role, Bundle option, int position)
	{
		final Intent intent = new Intent(getApplicationContext(), OptionsList.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//indicates if it is an event, condition or action
		intent.putExtra("role", role);
		//indicates which options is selected if any
		intent.putExtra("option", option);
		intent.putExtra("position", position);
		//
		startActivityForResult(intent, role);
	}


	public void generateEventRow() {
		Drawable drawable = this.getResources().getDrawable(((KeepDoingItApplication) this.getApplicationContext()).getDrawableID(event.getInt("type")));
		String description = ((KeepDoingItApplication) this.getApplicationContext()).generateEventDescription(event);

		ViewGroup eventRow = (ViewGroup) this.getLayoutInflater().inflate(R.layout.ruledetails_row, null);
		((TextView) eventRow.findViewById(R.id.text)).setText(Html.fromHtml(description));
		((ImageView) eventRow.findViewById(R.id.image)).setImageDrawable(drawable);
		((ImageView) eventRow.findViewById(R.id.image)).setImageLevel(0);

		OnClickListener editRow = new OnClickListener()
		{
			public void onClick(View v)
			{
				startActivityForResult(0, event, 0);
			}
		};
		eventRow.setOnClickListener(editRow);

		OnClickListener removeRow = new OnClickListener() {
			public void onClick(View v){
				//tirar do vetor //tirar do view parent //colocar o botao de add visivel //mostrar toast de desfazer
				ViewGroup parent = (ViewGroup) v.getParent();

				//
				Bundle option = (Bundle) event.clone();
				//
				event.clear();
				//
				logEditAction("remove", 0, option, 1);
				//
				
				eventVG.removeView(parent);
				//desabilitar botao de save
				addEvent.setVisibility(View.VISIBLE);

				//mUndoBarController.showUndoBar(false, "Event removed.", null);
			}
		};
		((ImageButton) eventRow.findViewById(R.id.remove)).setOnClickListener(removeRow);

		if (eventVG.getChildCount()>0)
		{
			eventVG.removeViewAt(0);
		}
		eventVG.addView(eventRow);
	}


	public void generateConditionRow(final int j) {
		Drawable drawable = this.getResources().getDrawable(((KeepDoingItApplication) this.getApplicationContext()).getDrawableID(conditions.get(j).getInt("type")));
		String description = "if " + ((KeepDoingItApplication) this.getApplicationContext()).generateConditionDescription(conditions.get(j));

		ViewGroup conditionRow = (ViewGroup) this.getLayoutInflater().inflate(R.layout.ruledetails_row, null);
		((TextView) conditionRow.findViewById(R.id.text)).setText(Html.fromHtml(description));
		((ImageView) conditionRow.findViewById(R.id.image)).setImageDrawable(drawable);
		((ImageView) conditionRow.findViewById(R.id.image)).setImageLevel(1);

		OnClickListener editRow = new OnClickListener()
		{
			public void onClick(View v)
			{
				int index = conditionsVG.indexOfChild(v);

				ViewGroup parent = (ViewGroup) v.getParent();
				//Toast.makeText(getApplicationContext(), String.valueOf(parent.getChildCount()) + " - " +String.valueOf(index), Toast.LENGTH_LONG).show();
				startActivityForResult(1, conditions.get(index), index);
			}
		};
		conditionRow.setOnClickListener(editRow);

		OnClickListener removeRow = new OnClickListener()
		{
			public void onClick(View v)
			{
				//tirar do vetor //tirar do view parent //colocar o botao de add visivel //mostrar toast de desfazer
				ViewGroup parent = (ViewGroup) v.getParent();
				int index = conditionsVG.indexOfChild(parent);

				//
				logEditAction("remove", 1, conditions.remove(index), index+2);
				//

				conditionsVG.removeView(parent);
				addCondition.setVisibility(View.VISIBLE);

				//mUndoBarController.showUndoBar(false, "Condition removed.", null);
			}
		};
		((ImageButton) conditionRow.findViewById(R.id.remove)).setOnClickListener(removeRow);
		//mostrar dialog com opcoes de configurar ou trocar a opcao selecionada
		//actionRow.setOnClickListener(filterResults);

		if (j<(conditionsVG.getChildCount()))
		{
			conditionsVG.removeViewAt(j);
		}
		conditionsVG.addView(conditionRow, j);
		//conditionsVG.addView(conditionRow);
	}


	public void generateActionRow(final int j) {
		Drawable drawable = this.getResources().getDrawable(((KeepDoingItApplication) this.getApplicationContext()).getDrawableID(actions.get(j).getInt("type")));
		String description = ((KeepDoingItApplication) this.getApplicationContext()).generateActionDescription(actions.get(j));

		ViewGroup actionRow = (ViewGroup) this.getLayoutInflater().inflate(R.layout.ruledetails_row, null);
		((TextView) actionRow.findViewById(R.id.text)).setText(Html.fromHtml(description));		
		((ImageView) actionRow.findViewById(R.id.image)).setImageDrawable(drawable);
		((ImageView) actionRow.findViewById(R.id.image)).setImageLevel(2);

		OnClickListener editRow = new OnClickListener()
		{
			public void onClick(View v)
			{
				int index = actionsVG.indexOfChild(v);
				startActivityForResult(2, actions.get(index), index);
			}
		};
		actionRow.setOnClickListener(editRow);

		OnClickListener removeRow = new OnClickListener() {
			public void onClick(View v){
				//tirar do vetor //tirar do view parent //colocar o botao de add visivel //mostrar toast de desfazer
				ViewGroup parent = (ViewGroup) v.getParent();
				int index = actionsVG.indexOfChild(parent);

				//
				logEditAction("remove", 2, actions.remove(index), index+2+conditions.size());
				//

				//actions.remove(index);
				actionsVG.removeView(parent);
				if (actions.isEmpty()) {
					addAction.setText("Add an action (required)");
					//desabilitar botao de save
				}
				addAction.setVisibility(View.VISIBLE);

				//mUndoBarController.showUndoBar(false, "Action removed.", null);
			}
		};
		((ImageButton) actionRow.findViewById(R.id.remove)).setOnClickListener(removeRow);

		if (j<(actionsVG.getChildCount()))
		{
			actionsVG.removeViewAt(j);
		}
		actionsVG.addView(actionRow, j);
		//actionsVG.addView(actionRow);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		getMenuInflater().inflate(R.menu.ruledetails_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_save:
			save();
			//go to my rules
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	protected void save()
	{
		if (event.isEmpty())
		{
			Toast.makeText(getApplicationContext(), "You have to add at least one event before saving the rule" , Toast.LENGTH_LONG).show();
			return;
		}

		if (actions.isEmpty())
		{
			Toast.makeText(getApplicationContext(), "You have to add at least one action before saving the rule" , Toast.LENGTH_LONG).show();
			return;
		}

		if (newRule)
		{
			// Defines an object to contain the new values to insert
			ContentValues mNewValues = new ContentValues();
			mNewValues.put(KeepDoingItProvider.Rules.ACTIVATED, 1);
			// Defines a new Uri object that receives the result of the insertion
			Uri mNewUri = getContentResolver().insert(KeepDoingItProvider.Rules.CONTENT_URI, mNewValues);
			//
			long ruleId = ContentUris.parseId(mNewUri);

			//
			ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

			//batch event
			{
				int type = event.getInt("type");
				String value = event.getString("value");
				String extraValue = event.getString("extra_value");

				batch.add(ContentProviderOperation.newInsert(KeepDoingItProvider.RuleParts.CONTENT_URI)
						.withValue(KeepDoingItProvider.RuleParts.ROLE, 0)
						.withValue(KeepDoingItProvider.RuleParts.RULE_ID, ruleId)
						.withValue(KeepDoingItProvider.RuleParts.TYPE, type)
						.withValue(KeepDoingItProvider.RuleParts.VALUE, value)
						.withValue(KeepDoingItProvider.RuleParts.EXTRA_VALUE, extraValue)
						.build());
			}

			//batch conditions
			for (int j=0; j<conditions.size(); j++) {
				int type = conditions.get(j).getInt("type");
				String value = conditions.get(j).getString("value");
				String extraValue = conditions.get(j).getString("extra_value");

				batch.add(ContentProviderOperation.newInsert(KeepDoingItProvider.RuleParts.CONTENT_URI)
						.withValue(KeepDoingItProvider.RuleParts.ROLE, 1)
						.withValue(KeepDoingItProvider.RuleParts.RULE_ID, ruleId)
						.withValue(KeepDoingItProvider.RuleParts.TYPE, type)
						.withValue(KeepDoingItProvider.RuleParts.VALUE, value)
						.withValue(KeepDoingItProvider.RuleParts.EXTRA_VALUE, extraValue)
						.build());
			}

			//batch actions
			for (int j=0; j<actions.size(); j++) {
				int type = actions.get(j).getInt("type");
				String value = actions.get(j).getString("value");
				String extraValue = actions.get(j).getString("extra_value");

				batch.add(ContentProviderOperation.newInsert(KeepDoingItProvider.RuleParts.CONTENT_URI)
						.withValue(KeepDoingItProvider.RuleParts.ROLE, 2)
						.withValue(KeepDoingItProvider.RuleParts.RULE_ID, ruleId)
						.withValue(KeepDoingItProvider.RuleParts.TYPE, type)
						.withValue(KeepDoingItProvider.RuleParts.VALUE, value)
						.withValue(KeepDoingItProvider.RuleParts.EXTRA_VALUE, extraValue)
						.build());
			}

			//insert batch
			try {
				//ContentProviderResult [] result = 
				getContentResolver().applyBatch(KeepDoingItProvider.AUTHORITY, batch);
			} catch (RemoteException e) {
				throw new RuntimeException("Problem applying batch operation", e);
			} catch (OperationApplicationException e) {
				throw new RuntimeException("Problem applying batch operation", e);
			}

			////
			Bundle rule = new Bundle();
			rule.putBundle("event", event);
			rule.putParcelableArrayList("conditions", conditions);
			rule.putParcelableArrayList("actions", actions);

			Bundle action = new Bundle();
			action.putInt("actionType", KeepDoingItApplication.ACTION_SAVE);
			action.putBundle("rule", rule);
			((KeepDoingItApplication) getApplicationContext()).writeLog(action);
		}
		else {
			//update old rule
			long ruleId = this.getIntent().getLongExtra("id", 0);

			//delete old rule parts before inserting the new ones
			String selection = KeepDoingItProvider.RuleParts.RULE_ID + " = ?";
			String[] selectionArgs = new String[]{String.valueOf(ruleId)};
			getContentResolver().delete(KeepDoingItProvider.RuleParts.CONTENT_URI, selection, selectionArgs);

			//
			ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

			//batch event
			{
				int type = event.getInt("type");
				String value = event.getString("value");
				String extraValue = event.getString("extra_value");

				batch.add(ContentProviderOperation.newInsert(KeepDoingItProvider.RuleParts.CONTENT_URI)
						.withValue(KeepDoingItProvider.RuleParts.ROLE, 0)
						.withValue(KeepDoingItProvider.RuleParts.RULE_ID, ruleId)
						.withValue(KeepDoingItProvider.RuleParts.TYPE, type)
						.withValue(KeepDoingItProvider.RuleParts.VALUE, value)
						.withValue(KeepDoingItProvider.RuleParts.EXTRA_VALUE, extraValue)
						.build());
			}

			//batch conditions
			for (int j=0; j<conditions.size(); j++) {
				int type = conditions.get(j).getInt("type");
				String value = conditions.get(j).getString("value");
				String extraValue = conditions.get(j).getString("extra_value");

				batch.add(ContentProviderOperation.newInsert(KeepDoingItProvider.RuleParts.CONTENT_URI)
						.withValue(KeepDoingItProvider.RuleParts.ROLE, 1)
						.withValue(KeepDoingItProvider.RuleParts.RULE_ID, ruleId)
						.withValue(KeepDoingItProvider.RuleParts.TYPE, type)
						.withValue(KeepDoingItProvider.RuleParts.VALUE, value)
						.withValue(KeepDoingItProvider.RuleParts.EXTRA_VALUE, extraValue)
						.build());
			}

			//batch actions
			for (int j=0; j<actions.size(); j++) {
				int type = actions.get(j).getInt("type");
				String value = actions.get(j).getString("value");
				String extraValue = actions.get(j).getString("extra_value");

				batch.add(ContentProviderOperation.newInsert(KeepDoingItProvider.RuleParts.CONTENT_URI)
						.withValue(KeepDoingItProvider.RuleParts.ROLE, 2)
						.withValue(KeepDoingItProvider.RuleParts.RULE_ID, ruleId)
						.withValue(KeepDoingItProvider.RuleParts.TYPE, type)
						.withValue(KeepDoingItProvider.RuleParts.VALUE, value)
						.withValue(KeepDoingItProvider.RuleParts.EXTRA_VALUE, extraValue)
						.build());
			}

			//insert batch
			try {
				//ContentProviderResult [] result = 
				getContentResolver().applyBatch(KeepDoingItProvider.AUTHORITY, batch);
			} catch (RemoteException e) {
				throw new RuntimeException("Problem applying batch operation", e);
			} catch (OperationApplicationException e) {
				throw new RuntimeException("Problem applying batch operation", e);
			}

		}
		////
		final Intent intent = new Intent(getApplicationContext(), MyRulesList.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		//overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}


	protected void onResume() {
		super.onResume();

		NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(1);
	}
}

