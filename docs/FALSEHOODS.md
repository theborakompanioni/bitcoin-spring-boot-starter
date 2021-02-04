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
- [Misc](#misc)


## Blocks
1. **Block height will only increase.**

   Not the chain with the highest number of blocks is considered as the majority chain (honest chain), but the chain 
   with the most cumulated [_chainwork_](https://github.com/bitcoin/bitcoin/blob/df536883d263781c2abe944afc85f681cda635ed/src/chain.h#L162). 
   As the work needed to find a valid block proof is dependent on the block [target](https://en.bitcoin.it/wiki/Target) 
   (see also [difficulty](https://en.bitcoin.it/wiki/Difficulty)), it is possible that a chain with a higher number 
   of blocks than the majority chain exists, if the difficulty was lower in this chain. 
   If a client for some reason first only sees the minority chain (whigh higher block count) and then gets presented
   the majority chain (with a higher chainwork) it will drop the minority chain in favor of the majority chain. In this
   case the valid block height (as seen by this node) might actually decrease.

1. **Block time will only increase.**
   
1. **When a miner finds a valid block, it is guaranteed to be included in the blockchain.**

1. **Each block always generates `${CURRENT_BLOCKREWARD}` amount of new Bitcoin.**

1. **The more leading `0`'s a block hash has (i.e. the lower the hash is), the more does the block contribute to total chainwork.**

   It's a common misbelief that blocks with a lower block hash (i.e. more leading zeros) contribute more to the cumulated
   [chainwork](https://github.com/bitcoin/bitcoin/blob/df536883d263781c2abe944afc85f681cda635ed/src/chain.h#L162) than a block 
   with a larger hash (less leading zeros). Calculating the block hash consists of a large number of (independent) SHA256 hashing operations 
   until a hash is found which is lower than the current [target](https://en.bitcoin.it/wiki/Target) (which is stored in the 
   [`nBits`](https://github.com/bitcoin/bitcoin/blob/df536883d263781c2abe944afc85f681cda635ed/src/chain.h#L180) field of the blockheader
   and gets adjusted every 2016 blocks as part of the difficulty adjustment algorithm).
   Each of this hashing operations is independent of all hashing operations before. As the result of SHA256 is pseudorandomly distributed
   the probability of finding a hash meeting the _target_ requirements is only dependent on the current _target_ value itself. 
   Any hash below  the _target_ will be considered as a valid block proof, but the probability of finding such a hash is the 
   same **for all values below the _target_** (no matter if it has 30 or 15 leading zeros).
   For this reason only the difficulty value (`= highest target / current target`) which was active at the time of block 
   generation is accounted to the amount of total work (see [validation.cpp#L3138](https://github.com/bitcoin/bitcoin/blob/20677ffa22e93e7408daadbd15d433f1e42faa86/src/validation.cpp#L3138)
   to see where the work of current block is added to `nChainWork` and see [`GetBlockProof(…)` in chain.cpp](https://github.com/bitcoin/bitcoin/blob/aaaaad6ac95b402fe18d019d67897ced6b316ee0/src/chain.cpp#L122-L135)
   to see how the block work is calculated only from the blockheader's `nBits` (=current target) header field).
   
## Transactions
1. **Once a valid transaction is in the mempool, it will end up in the blockchain.**
   
1. **Before a transaction becomes part of the blockchain it must be in the mempool.**

1. **If I see a transaction in my mympool I can be sure it is in all nodes' mempool.**

1. **If a transaction is not accepted in the mempool it cannot be accepted as valid in a block.**

1. **Yeah, but once it is in a block, it will stay in the blockchain forever.**

1. **Each transaction has exactly one receiver.**

1. **Each transaction has exactly one sender.**

1. **The destination of a Bitcoin transaction (output) is always an address.**

1. **A miner will always select the transactions with the highest fees.**

1. **All transaction hashes in the blockchain are unique.**

1. **Fees are a specified explicitly in a transaction.**

1. **If I make a RBF marked transaction I can always replace it by a different one, as long as it is still unconfirmed.***

1. **If I see a none-RBF marked transaction with enough fee, I can be pretty sure it will end up in the blockchain as it is.**

1. **If I see an unconfirmed payment to an address of mine, I can store the transaction ID as it will never change.**

1. **If the transaction ID of an unconfirmed payment has changed, it was clearly a malicious double-spend attempt.**


## Wallets
1. **All wallets support p2pkh transactions.**
   
1. **All wallets use standardized derivation paths.**
   
1. **Brain wallets are secure.**

1. **The 12 (or 15, 18, 21, 24) words of my seed phrase are everything I need to recover my wallet.**

1. **There is only one standard for mnemonic seed phrases (12/24 words).**

1. **Each derivation path (eg. `m/44'/0'/0'/0/0`, `m/44'/0'/0'/0/1`, ...) is guaranteed to derive a valid address.**

   BIP 32 key derivation consists of applying HMAC-SHA512 (see [BIP 32](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki) for all details) 
   and using the first 256 bits of the result as key material. Bitcoin uses the `secp256k1` elliptic curve for its underlying signature operations.
   Not all numbers from 0 to 2^256 are valid keys for `secp256k1`, especially the number 0 and all numbers `> n` (where `n` is the order of the curve)
   are not valid keys (per definition of secp256k1). As `n` of the `secp256k1` curve is very close to the possible maximum of 2^256 it is 
   very very unlikely (probability of less than 1 in 2^127) that the value derived as part of BIP 32 key derivation is not a valid private key.
   If such a case ever happens BIP 32 specifies that a wallet should just skip this index and proceed with the next higher index (see 
   [specification of key derviation](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki#private-parent-key--private-child-key)
   for details).
   
   So it is possible that wallets exist where the index of derived keys might have a gap. For these missing indexes then simply no address exist and
   the missing index should be silently ignored by wallets. However this case is so highly unlikely that it's very likely that nobody will ever 
   see this case in practice. (_Trivia: The author of [pycoin](https://github.com/richardkiss/pycoin) even prepared a [very special error message](https://github.com/richardkiss/pycoin/blob/9837d456a8b7cd8ef5f170b7bc41858957cc5e96/pycoin/key/bip32.py#L12)
   for the case should this ever happen. :)_ ). 

1. **If I sweep only parts of the funds on a paperwallet, the remaining rest will always stay on the paper wallet.**

1. **But if I use a wallet providing a dedicated "sweep paperwallet" function and spend only parts of the funds, the remaining funds will always end up at the exactly same address printed on the paper wallet.**


### Keys
1. **Each private key corresponds to exactly one address.**

1. **Compressed WIF Bitcoin private keys are shorter than uncompressed keys.**

1. **Every integer from 1 to 2^256 is a valid private key for a Bitcoin address.**

1. **It is possible to convert an existing Bitcoin private key to a BIP39 mnemonic seed (12/24 words seed).**

1. **It is safe to handout the single private key of an address which was (non hardly) derived from an extended key (xpub/xprv) to a person who also knows the xpub (not the xprv) from which the address was derived from.**


### Addresses
1. **Each Bitcoin address has exactly one private key.**

1. **It is always possible to derive an address from an input (or output).**

1. **All Bitcoin addresses have the same length (number of characters).**

1. **All Bitcoin addresses are case-sensitive.**


## Privacy
1. **Bitcoin is anonymous.**
  
1. **All coins spent within a single transaction (inputs) are controlled by the same entity (owned by the same person).**


## Exchanges 
1. **Exchanges will always allow withdrawal of funds.**

1. **The coins in my exchange account are mine.**

1. **The coins in my exchange account actually exist.**

1. **Orders which are successfully placed on exchanges are guaranteed to be executed.**

1. **Ask price will always be equal or higher than bid price.**


## Misc
1. **There are only 21 million bitcoin to ever exist.**

