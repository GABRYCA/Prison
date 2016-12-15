/*
 *  Prison is a Minecraft plugin for the prison game mode.
 *  Copyright (C) 2016 The Prison Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tech.mcprison.prison.database;

import tech.mcprison.prison.output.Output;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * An implementation of {@link Database} for SQLite.
 *
 * @author Faizaan A. Datoo
 * @since 3.0
 */
public class SQLiteDatabase extends Database {

    File dbFile;

    public SQLiteDatabase(File dbFile) {
        this.dbFile = dbFile;
    }

    @Override public boolean establishConnection() {
        if (!dbFile.exists()) {
            throw new IllegalStateException("Database file must exist.");
        }

        try {
            if (connection != null && !connection.isClosed()) {
                return true;
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            return true;
        } catch (SQLException e) {
            Output.get().logError("Failed to establish a connection to the SQLite database.", e);
            return false;
        } catch (ClassNotFoundException e) {
            Output.get().logError("You do not have the correct SQLite JDBC drivers.", e);
            return false;
        }
    }

    @Override public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            Output.get().logError(
                "Cannot close database connection.", e);
        }
    }

}
