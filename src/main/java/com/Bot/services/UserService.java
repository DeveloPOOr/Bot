package com.Bot.services;

import com.Bot.dao.UserDao;
import com.Bot.domain.Film;
import com.Bot.domain.User;

import java.util.List;

public class UserService {

    private UserDao usersDao = new UserDao();

    public UserService() {
    }

    public User findUser(Long id) {
        return usersDao.findById(id);
    }

    public void saveUser(User user) {
        usersDao.save(user);
    }

    public void deleteUser(User user) {
        usersDao.delete(user);
    }

    public void updateUser(User user) {
        usersDao.update(user);
    }



    public Film findFilmById(Long id) {
        return usersDao.findFilmById(id);
    }


}
