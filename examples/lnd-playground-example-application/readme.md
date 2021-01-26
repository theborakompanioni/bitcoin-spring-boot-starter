lnd-playground-example-application
===

A lightning playground app using lnd.

Start the application with
```shell
./gradlew -p examples/lnd-playground-example-application bootRun
```

Example console output:
```
2021-01-21 01:01:33.306  INFO 309543 --- [  restartedMain] .l.l.p.e.LndPlaygroundExampleApplication : Started LndPlaygroundExampleApplication in 18.561 seconds (JVM running for 19.232)
2021-01-21 01:01:33.948  INFO 309543 --- [  restartedMain] .e.LndPlaygroundExampleApplicationConfig : =================================================
2021-01-21 01:01:33.948  INFO 309543 --- [  restartedMain] .e.LndPlaygroundExampleApplicationConfig : [lnd] identity_pubkey: 03d2db919c802c7b69b24be758db10c0d7347aefab525e8f00e41a87904631eaec
2021-01-21 01:01:33.949  INFO 309543 --- [  restartedMain] .e.LndPlaygroundExampleApplicationConfig : [lnd] alias: tbk-lnd-example-application
2021-01-21 01:01:33.949  INFO 309543 --- [  restartedMain] .e.LndPlaygroundExampleApplicationConfig : [lnd] version: 0.11.1-beta commit=v0.11.1-beta-4-g3c4471f8818a07e63864d39a1c3352ce19e8f31d
2021-01-21 01:01:39.275 DEBUG 309543 --- [stMiner RUNNING] .t.s.t.b.r.ScheduledBitcoindRegtestMiner : Trying to mine one block with coinbase reward for address bcrt1qn2r960p8ykxv4ltxlmp7cxcercpqpteplzczwd
2021-01-21 01:01:39.292 DEBUG 309543 --- [stMiner RUNNING] .t.s.t.b.r.ScheduledBitcoindRegtestMiner : Mined 1 blocks with coinbase reward for address bcrt1qn2r960p8ykxv4ltxlmp7cxcercpqpteplzczwd
2021-01-21 01:01:39.293 DEBUG 309543 --- [stMiner RUNNING] .r.BitcoindRegtestMinerAutoConfiguration : Duration till next block: PT2.309S
2021-01-21 01:01:39.378  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : =================================================
2021-01-21 01:01:39.379  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [lnd] block height: 1
2021-01-21 01:01:39.379  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [lnd] block hash: 568c2f2d64a38f00d9e394210c38c85e434a881bb5946e9e53955bb748bb2233
2021-01-21 01:01:39.379  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [lnd] best header timestamp: 1611187299
2021-01-21 01:01:39.382  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : =================================================
2021-01-21 01:01:39.431  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [bitcoind] new best block (height: 1): 568c2f2d64a38f00d9e394210c38c85e434a881bb5946e9e53955bb748bb2233
2021-01-21 01:01:41.307 DEBUG 309543 --- [stMiner RUNNING] .t.s.t.b.r.ScheduledBitcoindRegtestMiner : Trying to mine one block with coinbase reward for address bcrt1qnjvm38z9pe4u4n5sm3vz7rx5g5rr36x6crxkrn
2021-01-21 01:01:41.314 DEBUG 309543 --- [stMiner RUNNING] .t.s.t.b.r.ScheduledBitcoindRegtestMiner : Mined 1 blocks with coinbase reward for address bcrt1qnjvm38z9pe4u4n5sm3vz7rx5g5rr36x6crxkrn
2021-01-21 01:01:41.315 DEBUG 309543 --- [stMiner RUNNING] .r.BitcoindRegtestMinerAutoConfiguration : Duration till next block: PT1.581S
2021-01-21 01:01:41.383  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : =================================================
2021-01-21 01:01:41.383  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [lnd] block height: 2
2021-01-21 01:01:41.384  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [lnd] block hash: 70160a48c15670c5136a057669bc11a57bf78c85f77e74b9a7c3f29ea8769079
2021-01-21 01:01:41.384  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [lnd] best header timestamp: 1611187301
2021-01-21 01:01:41.384  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : =================================================
2021-01-21 01:01:41.392  INFO 309543 --- [-pub-5e187872-0] .e.LndPlaygroundExampleApplicationConfig : [bitcoind] new best block (height: 2): 70160a48c15670c5136a057669bc11a57bf78c85f77e74b9a7c3f29ea8769079
```

# Resources
- Lightning Network: https://lightning.network/
- lnd (GitHub): https://github.com/LightningNetwork/lnd
- Lightningj (GitHub): https://github.com/lightningj-org/lightningj
- Protocol Buffers: https://developers.google.com/protocol-buffers

