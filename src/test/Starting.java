package test;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import views.LoginFrame;

public class Starting {

    public static void main(String[] args) {
        // 1. Khởi tạo CSDL
       // sql.init.initDatabase("phongkhamnhakhoainit.sql");

        // 2. Cài đặt Look & Feel hệ điều hành
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Mở màn hình đăng nhập
        
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}