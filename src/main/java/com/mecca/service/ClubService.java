// src/main/java/com/mecca/service/ClubService.java
package com.mecca.service;

import com.mecca.dao.ClubDAO;
import com.mecca.model.Club;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Business logic for clubs. */
public class ClubService {
    private final ClubDAO dao = new ClubDAO();

    public List<Club> getAllClubs() {
        try {
            return dao.findAll();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }
}