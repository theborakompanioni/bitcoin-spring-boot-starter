package org.tbk.bitcoin.txstats.example.score;

import com.google.common.io.BaseEncoding;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabel;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
public class TxScoreServiceImplTest {

    @Autowired
    private TxScoreServiceImpl sut;

    @Test
    public void address_reuse() {
        // https://blockchair.com/bitcoin/transaction/a0dc90c9856f19de61170eac0b43061e6835a8fba9c00f11338d020a85ee5322
        String txId = "a0dc90c9856f19de61170eac0b43061e6835a8fba9c00f11338d020a85ee5322";
        String rawTx = "01000000000101273b87fa45f0194dad72c9697e350657d1fa166b5a2a796c845c4eff366620e601000000171600146b78b5b0b4a048b4cec7bc6fc2c4bbaf71ddbe580000000002dd2d06000000000017a9141757e04765a01cd4e1dc4041337a813e3023f12987649d05000000000017a9141a80b5b5109b8400fc21b9a77c6529e549fb29e287024730440220131a2774f77ddf9e6df0f631638ec9b7a023e1fdf42ab5fabe76d116d90d0155022021d18c79fc1aaa7159aad942b2e0ca4340e24f6e1ffc800b49c1c3a16b509d0c012103cd0a20464b3d0d303c7b7ac2664a84f9bc05c77c8d6dbb465d58b3a26ebd7f9b00000000";

        byte[] rawTxBytes = BaseEncoding.base16().decode(rawTx.toUpperCase());
        Transaction tx = new Transaction(MainNetParams.get(), rawTxBytes);

        TxScoreService.ScoredTransaction scoredTransaction = this.sut.scoreTransaction(tx).blockFirst();

        Optional<ScoreLabel> labelOrEmpty = scoredTransaction.getLabels().stream()
                .filter(val -> "address_reuse".equals(val.getName()))
                .findFirst();

        assertThat(labelOrEmpty.isPresent(), is(true));
    }

    @Test
    public void known_miner() {
        // https://blockchair.com/bitcoin/transaction/4e5c226fd6d88c20ff56d10037c473bdf17a401878c9d41724bffb8762cb18d6
        String txId = "4e5c226fd6d88c20ff56d10037c473bdf17a401878c9d41724bffb8762cb18d6";
        String rawTx = "010000000001010000000000000000000000000000000000000000000000000000000000000000ffffffff6403d5aa092cfabe6d6da0543f6d8701d857a1f65e6fe71a145dcfb77a34a2f1d359f47831f9958bfe4f10000000f09f909f082f4632506f6f6c2f144d696e6564206279206d7978686771743135323200000000000000000000000000000005006dea0000000000000480378d25000000001976a914c825a1ecf2a6830c4401620c3a16f1995057c2ab88ac00000000000000002f6a24aa21a9edcdb930cfc7df849e66139646a8c4e3dc23537b10a0eadf4f602d9c3f5afc306a08000000000000000000000000000000002c6a4c2952534b424c4f434b3a611eed8a4638796352d228aa8ad46ed6723cebd73ba526128b2b411d0024f7b20000000000000000266a24b9e11b6dba88e7e667bd9afa3abaa5f32b3189439d90a5104573947b8914ffff5c41208d01200000000000000000000000000000000000000000000000000000000000000000053d563c";

        byte[] rawTxBytes = BaseEncoding.base16().decode(rawTx.toUpperCase());
        Transaction tx = new Transaction(MainNetParams.get(), rawTxBytes);

        TxScoreService.ScoredTransaction scoredTransaction = this.sut.scoreTransaction(tx).blockFirst();

        Optional<ScoreLabel> labelOrEmpty = scoredTransaction.getLabels().stream()
                .filter(val -> "known_miner".equals(val.getName()))
                .findFirst();

        assertThat(labelOrEmpty.isPresent(), is(true));
    }

    @Test
    public void unknown_miner() {
        // https://blockchair.com/bitcoin/transaction/803c54e7b5ddb749a4008aeee091bba4188aa1fc8c0afb654d8e5651d8e8b00e
        String txId = "803c54e7b5ddb749a4008aeee091bba4188aa1fc8c0afb654d8e5651d8e8b00e";
        String rawTx = "020000000001010000000000000000000000000000000000000000000000000000000000000000ffffffff4c036b020a04a7f2a75f737a2f4254432e636f6d2ffabe6d6d3a9d8feba39f917c5eab794a4ed060b238b7cf6602046f023a2469a5307533d9020000000ca3d3af18de6b0b69a6010000000000ffffffff04d3e8f8300000000016001497cfc76442fe717f2a3f0cc9c175f7561b6619970000000000000000266a24aa21a9ed218ddc76875ac68019325cc8210db1b647af8b376bb34f24e969f5512015f3ce00000000000000002b6a2952534b424c4f434b3a8cf943dcead292a3483e7432c224834d38c10bab975bdcf14a7d8822002b6ee10000000000000000266a24b9e11b6d2f9ee040d97850eca1449d0049ffe08879f211dd90cc9b8613f68ccb92c0ed400120000000000000000000000000000000000000000000000000000000000000000000000000";

        byte[] rawTxBytes = BaseEncoding.base16().decode(rawTx.toUpperCase());
        Transaction tx = new Transaction(MainNetParams.get(), rawTxBytes);

        TxScoreService.ScoredTransaction scoredTransaction = this.sut.scoreTransaction(tx).blockFirst();

        Optional<ScoreLabel> labelOrEmpty = scoredTransaction.getLabels().stream()
                .filter(val -> "unknown_miner".equals(val.getName()))
                .findFirst();

        assertThat(labelOrEmpty.isPresent(), is(true));
    }

    @Test
    public void script_types() {
        // https://blockchair.com/bitcoin/transaction/250b2c28ff7213633bba93fa4578144a00eed1fcb6378740d2071b7088a6aee8
        String txId = "250b2c28ff7213633bba93fa4578144a00eed1fcb6378740d2071b7088a6aee8";
        String rawTx = "02000000000101fb7d484fa4d2cbc0cc2327fc4658b5aa4a06a32f5e42ed53c442bafe3b8d79af00000000171600147daee78cc272c916caac1ac556f93b09ef7d9e41feffffff02b0fb0e000000000017a9147c11a4fa270b5e813814ab54cd78b474b07487f98708970100000000001976a914d4bdb64860effa25c01f68cb5f9d2a5f7f98e99688ac0247304402205bd79d88ddd6fbc33ea2aa65546244286bc841d1c1b2793cd8f7a66b65e41f6e02200a6ef2c5e8cf5350eef2b1747ec47c232089b9336d5ca1b53b8a5fcb01186ce5012102be723dd580b8bdfa46b0b8abf5a9def2e3f8abab7d34cd10c09e20bf9ee36893d8aa0900";

        byte[] rawTxBytes = BaseEncoding.base16().decode(rawTx.toUpperCase());
        Transaction tx = new Transaction(MainNetParams.get(), rawTxBytes);

        TxScoreService.ScoredTransaction scoredTransaction = this.sut.scoreTransaction(tx).blockFirst();

        Optional<ScoreLabel> labelOrEmpty = scoredTransaction.getLabels().stream()
                .filter(val -> "script_types".equals(val.getName()))
                .findFirst();

        assertThat(labelOrEmpty.isPresent(), is(true));
    }


    @Test
    public void simple_reuse_same_address_in_inputs() {
        // https://blockchair.com/bitcoin/transaction/b16f9802004ade2df1c764daa811f021bd424e5070da706cee8e2e6e384e0273
        String txId = "b16f9802004ade2df1c764daa811f021bd424e5070da706cee8e2e6e384e0273";
        String rawTx = "0100000009488f4463fc218388f26d0790419eec7641758fa3712d8f7c9d9189d5d0af5807000000006a47304402204f4b038866d6f133498d1aa960edee6952233c750e1a19cd92bfd2ba985f3f16022005184bbd1c8fb716b4e4345a589053784f9c51c0330dac1cda14d2b9c2ba8fc8012102f49853e0446929f293c1265879eb6f3b79ec648fb3ce95751f651fd381c8e863ffffffff5f70e2ff60027287d18511c83a266db6b7d66e209b264001af3bd2fc5cf56f1e000000006a473044022049e51c320ed7990e3e2812a4dcfcd40197d571144b3748cde0e42f1f421ef9c902200837f0acacafab7f685ef2e1e6789851a42582a2a927ccf7a24b1571cc20787901210365149a5ca007e003e112456ba0f575ab0f9775fd7ab9d80fece1b16b3ece762cffffffff22cc348777c82aa8b635ffc6bbccfa013184628b7eb28164b1e13386f5069f70010000006b483045022100adb8c7f3342e76264918215121ea005521b88cf7540ad6f3337df344c40bd1e102202c8af6e49ce21b91b5d32bd03f0f5f23c7610f21e3a36a5cb95413d3bef47b8a01210365149a5ca007e003e112456ba0f575ab0f9775fd7ab9d80fece1b16b3ece762cffffffff267982a9a10f89231ae54c76ed50aa8cf785cc680d4643d416b5e22181d30b9f000000006a473044022073698cd223847f8879723b425ffade58f49a43e67997ae7070f5882f6511a5a002207146953e1eed7fbd303fb918f83fc22e0d25994c69de3facdbb144b5e9cb04c301210365149a5ca007e003e112456ba0f575ab0f9775fd7ab9d80fece1b16b3ece762cffffffffd5344875082cd7486a06806062ad7b5c335f3cddb752250a33871c3349cc25ba000000006a473044022012c63fec1ddfffa472870e8b0f19e5747d9fe81b6164e5b8663f51f793c810b0022038c66f4fa0787ee25791f15af3b2ab766cbefdff7e1915e9dc634882fb1f8fad01210365149a5ca007e003e112456ba0f575ab0f9775fd7ab9d80fece1b16b3ece762cffffffffc0e5d023d55105c63158ff7348f2e68bf139e00192013ddcb31d5f2ea2c798d3000000006b483045022100d9eb4c8ff9076c06bc48f2ce0f189d1afeb02e32db54aed9f97a4a35f87c76dd0220548196cc2960d6fe279a754b249a26f36bb7d47a6740f4d5abd2dddb4163d306012102a4c07b2fc43364ede764488efb39229d684623a564ebd8c0aa1157867257f0faffffffff661991e6f3bab281274bac7bbac4f7d0eea42c216031f5c6d51a19339ebcc8d9010000006b483045022100c26be99eef838a54869cbdba80c61043c2b677f0640c245f1ae9a6894c316d6f02202e1c8057a63c6428353080c5d2ce14708c9a9c090784cd8556cfbe0c8f481b7f01210365149a5ca007e003e112456ba0f575ab0f9775fd7ab9d80fece1b16b3ece762cffffffff240622ecbeca78650ae059509f95cde3988da4c399c640d1bbecca6bfd6630fc000000006a473044022002f9dcd68f556421f8a1ef419805ebf1101703045b538b1451d5bdcc37662f110220658b45d48993034d4e03bdc23090e3556c03b6a19b84c26a9fa12365429d727d01210365149a5ca007e003e112456ba0f575ab0f9775fd7ab9d80fece1b16b3ece762cffffffff5bbead7dd54c9b0addff899f9d77250406bd5cc592212230d437c1f0254eeffd010000006b483045022100f58c0a03bd92e67ed1d8ea9c4b804cdb298ca5d8000746a4751b7dfa06d8bce802200890dbb0ee91596cb5178bb191b731cd347e89e68f6296847ddd966b6c0cacc501210365149a5ca007e003e112456ba0f575ab0f9775fd7ab9d80fece1b16b3ece762cffffffff020b110e00000000001976a91464e0dbdf7b8771ae38d70ab6b0e7c4c36ff19ae288ac821d0b05000000001976a914085d542fee1c923bdb371f95fac39bc3b34a2c9988ac00000000";

        byte[] rawTxBytes = BaseEncoding.base16().decode(rawTx.toUpperCase());
        Transaction tx = new Transaction(MainNetParams.get(), rawTxBytes);

        TxScoreService.ScoredTransaction scoredTransaction = this.sut.scoreTransaction(tx).blockFirst();

        Optional<ScoreLabel> labelOrEmpty = scoredTransaction.getLabels().stream()
                .filter(val -> "simple_reuse_same_address_in_inputs".equals(val.getName()))
                .findFirst();

        assertThat(labelOrEmpty.isPresent(), is(true));
    }


    @Test
    public void round_fee() {
        // https://blockchair.com/bitcoin/transaction/fb8c2c96d1f442e2e66fe1eccd519a5658eeb9ea5de7f79bef96d82b22755854
        String txId = "fb8c2c96d1f442e2e66fe1eccd519a5658eeb9ea5de7f79bef96d82b22755854";
        String rawTx = "0200000000010145fd7fd53a850b76a4dc3c52109ee4036238a87c78ba7adfbe0a7288c2a59ad10100000000fdffffff02f8240100000000001600147bf8e466c69b3e13558cfa1349a9600246421ee7f049020000000000160014310010ccde94f42580d790bfaa5ff07324f5c08102483045022100f52d77f2944209c5fde18d8a3c94473bd1ee595e65de4a79c5762ef11dc8ddde022071717d45b6df0f2fbd075d1e8080669a1e7cd256b8b063f824dc724c5142cd9e01210312972e5c7ae75d995761972a6c9cb020f4d1401f24c34a87454794cbfa5db0b2ceaa0900";

        byte[] rawTxBytes = BaseEncoding.base16().decode(rawTx.toUpperCase());
        Transaction tx = new Transaction(MainNetParams.get(), rawTxBytes);

        TxScoreService.ScoredTransaction scoredTransaction = this.sut.scoreTransaction(tx).blockFirst();

        Optional<ScoreLabel> labelOrEmpty = scoredTransaction.getLabels().stream()
                .filter(val -> "round_fee".equals(val.getName()))
                .findFirst();

        assertThat(labelOrEmpty.isPresent(), is(true));
    }

}
