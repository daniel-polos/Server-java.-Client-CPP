package bgu.spl.net.srv;

import bgu.spl.net.srv.Ack;

public class StatAck extends Ack {
    private short numPost;
    private short numFollowers;
    private short numFollowing;

    public StatAck(short opcode, short numPost, short numFollowers, short numFollowing) {
        super(opcode);
        this.numFollowers= numFollowers;
        this.numFollowing=numFollowing;
        this.numPost=numPost;
    }

    public byte[] getBytesArr(byte[] bytesArr) {
        byte[] returnA = new byte[10];
        for (int i = 0; i < 4; i++) {
            returnA[i] = bytesArr[i];
        }
        returnA[4]=(byte)((numPost >> 8) & 0xFF);
        returnA[5]=(byte)(numPost & 0xFF);
        returnA[6]=(byte)((numFollowers >> 8) & 0xFF);
        returnA[7]=(byte)(numFollowers & 0xFF);
        returnA[8]=(byte)((numFollowing >> 8) & 0xFF);
        returnA[9]=(byte)(numFollowing & 0xFF);
        return returnA;
    }

}
