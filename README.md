# coupon-webflux
## 사용 기술
Java8, SpringBoot, Webflux, Gradle, Swagger
## 실행 방법
```shell
# application run 
./gradlew build
java -jar build/libs/coupon-0.0.1-SNAPSHOT.jar
```
Swagger : http://localhost:8080/swagger-ui.html#/
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