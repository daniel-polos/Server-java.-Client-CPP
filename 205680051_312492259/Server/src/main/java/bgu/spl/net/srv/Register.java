package bgu.spl.net.srv;

public class Register implements Message {
    private String username;
    private String password;

    public Register(String username, String password){
        this.username=username;
        this.password=password;
    }

    public String getUsername (){
        return username;
    }

    public String getPassword (){
        return password;
    }

    public void setUsername(String username){
        this.username=username;
    }

    public void setPassword(String password){
        this.password=password;
    }

    public short getOpcode(){
        return 1;
    }
}
