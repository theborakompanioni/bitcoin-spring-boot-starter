package org.tbk.lightning.regtest.setup.devel.impl;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.tbk.lightning.cln.grpc.ClnRpcConfig;
import org.tbk.lightning.cln.grpc.ClnRpcConfigImpl;
import org.tbk.lightning.regtest.setup.devel.AbstractDevelClnNodeRegistrar;

import java.util.HexFormat;

class LocalClnNodeAppRegistrar extends AbstractDevelClnNodeRegistrar {

    /**
     * As this client connects to the node the application talks to,
     * it must be marked as "primary".
     */
    @Override
    protected BeanDefinitionCustomizer beanDefinitionCustomizer() {
        return bd -> bd.setPrimary(true);
    }

    @Override
    protected String beanNamePrefix() {
        return "nodeApp";
    }

    @Override
    protected String hostname() {
        return "regtest_cln0_app";
    }

    @Override
    protected ClnRpcConfig createClnRpcConfig(SslContext sslContext) {
        return ClnRpcConfigImpl.builder()
                .host("localhost")
                .port(19935)
                .sslContext(sslContext)
                .build();
    }

    @Override
    protected byte[] caCert() {
        // file `lightning-regtest-setup-devel/docker/data/cln0_app/regtest/ca.pem` as hex
        return HexFormat.of().parseHex("2d2d2d2d2d424547494e2043455254494649434154452d2d2d2d2d0d0a4d49494263444343415265674177494241674949415438587564366259476377436759494b6f5a497a6a304541774977466a45554d424947413155454177774c0d0a5932787549464a7662335167513045774942634e4e7a55774d5441784d4441774d444177576867504e4441354e6a41784d4445774d4441774d4442614d4259780d0a4644415342674e5642414d4d43324e73626942536232393049454e424d466b77457759484b6f5a497a6a3043415159494b6f5a497a6a304441516344516741450d0a4a32504b436c627a34526b376244596466365472774a456a455246703132517a59474c70494b3237727736436251356f726d372b31386845572b6669387038700d0a39646245346a4462446274376b6236494a744a37364b4e4e4d457377475159445652305242424977454949445932787567676c7362324e6862476876633351770d0a485159445652304f42425945464764676d393635467a38426f792b5143345663666a506b4933322f4d41384741315564457745422f7751464d414d42416638770d0a436759494b6f5a497a6a30454177494452774177524149674b41754841534446354e7649654243362b637562353578334561667a6c5938313237336c77506e450d0a5a4773434944595864765874495658676c694974474350757839352f394165734b694f377a4d6737425a777a6d3362370d0a2d2d2d2d2d454e442043455254494649434154452d2d2d2d2d");
    }

    @Override
    protected byte[] clientCert() {
        // file `lightning-regtest-setup-devel/docker/data/cln0_app/regtest/client.pem` as hex
        return HexFormat.of().parseHex("2d2d2d2d2d424547494e2043455254494649434154452d2d2d2d2d0d0a4d49494252544342374b41444167454341676b416e4936694152467359786b77436759494b6f5a497a6a304541774977466a45554d424947413155454177774c0d0a5932787549464a7662335167513045774942634e4e7a55774d5441784d4441774d444177576867504e4441354e6a41784d4445774d4441774d4442614d426f780d0a4744415742674e5642414d4d44324e736269426e636e426a49454e73615756756444425a4d424d4742797147534d34394167454743437147534d3439417745480d0a41304941424f724f2f657177514d7752496a2f4c2f7842527843504b67566b59764d504f6d747a43586c417869747179366a79615141726c50496133506534530d0a50776a69624e664a4c757935377a6c4b32546f384b6439615a2f326a485441624d426b4741315564455151534d42434341324e73626f494a6247396a5957786f0d0a62334e304d416f4743437147534d343942414d43413067414d45554349463773344c6d3964565777346443542b797443634a4d7a4f7157375558324c714f45470d0a6542366c6c636661416945412f34354b455a69502b7a3936654e4b484c7542656238794d5569374252554d4a35764d38625472644933453d0d0a2d2d2d2d2d454e442043455254494649434154452d2d2d2d2d");
    }

    @Override
    protected byte[] clientKey() {
        // file `lightning-regtest-setup-devel/docker/data/cln0_app/regtest/client-key.pem` as hex
        return HexFormat.of().parseHex("2d2d2d2d2d424547494e2050524956415445204b45592d2d2d2d2d0d0a4d494748416745414d424d4742797147534d34394167454743437147534d3439417745484247307761774942415151672b457a6b462b30686a63746e686c54430d0a4e4d73327a666f75685946312b446d383351725266567a6472384f6852414e43414154717a7633717345444d4553492f792f38515563516a796f465a474c7a440d0a7a707263776c35514d59726173756f386d6b414b35547947747a3375456a3849346d7a58795337737565383553746b3650436e66576d66390d0a2d2d2d2d2d454e442050524956415445204b45592d2d2d2d2d");
    }
}
