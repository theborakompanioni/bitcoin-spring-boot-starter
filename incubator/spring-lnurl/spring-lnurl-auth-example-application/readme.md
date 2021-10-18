🗲 lnurl-auth-example-application
===

A lnurl-auth example application.

- uses [spring-lnurl-auth-security](https://github.com/theborakompanioni/bitcoin-spring-boot-starter/tree/master/incubator/spring-lnurl)
  with default login page
- uses [jMolecules](https://github.com/xmolecules/jmolecules) for DDD
- uses [sqlite](https://sqlite.org) database for persistence
- uses in-memory k1 cache (reboots invalidate previous login challenges)
- automatically registers an `.onion` url

<p align="center">
    <img src="https://github.com/theborakompanioni/bitcoin-spring-boot-starter/raw/master/incubator/spring-lnurl/docs/assets/images/screenshot.png" alt="Screenshot" />
</p>

### Run
In order for lnurl-auth to work you must either serve your app over `https` (no self-signed cert allowed) or over tor.
Currently, there is only [Simple Bitcoin Wallet](https://github.com/btcontract/wallet/) that natively supports `onion` addresses.
Other wallets will probably support it in the near future.

Serving your app with `https` during development can be done with [ngrok](https://ngrok.com/):
```shell
./ngrok http 8080
# Forwarding  https://abcdef012345.ngrok.io -> http://localhost:8080 
```

Make sure to adapt the application configuration accordingly:
```yml
app:
  # use your own url here (e.g. https://myapp.ngrok.io)
  lnurl-auth-base-url: https://abcdef012345.ngrok.io
```

Start the application with
```shell
./gradlew -p incubator/spring-lnurl/spring-lnurl-auth-example-application bootRun
```

Example console output:
```
2021-07-27 13:33:13.626  INFO 1221814 --- [  restartedMain] o.t.l.l.e.LnurlAuthExampleApplication    : Starting LnurlAuthExampleApplication using Java 11.0.11 on localhost.localdomain
2021-07-27 13:33:17.724  INFO 1221814 --- [  restartedMain] org.berndpruenster.netlayer.tor.Tor      : Starting Tor
2021-07-27 13:33:21.382  INFO 1221814 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2021-07-27 13:33:21.524  INFO 1221814 --- [  restartedMain] o.t.l.l.e.LnurlAuthExampleApplication    : Started LnurlAuthExampleApplication in 8.305 seconds (JVM running for 8.974)
2021-07-27 13:33:21.562  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : =================================================
2021-07-27 13:33:21.562  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : ===== LNURL_AUTH ================================
2021-07-27 13:33:21.565  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : example auth url: https://abcdef012345.ngrok.io?tag=login&k1=90fcb971de936e7bb9a97015e537980e92f5c49f7550094edfc359be2feec270
2021-07-27 13:33:21.572  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : ===== TOR IS ENABLED ============================
2021-07-27 13:33:21.573  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : onion login: http://abcdef0123456789abcdef0123456789abcdef0123456789abcdef42.onion/login
2021-07-27 13:33:21.573  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : =================================================
```

# Resources
- lnurl RFC (GitHub): https://github.com/fiatjaf/lnurl-rfc
- Wallets supporting lnurl: https://github.com/fiatjaf/lnurl-rfc#lnurl-documents
- Simple Bitcoin Wallet (GitHub): https://github.com/btcontract/wallet/
- Lightning Login Buttons (GitHub): https://github.com/dergigi/lightning-buttons
- jMolecules (GitHub): https://github.com/xmolecules/jmolecules
- sqlite (GitHub): https://github.com/sqlite/sqlite
- ngrok Website: https://ngrok.com

