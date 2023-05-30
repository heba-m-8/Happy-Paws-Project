package com.example.happypaws

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.sql.SQLException

class AppointmentProvider : ContentProvider() {
    companion object {
        const val provider_name = "com.example.MyApplication.happypaws.AppointmentProvider"
        private val URL = "content://$AppointmentProvider/students"
        val CONTENT_URI = Uri.parse(URL)

        const val ID = "Pet ID"
        const val NAME = "Name"
        const val PHONE = "Phone number"
        const val PET = "Pet"
        const val APT_TYPE = "Appointment Type"
        const val APT_DATE = "Appointment Date"
        const val APT_TIME = "Appointment Time"

        const val APPOINTMENTS = 1
        const val PET_ID = 2
        val uriMatcher: UriMatcher? = null
        const val DATABASE_NAME = "Happy Paws Clinic"
        const val APTS_TABLE_NAME = "Appointments"
        const val DATABASE_VERSION = 1

        const val CREATE_DB_TABLE = "CREATE TABLE " + APTS_TABLE_NAME + " (" +
                "_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "NAME TEXT NOT NULL, " +
                "PHONE INTEGER NOT NULL, " +
                "PET TEXT NOT NULL, " +
                "APT_TYPE TEXT NOT NULL, " +
                "APT_DATE TEXT NOT NULL, " +
                "APT_TIME INTEGER NOT NULL" +
                ");"
    }


    private var sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
        sUriMatcher.addURI(AppointmentProvider.toString(), "Appointments", APPOINTMENTS)
        sUriMatcher.addURI(AppointmentProvider.toString(), "Pet ID", PET_ID)
    }

    private var db: SQLiteDatabase? = null

    private class DatabaseHelper constructor(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_DB_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $APTS_TABLE_NAME")
            onCreate(db)
        }
    }

    override fun onCreate(): Boolean {
        val context = context
        val dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase
        return db != null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val rowID = db!!.insert(APTS_TABLE_NAME, "", values)
        if (rowID > 0) {
            val uriVal = ContentUris.withAppendedId(CONTENT_URI, rowID)
            context!!.contentResolver.notifyChange(uriVal, null)
            return uriVal
        }
        throw SQLException("Failed to add a record into $uri")
    }

    override fun query(
        uri: Uri, projection: Array<String>?,
        selection: String?, selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        var sortOrder = sortOrder
        val qb = SQLiteQueryBuilder()
        qb.tables = APTS_TABLE_NAME
        when (uriMatcher!!.match(uri)) {
            PET_ID -> qb.appendWhere(ID + "=" + uri.pathSegments[1])
            else -> {
                null
            }
        }
        if (sortOrder == null || sortOrder === "") {
            sortOrder = NAME
        }
        val c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        c.setNotificationUri(context!!.contentResolver, uri)
        return c
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var count = 0
        when (uriMatcher!!.match(uri)) {
            APPOINTMENTS -> count = db!!.delete(
                APTS_TABLE_NAME, selection,
                selectionArgs
            )
            PET_ID -> {
                val id = uri.pathSegments[1]
                count = db!!.delete(
                    APTS_TABLE_NAME,
                    ID + " = " + id +
                            if (!TextUtils.isEmpty(selection)) " AND ($selection)" else "",
                    selectionArgs
                )
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        var count = 0
        count = when (uriMatcher!!.match(uri)) {
            APPOINTMENTS -> db!!.update(
                APTS_TABLE_NAME, values, selection,
                selectionArgs
            )
            PET_ID -> db!!.update(
                APTS_TABLE_NAME,
                values,
                ID + " = " + uri.pathSegments[1] + (if (!TextUtils.isEmpty(selection)) " AND ($selection)" else ""),
                selectionArgs)
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher!!.match(uri)) {
            APPOINTMENTS -> "vnd.android.cursor.dir/vnd.example.appointments"
            PET_ID -> "vnd.android.cursor.item/vnd.example.appointments"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

}


class BookAptActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_apt)

        var flag = "Check-up"
        val spinnerVal : Spinner = findViewById(R.id.apt_type_spinner)
        val options = arrayOf("Check-up","Sick visit", "Booster visit", "Grooming appointment", "Consultation", "Other")
        spinnerVal.adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, options)

        spinnerVal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                flag = options[p2]
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

    }
    fun onClickAddAppointment(view: View?) {
        val values = ContentValues()
        values.put(
            AppointmentProvider.NAME,
            (findViewById<View>(R.id.name_editxt) as EditText).text.toString()
        )
        values.put(
            AppointmentProvider.PHONE,
            (findViewById<View>(R.id.phone_editxt) as EditText).text.toString()
        )
        values.put(
            AppointmentProvider.PET,
            (findViewById<View>(R.id.pet_editxt) as EditText).text.toString()
        )
        values.put(
            AppointmentProvider.APT_TYPE,
            (findViewById<View>(R.id.apt_type_spinner) as Spinner).selectedItem.toString()
        )
        values.put(
            AppointmentProvider.APT_DATE,
            (findViewById<View>(R.id.apt_calendar) as CalendarView).date.toString()
        )
        values.put(
            AppointmentProvider.APT_TIME,
            (findViewById<View>(R.id.time_editxt) as EditText).text.toString()
        )

        val uri = contentResolver.insert(
            AppointmentProvider.CONTENT_URI, values
        )
        Toast.makeText(baseContext, uri.toString(), Toast.LENGTH_LONG).show()

        stopService()
    }

    fun onClickDeleteAppointment(view: View?) {
        val phoneNumber = (findViewById<View>(R.id.phone_editxt) as EditText).text.toString()

        val selection = "${AppointmentProvider.PHONE}=?"
        val selectionArgs = arrayOf(phoneNumber)

        val uri = AppointmentProvider.CONTENT_URI
        val rowsDeleted = contentResolver.delete(uri, selection, selectionArgs)

        if (rowsDeleted > 0) {
            Toast.makeText(baseContext, "Your appointment was deleted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(baseContext, "Failed to delete appointment, please try again.", Toast.LENGTH_SHORT).show()
        }

        stopService()
    }

    fun onClickUpdateAppointment(view: View?) {
        val phoneNumber = (findViewById<View>(R.id.phone_editxt) as EditText).text.toString()

        val selection = "${AppointmentProvider.PHONE}=?"
        val selectionArgs = arrayOf(phoneNumber)

        val values = ContentValues()
        values.put(AppointmentProvider.NAME, (findViewById<View>(R.id.name_editxt) as EditText).text.toString())
        values.put(AppointmentProvider.PET, (findViewById<View>(R.id.pet_editxt) as EditText).text.toString())
        values.put(AppointmentProvider.APT_TYPE, (findViewById<View>(R.id.apt_type_spinner) as Spinner).selectedItem.toString())
        values.put(AppointmentProvider.APT_DATE, (findViewById<View>(R.id.apt_calendar) as CalendarView).date.toString())
        values.put(AppointmentProvider.APT_TIME, (findViewById<View>(R.id.time_editxt) as EditText).text.toString())

        val uri = AppointmentProvider.CONTENT_URI
        val rowsUpdated = contentResolver.update(uri, values, selection, selectionArgs)

        if (rowsUpdated > 0) {
            Toast.makeText(baseContext, "Your appointment details were updated successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(baseContext, "Failed to update appointment, please try again.", Toast.LENGTH_SHORT).show()
        }

        stopService()
    }

    private fun stopService() {
        val serviceIntent = Intent(this, NewService::class.java)
        stopService(serviceIntent)
    }

}
