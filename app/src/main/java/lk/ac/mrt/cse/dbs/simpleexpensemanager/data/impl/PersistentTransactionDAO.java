/*
 * Copyright 2015 Department of Computer Science and Engineering, University of Moratuwa.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *                  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;


import static lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.SQLiteHelper.ACCOUNT_NO;
import static lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.SQLiteHelper.AMOUNT;
import static lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.SQLiteHelper.DATE;
import static lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.SQLiteHelper.TYPE;
import static lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.SQLiteHelper.TABLE_LOGS;
/**
 * This is an In-Memory implementation of TransactionDAO interface. This is not a persistent storage. All the
 * transaction logs are stored in a LinkedList in memory.
 */
public class PersistentTransactionDAO implements TransactionDAO {
    private final List<Transaction> transactions;

    private final SQLiteHelper helper;
    private SQLiteDatabase database;

    public PersistentTransactionDAO(Context context) throws ParseException {
        transactions = new LinkedList<>();
        helper = new SQLiteHelper(context);

        database = helper.getReadableDatabase();

        String[] projection = {
                DATE,
                ACCOUNT_NO,
                TYPE,
                AMOUNT
        };

        Cursor cursor = database.query(
                TABLE_LOGS,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        while(cursor.moveToNext()) {

            Date date = new SimpleDateFormat("dd-MM-yyyy").parse(cursor.getString(cursor.getColumnIndex(DATE)));
            ExpenseType expenseType = ExpenseType.valueOf(cursor.getString(cursor.getColumnIndex(TYPE)));

            Transaction transaction = new Transaction(
                    date,
                    cursor.getString(cursor.getColumnIndex(ACCOUNT_NO)),
                    expenseType,
                    cursor.getDouble(cursor.getColumnIndex(AMOUNT)));

            transactions.add(transaction);
        }
        cursor.close();
    }

    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        Transaction transaction = new Transaction(date, accountNo, expenseType, amount);
        transactions.add(transaction);

        //insert into the database
        database = helper.getWritableDatabase();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");

        ContentValues values = new ContentValues();
        values.put(DATE, df.format(date));
        values.put(ACCOUNT_NO, accountNo);
        values.put(TYPE, String.valueOf(expenseType));
        values.put(AMOUNT, amount);

        database.insert(TABLE_LOGS, null, values);
        database.close();
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        return transactions;
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        int size = transactions.size();
        if (size <= limit) {
            return transactions;
        }
        // return the last <code>limit</code> number of transaction logs
        return transactions.subList(size - limit, size);
    }

}
