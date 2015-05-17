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

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Content Provider that implements functions for all the apps database manipulations.
 * 
 * @author Rodrigo Maues
 * @version 1.0
 */
public class KeepDoingItProvider extends ContentProvider {
	//Constant values for the database
	public static final String AUTHORITY = "com.rm.keepdoingit.provider";  
	private static final String DATABASE_NAME = "keepdoingit.db";
	private static final int DATABASE_VERSION = 2;

	//Static references to the name of the tables
	public static final String INTERACTIONS_TABLE = "interactions";
	public static final String CONTEXTS_TABLE = "contexts";
	//
	public static final String RULES_TABLE = "rules";
	public static final String RULEPARTS_TABLE = "rule_parts";

	private static final int INTERACTIONS = 1; 
	private static final int CONTEXTS = 2;
	//
	private static final int RULES = 3;
	private static final int RULEPARTS = 4;


	//Responsible for handling the manipulations to the database
	private DBHelper mHelper;
	private static final UriMatcher mMatcher;  
	private static HashMap<String, String> mInteractionsProjection;
	private static HashMap<String, String> mContextsProjection;
	//
	private static HashMap<String, String> mRulesProjection;
	private static HashMap<String, String> mRulePartsProjection;

	//Projection variables that allow the manipulation of each table.
	static {
		mInteractionsProjection = new HashMap<String, String>();
		mInteractionsProjection.put(Interactions._ID, Interactions._ID);
		mInteractionsProjection.put(Interactions.TYPE, Interactions.TYPE);
		mInteractionsProjection.put(Interactions.VALUE, Interactions.VALUE);
		mInteractionsProjection.put(Interactions.EXTRA_VALUE, Interactions.EXTRA_VALUE);
		mInteractionsProjection.put(Interactions.TIMESTAMP, Interactions.TIMESTAMP);

		mContextsProjection = new HashMap<String, String>();
		mContextsProjection.put(Contexts._ID, Contexts._ID);
		mContextsProjection.put(Contexts.INTERACTION_ID, Contexts.INTERACTION_ID);
		mContextsProjection.put(Contexts.TYPE, Contexts.TYPE);
		mContextsProjection.put(Contexts.VALUE, Contexts.VALUE);
		mContextsProjection.put(Contexts.EXTRA_VALUE, Contexts.EXTRA_VALUE);

		//

		mRulesProjection = new HashMap<String, String>();
		mRulesProjection.put(Rules._ID, Rules._ID);
		mRulesProjection.put(Rules.ACTIVATED, Rules.ACTIVATED);

		mRulePartsProjection = new HashMap<String, String>();
		mRulePartsProjection.put(RuleParts._ID, RuleParts._ID);
		mRulePartsProjection.put(RuleParts.RULE_ID, RuleParts.RULE_ID);
		mRulePartsProjection.put(RuleParts.ROLE, RuleParts.ROLE);
		mRulePartsProjection.put(RuleParts.TYPE, RuleParts.TYPE);
		mRulePartsProjection.put(RuleParts.VALUE, RuleParts.VALUE);
		mRulePartsProjection.put(RuleParts.EXTRA_VALUE, RuleParts.EXTRA_VALUE);

	}  

	static {  
		mMatcher = new UriMatcher(UriMatcher.NO_MATCH);  
		mMatcher.addURI(AUTHORITY, INTERACTIONS_TABLE, INTERACTIONS); 
		mMatcher.addURI(AUTHORITY, CONTEXTS_TABLE, CONTEXTS);
		//
		mMatcher.addURI(AUTHORITY, RULES_TABLE, RULES);
		mMatcher.addURI(AUTHORITY, RULEPARTS_TABLE, RULEPARTS);
	} 

	/**
	 * Defines how to handle an deletion in the database according to the table.
	 */
	@Override  
	public int delete(Uri uri, String selection, String[] selectionArgs) {  
		SQLiteDatabase db = mHelper.getWritableDatabase();
		int count;

		switch (mMatcher.match(uri)) {
		case INTERACTIONS:
			count = db.delete(INTERACTIONS_TABLE, selection, selectionArgs);
			break;
		case CONTEXTS:
			count = db.delete(CONTEXTS_TABLE, selection, selectionArgs);
			break;
			//
		case RULES:
			count = db.delete(RULES_TABLE, selection, selectionArgs);
			break;
		case RULEPARTS:
			count = db.delete(RULEPARTS_TABLE, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		//return db.delete(uri.getLastPathSegment(), selection, selectionArgs);
		return count;
	}  

	@Override  
	public String getType(Uri uri) {  
		return null;  
	}  

	/**
	 * Defines how to handle an insertion in the database according to the table.
	 */
	@Override  
	public Uri insert(Uri uri, ContentValues values) {  
		SQLiteDatabase db = mHelper.getWritableDatabase(); 
		long rowId;

		switch (mMatcher.match(uri)) {  
		case INTERACTIONS:  
			rowId = db.insert(INTERACTIONS_TABLE, null, values);

			if (rowId > 0) {  
				Uri noteUri = ContentUris.withAppendedId(Interactions.CONTENT_URI, rowId);  
				getContext().getContentResolver().notifyChange(noteUri, null); 
				return noteUri;  
			}

		case CONTEXTS:  
			rowId = db.insert(CONTEXTS_TABLE, null, values);  

			if (rowId > 0) {  
				Uri noteUri = ContentUris.withAppendedId(Contexts.CONTENT_URI, rowId);  
				getContext().getContentResolver().notifyChange(noteUri, null); 
				return noteUri;  
			}
			//
		case RULES:  
			rowId = db.insert(RULES_TABLE, null, values);  

			if (rowId > 0) {  
				Uri noteUri = ContentUris.withAppendedId(Rules.CONTENT_URI, rowId);  
				getContext().getContentResolver().notifyChange(noteUri, null); 
				return noteUri;  
			}
		case RULEPARTS:  
			rowId = db.insert(RULEPARTS_TABLE, null, values);  

			if (rowId > 0) {  
				Uri noteUri = ContentUris.withAppendedId(RuleParts.CONTENT_URI, rowId);  
				getContext().getContentResolver().notifyChange(noteUri, null); 
				return noteUri;  
			}

		default:  
			throw new IllegalArgumentException("URI desconhecida " + uri);  
		}   
	}  

	@Override  
	public boolean onCreate() { 
		mHelper = new DBHelper(getContext());
		return true;  
	}  

	/**
	 * Defines how to handle an query in the database according to the table.
	 */
	@Override  
	public Cursor query(Uri uri, String[] projection, String selection,  
			String[] selectionArgs, String sortOrder) {  
		// Aqui usaremos o SQLiteQueryBuilder para construir  
		// a query que sera feita ao DB, retornando um cursor  
		// que enviaremos a aplicacao.
		SQLiteQueryBuilder builder = new  SQLiteQueryBuilder();  
		SQLiteDatabase database = mHelper.getReadableDatabase();  
		Cursor cursor;  
		switch (mMatcher.match(uri)) {  
		case INTERACTIONS:  
			builder.setTables(INTERACTIONS_TABLE);  
			builder.setProjectionMap(mInteractionsProjection);  
			break;  
		case CONTEXTS:  
			builder.setTables(CONTEXTS_TABLE);  
			builder.setProjectionMap(mContextsProjection);  
			break;
			//
		case RULES:  
			builder.setTables(RULES_TABLE);  
			builder.setProjectionMap(mRulesProjection);  
			break;
		case RULEPARTS:  
			builder.setTables(RULEPARTS_TABLE);  
			builder.setProjectionMap(mRulePartsProjection);  
			break;

		default:  
			throw new IllegalArgumentException("URI desconhecida " + uri);  
		}

		cursor = builder.query(database, projection, selection,  
				selectionArgs, null, null, sortOrder);  

		cursor.setNotificationUri(getContext().getContentResolver(), uri);  
		return cursor;  
	}   

	/**
	 * Defines how to handle an update in the database according to the table.
	 */
	@Override  
	public int update(Uri uri, ContentValues values, String where,  
			String[] whereArgs) {  
		SQLiteDatabase db = mHelper.getWritableDatabase();
		int count;

		switch (mMatcher.match(uri)) {
		case INTERACTIONS:
			count = db.update(INTERACTIONS_TABLE, values, where, whereArgs);
			break;
		case CONTEXTS:
			count = db.update(CONTEXTS_TABLE, values, where, whereArgs);
			break;
			//
		case RULES:
			count = db.update(RULES_TABLE, values, where, whereArgs);
			break;
		case RULEPARTS:
			count = db.update(RULEPARTS_TABLE, values, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count; 
	}

	/**
	 * Defines the columns in the Interactions table and defines the URI url
	 * to allow future access to this table.
	 */
	public static final class Interactions implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.parse("content://" 
				+ KeepDoingItProvider.AUTHORITY + "/interactions");  
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + KeepDoingItProvider.AUTHORITY;  

		public static final String TYPE = "type";
		public static final String VALUE = "value";  
		public static final String EXTRA_VALUE = "extra_value";
		public static final String TIMESTAMP = "timestamp";
	}

	/**
	 * Defines the columns in the Contexts table and defines the URI url
	 * to allow future access to this table.
	 */
	public static final class Contexts implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.parse("content://" 
				+ KeepDoingItProvider.AUTHORITY + "/contexts");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + KeepDoingItProvider.AUTHORITY;  

		public static final String INTERACTION_ID = "interaction_id";
		public static final String TYPE = "type";
		public static final String VALUE = "value";
		public static final String EXTRA_VALUE = "extra_value";
	}

	//

	public static final class Rules implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.parse("content://" 
				+ KeepDoingItProvider.AUTHORITY + "/rules");  
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + KeepDoingItProvider.AUTHORITY;  

		public static final String ACTIVATED = "activated";
	}


	public static final class RuleParts implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.parse("content://" 
				+ KeepDoingItProvider.AUTHORITY + "/rule_parts");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + KeepDoingItProvider.AUTHORITY;  

		public static final String RULE_ID = "rule_id";
		public static final String ROLE = "role";
		public static final String TYPE = "type";
		public static final String VALUE = "value";
		public static final String EXTRA_VALUE = "extra_value";
	}


	/**
	 * Class responsible for implementing the manipulations to the
	 * database, as the creation and upgrade of database tables.
	 */
	private static class DBHelper extends SQLiteOpenHelper {

		DBHelper(Context context) {  
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}  

		//Creates the database tables
		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL("CREATE TABLE " + INTERACTIONS_TABLE + " ( " 
					+ Interactions._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ Interactions.TYPE + " INTEGER," 
					+ Interactions.VALUE + " TEXT," 
					+ Interactions.EXTRA_VALUE + " TEXT DEFAULT \"\"," 
					+ Interactions.TIMESTAMP + " INTEGER);");

			db.execSQL("CREATE TABLE " + CONTEXTS_TABLE + " ( " 
					+ Contexts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
					+ Contexts.INTERACTION_ID + " INTEGER," 
					+ Contexts.TYPE + " INTEGER," 
					+ Contexts.VALUE + " TEXT," 
					+ Contexts.EXTRA_VALUE + " TEXT DEFAULT \"\"," 
					+ "FOREIGN KEY(" + Contexts.INTERACTION_ID + ") REFERENCES " 
					+ INTERACTIONS_TABLE +"("+ Interactions._ID + ") ON DELETE CASCADE);"); 

			//

			db.execSQL("CREATE TABLE " + RULES_TABLE + " ( " +
					Rules._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
					Rules.ACTIVATED + " INTEGER);");

			db.execSQL("CREATE TABLE " + RULEPARTS_TABLE + " ( " + 
					RuleParts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
					RuleParts.RULE_ID + " INTEGER," +
					RuleParts.ROLE + " INTEGER," + 
					RuleParts.TYPE + " INTEGER," + 
					RuleParts.VALUE + " TEXT," + 
					RuleParts.EXTRA_VALUE + " TEXT DEFAULT \"\"," +  
					"FOREIGN KEY(" + RuleParts.RULE_ID + ") REFERENCES " + RULES_TABLE +"("+ Rules._ID + ") ON DELETE CASCADE);");
		}

		//A real application should upgrade the database in place.
		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {

			db.execSQL("DROP TABLE IF EXISTS " + INTERACTIONS_TABLE);			
			db.execSQL("DROP TABLE IF EXISTS " + CONTEXTS_TABLE);
			//
			db.execSQL("DROP TABLE IF EXISTS " + RULES_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + RULEPARTS_TABLE);


			// Recreates the database with a new version
			onCreate(db);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
		    super.onOpen(db);
		    if (!db.isReadOnly()) {
		        // Enables foreign key constraints
		        db.execSQL("PRAGMA foreign_keys=ON;");
		    }
		}
	}

}
