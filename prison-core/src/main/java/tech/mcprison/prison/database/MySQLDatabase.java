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

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * An implementation of {@link Database} for MySQL.
 *
 * @author Faizaan A. Datoo
 * @since 3.0
 */
public class MySQLDatabase extends Database {

    private String host, username, password, database;
    private int port;

    public MySQLDatabase(String host, String username, String password, String database, int port) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.database = database;
        this.port = port;
    }

    @Override public boolean establishConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return true;
            }

            synchronized (this) {
                if (connection != null && !connection.isClosed()) {
                    return true;
                }

                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(
                    "jdbc:mysql://" + this.getHost() + ":" + this.getPort() + "/" + this
                        .getDatabase(), this.getUsername(), this.getPassword());
            }
            return true;
        } catch (SQLException e) {
            Output.get().logError("Failed to establish a connection to the MySQL database.", e);
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            Output.get().logError(
                "You do not have the required MySQL JDBC driver to connect to the database..", e);
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

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public int getPort() {
        return port;
    }

}
