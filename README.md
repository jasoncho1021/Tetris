## [v1.0] 기본 게임  
![tetris2](https://user-images.githubusercontent.com/12610035/131299100-0bff9fd9-d7d6-4e7d-b3b4-5bfda6addf8f.gif)  
* 회전시 벽이나 쌓여 있는 블럭 통과 안 하고 회전축 밀리도록 구현  

1. 생산자-소비자 큐 형태로 인풋 값 처리  
https://github.com/jasoncho1021/Tetris/blob/main/queue/impl/InputQueue.java#L39  
1-1. 생산자 둘 (1초 마다 DOWN 키값 입력하는 스레드, 콘솔 입력 스레드), 소비자 하나 (main 스레드)  
1-2. thread 상태 조작을 통한(wait, notifyAll) blocking queue 구현  

2. 어노테이션 스캔으로 테트리스 블럭 등록  
https://github.com/jasoncho1021/Tetris/blob/main/block/container/BlockContainer.java#L29  
2-1. Jar 파일 내에서도 스캔 가능하도록 FileSystem 처리  

3. 예외전환 전략을 통한 글로벌 예외처리  
https://github.com/jasoncho1021/Tetris/blob/main/TetrisGame.java#L53  

4. 우분투 쉘 플레이 설정  
4-1. enter 키 없이 바로 키값 입력 받을 수 있도록 쉘 옵션 설정  
4-2. 매번 동일한 위치에서 게임 화면 갱신될 수 있도록 쉘에 출력된 행 삭제하는 쉘 스크립트 작성 및 자바프로그램에서 실행  

## [v2.0] 멀티플레이 (클라이언트에서 게임 로직 처리)
Java NIO server  

* 접속 Phase  
1. 서버 접속   
2. 채팅 기능 수행  
3. 'r' 누르면 게임 준비상태로 갱신, 모든 접속자가 'r' 누른 상태면 게임 시작  
4. 'z' 입력시 게임 중단  
5. 게임 종료 후 채팅 기능 다시 수행  
6. 'Q' 입력 받으면 클라이언트 프로그램 종료  

* 게임 Phase  
1. 쉘창 tty 옵션 설정 및 게임 시작  
1-1. 클라이언트에서 키값 입력하면 게임에서 바로 처리(채팅 x) 

2. 한 줄 완료시 서버에 알림  
2-1. 상대방에게 한줄 밑에 깔도록 명령 전송  
2-2. 상대방은 밑에 새로운 줄이 깔림  
2-3. Job 큐 구현하여 멀티스레드 요청 Job 순서대로 처리  
   ( Job : 서버로부터 수신한 한 줄 깔기 명령 수행, 키보드로 입력 받은 키값 수행 )  

## [v3.0] 멀티플레이 (서버에서 게임 로직 처리)  
TcpSocketServer 구현 및 로직 이관 완료    
* AttackRequestQueue 는 Singleton 패턴으로 생성되며 멀티스레드(게임) 간에 공유된다.  

![Screenshot from 2021-10-11 19-30-07](https://user-images.githubusercontent.com/12610035/136775768-2b24af4f-6558-47b0-a605-af5b2fe526f9.png)  


## [v3.1] WebSocket 서버  
1. 포트 추가하여 WebSocket handshake 서버 구현 완료   
2. client html,js 코드 구현 완료    
3. 채팅 및 게임 플레이(공격기능) 동작 확인 완료  

![ttris22](https://user-images.githubusercontent.com/12610035/136033620-45d1f3dd-ceec-4dfa-86e9-c26c29c1d77b.gif)  


