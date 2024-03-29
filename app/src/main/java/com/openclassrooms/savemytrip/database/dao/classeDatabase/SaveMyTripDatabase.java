package com.openclassrooms.savemytrip.database.dao.classeDatabase;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;

import com.openclassrooms.savemytrip.database.dao.ItemDao;
import com.openclassrooms.savemytrip.database.dao.UserDao;
import com.openclassrooms.savemytrip.models.Item;
import com.openclassrooms.savemytrip.models.User;

/**
 * Created by Nelfdesign at 08/10/2019
 * com.openclassrooms.savemytrip.database.dao.classeDatabase
 */
@Database(entities = {Item.class, User.class}, version = 2, exportSchema = false)
public abstract class SaveMyTripDatabase extends RoomDatabase {

    // --- SINGLETON ---
    private static volatile SaveMyTripDatabase INSTANCE;

    //change the database to add the new field
    private static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `Item` ADD imageUri TEXT");
        }
    };

    // --- DAO ---
    public abstract ItemDao itemDao();
    public abstract UserDao userDao();

    // --- INSTANCE ---
    //add migration for the creation
    public static SaveMyTripDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SaveMyTripDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SaveMyTripDatabase.class, "MyDatabase.db")
                            .addMigrations(MIGRATION_1_2)
                            .addCallback(prepopulateDatabase())
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // ---

    private static Callback prepopulateDatabase(){
        return new Callback() {

            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);

                ContentValues contentValues = new ContentValues();
                contentValues.put("id", 1);
                contentValues.put("username", "Philippe");
                contentValues.put("urlPicture", "https://oc-user.imgix.net/users/avatars/15175844164713_frame_523.jpg?auto=compress,format&q=80&h=100&dpr=2");

                db.insert("User", OnConflictStrategy.IGNORE, contentValues);
            }
        };
    }
}
