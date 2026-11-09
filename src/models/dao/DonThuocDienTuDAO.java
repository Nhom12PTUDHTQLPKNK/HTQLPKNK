package models.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import sql.DBConnection;

public class DonThuocDienTuDAO {

    public List<String> getPrescribedEncounterCodes() throws SQLException {
        List<String> codes = new ArrayList<>();
        String sql = "SELECT DISTINCT maBenhAn FROM DonThuoc";
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            while(r.next()) if(r.getString(1)!=null) codes.add(r.getString(1).trim());
        }
        return codes;
    }

    public List<Object[]> getPlannedPatients() throws SQLException {
        List<Object[]> rows=new ArrayList<>();
        String sql="SELECT hs.maBenhAn,kh.tenKH,dv.tenDichVu,r.maRang,ctd.soLuong,dv.donGia FROM HoSoBenhAn hs JOIN KhachHang kh ON hs.maKhachHang=kh.maKhachHang LEFT JOIN ChiTietKhamRang r ON hs.maBenhAn=r.maBenhAn LEFT JOIN ChiTietChiDinh ctd ON hs.maBenhAn=ctd.maBenhAn AND (r.maRang=ctd.maRang OR ctd.maRang IS NULL) LEFT JOIN DichVu dv ON ctd.maDichVu=dv.maDichVu WHERE hs.trangThai LIKE N'%lập kế hoạch%'";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            while(r.next())rows.add(new Object[]{r.getString("maBenhAn"),r.getString("tenKH"),r.getString("tenDichVu"),r.getInt("maRang"),r.getInt("soLuong"),r.getDouble("donGia")});
        }
        return rows;
    }

    public String[] getPatientSummary(String encounterId) throws SQLException {
        String sql="SELECT kh.tenKH,kh.sdt,nv.tenNhanVien FROM HoSoBenhAn hs JOIN KhachHang kh ON hs.maKhachHang=kh.maKhachHang LEFT JOIN NhanVien nv ON hs.maNhanVien=nv.maNhanVien WHERE hs.maBenhAn=?";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,encounterId);try(ResultSet r=p.executeQuery()){return r.next()?new String[]{r.getString(1),r.getString(2),r.getString(3)}:null;}}
    }

    public List<Object[]> getAvailableMedicines() throws SQLException {
        List<Object[]> rows=new ArrayList<>();
        String sql="SELECT T.MaThuoc,T.TenThuoc,T.HoatChatChinh,N.TenNhomThuoc,DBC.TenDangBaoChe,DD.TenDuongDung,T.NhaSanXuat,T.QuyCachDongGoi,T.HinhAnh FROM THUOC T LEFT JOIN NHOMTHUOC N ON T.MaNhomThuoc=N.MaNhomThuoc LEFT JOIN DANGBAOCHE DBC ON T.MaDangBaoChe=DBC.MaDangBaoChe LEFT JOIN DUONGDUNG DD ON T.MaDuongDung=DD.MaDuongDung WHERE T.TrangThai=1 ORDER BY T.TenThuoc";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            while(r.next())rows.add(new Object[]{r.getString("MaThuoc"),r.getString("TenThuoc"),r.getString("HoatChatChinh"),r.getString("TenNhomThuoc"),r.getString("TenDangBaoChe"),r.getString("TenDuongDung"),r.getString("NhaSanXuat"),r.getString("QuyCachDongGoi"),r.getString("HinhAnh")});
        }
        return rows;
    }

    public int savePrescriptionBatch(String encounterId,List<Object[]> items) throws SQLException {
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try {
                String patientId=null,employeeId=null;
                try(PreparedStatement p=c.prepareStatement("SELECT maKhachHang,maNhanVien FROM HoSoBenhAn WHERE maBenhAn=?")){
                    p.setString(1,encounterId);try(ResultSet r=p.executeQuery()){if(r.next()){patientId=r.getString(1);employeeId=r.getString(2);}}
                }
                if(patientId==null)throw new SQLException("Không tìm thấy bệnh án hoặc khách hàng của bệnh án.");
                String sql="INSERT INTO DonThuoc(maDonThuoc,maBenhAn,maNhanVien,maKhachHang,maThuoc,tenThuoc,ngayKe,soLuong,cachDung,ghiChu) VALUES(?,?,?,?,?,?,GETDATE(),?,?,?)";
                int saved=0;long now=System.currentTimeMillis();
                try(PreparedStatement p=c.prepareStatement(sql)){
                    for(int i=0;i<items.size();i++){
                        Object[] item=items.get(i);p.setString(1,"DT_"+now+"_"+(i+1));p.setString(2,encounterId);p.setString(3,employeeId);p.setString(4,patientId);
                        p.setString(5,(String)item[0]);p.setString(6,(String)item[1]);p.setInt(7,((Number)item[2]).intValue());
                        p.setString(8,"ĐVT: "+item[3]+" | Cách dùng: "+item[4]+" | Liều dùng: "+item[5]);p.setString(9,"Đã kê đơn điện tử");p.addBatch();
                    }
                    for(int result:p.executeBatch())if(result>0||result==Statement.SUCCESS_NO_INFO)saved++;
                }
                c.commit();return saved;
            } catch(SQLException ex){c.rollback();throw ex;} finally {c.setAutoCommit(true);}
        }
    }

    // Lấy thông tin chi tiết bệnh án, khách hàng, kế hoạch điều trị theo mã bệnh án
    public ResultSet getPrescriptionHeaderInfo(String maBenhAn) throws SQLException {
        String query = "SELECT ba.maBenhAn, kh.maKhachHang, kh.tenKH, kh.ngaySinh, kh.gioiTinh, kh.sdt, " +
                       "kh.tienSuBenh, kh.diemTichLuy, kh.ngaySinh, " +
                       "khd.maKeHoach, dv.tenDichVu, nv.maNhanVien, nv.tenNhanVien, " +
                       "khd.ngayLap, khd.trangThai, khd.tongChiPhiDuKien " +
                       "FROM HoSoBenhAn ba " +
                       "JOIN KhachHang kh ON ba.maKhachHang = kh.maKhachHang " +
                       "JOIN NhanVien nv ON ba.maNhanVien = nv.maNhanVien " +
                       "LEFT JOIN KeHoachDieuTri khd ON ba.maBenhAn = khd.maBenhAn " +
                       "LEFT JOIN DichVu dv ON khd.maDichVu = dv.maDichVu " +
                       "WHERE ba.maBenhAn = ?";
        Connection conn = DBConnection.getConnection(); // Giả định lớp kết nối CSDL của bạn
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, maBenhAn);
        return pst.executeQuery();
    }

    // Lấy danh sách thuốc đã kê cho đơn thuốc
    public List<Object[]> getPrescriptionDetails(String maBenhAn) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String query = "SELECT dt.maThuoc, t.TenThuoc, dbc.TenDangBaoChe, ld.LieuDung, ld.TanSuat, " +
                       "ld.ThoiGianSuDung, dt.soLuong, t.QuyCachDongGoi, dt.cachDung, dt.ghiChu " +
                       "FROM DonThuoc dt " +
                       "JOIN THUOC t ON dt.maThuoc = t.MaThuoc " +
                       "JOIN DANGBAOCHE dbc ON t.MaDangBaoChe = dbc.MaDangBaoChe " +
                       "LEFT JOIN THUOCLIEUDUNG ld ON t.MaThuoc = ld.MaThuoc " +
                       "WHERE dt.maBenhAn = ?";
        Connection conn = DBConnection.getConnection();
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, maBenhAn);
        ResultSet rs = pst.executeQuery();
        int stt = 1;
        while (rs.next()) {
            list.add(new Object[]{
                stt++,
                rs.getString("maThuoc"),
                rs.getString("TenThuoc"),
                rs.getString("TenDangBaoChe"),
                rs.getString("LieuDung"),
                rs.getString("TanSuat"),
                rs.getString("ThoiGianSuDung"),
                rs.getInt("soLuong"),
                rs.getString("QuyCachDongGoi"), // Đơn vị tính/Quy cách
                rs.getString("cachDung")
            });
        }
        return list;
    }

    // Lưu hoặc cập nhật đơn thuốc vào CSDL SQL Server
    public void savePrescription(String maDonThuoc, String maBenhAn, String maNhanVien, 
                                 String maKhachHang, String maThuoc, String tenThuoc, 
                                 int soLuong, String cachDung, String ghiChu) throws SQLException {
        String query = "INSERT INTO DonThuoc (maDonThuoc, maBenhAn, maNhanVien, maKhachHang, maThuoc, tenThuoc, ngayKe, soLuong, cachDung, ghiChu) " +
                       "VALUES (?, ?, ?, ?, ?, ?, GETDATE(), ?, ?, ?)";
        Connection conn = DBConnection.getConnection();
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, maDonThuoc);
        pst.setString(2, maBenhAn);
        pst.setString(3, maNhanVien);
        pst.setString(4, maKhachHang);
        pst.setString(5, maThuoc);
        pst.setString(6, tenThuoc);
        pst.setInt(7, soLuong);
        pst.setString(8, cachDung);
        pst.setString(9, ghiChu);
        pst.executeUpdate();
    }
}
