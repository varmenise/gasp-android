/*
 * Copyright (c) 2013 Mark Prichard, CloudBees
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.demo.gasp.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cloudbees.demo.gasp.model.User;

/**
 * Adapter class mapping the Gasp database to com.cloudbees.demo.gasp.model.User
 * Implements insert, cursor and list methods: main interface to User data
 */
public class UserDataAdapter extends GaspDataAdapter<User> {
    private static final String TAG = UserDataAdapter.class.getName();

    private final String[] allColumns = {
            GaspSQLiteHelper.USERS_COLUMN_ID,
            GaspSQLiteHelper.USERS_COLUMN_NAME
    };

    private static final String idColumnName = GaspSQLiteHelper.USERS_COLUMN_ID;
    private static final String tableName = GaspSQLiteHelper.USERS_TABLE;

    public UserDataAdapter(Context context) {
        super(context);
    }

    @Override
    protected String getIdColumnName() {
        return idColumnName;
    }

    @Override
    protected String getTableName() {
        return tableName;
    }

    @Override
    public String[] getAllColumns() {
        return allColumns;
    }

    @Override
    protected User fromCursor(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(0));
        user.setName(cursor.getString(1));
        return user;
    }

    @Override
    protected void putValues(ContentValues values, User user) {
        values.put(GaspSQLiteHelper.USERS_COLUMN_ID, user.getId());
        values.put(GaspSQLiteHelper.USERS_COLUMN_NAME, user.getName());
    }
}
