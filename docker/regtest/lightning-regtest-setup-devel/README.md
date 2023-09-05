Local Regtest Lightning Network Docker Setup
===

## Bitcoin

## Local Regtest Lightning Network
### Nodes
#### CLN 0 (app)
The lightning node controlled by the application.

#### CLN 1 (Alice)
A user lightning node with a direct channel to the app node.

#### CLN 2 (Bob)
A user lightning node with a direct channel to the app node.

#### CLN 3 (Charlie)
A user lightning node with a direct channel to Bob and a private channel to Erin.

#### CLN 4 (Dave)
A user lightning node _without_ channels.
This is node solely exists to test the specific behaviour when no route can be found. 

#### CLN 5 (Erin)
A node with a single incoming private channel from Charlie.

#### LND 6 (Farid)
A user lightning node with a direct channel to Charlie.


### Channels
```mermaid
flowchart TB
   app -->|16_777_215 sat| alice
   app -->|8_388_607 sat| bob
   bob -->|4_194_303 sat| charlie
   farid -->|4_194_303 sat| bob
   charlie -. private 2_097_151 sat .-> erin
   app ~~~ dave
   alice ~~~ dave
   bob ~~~ dave
   charlie ~~~ dave
   erin ~~~ dave
   farid ~~~ dave
```


## Resources
- Node Personas: https://en.wikipedia.org/wiki/Alice_and_Bob#Cast_of_characters
- Mermaid Flowchart: https://mermaid.js.org/syntax/flowchart.html
