package io.extremum.descriptors.common.redisson;

import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.Decoder;
import org.redisson.codec.CompositeCodec;

/**
 * Redisson 3.10.5 contains a bug in CompositeCodec. It is fixed
 * in the current redisson version, but it would require some work to
 * upgrade to it. This will be addressed separately in CS-1271.
 *
 * @author rpuch
 */
public final class CompositeCodecWithQuickFix extends CompositeCodec {
    private final Codec mapValueCodec;

    public CompositeCodecWithQuickFix(Codec mapKeyCodec, Codec mapValueCodec) {
        super(mapKeyCodec, mapValueCodec);

        this.mapValueCodec = mapValueCodec;
    }

    @Override
    public Decoder<Object> getMapValueDecoder() {
        return mapValueCodec.getMapValueDecoder();
    }
}
