package com.esmanureral.login

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

//DATABASETABLE SINIFI: Kullanıcı ve etkinlik tablosu oluşturma,ekleme,çıkarma
class DatabaseTable(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    //Sütunlar
    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 118 //Düzenleme yaptığında artır.
        private const val TABLE_NAME = "data"//Kullanıcı verilerini tutan ana tablo
        private const val EVENT_TABLE_NAME = "events"//Etkinlik bilgilerini tutan tablo
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_EVENT_NAME = "event_name"
        private const val COLUMN_EVENT_DATE = "event_date"
        private const val COLUMN_EVENT_IMAGE = "event_image"
        private const val COLUMN_EVENT_USERID = "user_id" //profily için
        private const val COLUMN_PROFILE_IMAGE = "profile_image" //profily için
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Kullanıcı tablosu oluşturma sorgusu
        val createUserTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_NAME TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_PROFILE_IMAGE TEXT
            )
        """.trimIndent()
        db?.execSQL(createUserTableQuery)

        // Etkinlik tablosu oluşturma sorgusu
        val createEventTableQuery = """
            CREATE TABLE $EVENT_TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EVENT_NAME TEXT,
                $COLUMN_EVENT_DATE TEXT,
                $COLUMN_EVENT_IMAGE TEXT,
                $COLUMN_EVENT_USERID INT,
                FOREIGN KEY ($COLUMN_EVENT_USERID) REFERENCES $TABLE_NAME($COLUMN_ID)
            )
        """.trimIndent()
        db?.execSQL(createEventTableQuery)


        // Yorum tablosunu oluşturma sorgusu
        val createCommentsTableQuery = """
        CREATE TABLE event_comments (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER,
            event_id INTEGER,
            comment TEXT,
            created_at TEXT,
            FOREIGN KEY (user_id) REFERENCES $TABLE_NAME($COLUMN_ID),
            FOREIGN KEY (event_id) REFERENCES $EVENT_TABLE_NAME($COLUMN_ID)
        )
    """.trimIndent()
        db?.execSQL(createCommentsTableQuery)
    }

    //kullancıyı etkinliğe eklemek
    fun addUserToEvent(userId: Int, eventId: Int): Long {
        val values = ContentValues().apply {
            put("user_id", userId)
            put("event_id", eventId)
        }
        val db = writableDatabase
        return db.insert("participation", null, values)
    }

    //kullanıcı belli bi etkinliğe katılmış/katılmamış kontrolü
    fun isUserParticipating(userId: Int, eventId: Int): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM participation WHERE user_id = ? AND event_id = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString(), eventId.toString()))
        val isParticipating = cursor.moveToFirst()
        cursor.close()
        return isParticipating
    }

    // Veritabanını güncellediğimizde çalışacak kod, eskileri siler yeniden oluşturur.
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropUserTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        val dropEventTableQuery = "DROP TABLE IF EXISTS $EVENT_TABLE_NAME"
        db?.execSQL("DROP TABLE IF EXISTS event_comments")
        db?.execSQL(dropUserTableQuery)
        db?.execSQL(dropEventTableQuery)
        onCreate(db)
    }

    // Kullanıcı ekleme
    fun insertUser(username: String, password: String, name: String, email: String): Long {
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_NAME, name)
            put(COLUMN_EMAIL, email)
        }
        val db = writableDatabase
        return db.insert(TABLE_NAME, null, values)
    }

    // Kullanıcı adı ve şifresini okuma
    fun readUser(username: String, password: String): Int? {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME=? AND $COLUMN_PASSWORD=?"
        val selectionArgs = arrayOf(username, password)
        val cursor =
            db.query(TABLE_NAME, arrayOf(COLUMN_ID), selection, selectionArgs, null, null, null)

        val userId = if (cursor.moveToFirst()) {
            //kullanıcı varsa id sini al
            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
        } else {
            null
        }
        cursor.close()
        return userId
    }

    // Etkinlik ekleme
    fun insertEvent(eventName: String, eventDate: String, eventImage: String, userId: Int): Long {
        val values = ContentValues().apply {
            put(COLUMN_EVENT_NAME, eventName)
            put(COLUMN_EVENT_DATE, eventDate)
            put(COLUMN_EVENT_IMAGE, eventImage)
            put(COLUMN_EVENT_USERID, userId)
        }
        val db = writableDatabase
        return db.insert(EVENT_TABLE_NAME, null, values)
    }

    // Etkinlikleri okuma
    fun getAllEvents(): List<Triple<String, String, String>> {
        // Etkinlik bilgilerini depolamak için bir mutable listesi oluşturuluyor.
        val events = mutableListOf<Triple<String, String, String>>()//etkinlik listesi
        val db = readableDatabase
        val cursor = db.query(EVENT_TABLE_NAME, null, null, null, null, null, null)

        val eventNameIndex = cursor.getColumnIndex(COLUMN_EVENT_NAME)
        val eventDateIndex = cursor.getColumnIndex(COLUMN_EVENT_DATE)
        val eventImageIndex = cursor.getColumnIndex(COLUMN_EVENT_IMAGE)

        if (cursor.moveToFirst()) {
            do {
                val eventName = cursor.getString(eventNameIndex)
                val eventDate = cursor.getString(eventDateIndex)
                val eventImage = cursor.getString(eventImageIndex)
                events.add(Triple(eventName, eventDate, eventImage ?: ""))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return events
    }

    // Kullanıcı bilgilerini al
    fun getUserInfo(userId: Int): Pair<String?, String?> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_NAME, COLUMN_PROFILE_IMAGE),
            "$COLUMN_ID=?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )
        var userName: String? = null
        var profileImage: String? = null
        if (cursor != null && cursor.moveToFirst()) {
            // Kullanıcı adı ve profil resmini veritabanından al
            userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            profileImage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE))
        } else {
            Log.e("ProfileActivity", "Kullanıcı ID $userId bulunamadı")
        }

        cursor.close()
        return Pair(userName, profileImage)
    }
    // Kullanıcıya ait etkinlikleri al
    fun getEventsByUserId(userId: Int): List<Triple<String, String, String>> {
        val events = mutableListOf<Triple<String, String, String>>()
        val db = readableDatabase
        val selection = "$COLUMN_EVENT_USERID=?"
        val selectionArgs = arrayOf(userId.toString())
        val cursor = db.query(
            EVENT_TABLE_NAME,
            arrayOf(COLUMN_EVENT_NAME, COLUMN_EVENT_DATE, COLUMN_EVENT_IMAGE),
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val eventNameIndex = cursor.getColumnIndex(COLUMN_EVENT_NAME)
            val eventDateIndex = cursor.getColumnIndex(COLUMN_EVENT_DATE)
            val eventImageIndex = cursor.getColumnIndex(COLUMN_EVENT_IMAGE)

            do {
                val eventName = cursor.getString(eventNameIndex)
                val eventDate = cursor.getString(eventDateIndex)
                val eventImage = cursor.getString(eventImageIndex)
                events.add(Triple(eventName, eventDate, eventImage))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return events
    }

//kullanıcıya göre etkinlik bilgileri
    fun getEventsByUser(userId: Int): List<Triple<String, String, String>> {
        val events = mutableListOf<Triple<String, String, String>>()
        val db = readableDatabase
        val cursor = db.query(
            "events",
            arrayOf("event_name", "event_date", "event_image"),
            "user_id = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("event_name"))
                val date = cursor.getString(cursor.getColumnIndexOrThrow("event_date"))
                val image = cursor.getString(cursor.getColumnIndexOrThrow("event_image"))
                events.add(Triple(name, date, image ?: ""))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return events
    }

// Kullanıcının katıldığı etkinlikleri depolamak
    fun getEventsByParticipation(userId: Int): List<Triple<String, String, String>> {
        val events = mutableListOf<Triple<String, String, String>>()
        val db = readableDatabase
        val query = """
        SELECT e.$COLUMN_EVENT_NAME, e.$COLUMN_EVENT_DATE, e.$COLUMN_EVENT_IMAGE 
        FROM $EVENT_TABLE_NAME e
        INNER JOIN participation p ON e.$COLUMN_ID = p.event_id
        WHERE p.user_id = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val eventName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME))
                val eventDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DATE))
                val eventImage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_IMAGE)) ?: ""
                events.add(Triple(eventName, eventDate, eventImage))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return events
    }


    // Kullanıcının etkinlik katılımını sil
    fun removeUserFromEvent(userId: Int, eventId: Int): Int {
        val db = writableDatabase
        val whereClause = "user_id = ? AND event_id = ?"
        val whereArgs = arrayOf(userId.toString(), eventId.toString())
        return db.delete("participation", whereClause, whereArgs)
    }
// Belirli bir etkinlik için katılımcı sayısını getir
    fun getParticipantCountForEvent(eventId: Int): Int {
        val db = readableDatabase
        val query = """
        SELECT COUNT(p.user_id) as participant_count
        FROM participation p
        WHERE p.event_id = ?
    """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(eventId.toString()))
        var participantCount = 0

        if (cursor.moveToFirst()) {
            participantCount = cursor.getInt(cursor.getColumnIndexOrThrow("participant_count"))
        }

        cursor.close()
        return participantCount
    }
    fun addComment(userId: Int, eventId: Int, comment: String): Long {
        val values = ContentValues().apply {
            put("user_id", userId)
            put("event_id", eventId)
            put("comment", comment)
            put("created_at", System.currentTimeMillis().toString())
        }
        val db = writableDatabase
        return db.insert("event_comments", null, values)
    }


    fun getCommentsForEvent(eventId: Int): List<Comment> {
        val comments = mutableListOf<Comment>()
        val db = readableDatabase

        val cursor = db.rawQuery("""
        SELECT u.$COLUMN_NAME, c.comment 
        FROM event_comments c
        JOIN $TABLE_NAME u ON c.user_id = u.$COLUMN_ID
        WHERE c.event_id = ?
    """.trimIndent(), arrayOf(eventId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"))
                comments.add(Comment(username, comment))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return comments
    }
    fun getCommentsByEvent(eventId: Int): List<Pair<String, String>> {
        val comments = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val query = """
        SELECT c.comment, u.$COLUMN_NAME
        FROM event_comments c
        JOIN $TABLE_NAME u ON c.user_id = u.$COLUMN_ID
        WHERE c.event_id = ?
        ORDER BY c.created_at DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(eventId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"))
                val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                comments.add(Pair(username, comment))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return comments
    }

    // Kullanıcı ID'sine göre kullanıcı adını getir
    fun getUsernameById(userId: Int): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_USERNAME), // Kullanıcı adı sütunu
            "$COLUMN_ID=?", // Kullanıcı ID'sine göre filtre
            arrayOf(userId.toString()),
            null,
            null,
            null
        )

        var username: String? = null
        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
        }
        cursor.close()
        return username
    }
    fun isEventFavorited(userId: Int, eventId: Int): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM favorites WHERE user_id = ? AND event_id = ?",
            arrayOf(userId.toString(), eventId.toString())
        )
        val isFavorited = cursor.count > 0
        cursor.close()
        return isFavorited
    }
    fun removeEventFromFavorites(userId: Int, eventId: Int) {
        val db = writableDatabase
        db.delete(
            "favorites",
            "user_id = ? AND event_id = ?",
            arrayOf(userId.toString(), eventId.toString())
        )
    }
    fun addEventToFavorites(userId: Int, eventId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("event_id", eventId)
        }
        db.insert("favorites", null, values)
    }

}
