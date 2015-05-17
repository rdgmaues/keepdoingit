package com.rm.keepdoingit;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MyRulesListAdapter extends SimpleCursorAdapter {
	private Context context;
	private int layout;
	Cursor cursor;

	public MyRulesListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.context = context;
		this.layout = layout;
	}

	private static class ViewHolder {
		public ImageView[] ruleImages;
		public TextView ruleText;
		public Button activateButton;
		public Button deleteButton;
	}

	
	public long getRuleId(int position) {
		cursor = this.getCursor();
		cursor.moveToPosition(position);
		return cursor.getInt(cursor.getColumnIndexOrThrow(KeepDoingItProvider.Rules._ID));
	}
	
	public Bundle getRule(int position) {
		//convert to bundle
		cursor = this.getCursor();
		cursor.moveToPosition(position);

		final Integer ruleId = cursor.getInt(cursor.getColumnIndexOrThrow(KeepDoingItProvider.Rules._ID));

		//
		String[] projection = new String[]{KeepDoingItProvider.RuleParts._ID, KeepDoingItProvider.RuleParts.ROLE, 
				KeepDoingItProvider.RuleParts.RULE_ID, KeepDoingItProvider.RuleParts.TYPE,
				KeepDoingItProvider.RuleParts.VALUE, KeepDoingItProvider.RuleParts.EXTRA_VALUE};
		String selection = KeepDoingItProvider.RuleParts.RULE_ID + " = ?";
		String[] selectionArgs = new String[]{String.valueOf(ruleId)};

		Cursor partCursor = context.getContentResolver().query(KeepDoingItProvider.RuleParts.CONTENT_URI, projection, selection, selectionArgs, null);

		//
		Bundle event = new Bundle();
		ArrayList<Bundle> conditions = new ArrayList<Bundle>();
		ArrayList<Bundle> actions = new ArrayList<Bundle>();

		while (partCursor.moveToNext()) {
			int role = partCursor.getInt(partCursor.getColumnIndex(KeepDoingItProvider.RuleParts.ROLE));
			int type = partCursor.getInt(partCursor.getColumnIndex(KeepDoingItProvider.RuleParts.TYPE));
			String value = partCursor.getString(partCursor.getColumnIndex(KeepDoingItProvider.RuleParts.VALUE));
			String extraValue = partCursor.getString(partCursor.getColumnIndex(KeepDoingItProvider.RuleParts.EXTRA_VALUE));

			Bundle rulePart = new Bundle();
			rulePart.putInt("type", type);
			rulePart.putString("value", value);
			rulePart.putString("extra_value", extraValue);

			switch (role) {
			case 0: event = (Bundle) rulePart.clone(); break;
			case 1: conditions.add((Bundle) rulePart.clone()); break;
			case 2: actions.add((Bundle) rulePart.clone()); break;
			}
		}

		//
		Bundle rule = new Bundle();
		rule.putBundle("event", (Bundle) event.clone());
		rule.putParcelableArrayList("conditions", (ArrayList<Bundle>) conditions.clone());
		rule.putParcelableArrayList("actions", (ArrayList<Bundle>) actions.clone());

		return rule;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(layout, null);

			holder = new ViewHolder();

			holder.ruleText = (TextView) convertView.findViewById(R.id.rule_text);
			//E + C + C + C -> A + A + A
			holder.ruleImages = new ImageView[13];
			holder.ruleImages[0] = (ImageView) convertView.findViewById(R.id.rule_event);
			holder.ruleImages[1] = (ImageView) convertView.findViewById(R.id.rule_and_1);
			holder.ruleImages[2] = (ImageView) convertView.findViewById(R.id.rule_condition_1);
			holder.ruleImages[3] = (ImageView) convertView.findViewById(R.id.rule_and_2);
			holder.ruleImages[4] = (ImageView) convertView.findViewById(R.id.rule_condition_2);
			holder.ruleImages[5] = (ImageView) convertView.findViewById(R.id.rule_and_3);
			holder.ruleImages[6] = (ImageView) convertView.findViewById(R.id.rule_condition_3);
			holder.ruleImages[7] = (ImageView) convertView.findViewById(R.id.rule_then);
			holder.ruleImages[8] = (ImageView) convertView.findViewById(R.id.rule_action_1);
			holder.ruleImages[9] = (ImageView) convertView.findViewById(R.id.rule_and_4);
			holder.ruleImages[10] = (ImageView) convertView.findViewById(R.id.rule_action_2);
			holder.ruleImages[11] = (ImageView) convertView.findViewById(R.id.rule_and_5);
			holder.ruleImages[12] = (ImageView) convertView.findViewById(R.id.rule_action_3);

			holder.activateButton = (Button) convertView.findViewById(R.id.activate_button);
			holder.deleteButton = (Button) convertView.findViewById(R.id.delete_button);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		////
		cursor = this.getCursor();
		cursor.moveToPosition(position);

		final Integer ruleId = cursor.getInt(cursor.getColumnIndexOrThrow(KeepDoingItProvider.Rules._ID));

		final Integer activated = cursor.getInt(cursor.getColumnIndexOrThrow(KeepDoingItProvider.Rules.ACTIVATED));

		//
		String[] projection = new String[]{KeepDoingItProvider.RuleParts._ID, KeepDoingItProvider.RuleParts.ROLE, 
				KeepDoingItProvider.RuleParts.RULE_ID, KeepDoingItProvider.RuleParts.TYPE,
				KeepDoingItProvider.RuleParts.VALUE, KeepDoingItProvider.RuleParts.EXTRA_VALUE};
		String selection = KeepDoingItProvider.RuleParts.RULE_ID + " = ?";
		String[] selectionArgs = new String[]{String.valueOf(ruleId)};

		Cursor partCursor = context.getContentResolver().query(KeepDoingItProvider.RuleParts.CONTENT_URI, projection, selection, selectionArgs, null);

		//
		Bundle event = new Bundle();
		ArrayList<Bundle> conditions = new ArrayList<Bundle>();
		ArrayList<Bundle> actions = new ArrayList<Bundle>();

		while (partCursor.moveToNext()) {
			int role = partCursor.getInt(partCursor.getColumnIndex(KeepDoingItProvider.RuleParts.ROLE));
			int type = partCursor.getInt(partCursor.getColumnIndex(KeepDoingItProvider.RuleParts.TYPE));
			String value = partCursor.getString(partCursor.getColumnIndex(KeepDoingItProvider.RuleParts.VALUE));

			Bundle rulePart = new Bundle();
			rulePart.putInt("type", type);
			rulePart.putString("value", value);

			switch (role) {
			case 0: event = (Bundle) rulePart.clone(); break;
			case 1: conditions.add((Bundle) rulePart.clone()); break;
			case 2: actions.add((Bundle) rulePart.clone()); break;
			}
		}
		////

		//0
		holder.ruleImages[1].setVisibility(View.GONE);
		holder.ruleImages[2].setVisibility(View.GONE);
		holder.ruleImages[3].setVisibility(View.GONE);
		holder.ruleImages[4].setVisibility(View.GONE);
		holder.ruleImages[5].setVisibility(View.GONE);
		holder.ruleImages[6].setVisibility(View.GONE);
		//7
		holder.ruleImages[8].setVisibility(View.GONE);
		holder.ruleImages[9].setVisibility(View.GONE);
		holder.ruleImages[10].setVisibility(View.GONE);
		holder.ruleImages[11].setVisibility(View.GONE);
		holder.ruleImages[12].setVisibility(View.GONE);

		//
		String description = "";
		Drawable drawable = null;

		//event
		description += ((KeepDoingItApplication) context.getApplicationContext()).generateEventDescription(event);
		drawable = context.getResources().getDrawable(((KeepDoingItApplication) context.getApplicationContext()).getDrawableID(event.getInt("type")));
		holder.ruleImages[0].setImageDrawable(drawable);
		holder.ruleImages[0].setImageLevel(0);
		holder.ruleImages[0].setVisibility(View.VISIBLE);

		//conditions
		int i = 1;
		for (int j=0; j<conditions.size(); j++) {
			//+
			holder.ruleImages[j+i].setVisibility(View.VISIBLE);
			i++;

			drawable = context.getResources().getDrawable(((KeepDoingItApplication) context.getApplicationContext()).getDrawableID(conditions.get(j).getInt("type")));
			holder.ruleImages[j+i].setImageDrawable(drawable);
			holder.ruleImages[j+i].setImageLevel(1);
			holder.ruleImages[j+i].setVisibility(View.VISIBLE);

			////
			if (j==0) description += ", if";
			else {
				if (j==conditions.size()-1) description += " and"; 
				else description += ",";
			}

			description += " " + ((KeepDoingItApplication) context.getApplicationContext()).generateConditionDescription(conditions.get(j));

			if (j==conditions.size()-1) description += ",";
		}

		//->
		//actions
		i = 8;
		for (int j=0; j<actions.size(); j++) {
			if (j>0) {
				//+
				holder.ruleImages[j+i].setVisibility(View.VISIBLE);
				i++;

				////
				if (j==actions.size()-1) description += " and"; 
				else description += ",";
			}

			drawable = context.getResources().getDrawable(((KeepDoingItApplication) context.getApplicationContext()).getDrawableID(actions.get(j).getInt("type")));
			holder.ruleImages[j+i].setImageDrawable(drawable);
			holder.ruleImages[j+i].setImageLevel(2);
			holder.ruleImages[j+i].setVisibility(View.VISIBLE);

			////
			description += " " + ((KeepDoingItApplication) context.getApplicationContext()).generateActionDescription(actions.get(j));
		}

		//

		holder.ruleText.setText(Html.fromHtml(description));

		//

		if (activated == 0)
		{
			holder.ruleText.setTextColor(context.getResources().getColor(R.color.light_gray));
			holder.activateButton.setCompoundDrawablesWithIntrinsicBounds(
					context.getResources().getDrawable(R.drawable.ic_action_turnedoff), null, null, null);
			holder.activateButton.setText(context.getResources().getString(R.string.turnon));
			
			//Drawable d = holder.ruleImages[0].getDrawable();
			//d.mutate().setColorFilter(0xffcccccc, Mode.SRC_ATOP);
			//holder.ruleImages[0].setImageDrawable(d);
		}
		else 
		{
			holder.ruleText.setTextColor(context.getResources().getColor(R.color.gray));
			holder.activateButton.setCompoundDrawablesWithIntrinsicBounds(
					context.getResources().getDrawable(R.drawable.ic_action_turnedon), null, null, null);
			holder.activateButton.setText(context.getResources().getString(R.string.turnoff));
			
			//Drawable d = holder.ruleImages[0].getDrawable();
			//d.mutate().setColorFilter(0xff6699cc, Mode.SRC_ATOP);
			//holder.ruleImages[0].setImageDrawable(d);
		}

		//
		holder.activateButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) {
				//(de)activate rule
				String selection = KeepDoingItProvider.Rules._ID + " = ?";
				String[] selectionArgs = new String[]{String.valueOf(ruleId)};
				// Defines an object to contain the new values to update
				ContentValues values = new ContentValues();
				values.put(KeepDoingItProvider.Rules.ACTIVATED, Math.abs(activated-1));

				context.getContentResolver().update(KeepDoingItProvider.Rules.CONTENT_URI, values, selection, selectionArgs);

				// Restarting the Activity's loader to refresh the listview
				((MyRulesList) context).getSupportLoaderManager().restartLoader(0, null, (LoaderCallbacks<Cursor>)((MyRulesList) context));
			}
		});

		holder.deleteButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) {
				//delete rule
				String selection = KeepDoingItProvider.Rules._ID + " = ?";
				String[] selectionArgs = new String[]{String.valueOf(ruleId)};

				context.getContentResolver().delete(KeepDoingItProvider.Rules.CONTENT_URI, selection, selectionArgs);

				// Restarting the Activity's loader to refresh the listview
				((MyRulesList) context).getSupportLoaderManager().restartLoader(0, null, (LoaderCallbacks<Cursor>)((MyRulesList) context));
			}
		});

		return convertView;
	}
}
