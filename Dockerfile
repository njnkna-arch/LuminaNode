# --- ステージ1: Mavenでビルドを行う（プログラムを組み立てる） ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# テストをスキップして、ROOT.warを作成する
RUN mvn clean package -DskipTests

# --- ステージ2: Tomcatで実行する（サーバーを動かす） ---
FROM tomcat:10.1-jdk21
# 前のステージで作成された ROOT.war を Tomcatの公開フォルダにコピー
COPY --from=build /app/target/ROOT.war /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]
