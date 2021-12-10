package bgu.spl.net.srv;

public class PM implements Message {
    private String recipient;
    private String content;

    public PM (String recipient, String content){
        this.recipient=recipient;
        this.content=content;
    }

    public String getRecipient(){
        return recipient;
    }

    public String getContent(){
        return content;
    }

    public short getOpcode(){
        return 6;
    }
}
