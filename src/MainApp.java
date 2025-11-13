import javax.swing.*;
import java.awt.*;

/**
 * 🚀 MainApp (ไฟล์ใหม่)
 * คลาสนี้ทำหน้าที่เป็นจุดเริ่มต้น (Entry Point) ของโปรแกรม
 * และจัดการเฉพาะหน้าต่าง Login เท่านั้น
 */
public class MainApp {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatabaseManager dbManagerForLogin = new DatabaseManager();
        dbManagerForLogin.initDatabase();

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        
        int option = JOptionPane.showConfirmDialog(null, panel, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            Role userRole = dbManagerForLogin.checkLogin(user, pass);

            if (userRole != Role.UNKNOWN) {
                switch (userRole) {
                    case ADMIN:
                    case OFFICER:
                    case GUEST:
                        new StudentManagementGUI(userRole, user);
                        break;
                    case TEACHER:
                        new TeacherPortalGUI(userRole, user); 
                        break;
                    case STUDENT:
                        new StudentPortalGUI(userRole, user); 
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "ไม่พบ Role ที่กำหนดไว้!");
                        System.exit(0);
                }
            } else {
                JOptionPane.showMessageDialog(null, "❌ Wrong username or password!");
                System.exit(0);
            }
        } else {
            System.exit(0); 
        }
    }
}