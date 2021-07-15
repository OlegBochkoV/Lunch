package com.example.lunch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_reg;
    EditText editTextname;
    DBHelper dbHelper;

    private DatabaseReference myDataBase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        btn_reg = (Button) findViewById(R.id.btn_registration);
        editTextname = (EditText) findViewById(R.id.editText);

        btn_reg.setOnClickListener(this);

        dbHelper = new DBHelper(this);
    }

    @Override
    public void onClick(View v) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (v.getId()) {
            case R.id.btn_registration:
                if (!TextUtils.isEmpty(editTextname.getText().toString())) {
                    ContentValues cv = new ContentValues();
                    cv.put("name", editTextname.getText().toString());
                    cv.put("answer_today", "0");
                    cv.put("choice", "-");
                    cv.put("number_portion", "0");
                    db.insert("TABLE1", null, cv);

                    Cursor cursor = db.query("TABLE1", null, null, null, null, null, null);

                    int nameColIndex = cursor.getColumnIndex("name");
                    int answer_today = cursor.getColumnIndex("answer_today");
                    int choiceColIndex = cursor.getColumnIndex("choice");
                    int number_portion = cursor.getColumnIndex("number_portion");

                    cursor.moveToFirst();

                    Person person = new Person(cursor.getString(nameColIndex), cursor.getString(answer_today), cursor.getString(choiceColIndex), cursor.getString(number_portion));

                    myDataBase = FirebaseDatabase.getInstance().getReference(cursor.getString(nameColIndex));
                    myDataBase.setValue(person);
                    cursor.close();
                    db.close();
                    finish();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Одно из полей пустое", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onUserLeaveHint();
    }

    class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, "TABLE", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table TABLE1 (" + "name text," + "answer_today text," + "choice text," + "number_portion text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
