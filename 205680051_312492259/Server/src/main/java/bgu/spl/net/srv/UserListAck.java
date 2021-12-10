package bgu.spl.net.srv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UserListAck extends Ack {
    private ConcurrentLinkedQueue<String> userQueue;
    private String userListString = "";

    public UserListAck(short opcode , ConcurrentLinkedQueue<String> userQueue){
        super(opcode);
        this.userQueue= userQueue;
        for (String user: userQueue){
            userListString+=user;
        }
    }


    public byte[] stringToByte(byte[] bytesArr){
        ArrayList<Byte> dinamicA= new ArrayList<>();
        for(String userName: userQueue){
            byte[] a= userName.getBytes();
            for(int i=0;i<a.length;i++) {
                dinamicA.add(a[i]);
            }
            dinamicA.add((byte) '\0');
        }
        byte[] toReturn=new byte[6+dinamicA.size()];
        for (int i = 0; i < 4; i++) {
            toReturn[i] = bytesArr[i];
        }
        toReturn[4]=(byte)((userQueue.size() >> 8) & 0xFF);
        toReturn[5]=(byte)(userQueue.size() & 0xFF);
        for(int i=0;i<dinamicA.size();i++){
            toReturn[i+6]=dinamicA.get(i);
        }
        return toReturn;
    }
}
