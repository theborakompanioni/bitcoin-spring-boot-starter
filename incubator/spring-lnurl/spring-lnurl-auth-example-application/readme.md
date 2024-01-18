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
In order for lnurl-auth to work you must either serve your app over `https` (no self-signed cert allowed) or as Onion Service.
Currently, there is only [Simple Bitcoin Wallet][simple_bitcoin_wallet_github] that natively supports `onion` addresses (last checked 2021-07-27).
Other wallets will probably support it in the near future. Alternatively, you can use [lnpass][lnpass_homepage]
during development for testing or demonstrating the Lightning Login process.

#### Onion Service

Start the application with
```shell
./gradlew -p incubator/spring-lnurl/spring-lnurl-auth-example-application bootRun --args="--spring.profiles.active=development"
```

Example console output:
```
2021-07-27 13:33:13.626  INFO 1221814 --- [  restartedMain] o.t.l.l.e.LnurlAuthExampleApplication    : Starting LnurlAuthExampleApplication using Java 21.0.1
2021-07-27 13:33:17.724  INFO 1221814 --- [  restartedMain] org.berndpruenster.netlayer.tor.Tor      : Starting Tor
2021-07-27 13:33:21.382  INFO 1221814 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2021-07-27 13:33:21.524  INFO 1221814 --- [  restartedMain] o.t.l.l.e.LnurlAuthExampleApplication    : Started LnurlAuthExampleApplication in 8.305 seconds (JVM running for 8.974)
2021-07-27 13:33:21.562  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : =================================================
2021-07-27 13:33:21.572  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : ===== TOR IS ENABLED ============================
2021-07-27 13:33:21.573  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : onion login: http://abcdef0123456789abcdef0123456789abcdef0123456789abcdef42.onion/login
2021-07-27 13:33:21.573  INFO 1221814 --- [  restartedMain] .l.l.e.LnurlAuthExampleApplicationConfig : =================================================
```

Then visit the onion url in your Tor browser and log in with [lnpass][lnpass_homepage].

#### Clearnet

If you do not use Onion Services, serving your app with `https` during development can be done with [ngrok][ngrok_homepage]:
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

...or start the app with argument `app.lnurl-auth-base-url`:

Start the application with
```shell
./gradlew -p incubator/spring-lnurl/spring-lnurl-auth-example-application bootRun --args="--spring.profiles.active=development --app.lnurl-auth-base-url=https://abcdef012345.ngrok.io"
```


# Resources
- lnurl RFC (GitHub): https://github.com/fiatjaf/lnurl-rfc
- Wallets supporting lnurl: https://github.com/fiatjaf/lnurl-rfc#lnurl-documents
- lnpass: https://lnpass.github.io
- Simple Bitcoin Wallet (GitHub): https://github.com/btcontract/wallet/
- jMolecules (GitHub): https://github.com/xmolecules/jmolecules
- sqlite (GitHub): https://github.com/sqlite/sqlite
- ngrok Website: https://ngrok.com

[lnpass_homepage]: https://lnpass.github.io
[simple_bitcoin_wallet_github]: https://github.com/btcontract/wallet/
[ngrok_homepage]: https://ngrok.com/
