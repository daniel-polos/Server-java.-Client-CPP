//
// Created by sardi@wincs.cs.bgu.ac.il on 1/2/19.
//

#include <stdlib.h>
#include <connectionHandler.h>
#include <thread>
//#include "EncoderDecoder.h"

using namespace std;

class WriteThread {
private:
    ConnectionHandler &connectionHandler;
public:
    WriteThread(ConnectionHandler &connectionHandler) : connectionHandler(connectionHandler) {}

    void operator()() {
        while (connectionHandler.getLoginStatus()) {
            string input = "";
            getline(std::cin, input);
            string opcode = input.substr(0, input.find(' '));
            char bytesArr[2];
            if (opcode == "REGISTER" || opcode == "LOGIN") {
                if (opcode == "REGISTER")
                    shortToBytes(1, bytesArr);
                else
                    shortToBytes(2, bytesArr);
                connectionHandler.sendBytes(bytesArr, 2);
                input = input.substr(input.find(' ') + 1); // left: "<username> <password>"
                string username = input.substr(0, input.find(' ')); //
                string password = input.substr(input.find(' ') + 1);
                connectionHandler.sendLine(username);
                connectionHandler.sendLine(password);
            }
            if (opcode == "LOGOUT") {
                shortToBytes(3, bytesArr);
                connectionHandler.sendBytes(bytesArr, 2);
                while(connectionHandler.getWhileStoper()){

                }
                connectionHandler.setWhileStopper(true);
            }
            if (opcode == "FOLLOW") {
                shortToBytes(4, bytesArr);
                connectionHandler.sendBytes(bytesArr, 2);
                input = input.substr(input.find(' ') +1); // left: "<0/1 (Follow/Unfollow)> <NumberOfUsers> <UserName_1> … <UserName_n>"
                char folllowStatus = input[0];
                input = input.substr(2); // left: "<NumberOfUsers> <UserName_1> … <UserName_n>"
                string numOfUsersStr = input.substr(0, input.find(' ')); // the numOfUsers can be >9 (more than one digit)
                short numOfUsers = stoi(numOfUsersStr);
                char bytesArr1[3];
                bytesArr1[0] = folllowStatus;
                bytesArr1[1] = ((numOfUsers >> 8) & 0xFF);
                bytesArr1[2] = (numOfUsers & 0xFF);
                connectionHandler.sendBytes(bytesArr1, 3);
                string usernameList = input.substr(input.find(' ') + 1); // usernameList: "<UserName_1> … <UserName_n>"
                while ((int)usernameList.find(' ') != -1) {
                    string userName= usernameList.substr(0,usernameList.find(' '));
                    connectionHandler.sendLine(userName);
                    usernameList = usernameList.substr(usernameList.find(' ')+1);
                }
                connectionHandler.sendLine(usernameList);
            }
            if (opcode == "POST") {
                shortToBytes(5, bytesArr);
                connectionHandler.sendBytes(bytesArr, 2);
                input = input.substr(input.find(' '));
                connectionHandler.sendLine(input);
            }
            if (opcode == "PM") {
                shortToBytes(6, bytesArr);
                connectionHandler.sendBytes(bytesArr, 2);
                input=input.substr(3);
                string username = input.substr(0, input.find(' '));
                connectionHandler.sendLine(username);
                string content = input.substr(input.find(' '));
                connectionHandler.sendLine(content);
            }
            if (opcode == "USERLIST") {
                shortToBytes(7, bytesArr);
                connectionHandler.sendBytes(bytesArr, 2);
            }
            if (opcode == "STAT") {
                shortToBytes(8, bytesArr);
                connectionHandler.sendBytes(bytesArr, 2);
                string username = input.substr(input.find(' ')+1);
                connectionHandler.sendLine(username);
            }
        }
    }

    void shortToBytes(short num, char bytesArr[]) {
        bytesArr[0] = ((num >> 8) & 0xFF);
        bytesArr[1] = (num & 0xFF);
    }
};

class ReadThread {
private:
    ConnectionHandler &connectionHandler;
    bool isLogout = false;
public:
    ReadThread(ConnectionHandler &connectionHandler1) : connectionHandler(connectionHandler1) {}

    void operator()() {
        while (connectionHandler.getLoginStatus()) {
            char bytesArr[2];
            connectionHandler.getBytes(bytesArr, 2);
            short opcode = bytesToShort(bytesArr);
            string output = "";
            // NOTIFICATION
            if (opcode == 9) {
                output += "NOTIFICATION ";
                char bytesArr1[1];
                connectionHandler.getBytes(bytesArr1, 1);
                if(bytesArr1[0]==0x00){
                    output+="PM ";
                }
                else if (bytesArr1[0]==0x01){
                    output += "Public ";
                }
                string postingUser = "";
                connectionHandler.getLine(postingUser);
                output += postingUser.substr(0,postingUser.size()-1);
                string content = "";
                connectionHandler.getLine(content);
                output += content.substr(0,content.size()-1);
            }
            // ACK
            if (opcode == 10) {
                output += "ACK ";
                char messageOpcode[2];
                connectionHandler.getBytes(messageOpcode, 2);
                short msgOpcode = bytesToShort(messageOpcode);
                output += to_string(msgOpcode); // "ACK X"
                // LOGOUT ACK
                if (msgOpcode == 3) {
                    connectionHandler.setWhileStopper(false);
                    isLogout = true;
                    connectionHandler.setLoginStatus(false);
                }
                // FOLLOW ACK
                if (msgOpcode == 4) {
                    char numOfUsers[2];
                    connectionHandler.getBytes(numOfUsers, 2);
                    short numberOfUsers = bytesToShort(numOfUsers);
                    output += " " + to_string(numberOfUsers) + " "; // "ACK X Y "
                    string usernameList = "";
                    for (int i=0;i<numberOfUsers;i++){
                        connectionHandler.getLine(usernameList);
                        output += usernameList; // "ACK X Y <user1> ... <userY>"
                        output[output.size()-1]=' ';
                        usernameList = "";
                    }
                    for (int i=i;i<(int)output.size();i++){
                        if (output[i]=='\0')
                            output[i]=' ';
                    }
                    output = output.substr(0,output.size()-1);
                }
                // USERLIST ACK
                if (msgOpcode == 7) {
                    char numOfUsers[2];
                    connectionHandler.getBytes(numOfUsers, 2);
                    short numberOfUsers = bytesToShort(numOfUsers);
                    output += " " + to_string(numberOfUsers) + " "; // "ACK X Y "
                    string usernameList = "";
                    for (int i=0;i<numberOfUsers;i++){
                        connectionHandler.getLine(usernameList);
                        output += usernameList;// "ACK X Y <user1> ... <userY>"
                        output[output.size()-1]=' ';
                        usernameList="";
                    }
                    for (int i=i;i<(int)output.size();i++){
                        if (output[i]=='\0')
                            output[i]=' ';
                    }
                    output = output.substr(0,output.size()-1);
                }
                // STAT ACK
                if (msgOpcode == 8) {
                    char numPosts[2];
                    connectionHandler.getBytes(numPosts, 2);
                    short numberPosts = bytesToShort(numPosts);
                    output += " " + to_string(numberPosts); // "ACK X Y"
                    char numFollowers[2];
                    connectionHandler.getBytes(numFollowers, 2);
                    short numberFollowers = bytesToShort(numFollowers);
                    output += " " + to_string(numberFollowers); // "ACK X Y Z"
                    char numFollowing[2];
                    connectionHandler.getBytes(numFollowing, 2);
                    short numberFollowing = bytesToShort(numFollowing);
                    output += " " + to_string(numberFollowing); // "ACK X Y Z W"
                }
            }
            // ERROR
            if (opcode == 11) {
                output += "ERROR ";
                char messageOpcode[2];
                connectionHandler.getBytes(messageOpcode, 2);
                short msgOpcode = bytesToShort(messageOpcode);
                output += to_string(msgOpcode);
                if(msgOpcode==3){
                    connectionHandler.setWhileStopper(false);
                }
            }
            for (int i=i;i<(int)output.size();i++){
                if (output[i]=='\0')
                    output[i]=' ';
            }
            cout << output << endl;
            if (isLogout) {
                connectionHandler.setLoginStatus(false);
            }
        }
    }

    short bytesToShort(char *bytesArr) {
        short result = (short) ((bytesArr[0] & 0xff) << 8);
        result += (short) (bytesArr[1] & 0xff);
        return result;
    }

    void changeChar (string str, char oldC, char newC){
        for (int i=i;i<(int)str.size();i++){
            if (str[i]==oldC)
                str[i]=newC;
        }
    }
};


int main (int argc, char *argv[]) {
//    if (argc < 3) {
//        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
//        return -1;
//    }
//    std::string host = argv[1];
//    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(argv[1], atoi(argv[2]));
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << "127.0.0.1" << ":" << 7777 << std::endl;
        return 1;
    }
    ReadThread readThread(ref(connectionHandler));
    WriteThread writeThread(ref(connectionHandler));

    std::thread writeT(std::ref(writeThread));
    std::thread readT(std::ref(readThread));

    readT.join();
    writeT.join();
    return 0;
}




