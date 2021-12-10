package bgu.spl.net.srv;

import java.util.List;

public class FollowAck extends Ack {
    private List<String> successFollowers;
    private String successFollowersString = "";

    public FollowAck(short opcode,List<String> successFollowers) {
        super(opcode);
        this.successFollowers=successFollowers;
        for (int i = 0; i < successFollowers.size(); i++) {
            successFollowersString += successFollowers.get(i) + '\0';
        }
    }

    public List<String> getSuccessFollowers() {
        return successFollowers;
    }

    public short getNumOfSuccessFollowers() {
        return (short) successFollowers.size();
    }

    public String getSuccessFollowersString(List<String> successFollowers) {
        return successFollowersString;
    }

    public byte[] stringToBytes(byte[] bytesArr) {
        byte[] stringByte = getSuccessFollowersString(getSuccessFollowers()).getBytes();
        byte[] op = new byte[6 + stringByte.length];
        for (int i = 0; i < 4; i++) {
            op[i] = bytesArr[i];
        }
        op[4]=(byte)((getNumOfSuccessFollowers() >> 8) & 0xFF);
        op[5]=(byte)(getNumOfSuccessFollowers() & 0xFF);
        for (int i = 0; i < stringByte.length; i++) {
            op[i + 6] = stringByte[i];
        }
        return op;
    }
}
