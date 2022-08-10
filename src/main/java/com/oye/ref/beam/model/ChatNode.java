package com.oye.ref.beam.model;


import lombok.Builder;
import lombok.Data;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

import java.util.Objects;

@DefaultCoder(AvroCoder.class)
@Data
@Builder
public class ChatNode implements Comparable<ChatNode> {

    private String uidA;

    private String uidB;

    private String spm;

    private long timestamp;

    private String chatAction;

    private int isCover;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatNode chatNode = (ChatNode) o;
        return isCover == chatNode.isCover &&
                (uidA.equals(chatNode.uidA) ||
                        uidB.equals(chatNode.uidB))
                ;
    }

    @Override
    public int hashCode() {
        String a = "empty".equals(uidA) ? "0" : uidA;
        String b = "empty".equals(uidA) ? "0" : uidB;
        long value = Long.valueOf(a) + Long.valueOf(b);
        return Objects.hash(value, isCover);
    }

    @Override
    public int compareTo(ChatNode o) {
        long r = o.timestamp - this.timestamp;
        if (r > 0) {
            return 1;
        } else if (r < 0) {
            return -1;
        } else {
            return 0;
        }
    }
}
