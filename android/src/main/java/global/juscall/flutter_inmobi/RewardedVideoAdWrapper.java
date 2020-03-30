// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package global.juscall.flutter_inmobi;

import android.app.Activity;
import android.util.Log;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.InterstitialAdEventListener;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

public class RewardedVideoAdWrapper extends InterstitialAdEventListener {
    private static final String TAG = "flutter";

    private final InMobiInterstitial rewardedInstance;
    private final MethodChannel channel;
    private Status status;

    @Override
    public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial) {
        super.onAdLoadSucceeded(inMobiInterstitial);
        status = Status.LOADED;
        channel.invokeMethod("onRewardedVideoAdLoaded", argumentsMap("adResource", getMediationAdapterClassName()));
    }

    @Override
    public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus inMobiAdRequestStatus) {
        super.onAdLoadFailed(inMobiInterstitial, inMobiAdRequestStatus);
        Log.w(TAG, "onAdFailedToLoad: " + inMobiAdRequestStatus.getMessage() + "," + inMobiAdRequestStatus.getStatusCode());

        status = Status.FAILED;
        Log.w(TAG, "onRewardedVideoAdFailedToLoad: " + getIndex(inMobiAdRequestStatus.getStatusCode()));
        status = Status.FAILED;
        channel.invokeMethod("onRewardedVideoAdFailedToLoad", argumentsMap("errorCode", getIndex(inMobiAdRequestStatus.getStatusCode())));
    }

    @Override
    public void onAdReceived(InMobiInterstitial inMobiInterstitial) {
        super.onAdReceived(inMobiInterstitial);
        channel.invokeMethod("onRewardedVideoCompleted", argumentsMap("adResource", getMediationAdapterClassName()));
    }

    @Override
    public void onAdClicked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
        super.onAdClicked(inMobiInterstitial, map);
        channel.invokeMethod("onRewardedVideoAdOpened", argumentsMap("adResource", getMediationAdapterClassName()));
    }

    @Override
    public void onAdWillDisplay(InMobiInterstitial inMobiInterstitial) {
        super.onAdWillDisplay(inMobiInterstitial);
        channel.invokeMethod("onRewardedVideoStarted", argumentsMap("adResource", getMediationAdapterClassName()));
    }

    @Override
    public void onAdDisplayed(InMobiInterstitial inMobiInterstitial) {
        super.onAdDisplayed(inMobiInterstitial);
        channel.invokeMethod("onRewardedVideoStarted", argumentsMap("adResource", getMediationAdapterClassName()));
    }

    @Override
    public void onAdDisplayFailed(InMobiInterstitial inMobiInterstitial) {
        super.onAdDisplayFailed(inMobiInterstitial);
    }

    @Override
    public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
        super.onAdDismissed(inMobiInterstitial);
        channel.invokeMethod("onRewardedVideoAdClosed", argumentsMap("adResource", getMediationAdapterClassName()));
    }

    @Override
    public void onUserLeftApplication(InMobiInterstitial inMobiInterstitial) {
        super.onUserLeftApplication(inMobiInterstitial);
        channel.invokeMethod("onRewardedVideoAdLeftApplication", argumentsMap("adResource", getMediationAdapterClassName()));
    }

    @Override
    public void onRewardsUnlocked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
        super.onRewardsUnlocked(inMobiInterstitial, map);
        Log.d(TAG, "onRewardsUnlocked " + map.toString());
        //测试广告只有{default=default}
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            Log.v(TAG, "Unlocked " + value + " " + key);
        }
        channel.invokeMethod(
                "onRewarded",
                argumentsMap("rewardType", map.get("rewardType"), "rewardAmount", map.get("rewardAmount"), "adResource", getMediationAdapterClassName()));
    }
//    @Override
//    public void onAdRewardActionCompleted(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
//        Log.d(TAG, "Ad rewards unlocked!");
//        for (Object key : map.keySet()) {
//            Object value = map.get(key);
//            Log.v(TAG, "Unlocked " + value + " " + key);
//        }
//    }
    @Override
    public void onRequestPayloadCreated(byte[] bytes) {
        super.onRequestPayloadCreated(bytes);
    }

    @Override
    public void onRequestPayloadCreationFailed(InMobiAdRequestStatus inMobiAdRequestStatus) {
        super.onRequestPayloadCreationFailed(inMobiAdRequestStatus);
    }

    enum Status {
        CREATED,
        LOADING,
        FAILED,
        LOADED
    }

    public RewardedVideoAdWrapper(String adUnitId, Activity activity, MethodChannel channel) {
        this.channel = channel;
        this.status = Status.CREATED;
        this.rewardedInstance = new InMobiInterstitial(activity, Long.parseLong(adUnitId), this);
        this.rewardedInstance.setListener(this);
    }

    Status getStatus() {
        return status;
    }

    public void load(String adUnitId, Map<String, Object> targetingInfo) {
        status = Status.LOADING;
        AdRequestBuilderFactory factory = new AdRequestBuilderFactory(targetingInfo);
//        rewardedInstance.loadAd(adUnitId, factory.createAdRequestBuilder().build());
        rewardedInstance.load();
    }

    public void show() {
        if (status == Status.LOADED) {
            rewardedInstance.show();
            return;
        }
    }


    private Map<String, Object> argumentsMap(Object... args) {
        Map<String, Object> arguments = new HashMap<String, Object>();
        for (int i = 0; i < args.length; i += 2) arguments.put(args[i].toString(), args[i + 1]);
        return arguments;
    }

    public int getIndex(InMobiAdRequestStatus.StatusCode code) {
        if (code == InMobiAdRequestStatus.StatusCode.NO_ERROR) {
            return 0;
        } else if (code == InMobiAdRequestStatus.StatusCode.NETWORK_UNREACHABLE) {
            return 1;
        } else if (code == InMobiAdRequestStatus.StatusCode.NO_FILL) {
            return 2;
        } else if (code == InMobiAdRequestStatus.StatusCode.REQUEST_INVALID) {
            return 3;
        } else if (code == InMobiAdRequestStatus.StatusCode.REQUEST_PENDING) {
            return 4;
        } else if (code == InMobiAdRequestStatus.StatusCode.REQUEST_TIMED_OUT) {
            return 5;
        } else if (code == InMobiAdRequestStatus.StatusCode.INTERNAL_ERROR) {
            return 6;
        } else if (code == InMobiAdRequestStatus.StatusCode.SERVER_ERROR) {
            return 7;
        } else if (code == InMobiAdRequestStatus.StatusCode.AD_ACTIVE) {
            return 8;
        } else if (code == InMobiAdRequestStatus.StatusCode.EARLY_REFRESH_REQUEST) {
            return 9;
        } else if (code == InMobiAdRequestStatus.StatusCode.AD_NO_LONGER_AVAILABLE) {
            return 10;
        } else if (code == InMobiAdRequestStatus.StatusCode.MISSING_REQUIRED_DEPENDENCIES) {
            return 11;
        } else if (code == InMobiAdRequestStatus.StatusCode.REPETITIVE_LOAD) {
            return 12;
        } else if (code == InMobiAdRequestStatus.StatusCode.GDPR_COMPLIANCE_ENFORCED) {
            return 13;
        } else if (code == InMobiAdRequestStatus.StatusCode.LOAD_CALLED_AFTER_GET_SIGNALS) {
            return 14;
        } else if (code == InMobiAdRequestStatus.StatusCode.GET_SIGNALS_NOT_CALLED_FOR_LOAD_WITH_RESPONSE) {
            return 15;
        } else if (code == InMobiAdRequestStatus.StatusCode.GET_SIGNALS_CALLED_WHILE_LOADING) {
            return 16;
        } else if (code == InMobiAdRequestStatus.StatusCode.FETCHING_SIGNALS_STATE_ERROR) {
            return 17;
        } else if (code == InMobiAdRequestStatus.StatusCode.LOAD_WITH_RESPONSE_CALLED_WHILE_LOADING) {
            return 18;
        } else if (code == InMobiAdRequestStatus.StatusCode.INVALID_RESPONSE_IN_LOAD) {
            return 19;
        } else if (code == InMobiAdRequestStatus.StatusCode.MONETIZATION_DISABLED) {
            return 20;
        } else {
            //CALLED_FROM_WRONG_THREAD
            return 21;
        }

    }

    public String getMediationAdapterClassName() {
        return "Inmobi";
    }
}
