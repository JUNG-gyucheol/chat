package com.company;

import com.mysql.fabric.Server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ChatServer {

    ServerSocket server;

    Socket child;

    static final int PORT = 5000;

    // 쓰레드가 공유할 데이터 hsahMap
    HashMap<String, ObjectOutputStream> hm;

    //서버 객체 생성시 서버 연결 후 대기
    public ChatServer(){
        //포트 지정 서버소켓 생성
        try {
            //소켓 객체 생성 실패시 프로세스 종료
            server = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("*****채팅 서버 OPEN*****");
        System.out.println("서버는 클라이언트 요청 대기중.....");

        //쓰레드(클라이언트 소켓)간의 데이터 공유 객체
       hm = new HashMap<String ,ObjectOutputStream>();
        //서버가 클라이언트 요청 대기
        try {
            while (true) {
                //서버가 클라이언트 요청 대기(무한루프)
                child = server.accept();

                // 사용자 접속과 상관없이 데이터 송수신 처리(멀티쓰레딩)
                ChatServerThread cst = new ChatServerThread(child,hm);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }


    public static void main(String[] args){
        new ChatServer();
    }

}
