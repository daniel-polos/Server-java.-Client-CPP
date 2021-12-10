package bgu.spl.net.srv;

public class Logout implements Message{
    public Logout(){}

    public short getOpcode(){
        return 3;
    }
}
