package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.srv.*;
import bgu.spl.net.srv.Error;
import bgu.spl.net.srv.StatAck;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl<T> implements MessageEncoderDecoder<T> {
    private int count;
    private short result;
    private String tmpMsg;
    private int len=0;
    private short zeroCount;
    private byte followResult;
    private byte[] bytes=new byte[1<<10];
//    private short numOfUsers;


    public MessageEncoderDecoderImpl() {
        count = 0;
        result=0;
        tmpMsg="";
        zeroCount=0;

    }


    @Override
    public T decodeNextByte(byte nextByte) {
        count++;
        switch (count) {
            // getting the Opcode
            case 1:
                result = (short) ((nextByte & 0xff) << 8);
                break;
            case 2:
                result += (short) ((nextByte & 0xff));
                if (result==7){
                    count = 0;
                    tmpMsg = "";
                    return (T) new Userlist();
                }
                if(result==3){
                    count = 0;
                    tmpMsg = "";
                    return (T) new Logout();
                }
                break;
            // getting the rest of the message
            default:
                switch (result) {
                    // REGISTER
                    case 1:
                        if (zeroCount == 0)
                            zeroCount = 2;
                        if (nextByte == 0x00) {
                            zeroCount--;
                            if (zeroCount == 0) {
                                tmpMsg += popString();
                                Register register = new Register(tmpMsg.substring(0, tmpMsg.indexOf('\0')), tmpMsg.substring(tmpMsg.indexOf('\0') + 1));
                                count = 0;
                                tmpMsg = "";
                                return (T) register;
                            }
                        }
                        pushByte(nextByte);
                        return null;

                    // LOGIN
                    case 2:
                        if (zeroCount == 0)
                            zeroCount = 2;
                        if (nextByte == 0x00) {
                            zeroCount--;
                            if (zeroCount == 0) {
                                tmpMsg += popString();
                                Login login = new Login(tmpMsg.substring(0, tmpMsg.indexOf('\0')), tmpMsg.substring(tmpMsg.indexOf('\0') + 1));
                                count = 0;
                                tmpMsg = "";
                                return (T) login;
                            }
                        }
                        pushByte(nextByte);
                        return null;

                    // FOLLOW
                    case 4:
                        switch (count) {
                            case 3:
                                followResult=nextByte;
                              //  followResult = (short) ((nextByte & 0xff) << 8);
                                break;
                            case 4:
                                zeroCount = (short) ((nextByte & 0xff) << 8);
                                break;
                            case 5:
                                zeroCount += (short) ((nextByte & 0xff));
                                break;
                            default:
                                if (nextByte == '\0') {
                                    zeroCount--;
                                    if (zeroCount == 0) {
                                        tmpMsg += popString();
                                        tmpMsg=tmpMsg.substring(3);
                                        Follow followUnFollow=new Follow();
                                        if (followResult == '0') {
                                            followUnFollow.setFollowStatus(true);
                                        } else if (followResult=='1') {
                                            followUnFollow.setFollowStatus(false);
                                        }

                                        while (tmpMsg.length() > 0) {
                                            if (tmpMsg.indexOf('\0') != -1) {
                                                followUnFollow.follow(tmpMsg.substring(0, tmpMsg.indexOf('\0')));
                                            } else if (tmpMsg.indexOf('\0') == -1) {
                                                followUnFollow.follow(tmpMsg);
                                                break;
                                            }
                                            tmpMsg=tmpMsg.substring(tmpMsg.indexOf('\0') + 1);
                                             //   if(tmpMsg.indexOf('\0')==-1) {
                                              //      followUnFollow.follow(tmpMsg);
                                        }
                                        count = 0;
                                        tmpMsg = "";
                                        return (T) followUnFollow;
                                    }
                                }
                                break;
                        }
                        pushByte(nextByte);
                        return null;

                    // POST
                    case 5:
                        if (nextByte == 0x00) {
                            tmpMsg = popString();
                            Post post = new Post(tmpMsg);
                            int nextAt = 0;
                            while (tmpMsg != "") {
                                nextAt = tmpMsg.indexOf('@');
                                if (nextAt == -1) {
                                    tmpMsg = "";
                                } else {
                                    tmpMsg = tmpMsg.substring(nextAt + 1);
                                    int spaceIndex = tmpMsg.indexOf(' ');
                                    if (spaceIndex == -1) {
                                        post.addExtraFriend(tmpMsg);
                                        tmpMsg = "";
                                    } else {
                                        post.addExtraFriend(tmpMsg.substring(0, spaceIndex));
                                        tmpMsg = tmpMsg.substring(spaceIndex + 1);
                                    }
                                }
                            }
                            count = 0;
                            tmpMsg = "";
                            return (T) post;
                        }
                        pushByte(nextByte);
                        return null;

                    // PM
                    case 6:
                        if (zeroCount == 0)
                            zeroCount = 2;
                        if (nextByte == 0x00) {
                            zeroCount--;
                            if (zeroCount == 0) {
                                tmpMsg += popString();
                                PM pm = new PM(tmpMsg.substring(0, tmpMsg.indexOf('\0')), tmpMsg.substring(tmpMsg.indexOf('\0') + 1));
                                count = 0;
                                tmpMsg = "";
                                return (T) pm;
                            }
                        }
                        pushByte(nextByte);
                        return null;

                    // STAT
                    case 8:
                        if (nextByte == 0x00) {
                            tmpMsg = popString();
                            Stat stat = new Stat(tmpMsg);
                            count = 0;
                            tmpMsg = "";
                            return (T) stat;
                        }
                        pushByte(nextByte);
                        return null;
                }
        }

        return null;
    }


    @Override
    public byte[] encode(T message) {
        if (message instanceof Ack){
            short msgOpcode= ((Ack) message).getOpcode();
            byte[] bytesArr=shortToBytes((short)10,msgOpcode);
            if (message instanceof FollowAck){
                bytesArr=((FollowAck) message).stringToBytes(bytesArr);
            }
            if(message instanceof UserListAck){
                bytesArr=((UserListAck) message).stringToByte(bytesArr);
            }
            if(message instanceof StatAck){
                bytesArr=((StatAck) message).getBytesArr(bytesArr);
            }
            return bytesArr;
        }
        if (message instanceof Error){
            short msgOpcode= ((Error) message).getOpcode();
            byte[] bytesArr=shortToBytes((short)11,msgOpcode);
            return bytesArr;
        }
        if (message instanceof Notification){
            return ((Notification) message).getBytesArr();
        }
        return new byte[0];
    }


    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len]=nextByte;
        len++;
    }


    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        return result;
    }


    private byte[] shortToBytes(short opcode, short msgOpcode) {
        byte[] bytesArr1 = new byte[2];
        bytesArr1[0] = (byte)((opcode >> 8) & 0xFF);
        bytesArr1[1] = (byte)(opcode & 0xFF);
        byte[] bytesArr2 = new byte[2];
        bytesArr2[0] = (byte)((msgOpcode >> 8) & 0xFF);
        bytesArr2[1] = (byte)(msgOpcode & 0xFF);
        byte[] bytesArr = new byte[4];
        bytesArr[0]=bytesArr1[0];
        bytesArr[1]=bytesArr1[1];
        bytesArr[2]=bytesArr2[0];
        bytesArr[3]=bytesArr2[1];
        return bytesArr;
    }


    private byte[] stringToBytes(String string){
        byte[] bytesArr1 = new byte[2];

        return new byte[0];
    }
}
