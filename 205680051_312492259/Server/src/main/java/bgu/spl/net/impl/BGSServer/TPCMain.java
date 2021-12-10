package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.api.bidi.MessageEncoderDecoderImpl;
import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main(String[] args){
        DataBase dataBase=new DataBase();
        Server.threadPerClient(7777, ()->new BidiMessagingProtocolImpl(dataBase), MessageEncoderDecoderImpl::new).serve();
    }
}