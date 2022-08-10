package com.oye.ref.beam.constant;

import java.util.HashSet;
import java.util.Set;

public interface KeyActionsConstant {

    /**
     * key pay action
     */
    String ShowH5CoinRechargePage = "ShowH5CoinRechargePage";

    String ShowCoinRechargePage = "ShowCoinPurchasePage";

    String ShowVIPPurchasePage = "ShowVIPPurchasePage";

    String PaymentSuccess = "PaymentSuccess";

    String PaymentStart = "PaymentStart";

    String ClickAnchorProfile = "ClickAnchorProfile";

    String ShowAnchorPage = "ShowAnchorPage";

    String ShowSPM_01010160000 = "ShowSPM_01010160000";

    String ClickCall = "ClickCall";

    String PopUpCall = "PopUpCall";

    String ClickPrivateContent = "ClickPrivateContent";

    String SendMessage = "SendMessage";

    String ReceiveDeepChatInvite = "ReceiveDeepChatInvite";

    String ClickDeepChatInvite = "ClickDeepChatInvite";

    String ClickFastMatch = "ClickFastMatch";

    String ShowAnchorAuthVideo = "ShowAnchorAuthVideo";

    String UnlockChat = "UnlockChat";

    String SwitchFemale = "SwitchFemale";

    String ReadMessage = "ReadMessage";


    /**
     * key action
     */

    String SendGift = "SendGift";

    String ViewFreeContent = "ViewFreeContent";

    String AcceptFastMatch = "AcceptFastMatch";

    String ClickSPM_01010050301 = "ClickSPM_01010050301";

    String ClickSPM_01010050401 = "ClickSPM_01010050401";

    /**
     * pay funnel actions
     */


    String ClickSPM_01010020301 = "ClickSPM_01010020301";
    String ClickSPM_01010030101 = "ClickSPM_01010030101";
    String ClickSPM_01010110601 = "ClickSPM_01010110601";
    String ClickSPM_01010110801 = "ClickSPM_01010110801";
    String ClickSPM_01010110802 = "ClickSPM_01010110802";
    String ClickSPM_01010110402 = "ClickSPM_01010110402";
    String ClickSPM_01010170101 = "ClickSPM_01010170101";
    String ClickSPM_01010111101 = "ClickSPM_01010111101";
    String ClickSPM_01010111201 = "ClickSPM_01010111201";
    String ClickSPM_01010060202 = "ClickSPM_01010060202";
    String ClickSPM_01010060601 = "ClickSPM_01010060601";
    String ClickTopup = "ClickTopup";
    String ClickGift = "ClickGift";
    String ChooseGender = "ChooseGender";


    Set<String> keyActionSets = new HashSet<String>() {
        {
            add(ClickSPM_01010020301);
            add(ClickSPM_01010030101);
            add(ClickSPM_01010050301);
            add(ClickSPM_01010050401);
            add(ClickSPM_01010110601);
            add(ClickSPM_01010110802);
            add(ClickSPM_01010110402);
            add(ClickSPM_01010170101);
            add(ClickSPM_01010111101);
            add(ClickSPM_01010111201);
            add(ClickSPM_01010060202);
            add(ClickSPM_01010060601);

            add(ShowSPM_01010160000);

            add(ShowCoinRechargePage);
            add(ShowVIPPurchasePage);
            add(PaymentSuccess);
            add(PaymentStart);
            add(ClickAnchorProfile);
            add(ShowAnchorPage);
            add(PopUpCall);
            add(SendMessage);
            add(ReceiveDeepChatInvite);
            add(ClickFastMatch);
            add(ShowAnchorAuthVideo);
            add(SendGift);
            add(ViewFreeContent);
            add(AcceptFastMatch);
            add(ClickCall);
            add(ClickPrivateContent);
            add(ClickDeepChatInvite);
            add(ClickTopup);
            add(ClickGift);
            add(ChooseGender);
            add(SwitchFemale);
            add(UnlockChat);
            add(ReadMessage);
        }
    };

    Set<String> keyPayActionSets = new HashSet<String>() {
        {
//            add(ShowH5CoinRechargePage);
//            add(ShowCoinRechargePage);
//            add(ShowVIPPurchasePage);
            add(PaymentSuccess);

            add(PaymentStart);
            add(ClickAnchorProfile);
            add(ShowAnchorPage);
            add(ShowSPM_01010160000);
            add(ClickCall);
            add(PopUpCall);
            add(ClickPrivateContent);
            add(SendMessage);
            add(ReceiveDeepChatInvite);
            add(ClickDeepChatInvite);
            add(ClickFastMatch);
            add(ShowAnchorAuthVideo);

            add(ClickSPM_01010110802);
            add(ClickTopup);
            add(ClickSPM_01010030101);
            add(UnlockChat);
            add(SendGift);
            add(ChooseGender);
            add(SwitchFemale);
            add(ClickSPM_01010060202);
            add(ClickSPM_01010060601);
        }
    };


    Set<String> payFunnelActionSets = new HashSet<String>() {
        {

            add(ShowCoinRechargePage);
            add(ShowVIPPurchasePage);
            add(PaymentStart);

            add(ClickSPM_01010020301);
            add(ClickSPM_01010030101);
            add(ClickSPM_01010050301);
            add(ClickSPM_01010050401);
            add(ClickSPM_01010110601);
            add(ClickSPM_01010110802);
            add(ClickSPM_01010110402);
            add(ClickSPM_01010170101);
            add(ClickSPM_01010111101);
            add(ClickSPM_01010111201);
            add(ClickSPM_01010060202);
            add(ClickSPM_01010060601);
            add(ClickCall);
            add(ClickPrivateContent);
            add(ClickDeepChatInvite);
            add(ClickTopup);
            add(PopUpCall);
            add(ClickGift);
            add(ChooseGender);
            add(SwitchFemale);
            add(UnlockChat);
        }
    };


    Set<String> specificSpmIds = new HashSet<String>() {
        {
            add("01010020301");
            add("01010030101");
            add("01010050301");
            add("01010050401");
            add("01010050301");
            add("01010050401");
            add("01010110601");
            add("01010110802");
            add("01010110402");
            add("01010170101");
            add("01010111101");
            add("01010111201");
            add("01010060202");
            add("01010060601");
            add("01010110401");
            add("01010110501");
            add("01010050201");
        }
    };

    Set<String> generalSpmIds = new HashSet<String>() {
        {
            add("010100201");
            add("010100303");
            add("010100605");
            add("010100605");
            add("010100702");
            add("010100705");
            add("010101211");
            add("010101212");
            add("010101603");
            add("010102201");
            add("010102301");
            add("010102401");
            add("010102402");
            add("010100000");
            add("010102501");
            add("010100605");
            add("010101205");
            add("010101104");
        }
    };


}
