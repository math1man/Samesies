package com.dfaenterprises.samesies.apn;

import apns.ApnsConnection;
import apns.ApnsConnectionFactory;
import apns.ApnsException;
import apns.DefaultApnsConnectionFactory;
import apns.keystore.ClassPathResourceKeyStoreProvider;
import apns.keystore.KeyStoreProvider;
import apns.keystore.KeyStoreType;
import com.dfaenterprises.samesies.Constants;

import java.io.IOException;

/**
 * @author Ari Weiland
 */
public class ApnManager {

    private static ApnsConnectionFactory prodFactory = null;
    private static ApnsConnectionFactory devFactory = null;

    public static ApnsConnectionFactory getFactory(boolean isProd) throws ApnsException, IOException {
        DefaultApnsConnectionFactory.Builder builder = DefaultApnsConnectionFactory.Builder.get();
        if (isProd) {
            if (prodFactory == null) {
                KeyStoreProvider ksp = new ClassPathResourceKeyStoreProvider("SamesiesProdAPN.p12", KeyStoreType.PKCS12, Constants.APN_CERT_KEY);
                builder.setProductionKeyStoreProvider(ksp);
                prodFactory = builder.build();
            }
            return prodFactory;
        } else {
            if (devFactory == null) {
                KeyStoreProvider ksp = new ClassPathResourceKeyStoreProvider("SamesiesDevAPN.p12", KeyStoreType.PKCS12, Constants.APN_CERT_KEY);
                builder.setSandboxKeyStoreProvider(ksp);
                devFactory = builder.build();
            }
            return devFactory;
        }
    }

    public static ApnsConnection getPushConnection(boolean isProd) throws ApnsException, IOException {
        return getFactory(isProd).openPushConnection();
    }

    public static ApnsConnection getFeedbackConnection(boolean isProd) throws ApnsException, IOException {
        return getFactory(isProd).openFeedbackConnection();
    }

}
