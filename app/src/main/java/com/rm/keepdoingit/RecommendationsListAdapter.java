/*
 * PUC-Rio, Informatics Department
 * Final Programming Project
 * 
 * Semester: 2013.1
 * Author: Rodrigo Maues
 * Project: Keep Doing It
 * Version: 1.0
 */

package com.rm.keepdoingit;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Extended Android Adapter that is the bridge between a ListView and the data that backs the list.
 * The ListView can display any data provided that it is wrapped in this adapter.
 * 
 * @author Rodrigo Maues
 * @version 1.0
 */
public class RecommendationsListAdapter extends ArrayAdapter<Bundle> {
	private Context context;
	private ArrayList<Bundle> rules = new ArrayList<Bundle>();

	/**
	 * Class that holds the views used in each item of the list (and that saves memory)
	 */
	private static class ViewHolder {
		private ImageView[] ruleImages;
		private TextView ruleText;
	}

	/**
	 * Constructor for the adapter.
	 * @param context the application context
	 * @param layout the layout used in the items that has to be passed to the parent
	 * @param rules reference to the global rules variable
	 */
	public RecommendationsListAdapter(Context context, int layout, ArrayList<Bundle> rules) {
		super(context, layout, rules);
		this.context = context;
		this.rules = rules;
	}


	public Bundle getRule(int position) {
		return rules.get(position);
	}

	/**
	 * Default Adapter method that defines the look and feel of each item in the list.
	 * It is called everytime the item must be rendered.
	 */
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		//Checks if the layout was already inflated and if the view holder 
		//is already associated with each visual component of the layout 
		if (convertView == null) {
			//Inflates the layout of the list item
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.recommendationslist_row, null);

			holder = new ViewHolder();
			holder.ruleText = (TextView) convertView.findViewById(R.id.rule_text);
			//Rules format (E = event, C = condition, A = action): E + C + C + C -> A + A + A
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

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//Gets the automation rule related to a given list position
		Bundle rule = rules.get(position);

		//Sets the visibility of the icons in the automation rule invisible at first.
		//The icons that will be visible will depend on the automation rule.
		holder.ruleImages[1].setVisibility(View.GONE);
		holder.ruleImages[2].setVisibility(View.GONE);
		holder.ruleImages[3].setVisibility(View.GONE);
		holder.ruleImages[4].setVisibility(View.GONE);
		holder.ruleImages[5].setVisibility(View.GONE);
		holder.ruleImages[6].setVisibility(View.GONE);
		holder.ruleImages[8].setVisibility(View.GONE);
		holder.ruleImages[9].setVisibility(View.GONE);
		holder.ruleImages[10].setVisibility(View.GONE);
		holder.ruleImages[11].setVisibility(View.GONE);
		holder.ruleImages[12].setVisibility(View.GONE);

		//Gets the event, conditions and actions of the automation rule
		
		//Variable that will contain the built description of the automation rule
		String description = "";
		Drawable drawable = null;
		Bundle event = rule.getBundle("event");
		ArrayList<Bundle> conditions = rule.getParcelableArrayList("conditions");
		ArrayList<Bundle> actions = rule.getParcelableArrayList("actions");

		//Gets the icon and description information related to the event
		description += ((KeepDoingItApplication) context.getApplicationContext()).
				generateEventDescription(event);
		drawable = context.getResources().
				getDrawable(((KeepDoingItApplication) context.getApplicationContext()).
						getDrawableID(event.getInt("type")));
		holder.ruleImages[0].setImageDrawable(drawable);
		holder.ruleImages[0].setImageLevel(0);
		holder.ruleImages[0].setVisibility(View.VISIBLE);

		//Gets the icon and description information related to each condition
		int i = 1;
		for (int j=0; j<conditions.size(); j++) {
			//+
			holder.ruleImages[j+i].setVisibility(View.VISIBLE);
			i++;

			drawable = context.getResources().
					getDrawable(((KeepDoingItApplication) context.getApplicationContext()).
							getDrawableID(conditions.get(j).getInt("type")));
			holder.ruleImages[j+i].setImageDrawable(drawable);
			holder.ruleImages[j+i].setImageLevel(1);
			holder.ruleImages[j+i].setVisibility(View.VISIBLE);

			//Coordinates the description
			if (j==0) {
				description += ", if";
			} else {
				if (j==conditions.size()-1) {
					description += " and";
				} else {
					description += ",";
				}
			}
			
			description += " " + ((KeepDoingItApplication) context.getApplicationContext()).
					generateConditionDescription(conditions.get(j));
			
			if (j==conditions.size()-1) {
				description += ",";
			}
		}

		//->
		//Gets the icon and description information related to each action
		i = 8;
		for (int j=0; j<actions.size(); j++) {
			if (j>0) {
				//+
				holder.ruleImages[j+i].setVisibility(View.VISIBLE);
				i++;
				
				//Coordinates the description
				if (j==actions.size()-1) {
					description += " and";
				} else {
					description += ",";
				}
			}

			drawable = context.getResources().
					getDrawable(((KeepDoingItApplication) context.getApplicationContext()).
							getDrawableID(actions.get(j).getInt("type")));
			holder.ruleImages[j+i].setImageDrawable(drawable);
			holder.ruleImages[j+i].setImageLevel(2);
			holder.ruleImages[j+i].setVisibility(View.VISIBLE);

			description += " " + ((KeepDoingItApplication) context.getApplicationContext()).
					generateActionDescription(actions.get(j));
		}

		//Set the built description to the list item
		holder.ruleText.setText(Html.fromHtml(description));

		return convertView;
	}

}
