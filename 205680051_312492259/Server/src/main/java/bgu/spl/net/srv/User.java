package bgu.spl.net.srv;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private String name;
    private String password;
    private List<User> followingList;
    private List<User> followersList;
    private boolean isLogin;
    private ConcurrentHashMap<String,Message> unreadPostPM;
    private List<Message> postsList;
    private int connectionId;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        followingList = new LinkedList<User>();
        followersList = new LinkedList<User>();
        isLogin = false;
        unreadPostPM = new ConcurrentHashMap<String, Message>();
        postsList=new LinkedList<Message>();
        connectionId=-1;
    }

    public String getPassword (){
        return password;
    }
    public void addToPostsList(Message message){
        postsList.add(message);
    }

    public void addToUnreadPostPM (String name, Message m){
        unreadPostPM.put(name,m);
    }

    public ConcurrentHashMap<String,Message> getUnreadPostPM(){
        return unreadPostPM;
    }

    public List<User> getFollowingList() {
        return followingList;
    }

    public List<User> getFollowersList() {
        return followersList;
    }

    public boolean addToFollowingList(User user) {
        if (!followingList.contains(user)) {
            followingList.add(user);
            return true;
        }
        return false;
    }
    public String getName(){
        return this.name;
    }
    public boolean addToFollowersList(User user) {
        if (!followersList.contains(user)) {
            followersList.add(user);
            return true;
        }
        return false;
    }

    public boolean removeFromFollowingList(User user) {
        if (followingList.contains(user)) {
            followingList.remove(user);
            return true;
        }
        return false;
    }

    public boolean removeFromFollowersList(User user) {
        if (followersList.contains(user)) {
            followersList.remove(user);
            return true;
        }
        return false;
    }
    public int isLogin(){
        return connectionId;
    }

    public void connect(int newconnectionId) {
        this.connectionId=newconnectionId;
    }
    public void disconnect(){
        connectionId=-1;
    }

    public short getPostNum (){
        return (short) postsList.size();
    }

    public short getFollowingNum (){
        return (short) followingList.size();
    }

    public short getFollowersNum (){
        return (short) followersList.size();
    }
}
