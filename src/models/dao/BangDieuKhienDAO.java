package models.dao;

import models.entity.DashboardSnapshot;
import sql.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

/** Truy vấn và ánh xạ dữ liệu tổng quan cho màn hình bảng điều khiển. */
public class BangDieuKhienDAO {
    public DashboardSnapshot loadSnapshot() throws SQLException {
        DashboardSnapshot data = new DashboardSnapshot();
        try (Connection conn = DBConnection.getConnection()) {
            String statsSql = "SELECT (SELECT COUNT(*) FROM KhachHang) TongBenhNhan, "
                    + "(SELECT COUNT(*) FROM LichHen WHERE CAST(ngayHen AS DATE)=CAST(GETDATE() AS DATE)) LichHenHomNay, "
                    + "(SELECT COALESCE(SUM(daThanhToan),0) FROM PhieuThanhToan) DoanhThuThang, "
                    + "(SELECT COUNT(*) FROM LichHen WHERE trangThai=N'Chờ xác nhận') CanChuY";
            try (PreparedStatement ps=conn.prepareStatement(statsSql); ResultSet rs=ps.executeQuery()) {
                if(rs.next()) { data.totalPatients=rs.getInt("TongBenhNhan"); data.appointmentsToday=rs.getInt("LichHenHomNay"); data.revenue=rs.getDouble("DoanhThuThang"); data.pendingAppointments=rs.getInt("CanChuY"); }
            }
            String revenueSql="WITH Ngay AS (SELECT CAST(GETDATE() AS DATE) NgayDoanhThu UNION ALL SELECT DATEADD(DAY,-1,NgayDoanhThu) FROM Ngay WHERE NgayDoanhThu>DATEADD(DAY,-6,CAST(GETDATE() AS DATE))) SELECT n.NgayDoanhThu,COALESCE(SUM(p.daThanhToan),0) DoanhThu FROM Ngay n LEFT JOIN PhieuThanhToan p ON CAST(p.ngayThanhToan AS DATE)=n.NgayDoanhThu GROUP BY n.NgayDoanhThu ORDER BY n.NgayDoanhThu ASC OPTION (MAXRECURSION 7)";
            try(PreparedStatement ps=conn.prepareStatement(revenueSql);ResultSet rs=ps.executeQuery()) { DateTimeFormatter fmt=DateTimeFormatter.ofPattern("dd/MM");while(rs.next()){Date d=rs.getDate("NgayDoanhThu");if(d!=null){data.days.add(d.toLocalDate().format(fmt));data.dailyRevenue.add(rs.getDouble("DoanhThu"));}} }
            String serviceSql="SELECT TOP 5 dv.tenDichVu,SUM(ct.soLuong*ct.donGia) DoanhThu FROM ChiTietHoaDon ct INNER JOIN DichVu dv ON ct.maDichVu=dv.maDichVu INNER JOIN HoaDon hd ON ct.maHoaDon=hd.maHoaDon GROUP BY dv.tenDichVu ORDER BY SUM(ct.soLuong*ct.donGia) DESC";
            try(PreparedStatement ps=conn.prepareStatement(serviceSql);ResultSet rs=ps.executeQuery()){while(rs.next()){data.services.add(rs.getString("tenDichVu"));data.serviceRevenue.add(rs.getDouble("DoanhThu"));}}
            String patientSql="SELECT TOP 10 ba.maBenhAn,kh.tenKH,kh.sdt,COALESCE(dv.tenDichVu,ba.chuanDoan,N'Chưa cập nhật') DichVuGanNhat,ba.ngayKham,COALESCE(ba.trangThai,N'Chưa cập nhật') TrangThai FROM HoSoBenhAn ba INNER JOIN KhachHang kh ON ba.maKhachHang=kh.maKhachHang LEFT JOIN KeHoachDieuTri khdt ON khdt.maBenhAn=ba.maBenhAn LEFT JOIN DichVu dv ON dv.maDichVu=khdt.maDichVu WHERE ba.ngayKham IS NOT NULL ORDER BY ba.ngayKham DESC,ba.maBenhAn DESC";
            try(PreparedStatement ps=conn.prepareStatement(patientSql);ResultSet rs=ps.executeQuery()){while(rs.next()){Date d=rs.getDate("ngayKham");data.patients.add(new Object[]{rs.getString("maBenhAn"),rs.getString("tenKH"),rs.getString("sdt"),rs.getString("DichVuGanNhat"),d==null?"":d.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),rs.getString("TrangThai")});}}
            String appointmentSql="SELECT TOP 5 lh.maLichHen,kh.tenKH,lh.ngayHen,lh.trangThai FROM LichHen lh LEFT JOIN KhachHang kh ON lh.maKhachHang=kh.maKhachHang ORDER BY lh.ngayHen DESC,lh.maLichHen DESC";
            try(PreparedStatement ps=conn.prepareStatement(appointmentSql);ResultSet rs=ps.executeQuery()){while(rs.next())data.appointmentNotifications.add(new Object[]{rs.getString("tenKH"),rs.getString("ngayHen"),rs.getString("trangThai")});}
            String paymentSql="SELECT TOP 3 pt.maPhieu,kh.tenKH,pt.daThanhToan,pt.ngayThanhToan FROM PhieuThanhToan pt LEFT JOIN KhachHang kh ON pt.maKhachHang=kh.maKhachHang ORDER BY pt.ngayThanhToan DESC,pt.maPhieu DESC";
            try(PreparedStatement ps=conn.prepareStatement(paymentSql);ResultSet rs=ps.executeQuery()){while(rs.next())data.paymentNotifications.add(new Object[]{rs.getString("tenKH"),rs.getDouble("daThanhToan"),rs.getString("ngayThanhToan")});}
        }
        return data;
    }
}
