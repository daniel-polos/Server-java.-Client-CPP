package bgu.spl.net.srv;

public class Notification implements Message {
    //enum notificationType {PM, Public};
    private int type;
    public String postingUser;
    public String content;
    private short opcode;

    public Notification(String postingUser, String content,int type){
        this.postingUser=postingUser;
        this.content=content;
        this.type=type;

    }

    public short getOpcode(){
        return opcode;
    }

    public byte[] getBytesArr() {
        String tmpPostingUser = postingUser+'\0';
        String tmpContent = content+'\0';
        byte[] userBytes = tmpPostingUser.getBytes();
        byte[] userContent = tmpContent.getBytes();
        byte[] op = new byte[3+userBytes.length+userContent.length];
        op[0]=(byte)((9 >> 8) & 0xFF);
        op[1]=(byte)(9 & 0xFF);
        op[2]=(byte)type;
        for (int i=0;i<userBytes.length;i++){
            op[i+3]=userBytes[i];
        }
        for (int i=0;i<userContent.length;i++){
            op[i+3+userBytes.length]=userContent[i];
        }
        return op;
    }

}
