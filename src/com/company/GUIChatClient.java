package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GUIChatClient extends JFrame implements ActionListener {

    private JPanel GUIChatClient;
    // 화면 정보 - 접속창
    JButton btn_connect; //접속버튼
    JTextField txt_server_ip; // IP 입력창
    JTextField txt_name; // 접속할 이름(아이디)

    //화면정보  - 채팅창
    JButton btn_exit; // 채팅창에서 종료버튼
    JButton btn_send; // 채팅창에서 보내기버튼
    JTextArea txt_list; //메세지 출력되는 곳
    JTextField txt_input; //입력메세지

    CardLayout card;
    //채팅(통신)을 하기위한 정보

    //접속할 서버의 IP주소
    String IPAddress;
    //포트번호
    static final int PORT = 5000;
    //클라이언트 소켓
    Socket client = null;
    //데이터 입출력스트림 객체
    ObjectInputStream ois;
    ObjectOutputStream oos;
    //아이디
    String user_id;
    //서버에서 보낸 메세지를 받기 위한 쓰레드 객체
    ReciveDataThread rdt;

    public GUIChatClient() {
        super("채팅 클라이언트");
        card = new CardLayout();
        setLayout(card);

        //접속화면
        JPanel connect = new JPanel();
        connect.setBackground(Color.orange);
        connect.setLayout(new BorderLayout());

        //접속창 상단
        connect.add(new JLabel("다중 채팅 접속창", JLabel.CENTER), BorderLayout.NORTH);

        //접속창 센터
        JPanel connect_sub = new JPanel();
        connect_sub.setBackground(Color.pink);
        connect_sub.add(new JLabel("서버 아이피 :   "));
        txt_server_ip = new JTextField("127.0.0.1", 15);
        connect_sub.add(txt_server_ip);
        connect_sub.add(new JLabel(" 접속 아이디(대화명):    "));
        txt_name = new JTextField("사용자", 15);
        connect_sub.add(txt_name);


        connect.add(connect_sub, BorderLayout.CENTER);
        //접속창 하단
        btn_connect = new JButton("채팅 서버 접속");
        connect.add(btn_connect, BorderLayout.SOUTH);

        //접속시 버튼 클릭시 이벤트 처리
        btn_connect.addActionListener(this);

        //접속 버튼 클릭시 이벤트처리


//        add(connect);
        //채팅창 화면
        JPanel chat = new JPanel();
        chat.setLayout(new BorderLayout());
        chat.setBackground(Color.CYAN);
        //상단
        chat.add(new JLabel("채팅 프로그램 v1.0", JLabel.CENTER), BorderLayout.NORTH);
        //센터
        txt_list = new JTextArea();
        chat.add(txt_list, BorderLayout.CENTER);
        //하단
        JPanel chat_sub = new JPanel();
        //패널 chat_sub 절대 크기를 지정
        chat_sub.setPreferredSize(new Dimension(500, 60));

        txt_input = new JTextField();
        btn_send = new JButton("전송");
        btn_exit = new JButton("종료");

        //메세지 입력창 이벤트 리스너 연
        txt_input.addActionListener(this);


        //버튼 이벤트 리스너 연결
        btn_send.addActionListener(this);
        btn_exit.addActionListener(this);

        chat_sub.add(txt_input);
        chat_sub.add(btn_send);
        chat_sub.add(btn_exit);

        chat.add(chat_sub, BorderLayout.SOUTH);

        add(connect, "접속창");
        add(chat, "채팅창");

        card.show(this.getContentPane(), "접속창");
//        card.show(this.getContentPane(),"채팅창");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(700, 300, 300, 300);
        setVisible(true);

    }// 생성자

    // 접속에 필요한 동작 : 서버 아이피, 포트번호 소켓 생성 + 사용자 아이디 전송
    void init() throws IOException {
        //서버 아이피 주소 가져오기
        IPAddress = txt_server_ip.getText();
        // 서버 소켓 생성
        client = new Socket(IPAddress, PORT);

        //서버와 입출력 처리위한 입출력 스트림
        oos = new ObjectOutputStream(client.getOutputStream());

        ois = new ObjectInputStream(client.getInputStream());

        //사용자 일므을 서버로 전송
        user_id = txt_name.getText();
        oos.writeObject(user_id);
        oos.flush();

        //서버가 보낸 메세지를 받기(수신)
        rdt = new ReciveDataThread();
        Thread th = new Thread(rdt);
        th.start();
        System.out.println("클라인트는 서버의 메세지를 수신 대기중");

        //화면 전환
        card.show(this.getContentPane(), "채팅창");
        txt_input.requestFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        //서버 접속버튼 클릭
        if (obj == btn_connect) {
            System.out.println("접속버튼");
//                card.show(this.getContentPane(),"채팅창");
            try {
                init();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        //채팅창 - 종료 버튼 클릭
        else if (obj == btn_exit) {
            System.out.println("종료버튼");
            System.exit(0);
        }
        //채팅창 - 전송 버튼 클릭 or 입력창에서 엔터클릭
        else if (obj == btn_send || obj == txt_input) {
            System.out.println("전송버튼");
            //메세지를 입력받아서 서버로 전송(서버가 모든 클라이언트한테 방송)
            String sendData = txt_input.getText();

            try {
            //서버로 전송
                oos.writeObject(sendData);
                oos.flush();
                //채팅창에 남아있는 전송된 메세지 초기화
                txt_input.setText("");
                txt_input.requestFocus();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        } else {
            System.out.println("에러! 해당버튼 없음");

        }
    } //actionPerformed

    //서버가 보낸 메세지를 받는 클래스 구현(내부클래스  - 함수처럼 사용됨)
    class ReciveDataThread implements Runnable {
        //서버에서 보낸 메세지
        String receiveData;

        @Override
        public void run() {
            try {
                while (true) {
                //서버 매세지를 받아서
                receiveData = (String) ois.readObject();
                //클라이언트 채팅창에 추가
                txt_list.append(receiveData + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }


    public static void main(String[] args) {
        new GUIChatClient();
    }


}
