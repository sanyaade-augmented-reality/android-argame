package org.mixare.data;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import edu.fsu.cs.argame.R;

/**
 * Class to handle all database transactions of ARMap
 */
public class ARMapDB {
	
	private SQLiteDatabase db;
	private DatabaseHelper dbHelper;	
	
	private static final int DB_VERSION = 1;
    
    /** Name of the database **/
    private static final String DB_NAME = "armapdb";
    
    /** Table name for favorite entries **/
    private final static String TABLE_NAME = "favorites";
    
    /********************************* Column Names **************************************************/
	public final static String COLUMN_ID = "_id";
	public final static String COLUMN_TITLE = "title";
	public final static String COLUMN_TITLE_COUNT = "title_count";
	public final static String COLUMN_PHONE_NUM = "phone_num";
	public final static String COLUMN_CATEGORY = "category";
	public final static String COLUMN_CATEGORY_POS = "category_pos";
	public final static String COLUMN_LONGITUDE = "longitude";
	public final static String COLUMN_LATITUDE = "latitude";
	
	/** String array that contains all column names **/
	public final static String COLUMNS[] = {COLUMN_ID, COLUMN_TITLE, COLUMN_TITLE_COUNT, COLUMN_PHONE_NUM, COLUMN_CATEGORY,
											COLUMN_CATEGORY_POS, COLUMN_LONGITUDE, COLUMN_LATITUDE };
	
	/** Constant containing the query for creating the favorites table **/
	public final static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
											COLUMN_ID + " integer primary key autoincrement, " +
											COLUMN_TITLE + " text not null, " +
											COLUMN_TITLE_COUNT + " integer, " +
											COLUMN_PHONE_NUM + " text not null default 'others', " +
											COLUMN_CATEGORY + " text not null default 'others', " +
											COLUMN_CATEGORY_POS + " int, " +
											COLUMN_LONGITUDE + " double, " +
											COLUMN_LATITUDE + " double);";
	protected Context context;
	
	public ARMapDB(Context context){
		this.context = context;
		dbHelper = new DatabaseHelper(context);
		open();
		close();
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {          
            db.execSQL(CREATE_TABLE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }
    }
	
	/**
	 * @return the database
	 */
	public SQLiteDatabase getDb() {
		return db;
	}
	
	/**
	 * @param db the db to set
	 */
	public void setDb(SQLiteDatabase db) {
		this.db = db;
	}
	
	public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }
	
	public void close() {
		db.close();
		dbHelper.close();
    }
	
	/**
	 * Inserts a favorite row in the database. It inserts 1 as a default count for a favorite name.
	 * @param db The SQLiteDatabase to be used
	 * @param title the title for the favorite item to be inserted
	 * @param phoneNum the phone number for the favorite item to be inserted, may be empty
	 * @param category the category for the favorite item to be inserted
	 * @param categoryPos the position of the category in the category array
	 * @param longitude the longitude for the favorite item to be inserted
	 * @param latitude the latitude for the favorite item to be inserted
	 * @return the row ID of the inserted favorite or -1 if insert is unsuccessful
	 */
	public int insert(SQLiteDatabase db, String title, String phoneNum, String category, int categoryPos, double longitude, double latitude) {
		int id = -1;
		
		ContentValues args = new ContentValues();
		args.put(COLUMN_TITLE, title);
		args.put(COLUMN_TITLE_COUNT, 1);
		args.put(COLUMN_PHONE_NUM, phoneNum);
		args.put(COLUMN_CATEGORY, category);
		args.put(COLUMN_CATEGORY_POS, categoryPos);
		args.put(COLUMN_LONGITUDE, longitude);
		args.put(COLUMN_LATITUDE, latitude);
		
		id = (int)db.insertOrThrow(TABLE_NAME, null, args);	
		
		return id;
	}
		
	/**
	 * Updates the item in the database specified the by the whereClause
	 * @param db The SQLiteDatabase to be used
	 * @param values the new values to be inserted
	 * @param whereClause specifies which rows will be affected in the database
	 * @return number of rows affected
	 */
	public int update(SQLiteDatabase db,  ContentValues values, String whereClause) {
		return db.update(TABLE_NAME, values, whereClause, null);
	}
	
	/**
	 * Deletes the item in the database specified the by the whereClause
	 * @param db The SQLiteDatabase to be used
	 * @param whereClause the optional WHERE clause to apply when deleting. Passing null will delete all rows.
	 * @return the number of rows affected 
	 */
	public int delete(SQLiteDatabase db, String whereClause) {
		return db.delete(TABLE_NAME, whereClause, null);
	}
	
	/**
	 * Passes a query to the favorites table
	 * @param db The SQLiteDatabase to be used
	 * @param selection A filter declaring which row to return
	 * @param orderBy the order of the rows to be returned
	 * @return the Cursor that contains all the selected items
	 */
	public Cursor fetch(SQLiteDatabase db, String selection, String orderBy) {
		return db.query(TABLE_NAME, COLUMNS,
				((selection==null)||(selection.equals("")))?null:selection,
				null, null, null,
				((orderBy==null)||(orderBy.equals("")))?null:orderBy);
	}
	
}
