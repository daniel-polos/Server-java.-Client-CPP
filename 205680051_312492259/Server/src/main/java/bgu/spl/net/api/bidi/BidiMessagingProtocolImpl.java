package bgu.spl.net.api.bidi;


import bgu.spl.net.impl.BGSServer.DataBase;
import bgu.spl.net.srv.*;
import bgu.spl.net.srv.Error;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Message> {
    private static DataBase dataBase;
    private int connectionId;
    Connections<Message> connections;
    private boolean shouldTermite;

    public BidiMessagingProtocolImpl(DataBase dataBase) {
        this.dataBase=dataBase;
        shouldTermite=false;
    }

    @Override
    public void start(int connectionId, Connections<Message> connections) {
        this.connections = connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(Message message) {
        //Register-1
        if (message!=null && message instanceof Register) {
            String username = ((Register) message).getUsername();
            String password = ((Register) message).getPassword();
            User user = new User(username,password);
            boolean bool = dataBase.registerUser(username,user);
            //boolean bool = dataBase.registerUser(((Register) message).getUsername(), new User(((Register) message).getUsername(), ((Register) message).getPassword()));
            if (!bool) {
                Error error = new Error((short) 1);
                connections.send(connectionId, (Message) error);
            } else {
                Ack ack = new Ack((short) 1);
                connections.send(connectionId, (Message) ack);
            }
        }
        //Login-2
        if (message instanceof Login) {
            String name = ((Login) message).getUsername();
            String password = ((Login) message).getPassword();
            if (dataBase.isRegsitered(name)) {
                User user=dataBase.getRegisteredHash().get(name);
                synchronized (user){
                boolean passwordChk = password.equals(dataBase.getRegisteredHash().get(name).getPassword());
                if (-1==dataBase.getRegisteredHash().get(name).isLogin() && passwordChk && dataBase.loginUser(connectionId, name)) {
                    user.connect(connectionId);
                    connections.send(connectionId, (Message) new Ack((short) 2));
                    ConcurrentHashMap<String, Message> tmpHM = user.getUnreadPostPM();
                    for (Map.Entry<String, Message> entry : tmpHM.entrySet()) {
                        String username = entry.getKey();
                        Message m = entry.getValue();
                        if (m instanceof Post)
                            connections.send(connectionId, (Message) new Notification(username, ((Post) m).getMessage(), 1));
                        if (m instanceof PM) {
                            connections.send(connectionId, (Message) new Notification(username, ((PM) m).getContent(), 0));
                        }
                        tmpHM.clear();
                    }
                    tmpHM.clear();
                } else {
                    Error error = new Error((short) 2);
                    connections.send(connectionId, (Message) error);
                }}
            } else {
                Error error = new Error((short) 2);
                connections.send(connectionId, (Message) error);
            }
        }
        //Logout-3
        if (message instanceof Logout) {
            String name = dataBase.getLogedinHash().get(connectionId);
            boolean existChk = dataBase.logoutUser(connectionId, name);
            if (existChk) {
                dataBase.getRegisteredHash().get(name).disconnect();
                connections.send(connectionId,new Ack((short) 3));
                shouldTermite=true;
            } else {
                Error error = new Error((short) 3);
                connections.send(connectionId, (Message) error);
            }
        }
        //Follow-4
        if (message instanceof Follow) {
            if (dataBase.isLogedin(connectionId)) {
                String name = dataBase.getLogedinHash().get(connectionId);
                User user = dataBase.getRegisteredHash().get(name);
                List<User> followingList = user.getFollowingList();
                boolean isAck = false;
                List<String> successfullFollowers = new LinkedList<String>();
                if (((Follow) message).getFollowStatus()) {
                    //to follow list
                    for (String toFollow : ((Follow) message).getFollowers()) {
                        if (dataBase.isRegsitered(toFollow) && (!followingList.contains(dataBase.getRegisteredHash().get(toFollow)))) {
                            User toFollowUser = dataBase.getRegisteredHash().get(toFollow);
                            user.addToFollowingList(toFollowUser);
                            toFollowUser.addToFollowersList(user);
                            isAck = true;
                            successfullFollowers.add(toFollow);
                        }
                    }
                    if (isAck) {
                        connections.send(connectionId, (Message) new FollowAck((short) 4, successfullFollowers));
                    } else {
                        connections.send(connectionId, (Message) new Error((short) 4));
                    }
                }
                //else it's an unfollow list
                else {
                    for (String toUnfollow : ((Follow) message).getFollowers()) {
                        if (dataBase.isRegsitered(toUnfollow) && followingList.contains(dataBase.getRegisteredHash().get(toUnfollow))) {
                            User toUnfollowUser = dataBase.getRegisteredHash().get(toUnfollow);
                            user.removeFromFollowingList(toUnfollowUser);
                            toUnfollowUser.removeFromFollowersList(user);
                            successfullFollowers.add(toUnfollow);
                            isAck = true;
                        }
                    }
                    if (isAck) {
                        connections.send(connectionId, (Message) new FollowAck((short) 4, successfullFollowers));
                    }
                    //no one succeeded-> error
                    else {
                        connections.send(connectionId, (Message) new Error((short) 4));
                    }
                }
            } else connections.send(connectionId, (Message) new Error((short) 4));
        }
        //Post-5
        if (message instanceof Post) {
            if (dataBase.isLogedin(connectionId)) {
                String userName = dataBase.getLogedinHash().get(connectionId);
                User user = dataBase.getRegisteredHash().get(userName);
                user.addToPostsList(message);
                dataBase.postPMtoInventory((Post) message);
                List<User> followersList = user.getFollowersList();
                //all followers
                for (User followerUser : followersList) {
                    int followerID = followerUser.isLogin();
                    if (followerID != -1) {
                        connections.send(followerID, (Message) new Notification(userName, (((Post) message).getMessage()), 1));
                    } else {
                        followerUser.addToUnreadPostPM(userName,((Post) message));
                    }
                }
                //extra friend
                for (String extraUserName : ((Post) message).getExtraFriends()) {
                    User extraUser = dataBase.getRegisteredHash().get(extraUserName);
                    if (extraUser != null && !(followersList.contains(extraUser))) {
                        synchronized (extraUser){
                        int extraUserID = extraUser.isLogin();
                        if (extraUserID != -1) {
                            connections.send(extraUserID, (Message) new Notification(user.getName(), (((Post) message).getMessage()),1));
                        } else {
                            extraUser.addToUnreadPostPM(userName,((Post) message));
                        } }
                    }
                }
                connections.send(connectionId, (Message) new Ack((short) 5));
            }
            //he's not log in
            else {
                connections.send(connectionId, (Message) new Error((short) 5));
            }
        }
        //PM-6
        if (message instanceof PM) {
            if (dataBase.isLogedin(connectionId)) {
                String userName = dataBase.getLogedinHash().get(connectionId);
                User user = dataBase.getRegisteredHash().get(userName);
                User recipient = dataBase.getRegisteredHash().get(((PM) message).getRecipient());
                if (recipient != null) {
                    synchronized (recipient){
                    int recipientID = recipient.isLogin();
                    if (recipientID != -1) {
                        connections.send(recipientID, (Message) new Notification(userName, ((PM) message).getContent(), 0));
                    } else {
                        recipient.addToUnreadPostPM(userName,((PM) message));
                    }
                    connections.send(connectionId, (Message) new Ack((short) 6));
                    dataBase.postPMtoInventory((Message) message);
                }} else {
                    connections.send(connectionId, (Message) new Error((short) 6));
                }
            }
            //he's not log in
            else {
                connections.send(connectionId, (Message) new Error((short) 6));
            }
        }
        //UserList-7
        if (message instanceof Userlist) {
            if (dataBase.getLogedinHash().containsKey(connectionId)) {
                connections.send(connectionId, (Message) new UserListAck((short) 7,dataBase.getRegisteredUsers()));
            }
            //he's not log in
            else {
                connections.send(connectionId, (Message) new Error((short) 7));
            }
        }
        //Stat-8
        if (message instanceof Stat) {
            if (dataBase.isLogedin(connectionId)) {
                User user = dataBase.getRegisteredHash().get(((Stat) message).getUsername());
                if (user!=null){
                    connections.send(connectionId, (Message) new StatAck((short) 8,user.getPostNum(), user.getFollowersNum(), user.getFollowingNum()));
                }
                else {
                    connections.send(connectionId, (Message) new Error((short) 8));
                }
            }
            //he's not log in
            else {
                connections.send(connectionId, (Message) new Error((short) 8));
            }
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTermite;//TODO no logics, probably should change according to logout
    }
}
