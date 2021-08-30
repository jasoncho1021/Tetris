![tetris2](https://user-images.githubusercontent.com/12610035/131299100-0bff9fd9-d7d6-4e7d-b3b4-5bfda6addf8f.gif)  
* 회전시 벽이나 쌓여 있는 블럭 통과 안 하고 회전축 밀리도록 구현  

## 요구사항
1. 생산자-소비자 큐 형태로 인풋 값 처리  
1-1. thread 상태 조작을 통한(wait, notifyAll) blocking queue 구현  

2. 어노테이션 스캔으로 테트리스 블럭 모양 수집  
2-1. Jar 파일 내에서도 스캔 가능하도록 FileSystem 처리  

3. 우분투 쉘 플레이  
3-1. 키값 enter 키 없이 바로 입력 받을 수 있도록 쉘 옵션 설정  
3-2. 쉘 창 지우는 스크립트 작성하여 실행

4. 예외전환 전략을 통한 글로벌 예외처리 
