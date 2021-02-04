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
   
1. **Block time will only increase.**
   
1. **When a miner finds a valid block, it is guaranteed to be included in the blockchain.**

1. **Each block always generates `${CURRENT_BLOCKREWARD}` amount of new Bitcoin.**


## Transactions
1. **Once a valid transaction is in the mempool, it will end up in the blockchain.**
   
1. **Before a transaction becomes part of the blockchain it must be in the mempool.**
   
1. **Yeah, but once it is in a block, it will stay in the blockchain forever.**

1. **Each transaction has exactly one receiver.**

1. **Each transaction has exactly one sender.**

1. **The destination of a Bitcoin transaction (output) is always an address.**

1. **A miner will always select the transactions with the highest fees.**

1. **All transaction hashes in the blockchain are unique.**

1. **Fees are a specified explicitly in a transaction.**


## Wallets
1. **All wallets support p2pkh transactions.**
   
1. **All wallets use standardized derivation paths.**
   
1. **Brain wallets are secure.**

1. **The 12 (or 15, 18, 21, 24) words of my seed phrase are everything I need to recover my wallet.**

1. **There is only one standard for mnemonic seed phrases (12/24 words).**

1. **Each derivation path (eg. `m/44'/0'/0'/0/0`, `m/44'/0'/0'/0/1`, ...) is guaranteed to derive a valid address.**

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

