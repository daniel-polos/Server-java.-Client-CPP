package bgu.spl.net.srv;

public class Error implements Message{
    private short opcode;

    public Error(short opcode) {
        this.opcode = opcode;
    }

    public short getOpcode(){
        return opcode;
    }

}
