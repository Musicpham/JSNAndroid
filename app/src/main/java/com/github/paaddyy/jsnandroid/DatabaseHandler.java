package com.github.paaddyy.jsnandroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
	
	//DECLARE GENERAL-PARAMETERS
	public static final String DB_NAME = "database.SQLite";
	public static final int DB_VERSION = 1;
	
	//DECLARE COLUMNS-PARKING
	public static final String TB_NAME_PARKING = "parking";
	public static final String SPACE_PARKING = "SPACE";
	public static final String _FREE = "_FREE";
	public static final String _GROUP_PARK = "_GROUP";
	
	//DECLARE COLUMNS-GROUPS
	public static final String TB_NAME_GROUPS = "groups";
	public static final String ID = "ID";
	public static final String _GROUP_GROUPS = "_GROUP";
	
	//DECLARE COLUMNS-PARKING
	public static final String TB_NAME_PARKING_COLOR = "parking_color";
	public static final String SPACE_PARKING_COLOR = "SPACE";
	public static final String _COLOR_LEFT = "COLOR_LEFT";
	public static final String _COLOR_RIGHT = "COLOR_RIGHT";
	
	
	//CONSTRUCTOR TO INITIALIZE THE DATABASE IF NOT EXISTS
	public DatabaseHandler(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//CREATE PARKING-TABLE
		String CREATE_PARKING_TABLE = "CREATE TABLE IF NOT EXISTS " + TB_NAME_PARKING
				+ " (" 
				+ SPACE_PARKING + " INTEGER PRIMARY KEY NOT NULL,"
				+ _FREE + " INTEGER NOT NULL,"
				+ _GROUP_PARK + " INTEGER NOT NULL, "
				+ " FOREIGN KEY ("+ _GROUP_PARK +") REFERENCES "+ TB_NAME_GROUPS+"("+ID+")"
				+ ");";
		
		//CREATE GROUPS-TABLE
		String CREATE_GROUPS_TABLE = "CREATE TABLE IF NOT EXISTS " + TB_NAME_GROUPS
				+ " (" 
				+ ID + " INTEGER PRIMARY KEY NOT NULL,"
				+ _GROUP_GROUPS + " TEXT NOT NULL"
				+ ");";
		
		//CREATE GROUPS-TABLE
		String CREATE_PARKING_COLOR_TABLE = "CREATE TABLE IF NOT EXISTS " + TB_NAME_PARKING_COLOR
				+ " (" 
				+ SPACE_PARKING_COLOR + " INTEGER NOT NULL,"
				+ _COLOR_LEFT + " INTEGER NOT NULL,"
				+ _COLOR_RIGHT + " INTEGER NOT NULL,"
				+ " FOREIGN KEY ("+ SPACE_PARKING_COLOR +") REFERENCES "+ TB_NAME_PARKING+"("+SPACE_PARKING+")"
				+ ");";
		
		
		//EXECUTE THE SQL-STATEMENTS
		db.execSQL(CREATE_GROUPS_TABLE);
		db.execSQL(CREATE_PARKING_TABLE);
		db.execSQL(CREATE_PARKING_COLOR_TABLE);	
		
		initializeDatabase(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//DROP ALL OLD TABLES IF EXISTED
        db.execSQL("DROP TABLE IF EXISTS " + TB_NAME_PARKING);
        db.execSQL("DROP TABLE IF EXISTS " + TB_NAME_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TB_NAME_PARKING_COLOR);
        
        //RESTART THE CREATE PROCESS
        onCreate(db);

	}
	
	public void initializeDatabase(SQLiteDatabase db){
		
		String GROUPS = "INSERT INTO " + TB_NAME_GROUPS +" VALUES (1,'FHDW'), (2,'BIB');";
		String PARKING = "INSERT INTO " + TB_NAME_PARKING +" VALUES (1,1,1), (2,1,1), (3,1,1), (4,1,1), (5,1,2), (6,1,2), (7,1,2), (8,1,2), (9,1,2);";
		String PARKING_COLOR = "INSERT INTO " + TB_NAME_PARKING_COLOR + " VALUES (1, 0, 0), (2, 120, 0), (3, 240, 0), (4, 0, 120), (5, 120, 120), (6, 240, 120), "
				+ "(7, 0, 240), (8,120,240), (9, 240, 240);";
	
		db.execSQL(GROUPS);
		db.execSQL(PARKING);
		db.execSQL(PARKING_COLOR);
	}
	
	public int getFreeSpace(){
		SQLiteDatabase db = this.getReadableDatabase();
		int space = 0;
		
		String query = "SELECT * FROM PARKING WHERE _FREE = 1 LIMIT 1";
		Cursor cursor = db.rawQuery(query, null);
		
		//GET DATA AND WRITE INTO VALUE
		if (null != cursor && cursor.moveToFirst()) {
		    space = Integer.parseInt(cursor.getString(0));
		}
		
		db.close();
		return space;
	}
	
	public int changeStatusOfSpace(String space){
		SQLiteDatabase db = this.getReadableDatabase();
		
		String reverse = "-1";
		int status = -1;
		
		status = getStatusOfSpace(space);
		
		if(status == 0){
			reverse = "1";
		}else if(status == 1){
			reverse = "0";
		}
		
		ContentValues cv = new ContentValues();
		cv.put("_FREE", reverse); 
		
		db.update(TB_NAME_PARKING, cv, "SPACE="+space, null);
		
		db.close();
		return Integer.parseInt(reverse);
	}
	
	public int getStatusOfSpace(String space){
		int status = -1;
		SQLiteDatabase db = this.getReadableDatabase();
		
		String query = "SELECT _FREE FROM PARKING WHERE SPACE = "+ space + " LIMIT 1";
		Cursor cursor = db.rawQuery(query, null);
		
		//GET DATA AND WRITE INTO VALUE
		if (null != cursor && cursor.moveToFirst()) {
		    status = Integer.parseInt(cursor.getString(0));
		}
		
		return status;
	}
}
