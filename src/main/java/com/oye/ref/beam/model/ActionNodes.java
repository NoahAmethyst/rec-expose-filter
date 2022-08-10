package com.oye.ref.beam.model;


import lombok.Builder;
import lombok.Data;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

@DefaultCoder(AvroCoder.class)
@Data
@Builder
public class ActionNodes {

    private UserAction currentAction;
    private UserAction previousAction;


}
