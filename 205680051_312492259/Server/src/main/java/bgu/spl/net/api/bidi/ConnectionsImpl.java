package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    private ConcurrentHashMap<Integer, ConnectionHandler<T>> connectionHash;

    public ConnectionsImpl(){this.connectionHash = new ConcurrentHashMap<>();}

    @Override
    public boolean send(int connectionId, T msg) {
        if(connectionHash.containsKey(connectionId)){
            connectionHash.get(connectionId).send(msg);
        }
        return false;
    }

    @Override
    public void broadcast(T msg) {
        for(Integer conn: connectionHash.keySet()){
            connectionHash.get(conn).send(msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {

    }

    public void addConnection(int connId, ConnectionHandler cooHandler){
        connectionHash.putIfAbsent(connId,cooHandler);
    }
}
