# coupon-webflux
## 사용 기술
Java8, SpringBoot, Webflux, MongoDB, Gradle, Swagger
## 실행 방법
```shell
# mongo
docker run -p 27017:27017 --name mongo_boot -d mongo

# application run 
./gradlew build
java -jar build/libs/coupon-0.0.1-SNAPSHOT.jar
```
Swagger : http://localhost:8080/swagger-ui.html#/  
    - /users/sigin api로 회원 가입 한 후 swagger 상단에 Authorize 버튼을 눌러 Bearer {제공받은 JWT 토큰} 추가
    
## 기능 정의
1. 랜덤한 코드의 쿠폰을 N개 생성하여 데이터베이스에 보관하는 API를 구현하세요.
    - 쿠폰-쿠폰번호 (1:N) 형태로 구현
2. 생성된 쿠폰중 하나를 사용자에게 지급하는 API를 구현하세요.
    - 지급하기 전 쿠폰이 만료되었는지 체크
3. 사용자에게 지급된 쿠폰을 조회하는 API를 구현하세요.
4. 지급된 쿠폰중 하나를 사용하는 API를 구현하세요. (쿠폰 재사용은 불가)
    - 사용하기 전 쿠폰이 만료되었는지 체크
    - 이미 사용한 쿠폰이면 Exception 발생
5. 지급된 쿠폰중 하나를 사용 취소하는 API를 구현하세요. (취소된 쿠폰 재사용 가능)
    - 사용되지 않은 쿠폰이면 Exception 발생
6. 발급된 쿠폰 중 당일 만료된 전체 쿠폰 목록을 조회하는 API를 구현하세요.
    - 당일이어도 현시간 이후에 만료되는 쿠폰들도 있기 때문에 당일 00:00 ~ 현재 시간 사이에 만료되는 쿠폰번호 조회
7. (선택) 발급된 쿠폰중 만료 3일전 사용자에게 메세지("쿠폰이 3일 후 만료됩니다.")를 발송하는 기능을 구현하 세요. (실제 메세지를 발송하는것이 아닌 stdout 등으로 출력하시면 됩니다.)
    - 3일 후 만료되는 쿠폰을 조회 하여 해당 하는 쿠폰 번호를 조회 하여 발급받은 유저들에게 메세지 발송 로그 남김
8. (선택) 10만개 이상 벌크 csv Import 기능 구현
    - 프로젝트 디렉토리 root path에 샘플 파일인 coupon-service.csv(10만건) 추가
    - csv파일을 한번에 다 읽어오면 메모리에 부담이 갈 수 있어 temp 파일을 생성 후 100건씩 읽어서 db에 insert 하도록 구현
    - Monogo DB는 4.0 이상부터 레플리카 구성시 트랜잭션 가능하지만, 과제 제출시 구성을 추가 할 수 없어 현재는 트랜잭션처리가 되지 않음
    - ReactiveMongoTemplate을 사용하면 bulk insert 가 되지만 upsert가 되지 않기 때문에 편의상 ReactiveMongoRepository로 구현
9. (선택) PI 인증을 위해 JWT(Json Web Token)를 이용해서 Token 기반 API 인증 기능을 개발하고 각 API 호출 시에 HTTP Header에 발급받은 토큰을 가지고 호출하세요.
    - signup 계정생성 API: ID, PW를 입력 받아 내부 DB에 계정을 저장하고 토큰을 생성하여 출력한다.
        - 단, 패스워드는 안전한 방법으로 저장한다. -> SHA-256으로 저장
    - signin 로그인 API: 입력으로 생성된 계정 (ID, PW)으로 로그인 요청하면 토큰을 발급한다.