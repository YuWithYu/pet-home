import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String encoded = encoder.encode(password);
        System.out.println("Original password: " + password);
        System.out.println("BCrypt encoded: " + encoded);
        
        // 验证密码
        boolean matches = encoder.matches(password, encoded);
        System.out.println("Verification result: " + matches);
    }
}
