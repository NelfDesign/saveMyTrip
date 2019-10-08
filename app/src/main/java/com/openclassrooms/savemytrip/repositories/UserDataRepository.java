package com.openclassrooms.savemytrip.repositories;

import android.arch.lifecycle.LiveData;

import com.openclassrooms.savemytrip.database.dao.UserDao;
import com.openclassrooms.savemytrip.models.User;

/**
 * Created by Nelfdesign at 08/10/2019
 * com.openclassrooms.savemytrip.repositories
 */
public class UserDataRepository {

    // Field interface
    private final UserDao userDao;
    //constructor
    public UserDataRepository(UserDao userDao) { this.userDao = userDao; }

    // --- GET USER ---
    public LiveData<User> getUser(long userId) { return this.userDao.getUser(userId); }

}
