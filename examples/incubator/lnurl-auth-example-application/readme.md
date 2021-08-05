🗲 lnurl-auth-example-application
===

A lnurl-auth example application.

<p align="center">
    <img src="https://github.com/theborakompanioni/bitcoin-spring-boot-starter/raw/master/examples/incubator/lnurl-auth-example-application/docs/assets/images/screenshot.png" alt="Screenshot" />
</p>

Start the application with
```shell
./gradlew -p examples/incubator/lnurl-auth-example-application bootRun
```

Example console output:
```
2021-07-27 13:33:13.626  INFO 1221814 --- [  restartedMain] o.t.l.l.e.LnurlAuthExampleApplication    : Starting LnurlAuthExampleApplication using Java 11.0.11 on localhost.localdomain with PID 1221814 (/home/void/workspace/theborakompanioni/bitcoin-spring-boot-starter/examples/incubator/lnurl-auth-example-application/build/classes/java/main started by void in /home/void/workspace/theborakompanioni/bitcoin-spring-boot-starter/examples/incubator/lnurl-auth-example-application)
2021-07-27 13:33:14.841  INFO 1221814 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2021-07-27 13:33:14.849  INFO 1221814 --- [  restartedMain] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-07-27 13:33:14.850  INFO 1221814 --- [  restartedMain] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.50]
2021-07-27 13:33:14.913  INFO 1221814 --- [  restartedMain] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2021-07-27 13:33:14.914  INFO 1221814 --- [  restartedMain] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1241 ms
2021-07-27 13:33:17.724  INFO 1221814 --- [  restartedMain] org.berndpruenster.netlayer.tor.Tor      : Starting Tor
2021-07-27 13:33:21.382  INFO 1221814 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2021-07-27 13:33:21.524  INFO 1221814 --- [  restartedMain] o.t.l.l.e.LnurlAuthExampleApplication    : Started LnurlAuthExampleApplication in 8.305 seconds (JVM running for 8.974)
2021-07-27 13:33:21.562  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : =================================================
2021-07-27 13:33:21.562  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : ===== LNURL_AUTH ================================
2021-07-27 13:33:21.562  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : =================================================
2021-07-27 13:33:21.562  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : login page: https://myonionurl.onion/login
2021-07-27 13:33:21.563  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : =================================================
2021-07-27 13:33:21.565  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : example auth url: https://myonionurl.onion?tag=login&k1=90fcb971de936e7bb9a97015e537980e92f5c49f7550094edfc359be2feec270
2021-07-27 13:33:21.565  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : =================================================
```

# Resources
- lnurl RFC (GitHub): https://github.com/fiatjaf/lnurl-rfc
- Wallets supporting lnurl: https://github.com/fiatjaf/awesome-lnurl#wallets
- jMolecules (GitHub): https://github.com/xmolecules/jmolecules

