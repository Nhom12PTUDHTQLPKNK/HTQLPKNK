package models.dao;

import sql.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Truy vấn dữ liệu cho màn hình tra cứu khách hàng. */
public class TraCuuKhachHangDAO {
    public static final class CustomerRows {
        public final List<Object[]> rows;
        public final double[] statistics;
        public CustomerRows(List<Object[]> rows, double[] statistics) { this.rows=rows; this.statistics=statistics; }
    }

    public CustomerRows search(String keyword, int tier, int sort, int silver, int gold) throws SQLException {
        String[] orderBy={"maKhachHang","tenKH COLLATE Vietnamese_CI_AS","ISNULL(diemTichLuy,0) DESC, tenKH","ISNULL(diemTichLuy,0), tenKH"};
        StringBuilder sql=new StringBuilder("SELECT maKhachHang,tenKH,gioiTinh,sdt,CONVERT(VARCHAR(10),ngaySinh,103),ISNULL(diemTichLuy,0) FROM KhachHang WHERE 1=1");
        if(keyword!=null&&!keyword.trim().isEmpty())sql.append(" AND (maKhachHang LIKE ? OR sdt LIKE ? OR tenKH COLLATE Vietnamese_CI_AI LIKE ?)");
        if(tier==3)sql.append(" AND ISNULL(diemTichLuy,0) < ?");
        else if(tier==2)sql.append(" AND ISNULL(diemTichLuy,0) >= ? AND ISNULL(diemTichLuy,0) < ?");
        else if(tier==1)sql.append(" AND ISNULL(diemTichLuy,0) >= ?");
        sql.append(" ORDER BY ").append(orderBy[Math.max(0,Math.min(sort,orderBy.length-1))]);
        List<Object[]> rows=new ArrayList<>(); double[] stats=new double[4];
        try(Connection c=DBConnection.getConnection()){
            try(PreparedStatement p=c.prepareStatement(sql.toString())){
                int i=1; if(keyword!=null&&!keyword.trim().isEmpty()){String like="%"+keyword.trim()+"%";p.setString(i++,like);p.setString(i++,like);p.setString(i++,like);}
                if(tier==3)p.setInt(i,silver);else if(tier==2){p.setInt(i++,silver);p.setInt(i,gold);}else if(tier==1)p.setInt(i,gold);
                try(ResultSet r=p.executeQuery()){while(r.next())rows.add(new Object[]{r.getString(1),r.getString(2),nz(r.getString(3)),nz(r.getString(4)),nz(r.getString(5)),r.getInt(6)});}
            }
            try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*),ISNULL(SUM(diemTichLuy),0),ISNULL(AVG(CAST(ISNULL(diemTichLuy,0) AS FLOAT)),0),ISNULL(SUM(CASE WHEN diemTichLuy>=? THEN 1 ELSE 0 END),0) FROM KhachHang")){
                p.setInt(1,gold);try(ResultSet r=p.executeQuery()){if(r.next())for(int j=0;j<4;j++)stats[j]=r.getDouble(j+1);}
            }
        }
        return new CustomerRows(rows,stats);
    }

    public String[] getDetails(String customerId) throws SQLException {
        String sql="SELECT k.tenKH,ISNULL(k.diemTichLuy,0),ISNULL(k.sdt,''),ISNULL(CONVERT(VARCHAR(10),k.ngaySinh,103),''),ISNULL(k.gioiTinh,''),ISNULL(k.diaChi,''),"+
                "(SELECT COUNT(*) FROM HoSoBenhAn b WHERE b.maKhachHang=k.maKhachHang),ISNULL((SELECT CONVERT(VARCHAR(10),MAX(b.ngayKham),103) FROM HoSoBenhAn b WHERE b.maKhachHang=k.maKhachHang),''),"+
                "ISNULL((SELECT TOP 1 CONVERT(VARCHAR(10),l.ngayHen,103)+' - '+ISNULL(l.gioKham,'') FROM LichHen l WHERE l.maKhachHang=k.maKhachHang AND l.ngayHen>=CAST(GETDATE() AS DATE) AND l.trangThai<>N'Hoàn thành' ORDER BY l.ngayHen,l.gioKham),''),"+
                "CAST(ISNULL((SELECT SUM(ct.soLuong*ct.donGia) FROM ChiTietHoaDon ct JOIN HoaDon h ON h.maHoaDon=ct.maHoaDon WHERE h.maKhachHang=k.maKhachHang),0) AS BIGINT),ISNULL(k.tienSuBenh,'') FROM KhachHang k WHERE k.maKhachHang=?";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,customerId);try(ResultSet r=p.executeQuery()){if(!r.next())return new String[]{"","0","","","","","0","","","0",""};String[] d=new String[11];for(int i=0;i<d.length;i++)d[i]=nz(r.getString(i+1));return d;}}
    }

    public boolean adjustPoints(String customerId,int points) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE KhachHang SET diemTichLuy=CASE WHEN ISNULL(diemTichLuy,0)+?<0 THEN 0 ELSE ISNULL(diemTichLuy,0)+? END WHERE maKhachHang=?")){
            p.setInt(1,points);p.setInt(2,points);p.setString(3,customerId);return p.executeUpdate()>0;
        }
    }
    private static String nz(String value){return value==null?"":value;}
}
