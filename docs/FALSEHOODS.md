# Falsehoods Programmers Believe About Bitcoin

In the spirit of [Falsehoods Programmers Believe About Phone Numbers](https://github.com/google/libphonenumber/blob/master/FALSEHOODS.md), 
here is a list of mistaken perspectives on Bitcoin.

- [Blocks](#blocks)
- [Transactions](#transactions)
- [Wallets](#wallets)
  - [Keys](#keys)
  - [Addresses](#addresses)
- [Privacy](#privacy)
- [Exchanges](#exchanges)
- [Total supply](#total-supply)
- [Misc](#misc)


## Blocks
1. **Block height will only increase.**

   Not the chain with the highest number of blocks is considered as the majority chain (honest chain), but the chain 
   with the most cumulated [_chainwork_](https://github.com/bitcoin/bitcoin/blob/df536883d263781c2abe944afc85f681cda635ed/src/chain.h#L162). 
   As the work needed to find a valid block proof is dependent on the block [target](https://en.bitcoin.it/wiki/Target) 
   (see also [difficulty](https://en.bitcoin.it/wiki/Difficulty)), it is possible that a chain with a higher number 
   of blocks than the majority chain exists, if the difficulty was lower in this chain. 
   If a client for some reason first only sees the minority chain (with higher block count) and then gets presented
   the majority chain (with a higher chainwork) it will drop the minority chain in favor of the majority chain. In this
   case the valid block height (as seen by this node) might actually decrease.
   
   It is a very difficult exercise to even imagine a scenario of how this could happen.
   
   _Thought experiment_: Suppose a miner with massive hash power outperforming the majority chain, but not publicly announcing the blocks. 
   After 2016 blocks a difficulty adjustment takes place and the difficulty on the hidden chain increases by a lot. 
   All blocks mined now on the hidden chain contain more chainwork than corresponding blocks on the public chain. 
   The miner produces a few more blocks, stops mining and waits for the other chain to catch up to his blocksize + 1 and then announces his blocks. 
   All nodes will follow the chain with lesser blockheight because it includes more chainwork.
   
   It is left as an exercise for the reader to imagine how likely the occurrence of this case may be.
   
   A curious situation that impacted applications relying on a constantly increasing block height is the [March 2013 Chain Fork](#Bitcoin-never-had-a-double-spend). 
   If you can come up with an additional scenario, please - for the sake of Satoshi - create a PR!

1. **Block time will only increase.**

   No. That's not how time works in Bitcoin. A block does not necessarily have to have a higher timestamp than its predecessor.
   The network has no physical connection to the real world - so how does it know what time it is?
   The only way for Bitcoin to know is by integrating a concept of time in the consensus rules.
   
   Clocks are imprecise and [time is a difficult concept](https://github.com/kdeldycke/awesome-falsehood#dates-and-time). There is no single source of truth.
   It is even stated in the comment of function [`GetTimeOffset()`](https://github.com/bitcoin/bitcoin/blob/ad90dd9f313aa4a2f87675b4392b85c0b06a5a83/src/timedata.cpp#L21-L32):
   > Never go to sea with two chronometers; take one or three.

   A beautiful explanation on why this has no negative impact on the proper functioning of the network has been described by [dergigi](https://dergigi.com):
   > For Bitcoin, the fact that our human timestamps are imprecise doesn’t matter too much. It also doesn’t matter that we have no absolute reference frame in the first place. They only have to be precise enough to calculate a somewhat reliable average across 2016 blocks. To guarantee that, a block’s "meatspace" timestamp is only accepted if it fulfills two criteria:
   >  1. The timestamp has to be greater than the median timestamp of the previous 11 blocks.
   >  1. The timestamp has to be less than the network-adjusted time plus two hours. (The “network-adjusted time” is simply the median of the timestamps returned by all nodes connected to you.)

   The whole article ["Bitcoin is Time"](https://dergigi.com/2021/01/14/bitcoin-is-time/) by dergigi is well worth reading.

   It is these rules that allow a block with a lower timestamp than its predecessor to be considered valid.

   However, if a block violates these rules either `"time-too-old: block's timestamp is too early"`
   or `"time-too-new: block timestamp too far in the future"` validation errors in [`ContextualCheckBlockHeader()`](https://github.com/bitcoin/bitcoin/blob/a12962ca894075ae203ab808db4ba5dab23346d1/src/validation.cpp#L3361-L3367)
   will keep it from being accepted.

   Bitcoin has three sources of time:
   - System clock
   - Median of other nodes clocks (see [`GetMedianTimePast()`](https://github.com/bitcoin/bitcoin/blob/92fee79dab384acea47bf20741a9847a58253330/src/chain.h#L270-L284))
   - The user (asking the user to fix the system clock if the first two disagree)

1. **When a miner finds a valid block, it is guaranteed to be included in the blockchain.**
   
1. **Okay, but when a miner finds a valid block single-handedly, it is guaranteed to be included in the blockchain.**

   Not if the block's hash [collides with the hash of an earlier block](https://bitcoin.stackexchange.com/a/38385). 
   As the [inventory vector](https://en.bitcoin.it/wiki/Protocol_documentation#Inventory_Vectors) of the described new 
   block would be a duplicate of an already known block, no other node would request this block. It would just be 
   ignored, as if never discovered in the first place. Everybody would be convinced that no new block has been found at that height yet.

1. **Each block always generates `${CURRENT_BLOCKREWARD}` amount of new Bitcoin.**

   In [block 124724](https://blockchair.com/bitcoin/block/124724) the coinbase transaction is missing one Satoshi.
   [Block 501726](https://blockchair.com/bitcoin/block/501726) is even missing the whole block reward.
   
1. **The more leading `0`'s a block hash has (i.e. the lower the hash is), the more does the block contribute to total chainwork.**

   It's a common misbelief that blocks with a lower block hash (i.e. more leading zeros) contribute more to the cumulated
   [chainwork](https://github.com/bitcoin/bitcoin/blob/df536883d263781c2abe944afc85f681cda635ed/src/chain.h#L162) than a block 
   with a larger hash (less leading zeros). Calculating the block hash consists of many (independent) SHA256 hashing operations 
   until a hash is found which is lower than the current [target](https://en.bitcoin.it/wiki/Target) (which is stored in the 
   [`nBits`](https://github.com/bitcoin/bitcoin/blob/df536883d263781c2abe944afc85f681cda635ed/src/chain.h#L180) field of the blockheader
   and gets adjusted every 2016 blocks as part of the difficulty adjustment algorithm).
   Each of this hashing operations is independent of all hashing operations before. As the result of SHA256 is pseudorandomly distributed
   the probability of finding a hash meeting the _target_ requirements is only dependent on the current _target_ value itself. 
   Any hash below  the _target_ will be considered as a valid block proof, but the probability of finding such a hash is the 
   same **for all values below the _target_** (no matter if it has 15 or 30 leading zeros).
   For this reason only the difficulty value (`= highest target / current target`) which was active at the time of block 
   generation is accounted to the amount of total work (see [validation.cpp#L3138](https://github.com/bitcoin/bitcoin/blob/20677ffa22e93e7408daadbd15d433f1e42faa86/src/validation.cpp#L3138)
   to see where the work of current block is added to `nChainWork` and see [`GetBlockProof(…)` in chain.cpp](https://github.com/bitcoin/bitcoin/blob/aaaaad6ac95b402fe18d019d67897ced6b316ee0/src/chain.cpp#L122-L135)
   to see how the block work is calculated only from the blockheader's `nBits` (=current target) header field).
   
1. **Difficulty adjustment is based off of the previous 2016 blocks.**

   The difficulty adjustment algorithm has an off-by-one bug that leads to the calculation based off of the previous 2015 blocks, rather than precisely 2016.
   
1. **Empty blocks are empty.**
 
   Empty blocks still contain data. They aren't devoid of data, they simply do not have transactions other than the
   coinbase transaction in them. Since an empty block does not contain any transactions from the mempool, it is 
   considered to be empty. Empty blocks are still computationally expensive because miners still have to produce a 
   Proof of Work. They still have block headers which are 80 bytes and have all the fields that non-empty blocks do. 


## Transactions
1. **Once a valid transaction is in the mempool, it will end up in the blockchain.**
   
   Transactions can be dropped from the mempool. A node's mempool can only occupy as much memory as is configured 
   through `maxmempool`. When this limit is reached, it will drop the transactions with the lowest feerate and increase 
   its `mempoolminfee`. It will communicate its new `mempoolminfee` to its peers, basically telling peers not to 
   forward transactions below that feerate for the time being. Note that every node does this individually, so a node 
   with a larger mempool or different architecture may drop transactions earlier or later.
   
1. **Before a transaction becomes part of the blockchain it must be in the mempool.**

1. **If I see a transaction in my mempool I can be sure it is in all nodes' mempool.**

   No.
   - Transactions need time to be propagated to every node. If you see it, it does not mean everybody sees it.
   - Every node is configured differently and has certain constraints (e.g. `maxmempool` size reached).
   - A node is not obligated to include a transaction in the mempool and can decline to do so at will.

1. **If a transaction is not accepted in the mempool it cannot be accepted as valid in a block.**

   There are various reasons a transaction might not be accepted, propagated or requested by nodes, e.g. 
   a transaction has a fee rate below the `minrelaytxfee`.
   
   The `minrelaytxfee` specifies a feerate acting as a lower bound for a node's mempool. A node will not admit unconfirmed 
   transactions below that feerate to its mempool and thus will not relay them to its peers. The `minrelaytxfee` is a 
   configuration setting and can be specified by each node operator independently.
   
   This does not mean it is invalid or cannot be included in a block.
   
1. **Yeah, but once it is in a block, it will stay in the blockchain forever.**

1. **Each transaction has exactly one receiver.**

1. **Each transaction has exactly one sender.**

1. **The destination of a Bitcoin transaction (output) is always an address.**

1. **A miner will always select the transactions with the highest fees.**

1. **All transaction hashes in the blockchain are unique.**

   Nope. See [BIP30](https://github.com/bitcoin/bips/blob/master/bip-0030.mediawiki).
    
   To counter this problem, the following network rule has been introduced:
   * Blocks are not allowed to contain a transaction whose identifier matches that of an earlier, not-fully-spent transaction in the same chain.


1. **Fees are a specified explicitly in a transaction.**

   No, the transaction fee is the difference between the amount being spent and the amount being received.
   It is not explicitly specified in a transaction, instead it must be calculated with `sum(inputs) - sum(outputs)`.

1. **Okay, but every transaction must pay a fee, right?**

   No, the only rule is that transaction fees must be greater than or equal to zero.
   For example: [See this transaction where input and output amounts are equal](https://mempool.space/tx/0301e0480b374b32851a9462db29dc19fe830a7f7d7a88b81612b9d42099c0ae), which means: No fees were paid (at least not on-chain).

1. **If I make an RBF marked transaction I can always replace it by a different one, as long as it is still unconfirmed.**

1. **If I see a none-RBF marked transaction with enough fee, I can be pretty sure it will end up in the blockchain as it is.**

1. **If I see an unconfirmed payment to an address of mine, I can store the transaction ID as it will never change.**

1. **If the transaction ID of an unconfirmed payment has changed, it was clearly a malicious double-spend attempt.**


## Wallets   
1. **All wallets use standardized derivation paths.**

1. **The 12 (or 15, 18, 21, 24) words of my seed phrase are everything I need to recover my wallet.**

1. **There is only one standard for mnemonic seed phrases (12/24 words).**

1. **Each derivation path (e.g. `m/44'/0'/0'/0/0`, `m/44'/0'/0'/0/1`, ...) is guaranteed to derive a valid address.**

   BIP 32 key derivation consists of applying HMAC-SHA512 (see [BIP32](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki) for all details) 
   and using the first 256 bits of the result as key material. Bitcoin uses the `secp256k1` elliptic curve for its underlying signature operations.
   Not all numbers from 0 to 2^256 are valid keys for `secp256k1`, especially the number 0 and all numbers `> n` (where `n` is the order of the curve)
   are not valid keys (per definition of secp256k1). As `n` of the `secp256k1` curve is very close to the possible maximum of 2^256 it is 
   very unlikely (probability of less than 1 in 2^127) that the value derived as part of BIP 32 key derivation is not a valid private key.
   If such a case ever happens BIP 32 specifies that a wallet should just skip this index and proceed with the next higher index (see 
   [specification of key derivation](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki#private-parent-key--private-child-key)
   for details).
   
   So it is possible that wallets exist where the index of derived keys might have a gap. For these missing indexes then simply no address exists, and
   the missing index should be silently ignored by wallets. However, this case is so highly improbable it's very likely that nobody will ever 
   see this case in practice. (_Trivia: The author of [pycoin](https://github.com/richardkiss/pycoin) even prepared a [very special error message](https://github.com/richardkiss/pycoin/blob/9837d456a8b7cd8ef5f170b7bc41858957cc5e96/pycoin/key/bip32.py#L12)
   for the case should this ever happen. :)_ ). 

1. **If I sweep only parts of the funds on a paperwallet, the remaining rest will always stay on the paper wallet.**

1. **But if I use a wallet providing a dedicated "sweep paperwallet" function and spend only parts of the funds, the remaining funds will always end up at the exactly same address printed on the paper wallet.**

1. **Two different xpub strings will derive different Bitcoin addresses**

   [BIP32](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki) specifies a (base58-based) serialization format for extended public and private keys
   which can be used to derive Bitcoin addresses or private keys from it. These key strings usually start with the prefix `xpub` or `xprv`. Common use cases
   are to use the extended public key ("xpub") on a shop system to derive new receive addresses per order without having the private key available on the server,
   or a Bitcoin broker might receive an xpub from its customer as part of a dollar-cost-averaging saving plan to send Bitcoin for each order to a new unused address.

   To enable this the xpub contains a key which is then used in the BIP32 algorithm to deterministically derive new subkeys from it.
   But the serialized format also contains some metadata like `depth` (how "deep" this key is located within the BIP32 hierarchy) and `parent fingerprint`
   (a fingerprint identifying the parent xpub, just to enable wallets to correlate keys within the hierarchy) and some other metadata.

   But especially these 2 fields can easily be changed or wiped (for example a user doesn't want to reveal that this xpub is part of a larger tree) but the raw 
   key material still will be same and thus generate the same addresses.

   _Example_:

   A random xpub with a depth value of `2` and a parent fingerprint of `0x5c1bd648`:
   ```
   xpub6ASuArnXKPbfEwhqN6e3mwBcDTgzisQN1wXN9BJcM47sSikHjJf3UFHKkNAWbWMiGj7Wf5uMash7SyYq527Hqck2AxYysAA7xmALppuCkwQ
   ```
   
   Now the same key with just set these 2 fields to `0x0`:
   ```
   xpub661MyMwAqRbcHWnXMP3g53rv1b6WkhCQMX4WuGL6XLGxp3obJ6EoRvZ7gZbPKQRi7U2mCkUwMQWhbTU4hkpyaFR1Kw5c5s7ZZLLtV9FSy28
   ```
   
   The serialized base58 representation now looks totally different, but if you use these 2 xpubs they will derive the same Bitcoin 
   addresses (you can test this on online [BIP32 calculator tools](https://bitaps.com/bip32)): 

      - `1BiCdXSDHyeXSzmx2paVPFVTrmyx7BeCGD`
      - `132EVXhbnaeF2U9ZkakX6FJWqLzmPazQA7`
      - and so on …

### Keys
1. **Each private key corresponds to exactly one address.**

1. **Compressed WIF Bitcoin private keys are shorter than uncompressed keys.**

1. **Every integer from 1 to 2^256 is a valid private key for a Bitcoin address.**

1. **It is possible to convert an existing Bitcoin private key to a BIP39 mnemonic seed (12/24 words seed).**

1. **It is safe to hand out the single private key of an address which was (non hardly) derived from an extended key (xpub/xprv) to a person who also knows the xpub (not the xprv) from which the address was derived from.**


### Addresses
1. **Each Bitcoin address has exactly one private key.**

1. **It is always possible to derive an address from an input (or output).**

1. **All Bitcoin addresses have the same length (number of characters).**

1. **All Bitcoin addresses are case-sensitive.**


## Privacy
1. **Bitcoin is anonymous.**
   
   All coins have a clear and visible history of ownership tracing back to their creation. In addition, the blockchain 
   allows all transactions to be viewed by anyone in the network, so transactions, addresses, and supply can be easily 
   audited by any and all network participants.
   
   Addresses are merely strings of letters and numbers, and are not inherently connected to a given user or wallet. 
   Both users and wallets can use an arbitrary number of addresses to store bitcoin, and multisig addresses can hold 
   bitcoin belonging to multiple users. Thus, Bitcoin is most accurately defined as pseudonymous rather than anonymous. 
   
1. **All coins spent within a single transaction (inputs) are controlled by the same entity (owned by the same person).**


## Exchanges
Always remember: Get your coins off of exchanges.

1. **Exchanges will always allow withdrawal of funds.**

1. **The coins in my exchange account are mine.**

1. **The coins in my exchange account actually exist.**

1. **Orders which are successfully placed on exchanges are guaranteed to be executed.**

1. **Ask price will always be equal or higher than bid price.**


## Total supply
1. **There are exactly 21 million bitcoin to ever exist.**

   The total number of bitcoins has an asymptote at 21 million, due to a side effect of the data structure of the blockchain – 
   specifically the integer storage type of the transaction output – [the exact value would be 20,999,999.9769 bitcoin](https://en.bitcoin.it/wiki/Controlled_supply#Projected_Bitcoins_Long_Term).
   However, due to miner underpayment, the total number is even less.
   
   It is therefore impossible to know exactly how many bitcoin will exist in the year 2140, but it will be less than 21 millions.

1. **Okay, but at least I can be sure the supply is never going to be greater than 21 million.**
   
   Well.. no.
   
   Back in August 2010 there was an incident at blockheight [#74638](https://blockchair.com/bitcoin/block/74638): Someone discovered that transaction amounts had no overflow check implemented (back then)
   and created a transaction which included 2 outputs each with 92233720368.54277039 BTC (92 billion!). This transaction was considered as valid by all nodes in 
   the network, because the sum of the inputs (+ fees) _seemed_ to be equal to the sum of these 2 outputs (because the sum caused an integer overflow). This [was recognized quickly
   by the Bitcoin community](https://bitcointalk.org/index.php?topic=822.0) and within a few hours a 
   [bugfix was developed](https://github.com/bitcoin/bitcoin/commit/2d12315c94f12d62b2f2aa39e63511a2042fe55d). As soon as the majority of miners ran the bugfixed version, the chain containing
   this block was rejected as invalid and dropped by all nodes, and the new majority chain did no longer contain this block (so you will not see this block anymore in today's blockchain).
   You can still see the traces of this incident in the blockchain by looking at the timestamps of [block 74637](https://blockchair.com/bitcoin/block/74637) (_2010-08-15 17:02_) 
   and [block 74638](https://blockchair.com/bitcoin/block/74638) (_2010-08-15 23:53_). They are several hours apart because that is the time in which the chain with the invalid transaction
   existed until it was later discarded in favor of the honest majority chain.
   
   So, technically, there was a short period in time, where the total amount of Bitcoin was higher than 21 million. If the RPC call `bitcoin-cli gettxoutsetinfo` would already have
   existed in 2010, it would have returned a total amount of over 184 billion total BTC between 17:02 and 23:53 on 2010-08-15.

1. **Okay, but a finite total supply has been enforced from the beginning and will always be enforced forever.**

   Actually, Satoshi added indefinite inflation to the protocol. 
   It was removed in 2014, as proposed by Pieter Wuille in [BIP42](https://github.com/bitcoin/bips/blob/master/bip-0042.mediawiki)

   > Although it is widely believed that Satoshi was an inflation-hating goldbug he never said this, and in fact programmed Bitcoin's money supply to grow indefinitely, forever. He modeled the monetary supply as 4 gold mines being discovered per mibillenium (1024 years), with equal intervals between them, each one being depleted over the course of 140 years.
   > 
   > [...]
   >
   > To combat this, this document proposes a controversial change: making Bitcoin's monetary supply finite.
   
   Will this lead to a softfork taking place in the year 2214 if a miner happens to run old software?


## Misc
1. **All UTXOs are spendable.**

   Some outputs are provably unspendable (for example the [50 BTC output in the genesis block can never be spent](https://bitcoin.stackexchange.com/a/10019/109728)) as 
   well as all [outputs with a `OP_RETURN` script](https://en.bitcoin.it/wiki/OP_RETURN) and others where it is highly likely the coins
   can never be spent (for example addresses which look "generated" in a way that somebody just tried to find a valid checksum for the address string, but it is very, very unlikely that 
   the address really resulted from an actual random hashing operation). Examples: [1CounterpartyXXXXXXXXXXXXXXXUWLpVr](https://www.blockchain.com/btc/address/1CounterpartyXXXXXXXXXXXXXXXUWLpVr) 
   or [1BitcoinEaterAddressDontSendf59kuE](https://www.blockchain.com/btc/address/1BitcoinEaterAddressDontSendf59kuE). One can safely assume, that these coins are unspendable forever.
   Besides all this, there are lots of addresses where the private key is lost.
   
1. <a id="Bitcoin-never-had-a-double-spend"></a>**Bitcoin never had a double spend.**
   
   Ouch! There was a little problem on March 11, 2013, starting at blockheight [#225430](https://blockchair.com/bitcoin/block/225430) during an upgrade from v0.7 to v0.8 ...
   
   A user described an interesting situation on that day that led to [OKPay suffering a $10,000 double spend](https://bitcointalk.org/index.php?topic=152348.0).
   > 08:08 – Well before I knew what later have happened, [...], I paid OKPAY address 12z2n8YCJw1BEsJhhQPLCTuLqwH341nKnE 211.9093 BTC and 0.0005 BTC as transaction fee.
   > 
   > 09:30 – The transaction was included in version 0.8's fork, block 225446
   > 
   > 10:08 – Deposit completed [...]
   > 
   > 12:53 – After some study, I recognized, the transaction, though included in version 0.8's fork, was never confirmed by the pre-0.8 fork, so I decided to make two double spend transactions on two of the vins of the OKPAY transaction [...]
   > 
   > 13:01 – The double spend transaction was included in pre-0.8 fork block 225446
   
   Even waiting for confirmations would not have mitigated this scenario and could have had even bigger impacts by
   making hostile miners more motivated to attempt a [Goldfinger attack](https://www.jkroll.com/papers/oakland15_bitcoin-sok.pdf).
   After five years, no block explorer shows the original transactions anymore. That's why it's up for debate whether it was a real double spend.
   See [March 2013 Chain Fork Post-Mortem (BIP50)](https://github.com/bitcoin/bips/blob/master/bip-0050.mediawiki) or read the [full story of the 2013 Bitcoin fork](https://freedom-to-tinker.com/2015/07/28/analyzing-the-2013-bitcoin-fork-centralized-decision-making-saved-the-day) - it's fascinating.
