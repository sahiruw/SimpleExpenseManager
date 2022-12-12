package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "200726J.sqlite";
    private static final int VERSION = 1;

    //TABLE NAMES
    public static final String TABLE_ACCOUNT = "account";
    public static final String TABLE_LOGS = "log";

    //COLUMN NAMES
    public static final String ACCOUNT_NO = "accountNo";

    public static final String NAME = "name";
    public static final String BANK = "bank";
    public static final String BALANCE = "balance";

    public static final String ID = "id";
    public static final String DATE = "date";
    public static final String TYPE = "type";
    public static final String AMOUNT = "amount";


    public SQLiteHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_ACCOUNT + "(" +
                ACCOUNT_NO + " TEXT PRIMARY KEY, " + NAME + " TEXT NOT NULL, " +
                BANK + " TEXT NOT NULL, " + BALANCE + " REAL NOT NULL)");


        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_LOGS + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ACCOUNT_NO + " TEXT," +
                DATE + " TEXT NOT NULL, " +
                TYPE + " TEXT NOT NULL, " +
                AMOUNT + " REAL NOT NULL, " +
                "FOREIGN KEY (" + ACCOUNT_NO + ") REFERENCES " + TABLE_ACCOUNT + "(" + ACCOUNT_NO + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on version upgrade create new tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
        onCreate(db);
    }

}