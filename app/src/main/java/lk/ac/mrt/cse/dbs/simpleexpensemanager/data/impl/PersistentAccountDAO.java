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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

import static lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.SQLiteHelper.ACCOUNT_NO;
import static lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.SQLiteHelper.BALANCE;
import static lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.SQLiteHelper.BANK;
import static lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.SQLiteHelper.NAME;
import static lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.SQLiteHelper.TABLE_ACCOUNT;

/**
 * This is an In-Memory implementation of the AccountDAO interface. This is not a persistent storage. A HashMap is
 * used to store the account details temporarily in the memory.
 */
public class PersistentAccountDAO implements AccountDAO {
    private final Map<String, Account> accounts;

    private final SQLiteHelper helper;
    private SQLiteDatabase database;

    public PersistentAccountDAO(Context context) {

        this.accounts = new HashMap<>();

        helper = new SQLiteHelper(context);
        database = helper.getReadableDatabase();
        String[] columns = {
                ACCOUNT_NO,
                BANK,
                NAME,
                BALANCE
        };
        Cursor cursor = database.query(
                TABLE_ACCOUNT,
                columns,
                null,
                null,
                null,
                null,
                null
        );

        while(cursor.moveToNext()) {
            Account account = new Account(
                    cursor.getString(cursor.getColumnIndex(ACCOUNT_NO)),
                    cursor.getString(cursor.getColumnIndex(BANK)),
                    cursor.getString(cursor.getColumnIndex(NAME)),
                    cursor.getDouble(cursor.getColumnIndex(BALANCE))
            );
            System.out.println(account.getAccountNo());
            this.accounts.put(account.getAccountNo(), account);
        }
        cursor.close();
    }

    @Override
    public List<String> getAccountNumbersList() {
        return new ArrayList<>(accounts.keySet());
    }

    @Override
    public List<Account> getAccountsList() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        if (accounts.containsKey(accountNo)) {
            return accounts.get(accountNo);
        }
        String msg = "Account " + accountNo + " is invalid.";
        throw new InvalidAccountException(msg);
    }

    @Override
    public void addAccount(Account account) {
        this.accounts.put(account.getAccountNo(), account);

        //insert into the database
        database = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ACCOUNT_NO, account.getAccountNo());
        values.put(NAME, account.getAccountHolderName());
        values.put(BANK, account.getBankName());
        values.put(BALANCE,account.getBalance());

        database.insert(TABLE_ACCOUNT, null, values);
        database.close();
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        if (!accounts.containsKey(accountNo)) {
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }
        accounts.remove(accountNo);

        //update database
        database = helper.getWritableDatabase();
        String[] accountsToDelete = {accountNo};
        database.delete(TABLE_ACCOUNT, ACCOUNT_NO + " = ?", accountsToDelete);
        database.close();
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        if (!accounts.containsKey(accountNo)) {
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }
        Account account = accounts.get(accountNo);


        double balance = account.getBalance();
        // specific implementation based on the transaction type
        switch (expenseType) {
            case EXPENSE:
                balance = balance - amount;
                break;
            case INCOME:
                balance = balance + amount;
                break;
        }

        account.setBalance(balance);
        accounts.put(accountNo, account);

        // to update database
        ContentValues values = new ContentValues();
        values.put(BALANCE, account.getBalance() - amount);

        database = helper.getWritableDatabase();
        String[] projection = {
                BALANCE
        };

        String[] accountsToUpdate = { accountNo };
        database.update(TABLE_ACCOUNT, values, ACCOUNT_NO + " = ?", accountsToUpdate);
        database.close();
    }
}
