bitcoin-zeromq-client
===

A module containing a spring boot starter for a Bitcoin Core ZeroMq API client.
The starter will automatically create autowireable `ZeroMqMessagePublisherFactory` beans
for every zmq endpoint:

```yaml
org.tbk.bitcoin.zeromq:
  network: mainnet
  zmqpubrawblock: tcp://localhost:28332
  zmqpubrawtx: tcp://localhost:28333
  zmqpubhashblock: tcp://localhost:28334
  zmqpubhashtx: tcp://localhost:28335
```

Here is an example of how to subscribe to messages:
```java
@Slf4j
public final class SubscribeToBitcoinTransactionsViaZeroMqExample {

  @Autowire
  @Qualifier("bitcoinRawTxZeroMqMessagePublisherFactory")
  private MessagePublisherFactory<byte[]> rawTxMessageFactory;

  public void start() {
    this.rawTxMessageFactory.create().subscribe(rawTx -> {
      log.info("Got raw transaction: {}", rawTx);
    });
  }
}
```

See the autoconfig class `BitcoinZeroMqClientAutoConfiguration` for more details.

Also, if you have [bitcoinj](https://github.com/bitcoinj/bitcoinj) in the classpath, it will create a bean
of type `BitcoinjTransactionPublisherFactory` and `BitcoinjBlockPublisherFactory` which will emit `bitcoinj` types for your convenience.


# Resources
- Accessing Bitcoin's ZeroMQ interface: https://bitcoindev.network/accessing-bitcoins-zeromq-interface/
- JermoMq (GitHub): https://github.com/zeromq/jeromq
- Bitcoinj (GitHub): https://github.com/bitcoinj/bitcoinj
- Project Reactor (GitHub): https://github.com/reactor/reactor-core
