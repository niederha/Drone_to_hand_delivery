package felix_loc_herman.drone_delivery;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Profile implements Serializable {

    String username;
    private String password; // hashed
    String photoPath;

    Profile(String username, String password, String photoPath) {
        this.username = username;
        setPassword(password);
        this.photoPath = photoPath;
    }

    public void setPassword(String password) {
        this.password = md5(password);
    }

    public String getHashedPassword(){
        return password;
    }

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
