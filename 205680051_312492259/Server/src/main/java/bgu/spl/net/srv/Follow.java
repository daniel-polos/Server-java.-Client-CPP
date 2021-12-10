package bgu.spl.net.srv;

import java.util.LinkedList;
import java.util.List;

public class Follow implements Message {
    private List<String> followers;
    private boolean followStatus;

    public Follow() {
        followers = new LinkedList<String>();
        followStatus = true;
    }

    public void follow(String username) {
        followers.add(username);
    }

    public List<String> getFollowers(){
        return followers;
    }

    public int getNumOfFollowers() {
        return followers.size();
    }

    public void setFollowStatus(boolean bool) {
        followStatus = bool;
    }

    public boolean getFollowStatus() {
        return followStatus;
    }

    public short getOpcode(){
        return 4;
    }
}
