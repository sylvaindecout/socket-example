package test.sdc.socket.common;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.compression.JdkZlibEncoder;
import io.netty.handler.codec.compression.SnappyFramedDecoder;
import io.netty.handler.codec.compression.SnappyFramedEncoder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Data compression formats used for client-server communication.
 */
public enum DataCompressionFormat {

    SNAPPY("SNAPPY") {
        @Override
        public ChannelHandler newEncoder() {
            return new SnappyFramedEncoder();
        }

        @Override
        public ChannelHandler newDecoder() {
            return new SnappyFramedDecoder();
        }
    },
    JDK_ZLIB("ZLIB") {
        @Override
        public ChannelHandler newEncoder() {
            return new JdkZlibEncoder();
        }

        @Override
        public ChannelHandler newDecoder() {
            return new JdkZlibDecoder();
        }
    };

    private static final Map<String, DataCompressionFormat> KEY_INDEX = new HashMap<>();

    static {
        for (final DataCompressionFormat instance : DataCompressionFormat.values()) {
            final String keyUC = instance.key == null ? null : instance.key.toUpperCase(Locale.ROOT);
            KEY_INDEX.put(keyUC, instance);
        }
    }

    private final String key;

    /**
     * Constructor.
     *
     * @param key configuration key
     */
    DataCompressionFormat(final String key) {
        this.key = key;
    }

    /**
     * Get instance from corresponding key (not case sensitive).
     *
     * @param key key
     * @return instance
     */
    public static Optional<DataCompressionFormat> of(final String key) {
        final String keyUC = key == null ? null : key.toUpperCase(Locale.ROOT);
        return KEY_INDEX.containsKey(keyUC)
                ? Optional.of(KEY_INDEX.get(keyUC))
                : Optional.empty();
    }

    /**
     * Get key used to identify format.
     *
     * @return key used to identify format
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Create new data encoder instance that handles data compression for current format.
     *
     * @return data encoder
     */
    public abstract ChannelHandler newEncoder();

    /**
     * Create new data decoder instance that handles data compression for current format.
     *
     * @return data decoder
     */
    public abstract ChannelHandler newDecoder();

}