package bgu.spl.net.srv;

import java.util.LinkedList;
import java.util.List;

public class Post implements Message {
    private String message;
    List<String> extraFriends;

    public Post(String message){
        this.message=message;
        extraFriends= new LinkedList<>();
    }

    public void addExtraFriend(String friend){
        extraFriends.add(friend);
    }

    public List<String> getExtraFriends(){
        return extraFriends;
    }

    public String getMessage(){
        return message;
    }

    public short getOpcode(){
        return 5;
    }
}
