spring-testcontainer-tor-starter
===

Try running the [spring-testcontainer-tor-example-application](../spring-testcontainer-tor-example-application) for more information!
Start the application with
```shell
./gradlew -p spring-testcontainer/spring-testcontainer-tor-example-application bootRun
```
Example console output:
```
2021-01-03 14:51:17.300  INFO 66700 --- [  restartedMain] t.s.t.t.e.TorContainerExampleApplication : Started TorContainerExampleApplication in 3.844 seconds (JVM running for 4.627)
2021-01-03 14:51:17.369  INFO 66700 --- [  restartedMain] t.s.t.t.e.TorContainerExampleApplication : =================================================
2021-01-03 14:51:22.541  INFO 66700 --- [  restartedMain] t.s.t.t.e.TorContainerExampleApplication : Tor is enabled.
2021-01-03 14:51:22.542  INFO 66700 --- [  restartedMain] t.s.t.t.e.TorContainerExampleApplication : =================================================
2021-01-03 14:51:22.951 DEBUG 66700 --- [  restartedMain] .t.s.t.t.c.TorContainerAutoConfiguration : my_spring_application_8080 => kizeqfbins5mp3pdsvcn2baqaqfmrvv7fx5gak2flfmagffhpetkjiqd.onion
```

Now you can access your application either through your local tor instance or from the container
(see `docker ps | grep 'tbk-testcontainer-btcpayserver-tor'` to find the mapped tor port 9050). e.g.:

```
$ docker ps | grep 'tbk-testcontainer-btcpayserver-tor'
10b7f0799016        btcpayserver/tor:0.4.2.7    "./entrypoint.sh tor"     2 hours ago         Up 2 hours          0.0.0.0:33243->9050/tcp, 0.0.0.0:33242->9051/tcp   tbk-testcontainer-btcpayserver-tor-5297fbc
```

```
$ torsocks -p 33243 curl kizeqfbins5mp3pdsvcn2baqaqfmrvv7fx5gak2flfmagffhpetkjiqd.onion
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>Tor Container Example Application</title>
</head>
<body>
<h1>It works!</h1>
</body>
</html>
```

# Resources
- docker btcpayserver/tor (GitHub): https://github.com/btcpayserver/dockerfile-deps/tree/master/Tor
- Set up Your Onion Service: https://community.torproject.org/onion-services/setup/