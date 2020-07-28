package com.company;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

// 클라이언트 접속을 유지하면서, 데이터 송수신
// 클라이언트가 서버에 접속할때 마다 해당 객체 생
public class ChatServerThread implements Runnable {

    // 클라이언트와 통신하는 소켓
    Socket child;
    // 데이터 송/수신 객체
    ObjectInputStream ois;
    ObjectOutputStream oos;

    //사용자ID
    String user_id;
    //쓰레드간의 데이터를 공유
    HashMap<String, ObjectOutputStream> hm;

    // 객체 생성시 소켓정보, 해쉬 맵 정보를 가져오기
    public ChatServerThread() {
    }

    public ChatServerThread(Socket s, HashMap hm) {
        //클라이언트 접속 IP주소를 출력(서버확인용)
        child = s;
        this.hm = hm;

        System.out.println(child.getInetAddress() + "로 부터 연결 요청 받음");

        //데이터 송수신을 위한 스트림 객체 생성
        try {
            ois = new ObjectInputStream(child.getInputStream());
            oos = new ObjectOutputStream(child.getOutputStream());

            //사용자의 아이디 정보를 가져와서 출력
            user_id = (String) ois.readObject();

            //서버에 접속되어있는 (방에 있는) 모든 클라이언트에게 전달(브로드캐스트)
            // xxx님이 접속 하셨습니다.

            broadCast(user_id+"님이 로그인하였습니다.");

            System.out.println("접속한 클라이언트 아이디 : " + user_id);

            //여러 클라이언트에게 공유되는 데이터를 동기화 처리
            //서버가 접속하는 클라이언트 정보를 저장하는 공간 hashMap을 동기화
            synchronized (hm) {
                //해쉬맵에 아이디/출력스트림 저장
                //모든 접속된 클아이언트가 공유해야하는 값이기 때문에 동기화처리가 필요하다.
                hm.put(this.user_id, oos);

            }

            //사용자의 아이디 정보를 가져와서 출력
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        // 전달받는 메세지
        String receiveData;

        try {
            //클라이언트로 부터 메세지 수신
            receiveData = (String) ois.readObject();

            //받은 메세지를 모든 클라이언트한테 전달(브로드 캐스트)
            broadCast(user_id + " : "+ receiveData);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("클라이언트 요청 종료(강제 종료)");
        } finally {
            // 사용자 종료시 hm저장된 정보를 제거
            synchronized (hm) {
                // 사용자 ID를 사용해서 저장된 정보를 제거
                hm.remove(user_id);
            }
            //사용자가 채팅방에서 나간사실을 알려주기(브로드캐스트)
            broadCast(user_id+ " 님이 대화방을 나갔습니다!!");
            //서버에서 확인용
            System.out.println(user_id + "님이 나갔습니다.");
            try {
                if (child != null) {
                    child.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }//finally
    }//run

    //broadCast 방송 : 메세지를 전달받아서 모든 클라이언트에게 채팅방에 전달
    public void broadCast(String message) {
        //사용자의 정보를 저장하는 HashMap 동기화해서
        //출력정보를 사용해서 메시지 전달
        synchronized (hm) {
            //해쉬맵에 저장된 모든 출력스트림을 사용해서
            //메세지 전달
            try {
                for (ObjectOutputStream oos : hm.values()) {
                    oos.writeObject(message);
                    oos.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}//class
