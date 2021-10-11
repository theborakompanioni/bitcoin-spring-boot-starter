bitcoin-electrum-gateway-example-application
===

A small demo application with bitcoin/electrumx/electrum in regtest mode.


1. Start application with
```shell
./gradlew -p examples/incubator/bitcoin-electrum-gateway-example-application bootRun
```

Example log output:
```
2021-05-16 15:12:24.480  INFO 684724 --- [chLoop STARTING] o.t.e.g.e.watch.ElectrumWalletWatchLoop  : start watching addresses: [bcrt1q9edm5yjs08xmczqxeaap4rvpr03z7f6gw9e3fu, bcrt1qr6dr2g6p0nlpkp5pmkn826v8q5ay33uvwxhzk6, bcrt1qwlgve3mcp73zwlutr37sh957a5zezp0n5yjlv0, bcrt1q9700en2u25q22crwv2zfy7ts9x86vnpuc2phne, bcrt1qtwcvnlhpde26nrnsqsq5cw6dx8d6et7htjzkw2, bcrt1qdn2k8xftjwpn2k355k56jfq7qtjrfccxwwazvs, bcrt1q2vrwuy4jqerpuj9q5t46xj3l7hwnt7zvymxk9k, bcrt1qv89f8cqruw3gxhtenl2pr7px6yqdt9cc3utr2q, bcrt1qs47d8fh3ed247zepgtg0mw496ju3j2g96l8lyf, bcrt1q0xtrupsjmqr7u7xz4meufd3a8pt6v553m8nmvz, bcrt1ql6nmvlw25n7gsky8g2rngxxre2tnsej5uaa88c, bcrt1qv4pfah6hhvesyxduc2y2mhrhsqa8dvf9tfnuct, bcrt1qxm44tl59hjdhddkljqgl5v4aua2a9mvzjggn95, bcrt1qyn8gxzw9l2cn494j4aydjqwca7a38dgsf9ncrx, bcrt1qmtqfd70rr3aw2auna3lkgemcdh2wes26s2p8cn, bcrt1qz6g2zuudrqn0c9d6mgy5zq93w5z652jdxrpv9f, bcrt1qt9hhzwlfhmvx8n6jwkknmqg60lewxmfgqaxv48, bcrt1qyzvsvntvzdxxmjewgx68rykz2zr3hdkdtlhcjy, bcrt1qzj07yl858frgvdjwqg9s6cqq60w8spzwxjxuwl, bcrt1q4gcuuny9vm3fhz7yh4qcacw93h8n9e782m5u0v, bcrt1qxlpjm3pazeehpw063ndlsdcsda72fwcc2xlp8k, bcrt1qf8vvfcw2839ah6scymd6savchr6csytu57t0e7, bcrt1qm3v86gxj9chjkmy5wt7utt7k675mvfrp9q9at9, bcrt1qj8kfa0dpza9ajfugqekk47wa6pcu29uyul06sx, bcrt1quj827vex8szr9f3dlec84tm9a4xunehrpnpzh5, bcrt1qawwue5c28c8vl0cl58hjph5gx5l6a6s8xep86g]
2021-05-16 15:12:25.148  INFO 684724 --- [  restartedMain] .e.g.e.ElectrumGatewayExampleApplication : Started ElectrumGatewayExampleApplication in 36.164 seconds (JVM running for 37.301)
2021-05-16 15:12:49.037  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.l.BitcoindStatusLogging          : ============================
2021-05-16 15:12:49.037  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.l.BitcoindStatusLogging          : Bitcoin Core (/Satoshi:0.21.1(tbk)/) Status
2021-05-16 15:12:49.038  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.l.BitcoindStatusLogging          : Chain: regtest
2021-05-16 15:12:49.038  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.l.BitcoindStatusLogging          : Connections: 0
2021-05-16 15:12:49.038  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.l.BitcoindStatusLogging          : Headers: 124
2021-05-16 15:12:49.038  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.l.BitcoindStatusLogging          : Blocks: 124
2021-05-16 15:12:49.038  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.l.BitcoindStatusLogging          : Best block hash: 3803167bad78422935efeef724ba33b7dfde9b6b069ae2b74b1ee170973be006
2021-05-16 15:12:49.038  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.l.BitcoindStatusLogging          : Difficulty: 0.0000000004656542373906925
2021-05-16 15:12:49.038  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.l.BitcoindStatusLogging          : UTXO: 111 (6200.00 BTC)
2021-05-16 15:12:49.039  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.l.BitcoindStatusLogging          : ============================
2021-05-16 15:12:49.051  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.e.l.ElectrumdStatusLogging       : ============================
2021-05-16 15:12:49.052  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.e.l.ElectrumdStatusLogging       : Electrum Daemon (3.3.8) Status
2021-05-16 15:12:49.052  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.e.l.ElectrumdStatusLogging       : Connected: true
2021-05-16 15:12:49.052  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.e.l.ElectrumdStatusLogging       : Blockheight: 121/121
2021-05-16 15:12:49.052  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.e.l.ElectrumdStatusLogging       : Current wallet: /home/electrum/.electrum/regtest/wallets/default_wallet
2021-05-16 15:12:49.052  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.e.l.ElectrumdStatusLogging       : Wallet synchronized: true
2021-05-16 15:12:49.086  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.e.l.ElectrumdStatusLogging       : Transactions: 124
2021-05-16 15:12:49.088  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.e.l.ElectrumdStatusLogging       : Balance: 5250.00149201 BTC total
2021-05-16 15:12:49.088  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.e.l.ElectrumdStatusLogging       :          250.00 BTC confirmed
2021-05-16 15:12:49.088  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.e.l.ElectrumdStatusLogging       :          0.00 BTC unconfirmed
2021-05-16 15:12:49.088  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.e.l.ElectrumdStatusLogging       :          250.00 BTC spendable
2021-05-16 15:12:49.088  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.e.l.ElectrumdStatusLogging       :          5000.00149201 BTC unmatured
2021-05-16 15:12:49.088  INFO 684724 --- [-pub-5905d1ef-0] o.t.b.r.e.l.ElectrumdStatusLogging       : ============================
2021-05-16 15:12:50.084  INFO 684724 --- [tchLoop RUNNING] .e.g.e.w.ElectrumDaemonWalletSendBalance : found end balance: 5250.00149201 BTC
2021-05-16 15:12:50.169  INFO 684724 --- [tchLoop RUNNING] .e.g.e.w.ElectrumDaemonWalletSendBalance : rawTx (signed): SimpleRawTx(hex=02000000000105d45d48252fa9b62c5a251a5f5880999a16a820fe7f9abdea7f64bc4034110d520000000000feffffff185b6ef7ff8d16ce99da91d753c2d49daebf1c0e73b348e6a212e9e5412d5d590000000000feffffffae584e0768c4260e9b3b1fcede25e04eebc5297f64e220616f18668fc78662690000000000feffffffb805e86cc4ca08b61d770f68fdc98afb19c8dc76ab3f5232af4793a965060ccc0000000000feffffff6b5102122d06c822cd7e61a7797e3666502bf428376288dbe127ce7bffde19d70000000000feffffff019c021dd205000000160014aeea96c1436ad0dcebce853b9df55d5380c67b8202473044022012359db185afd2e6d1559d890895e0517f930ffbb7c199e5125f82fbd6d275ef022023fd66b19405651163af7d91079e74dcc296a2ad81398f0e1321c38137759ad3012102595181ef386bf74a43efcb03b34b5843acdd1883c78393d933903e8d2e4baf1c0247304402203906b713980ac75eaa549c69e11bbfec5ec7b474f75c7e6c7acc94746a43cc7c02203e480f6795905eb348d7d1391454851f2f1fb316408f5e62a3b413011786bcdf012102595181ef386bf74a43efcb03b34b5843acdd1883c78393d933903e8d2e4baf1c024730440220143664ffa41116ad0cc25e45a3a511294d116917c599b97cff314005e0a29aa702205d47c5a1970d970666794823e3ce09362353e7c91b459e2b8761ef345aed35bf012102595181ef386bf74a43efcb03b34b5843acdd1883c78393d933903e8d2e4baf1c0247304402203c64d7136f17cf8e1a5587bff4203ea0f9f5171fd2bf0f9b5f1462de9574717002205e961c59155be41a55cc9583f6ea3f67fcbce9dad93c720543086c0a1b60cf03012102595181ef386bf74a43efcb03b34b5843acdd1883c78393d933903e8d2e4baf1c02483045022100e61ad2ae1ae3a7be5a2781566219089087adab99ed95ca0e0e219a4ecfba4578022053a8f705e1f2afed3bb5c0cd7d947ea086d323b490666b39605fe2290084cf84012102595181ef386bf74a43efcb03b34b5843acdd1883c78393d933903e8d2e4baf1c79000000, complete=true, finalized=true)
2021-05-16 15:12:50.175  INFO 684724 --- [tchLoop RUNNING] .e.g.e.w.ElectrumDaemonWalletSendBalance : broadcast: 07cdcaa5daf5ddad719e71815690bd6c3afa6962e80337735dd3381453db1ad3
2021-05-16 15:12:50.175  INFO 684724 --- [tchLoop RUNNING] o.t.e.g.e.watch.ElectrumWalletWatchLoop  : Run 5 completed on 2021-05-16T15:12:50.175509021 after 144.1 ms
````

2. Find the port of electrumx via docker
```shell
docker ps
```

Example output:
```
CONTAINER ID        IMAGE                                   COMMAND                   CREATED             STATUS              PORTS                                                                                                                                                                                                          NAMES
a82b555c724e        lukechilds/electrumx:v1.15.0            "init"                    4 minutes ago       Up 3 minutes        0.0.0.0:33378->8000/tcp, 0.0.0.0:33377->50001/tcp, 0.0.0.0:33376->50002/tcp, 0.0.0.0:33375->50004/tcp                                                                                                          tbk-testcontainer-lukechilds-electrumx-2f43982c
```
Use the port that is mapped to `50001`: `0.0.0.0:33377->50001/tcp` -> `33377`

3. Start an electrum daemon in regtest mode connecting to the electrumx server:
```shell
./electrum --regtest --oneserver --server 127.0.0.1:33377:t
```

4. Create a regtest wallet with the seed taken from the `application.yml` file