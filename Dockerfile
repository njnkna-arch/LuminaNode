FROM tomcat:10.1-jdk21
# MavenでビルドされたROOT.warをTomcatの実行フォルダにコピー
COPY target/ROOT.war /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]
