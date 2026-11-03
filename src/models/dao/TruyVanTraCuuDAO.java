package models.dao;

import sql.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Database access used by the reusable search view. */
public class TruyVanTraCuuDAO {
    public List<Object[]> search(String sourceQuery, String[] searchColumns, String keyword,
                                 LocalDate selectedDate, String dateFilterColumn,
                                 int resultColumnCount) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT * FROM (")
                .append(sourceQuery).append(") AS lookupData WHERE (? = N''");
        for (String column : searchColumns) {
            sql.append(" OR CAST(lookupData.[").append(column)
                    .append("] AS NVARCHAR(4000)) LIKE ?");
        }
        sql.append(")");
        if (selectedDate != null && dateFilterColumn != null) {
            sql.append(" AND CAST(lookupData.[").append(dateFilterColumn).append("] AS DATE) = ?");
        }
        sql.append(" ORDER BY 1");

        List<Object[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setString(1, keyword);
            for (int i = 0; i < searchColumns.length; i++) {
                statement.setString(i + 2, "%" + keyword + "%");
            }
            if (selectedDate != null && dateFilterColumn != null) {
                statement.setDate(searchColumns.length + 2, java.sql.Date.valueOf(selectedDate));
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Object[] row = new Object[resultColumnCount];
                    for (int column = 0; column < resultColumnCount; column++) {
                        row[column] = result.getObject(column + 1);
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
