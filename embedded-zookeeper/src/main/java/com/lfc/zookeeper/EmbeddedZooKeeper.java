package com.lfc.zookeeper;

import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

import java.io.File;
import java.util.Properties;
import java.util.UUID;

public class EmbeddedZooKeeper {

    public static void main(String[] args) throws Exception {
        int port = 2181;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        Properties properties = new Properties();
        File file = new File(System.getProperty("java.io.tmpdir")
                + File.separator + UUID.randomUUID());
        file.deleteOnExit();
        properties.setProperty("dataDir", file.getAbsolutePath());
        properties.setProperty("clientPort", String.valueOf(port));

        QuorumPeerConfig quorumPeerConfig = new QuorumPeerConfig();
        quorumPeerConfig.parseProperties(properties);

        ZooKeeperServerMain zkServer = new ZooKeeperServerMain();
        ServerConfig configuration = new ServerConfig();
        configuration.readFrom(quorumPeerConfig);

        try {
            zkServer.runFromConfig(configuration);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
