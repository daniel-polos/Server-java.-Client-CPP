package bgu.spl.net.srv;

public class Stat implements Message {
    private String username;

    public Stat(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
