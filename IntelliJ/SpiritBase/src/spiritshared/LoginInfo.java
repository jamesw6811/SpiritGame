/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritshared;

/**
 *
 * @author jamesw
 */
public class LoginInfo {
    public String username;
    public byte[] password;
    public LoginInfo(String u, byte[] p){
        username = u;
        password = p;
    }
}
