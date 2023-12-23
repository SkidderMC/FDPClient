package net.ccbluex.liquidbounce.handler.protocol.api;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.raphimc.vialegacy.protocols.classic.protocola1_0_15toc0_28_30.providers.ClassicMPPassProvider;
import net.raphimc.vialegacy.protocols.release.protocol1_3_1_2to1_2_4_5.providers.OldAuthProvider;
import net.raphimc.vialegacy.protocols.release.protocol1_7_2_5to1_6_4.storage.HandshakeStorage;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.Scanner;
import java.util.logging.Level;

public class ProtocolClassicMPPassProvider extends ClassicMPPassProvider {

    @Override
    public String getMpPass(UserConnection user) {
        final HandshakeStorage handshakeStorage = user.get(HandshakeStorage.class);

        return getBetacraftMpPass(user, user.getProtocolInfo().getUsername(), handshakeStorage.getHostname(), handshakeStorage.getPort());
    }

    private static String getBetacraftMpPass(final UserConnection user, final String username, final String ip, final int port) {
        try {
            final String server = InetAddress.getByName(ip).getHostAddress() + ":" + port;

            Via.getManager().getProviders().get(OldAuthProvider.class).sendAuthRequest(user, sha1(server.getBytes()));

            final InputStream connection = new URL("https://api.betacraft.uk/getmppass.jsp?user=" + username + "&server=" + server).openStream();
            Scanner scanner = new Scanner(connection);
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.next());
            }
            connection.close();

            if (response.toString().contains("FAILED") || response.toString().contains("SERVER NOT FOUND")) return "0";

            return response.toString();
        } catch (Throwable t) {
            Via.getPlatform().getLogger().log(Level.WARNING, "An unknown error occurred while authenticating with BetaCraft", t);
            return "0";
        }
    }

    private static String sha1(final byte[] input) {
        try {
            Formatter formatter = new Formatter();
            final byte[] hash = MessageDigest.getInstance("SHA-1").digest(input);
            for (byte b : hash) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        } catch (Exception e) {
            return null;
        }
    }

}