package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.Message;
import bgu.spl.net.srv.PM;
import bgu.spl.net.srv.Post;
import bgu.spl.net.srv.User;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class DataBase {
    private ConcurrentHashMap<Integer, String> logedinHash;
    private ConcurrentHashMap<String, User> registeredHash;
    private List<Message> postPMIventory;
    private ConcurrentHashMap<User, LinkedList<Post>> postsHash;
    private ConcurrentLinkedQueue<String> userQueue;

    public DataBase(){
        logedinHash = new ConcurrentHashMap<>();
        registeredHash = new ConcurrentHashMap<>();
        postsHash = new ConcurrentHashMap<>();
        postPMIventory = new LinkedList<Message>();
        userQueue = new ConcurrentLinkedQueue<String>();
    }

    public synchronized boolean loginUser(Integer connectionId,String username){
        if (!logedinHash.containsKey(connectionId)){
            logedinHash.put(connectionId,username);
            return true;
        }
        return false;
    }

    public boolean logoutUser(Integer connectionId,String username){
        return logedinHash.remove(connectionId,username);
    }

    public synchronized boolean registerUser(String string, User user){
        if (!registeredHash.containsKey(string)){
            registeredHash.put(string,user);
            userQueue.add(string);
            return true;
        }
        return false;
    }

    public boolean isLogedin(Integer connectionId){
        return logedinHash.containsKey(connectionId);
    }

    public boolean isRegsitered(String name){
        return registeredHash.containsKey(name);
    }

    public boolean unregisterUser(Integer connectionId,String username){
        return registeredHash.remove(connectionId,username);
    }

    public boolean post(User user, Post post){
        postsHash.putIfAbsent(user,new LinkedList<Post>());
        return postsHash.get(user).add(post);

    }

    public boolean postPMtoInventory (Message msg){
        if (msg instanceof Post || msg instanceof PM)
            return postPMIventory.add(msg);
        return false;
    }

    public ConcurrentHashMap<String, User> getRegisteredHash(){
        return registeredHash;
    }

    public ConcurrentHashMap<Integer, String> getLogedinHash(){
        return logedinHash;
    }

    public ConcurrentLinkedQueue<String> getRegisteredUsers (){
        return userQueue;
    }
}
