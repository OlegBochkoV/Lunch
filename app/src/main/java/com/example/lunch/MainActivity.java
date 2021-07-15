package com.example.lunch;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_yes, btn_no, btn_reset;
    EditText editText_number_portion;
    TextView textView;
    TableLayout tableLayout;
    DBHelper dbHelper;
    private DatabaseReference myDataBase;
    ArrayList<String> arrayList_Name = null;
    ArrayList<String> arrayList_Portion = null;
    ArrayList<String> arrayList_AnswerToday = null;

    public static final String TAG = "ExampleJobService";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scheduleJob();
//        setNotifyAlarm();

        init_view();
        update_list();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("TABLE1", null, null, null, null, null, null);
        if (!c.moveToFirst()) { //проверим зарегистрирован пользователь или нет
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        }
        c.close();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    block_button();
                }
            }
        });
        thread.start();
        Thread block_reset = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean state = false;
                while (!state) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor cursor = db.query("TABLE1", null, null, null, null, null, null);
                    if (cursor.getCount() != 0) {
                        state = true;
                    }
                    if (state) {
                        cursor.moveToFirst();
                        int nameColIndex = cursor.getColumnIndex("name");
                        if (cursor.getString(nameColIndex).equals("admin")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn_yes.setVisibility(View.INVISIBLE);
                                    btn_no.setVisibility(View.INVISIBLE);
                                    btn_reset.setVisibility(View.VISIBLE);
                                    editText_number_portion.setVisibility(View.INVISIBLE);
                                    textView.setVisibility(View.INVISIBLE);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btn_yes.setVisibility(View.VISIBLE);
                                    btn_no.setVisibility(View.VISIBLE);
                                    btn_reset.setVisibility(View.INVISIBLE);
                                    editText_number_portion.setVisibility(View.VISIBLE);
                                    textView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                    cursor.close();
                }
            }
        });
        block_reset.start();
    }

    void init_view() {
        arrayList_Name = new ArrayList<>();
        arrayList_Portion = new ArrayList<>();
        arrayList_AnswerToday = new ArrayList<>();

        btn_yes = (Button) findViewById(R.id.btn_yes);
        btn_no = (Button) findViewById(R.id.btn_no);
        btn_reset = (Button) findViewById(R.id.btn_reset);

        editText_number_portion = (EditText) findViewById(R.id.editText_number_portion);

        textView = (TextView) findViewById(R.id.textView2);

        tableLayout = (TableLayout) findViewById(R.id.table);

        btn_yes.setOnClickListener(this);
        btn_no.setOnClickListener(this);
        btn_reset.setOnClickListener(this);

        dbHelper = new DBHelper(this);
    }

    void update_list() {
        myDataBase = FirebaseDatabase.getInstance().getReference();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;

                arrayList_Portion.clear();
                arrayList_Name.clear();
                arrayList_AnswerToday.clear();

                tableLayout.removeAllViews();

                TableRow tableRow1 = new TableRow(MainActivity.this);
                tableRow1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                TextView tv6 = new TextView(MainActivity.this);
                TextView tv7 = new TextView(MainActivity.this);
                TextView tv8 = new TextView(MainActivity.this);
                TextView tv9 = new TextView(MainActivity.this);

                tv6.setTextSize(18);
                tv7.setTextSize(18);
                tv8.setTextSize(18);
                tv9.setTextSize(18);

                tv6.setTextColor(Color.WHITE);
                tv7.setTextColor(Color.WHITE);
                tv8.setTextColor(Color.WHITE);
                tv9.setTextColor(Color.WHITE);

                tv6.setTypeface(Typeface.DEFAULT_BOLD);
                tv7.setTypeface(Typeface.DEFAULT_BOLD);
                tv8.setTypeface(Typeface.DEFAULT_BOLD);
                tv9.setTypeface(Typeface.DEFAULT_BOLD);

                tv6.setBackgroundResource(R.drawable.myshape);
                tv7.setBackgroundResource(R.drawable.myshape);
                tv8.setBackgroundResource(R.drawable.myshape);
                tv9.setBackgroundResource(R.drawable.myshape);

                tv6.setText(" ФИО ");
                tv7.setText(" Выбор сделан ");
                tv8.setText(" Количество порций ");
                tv9.setText(" Результат ");

                tableRow1.addView(tv7);
                tableRow1.addView(tv6);
                tableRow1.addView(tv8);
                tableRow1.addView(tv9);

                tableLayout.addView(tableRow1);
                int numberPersonWasEat = 0;
                int numberPortion = 0;


                int numberPersonInDataBase = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Person person = ds.getValue(Person.class);
                    if (!person.name.equals("admin")) numberPersonInDataBase += 1;
                }

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Person person = ds.getValue(Person.class);
                    if (!person.name.equals("admin")) {
                        TableRow tableRow = new TableRow(MainActivity.this);
                        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                        arrayList_AnswerToday.add(person.answer_today);
                        arrayList_Name.add(person.name);
                        arrayList_Portion.add(person.number_portion);

                        if (person.choice.equals("yes")) {
                            numberPersonWasEat += 1;
                        }
                        numberPortion += Integer.parseInt(person.number_portion);

                        TextView tv2 = new TextView(MainActivity.this);
                        TextView tv3 = new TextView(MainActivity.this);
                        TextView tv4 = new TextView(MainActivity.this);

                        TextView resultTextView = new TextView(MainActivity.this);
                        resultTextView.setTextColor(Color.WHITE);
                        resultTextView.setBackgroundResource(R.drawable.myshape);

                        CheckBox checkBox = new CheckBox(MainActivity.this);
                        checkBox.setBackgroundResource(R.drawable.myshape);
                        checkBox.setClickable(false);

                        if (arrayList_AnswerToday.get(i).equals("1")) {
                            checkBox.setChecked(true);
                        } else {
                            checkBox.setChecked(false);
                        }

                        tv2.setTextColor(Color.WHITE);
                        tv3.setTextColor(Color.WHITE);
                        tv4.setTextColor(Color.WHITE);

                        tv2.setBackgroundResource(R.drawable.myshape);
                        tv3.setBackgroundResource(R.drawable.myshape);
                        tv4.setBackgroundResource(R.drawable.myshape);

                        tv2.setText(" " + arrayList_Name.get(i) + " ");
                        tv3.setText(" " + arrayList_AnswerToday.get(i) + " ");
                        tv4.setText(" " + arrayList_Portion.get(i) + " ");
                        resultTextView.setText("Итого: " + numberPersonWasEat + " сотруд., " + numberPortion + " порц.");

                        i++;

                        tableRow.addView(checkBox);

                        tableRow.addView(tv2);
                        tableRow.addView(tv4);
                        if (i == numberPersonInDataBase)
                            tableRow.addView(resultTextView);

                        tableLayout.addView(tableRow);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        myDataBase.addValueEventListener(valueEventListener);
    }

    void block_button() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query("TABLE1", null, null, null, null, null, null);
        final boolean[] person = {false};
        if (cursor.moveToFirst()) {
            int nameColIndex = cursor.getColumnIndex("name");
            if (cursor.getString(nameColIndex).equals("admin")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        person[0] = true;
                        textView.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
        cursor.close();

        Calendar calendar = Calendar.getInstance();
        String day_of_week = Calendar.MONDAY + ", " + Calendar.TUESDAY + ", " + Calendar.WEDNESDAY + ", " + Calendar.THURSDAY + ", " + Calendar.FRIDAY;
        if (day_of_week.contains(calendar.get(Calendar.DAY_OF_WEEK) + "")) {
            if (calendar.get(Calendar.HOUR_OF_DAY) >= 11) {
                if (calendar.get(Calendar.HOUR_OF_DAY) == 11 & calendar.get(Calendar.MINUTE) > 39) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_yes.setVisibility(View.INVISIBLE);
                            btn_no.setVisibility(View.INVISIBLE);
                            editText_number_portion.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                        }
                    });
                } else if (calendar.get(Calendar.HOUR_OF_DAY) > 11) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_yes.setVisibility(View.INVISIBLE);
                            btn_no.setVisibility(View.INVISIBLE);
                            editText_number_portion.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            } else if (calendar.get(Calendar.HOUR_OF_DAY) == 0 & calendar.get(Calendar.MINUTE) == 0) {
                choice_person("-", "0", "0");
            } else if (calendar.get(Calendar.HOUR_OF_DAY) < 11) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (person[0]) {
                            btn_yes.setVisibility(View.INVISIBLE);
                            btn_no.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                            editText_number_portion.setVisibility(View.INVISIBLE);
                        } else {
                            btn_yes.setVisibility(View.VISIBLE);
                            btn_no.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.VISIBLE);
                            editText_number_portion.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        } else {
            choice_person("-", "0", "0");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn_yes.setVisibility(View.INVISIBLE);
                    btn_no.setVisibility(View.INVISIBLE);
                    textView.setVisibility(View.INVISIBLE);
                    editText_number_portion.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                choice_person("yes", editText_number_portion.getText().toString(), "1");
                break;
            case R.id.btn_no:
                choice_person("no", "0", "1");
                break;
            case R.id.btn_reset:
                reset_database();
                break;
        }
    }

    void reset_database() {
        for (int i = 0; i < arrayList_Name.size(); i++) {
            myDataBase = FirebaseDatabase.getInstance().getReference();
            myDataBase.child(arrayList_Name.get(i)).child("choice").setValue("-");
            myDataBase.child(arrayList_Name.get(i)).child("answer_today").setValue("0");
            myDataBase.child(arrayList_Name.get(i)).child("name").setValue(arrayList_Name.get(i));
            myDataBase.child(arrayList_Name.get(i)).child("number_portion").setValue("0");
        }
    }

    void choice_person(String str, String number_portion, String answer_today) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query("TABLE1", null, null, null, null, null, null);

        int nameColIndex = cursor.getColumnIndex("name");

        cursor.moveToFirst();

        Person person = new Person(cursor.getString(nameColIndex), answer_today, str, number_portion);

        ContentValues cv = new ContentValues();
        cv.put("name", cursor.getString(nameColIndex));
        cv.put("answer_today", answer_today);
        cv.put("choice", str);
        cv.put("number_portion", number_portion);
        database.insert("TABLE1", null, cv);

        myDataBase = FirebaseDatabase.getInstance().getReference(cursor.getString(nameColIndex));
        myDataBase.setValue(person);

        cursor.close();
    }

    static class DBHelper extends SQLiteOpenHelper {
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

    public void scheduleJob() {
        ComponentName componentName = new ComponentName(this, ExampleJobService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setPersisted(true)
                .setPeriodic(15*60*1000)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(info);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled create");
        } else {
            Log.d(TAG, "Job scheduled failed");
        }
    }

    public void cancelJob() {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123);
        Log.d(TAG,"Job scheduled cancel");
    }

    public void setNotifyAlarm() {
        Intent intent = new Intent(this, NotifyServiceExample.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, pendingIntent);
        Log.d(TAG,"this");

    }
}