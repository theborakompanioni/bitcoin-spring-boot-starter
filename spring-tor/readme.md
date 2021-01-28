spring-tor
===

A module containing a spring boot starter for an embedded [Tor daemon](https://www.torproject.org/).
The starter will automatically expose your application as hidden service!
Easily create hidden service sockets programmatically within your Spring Boot application.

A common configuration can look like this:
```yaml
org.tbk.tor:
  enabled: true  # whether auto-config should run - default is `true`
  auto-publish-enabled: true # auto publish the web port as hidden service - default is `true`
  working-directory: 'my-tor-directory' # the working directory for tor - default is `tor-working-dir`
  startup-timeout: 30s # max startup duration for tor to successfully start - default is `60s`
```

Start the example application with
```shell
./gradlew -p spring-tor/spring-tor-example-application bootRun
```
Example output (2021-01-21):
```
2021-01-21 01:23:30.035  INFO 313251 --- [  restartedMain] org.berndpruenster.netlayer.tor.Tor      : Starting Tor
2021-01-21 01:23:33.490  INFO 313251 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2021-01-21 01:23:33.511  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : Started TorExampleApplication in 8.417 seconds (JVM running for 8.972)
2021-01-21 01:23:33.605  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : =================================================
2021-01-21 01:23:33.606  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : url: http://<your_onion_url>.onion:80
2021-01-21 01:23:33.607  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : virtual host: <your_onion_url>.onion
2021-01-21 01:23:33.607  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : virtual port: 80
2021-01-21 01:23:33.607  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : host: 127.0.0.1
2021-01-21 01:23:33.607  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : port: 8080
2021-01-21 01:23:33.607  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : directory: /home/tbk/workspace/spring-boot-bitcoin-starter/spring-tor/spring-tor-example-application/tor-working-dir/spring_boot_app
2021-01-21 01:23:33.608  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : -------------------------------------------------
2021-01-21 01:23:33.608  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : run: torsocks -p 46735 curl http://<your_onion_url>.onion:80/index.html -v
2021-01-21 01:23:33.608  INFO 313251 --- [  restartedMain] o.t.t.s.example.TorExampleApplication    : =================================================
```


# Resources
- netlayer (GitHub): https://github.com/JesusMcCloud/netlayer
- Bisq usage of netlayer: https://github.com/bisq-network/bisq/blob/master/p2p/src/main/java/bisq/network/p2p/network/NewTor.java