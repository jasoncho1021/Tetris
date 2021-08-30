![tetris2](https://user-images.githubusercontent.com/12610035/131299100-0bff9fd9-d7d6-4e7d-b3b4-5bfda6addf8f.gif)
## 요구사항
1. 어노테이션 스캔으로 테트리스 블럭 모양 수집  
1-1. Jar 파일 내에서도 스캔 가능하도록 FileSystem 처리

2. 생산자-소비자 큐 형태로 인풋 값 처리  
2-1. thread 상태 조작을 통한(wait, notifyAll) blocking 구현  

3. 우분투 쉘 플레이  
3-1. 키값 enter 키 없이 바로 입력 받기(non-blocking, polling)  
3-2. 쉘스크립트로 쉘 창 지우기  

4. 예외전환 전략을 통한 글로벌 예외처리 
