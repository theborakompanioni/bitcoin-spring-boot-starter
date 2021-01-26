spring-testcontainer
===

This module contains a fast and easy way to start one or multiple instances of external services within 
docker containers programmatically directly from your application.
Please note, that **these modules are intended to be used in regtest mode only**.

### spring-testcontainer-bitcoind-starter
Start and run [Bitcoin Core](https://github.com/bitcoin/bitcoin) daemons.

This spring boot starter module can be used in combination with
[bitcoin-jsonrpc-client](#bitcoin-jsonrpc-client) and [bitcoin-zeromq-client](#bitcoin-zeromq-client).

### spring-testcontainer-lnd-starter
Start and run [lnd](https://github.com/LightningNetwork/lnd) daemons.

Currently, it **can only** be used in combination with [spring-testcontainer-bitcoind-starter](#spring-testcontainer-bitcoind-starter)!

### spring-testcontainer-electrumx-starter
Start and run [ElectrumX](https://github.com/spesmilo/electrumx) instances.

It can be used in combination with [spring-testcontainer-bitcoind-starter](#spring-testcontainer-bitcoind-starter)!

### spring-testcontainer-electrum-personal-server-starter
Start and run [Electrum Personal Server](https://github.com/chris-belcher/electrum-personal-server) instances.

It can be used in combination with [spring-testcontainer-bitcoind-starter](#spring-testcontainer-bitcoind-starter)!

### spring-testcontainer-electrum-daemon-starter
Start and run [Electrum](https://github.com/spesmilo/electrum) daemons. 

It can be used in combination with [spring-testcontainer-electrumx-starter](#spring-testcontainer-electrumx-starter)
and [spring-testcontainer-electrum-personal-server-starter](#spring-testcontainer-electrum-personal-server-starter)!

### spring-testcontainer-tor-starter
Start and run [Tor](https://www.torproject.org/) daemons. 
See [spring-testcontainer-tor-starter](spring-testcontainer/spring-testcontainer-tor-starter) or the
[example application](spring-testcontainer/spring-testcontainer-tor-example-application) for more information!

# Resources
- Testcontainers (GitHub): https://github.com/testcontainers/testcontainers-java/
- Testcontainers Documentation:
  - Creating a generic container: https://www.testcontainers.org/features/creating_container/
- Docker Documentation: 
  - Dockerfile reference: https://docs.docker.com/engine/reference/builder/
  - Env variables: https://docs.docker.com/engine/reference/commandline/cli/#environment-variables