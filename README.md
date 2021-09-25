![tetris2](https://user-images.githubusercontent.com/12610035/131299100-0bff9fd9-d7d6-4e7d-b3b4-5bfda6addf8f.gif)  
* 회전시 벽이나 쌓여 있는 블럭 통과 안 하고 회전축 밀리도록 구현  

## 요구사항
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
4-2. 매번 동일한 위치에서 게임 화면 갱신될 수 있도록 쉘에 출력된 행 삭제하는 쉘 스크립트 작성    

## SEVER 요구사항  

* 접속 Phase  
1. 서버 접속(= 방입장)  
2. 각 유저로 부터 's' 입력 받으면 'ready' 화면에 뿌려줌(채팅상태)  
3. 둘 다 ready 상태가 되면 게임 실행 명령 전송  

* 게임 Phase  
1. 쉘창 tty 옵션 설정 및 게임 시작  
1-1. 클라이언트에서 키값 입력하면 서버에 안 보내야 한다  
2. 한 줄 완료시 서버에 알림  
3-1. 상대방에게 한줄 밑에 깔도록 명령 전송  
3-2. 상대방은 밑에 새로운 줄이 깔림  
3-3. 메세지 큐  
