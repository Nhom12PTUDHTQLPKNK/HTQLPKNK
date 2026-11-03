package models.dao;

import sql.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Executes parameterized date range reports for Swing report views. */
public class TruyVanBaoCaoNgayDAO {
    public List<Object[]> query(String sql, LocalDate from, LocalDate to, int columnCount) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(from));
            statement.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) row[i] = result.getObject(i + 1);
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
