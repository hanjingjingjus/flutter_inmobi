// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package global.juscall.flutter_inmobi;

import android.app.Activity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.BannerAdEventListener;
import com.inmobi.ads.listeners.InterstitialAdEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

abstract class MobileAd {
    private static final String TAG = "flutter";
    private static SparseArray<MobileAd> allAds = new SparseArray<MobileAd>();

    final Activity activity;
    final MethodChannel channel;
    final int id;
    Status status;
    double anchorOffset;
    double horizontalCenterOffset;
    int anchorType;

    public enum Status {
        CREATED,
        LOADING,
        FAILED,
        PENDING, // The ad will be shown when status is changed to LOADED.
        LOADED,
    }

    private MobileAd(int id, Activity activity, MethodChannel channel) {
        this.id = id;
        this.activity = activity;
        this.channel = channel;
        this.status = Status.CREATED;
        this.anchorOffset = 0.0;
        this.horizontalCenterOffset = 0.0;
        this.anchorType = Gravity.BOTTOM;
        allAds.put(id, this);
    }

    static Banner createBanner(Integer id, Integer adSizeHeight, Integer adSizeWidth, Activity activity, MethodChannel channel) {
        MobileAd ad = getAdForId(id);
        return (ad != null) ? (Banner) ad : new Banner(id, adSizeHeight, adSizeWidth, activity, channel);
    }

    static Interstitial createInterstitial(Integer id, Activity activity, MethodChannel channel) {
        MobileAd ad = getAdForId(id);
        return (ad != null) ? (Interstitial) ad : new Interstitial(id, activity, channel);
    }

    static MobileAd getAdForId(Integer id) {
        return allAds.get(id);
    }

    Status getStatus() {
        return status;
    }

    abstract void load(String adUnitId, Map<String, Object> targetingInfo);

    abstract void show();

    abstract String getMediationAdapterClassName();

    void dispose() {
        allAds.remove(id);
    }

    static void disposeAll() {
        for (int i = 0; i < allAds.size(); i++) {
            allAds.valueAt(i).dispose();
        }
        allAds.clear();
    }

//    private Map<String, Object> argumentsMap(Object... args) {
//        Map<String, Object> arguments = new HashMap<String, Object>();
//        arguments.put("id", id);
//        for (int i = 0; i < args.length; i += 2) arguments.put(args[i].toString(), args[i + 1]);
//        return arguments;
//    }
//
//    @Override
//    public void onAdLoaded() {
//        boolean statusWasPending = status == Status.PENDING;
//        status = Status.LOADED;
//        channel.invokeMethod("onAdLoaded", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
//        if (statusWasPending) show();
//    }
//
//    @Override
//    public void onAdFailedToLoad(int errorCode) {
//        Log.w(TAG, "onAdFailedToLoad: " + errorCode);
//        status = Status.FAILED;
//        channel.invokeMethod("onAdFailedToLoad", argumentsMap("errorCode", errorCode));
//    }
//
//    @Override
//    public void onAdOpened() {
//        channel.invokeMethod("onAdOpened", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
//    }
//
//    @Override
//    public void onAdClicked() {
//        channel.invokeMethod("onAdClicked", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
//    }
//
//    @Override
//    public void onAdImpression() {
//        channel.invokeMethod("onAdImpression", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
//    }
//
//    @Override
//    public void onAdLeftApplication() {
//        channel.invokeMethod("onAdLeftApplication", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
//    }
//
//    @Override
//    public void onAdClosed() {
//        channel.invokeMethod("onAdClosed", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
//    }

    static class Banner extends MobileAd {
        private InMobiBanner adView;
        private Integer adSizeHeight;
        private Integer adSizeWidth;

        @Override
        public String getMediationAdapterClassName() {
            return "Inmobi";
//            try {
//                return adView != null ? adView.getMediationAdapterClassName() : "";
//            } catch (Exception e) {
//                return "";
//            }
        }

        private Banner(Integer id, Integer adSizeWidth, Integer adSizeHeight, Activity activity, MethodChannel channel) {
            super(id, activity, channel);
            this.adSizeWidth = adSizeWidth;
            this.adSizeHeight = adSizeHeight;
        }

        private Map<String, Object> argumentsMap(Object... args) {
            Map<String, Object> arguments = new HashMap<String, Object>();
            arguments.put("id", id);
            for (int i = 0; i < args.length; i += 2) arguments.put(args[i].toString(), args[i + 1]);
            return arguments;
        }

        @Override
        void load(String adUnitId, Map<String, Object> targetingInfo) {
            if (status != Status.CREATED) return;
            status = Status.LOADING;

            adView = new InMobiBanner(activity, Long.parseLong(adUnitId));
//            adView.setAdSize(adSize);
//            adView.setAdUnitId(adUnitId);
            adView.setBannerSize(adSizeWidth, adSizeHeight);
            adView.setListener(new BannerAdEventListener() {
                @Override
                public void onAdLoadSucceeded(InMobiBanner inMobiBanner) {
                    super.onAdLoadSucceeded(inMobiBanner);
                    boolean statusWasPending = status == Status.PENDING;
                    status = Status.LOADED;
                    channel.invokeMethod("onAdLoaded", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
                    if (statusWasPending) show();

                }

                @Override
                public void onAdLoadFailed(InMobiBanner inMobiBanner, InMobiAdRequestStatus inMobiAdRequestStatus) {
                    super.onAdLoadFailed(inMobiBanner, inMobiAdRequestStatus);
                    Log.w(TAG, "onAdFailedToLoad: " + inMobiAdRequestStatus.getMessage() + inMobiAdRequestStatus.getStatusCode());

                    status = Status.FAILED;
                    channel.invokeMethod("onAdFailedToLoad", argumentsMap("errorCode", getIndex(inMobiAdRequestStatus.getStatusCode())));
                }

                @Override
                public void onAdClicked(InMobiBanner inMobiBanner, Map<Object, Object> map) {
                    super.onAdClicked(inMobiBanner, map);
                    channel.invokeMethod("onAdClicked", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
                }

                @Override
                public void onAdDisplayed(InMobiBanner inMobiBanner) {
                    super.onAdDisplayed(inMobiBanner);
                    channel.invokeMethod("onAdOpened", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));

                }

                @Override
                public void onAdDismissed(InMobiBanner inMobiBanner) {
                    super.onAdDismissed(inMobiBanner);
                    channel.invokeMethod("onAdClosed", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
                }

                @Override
                public void onUserLeftApplication(InMobiBanner inMobiBanner) {
                    super.onUserLeftApplication(inMobiBanner);
                    channel.invokeMethod("onAdLeftApplication", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
                }

                @Override
                public void onRewardsUnlocked(InMobiBanner inMobiBanner, Map<Object, Object> map) {
                    super.onRewardsUnlocked(inMobiBanner, map);
                }

                @Override
                public void onRequestPayloadCreated(byte[] bytes) {
                    super.onRequestPayloadCreated(bytes);
                }

                @Override
                public void onRequestPayloadCreationFailed(InMobiAdRequestStatus inMobiAdRequestStatus) {
                    super.onRequestPayloadCreationFailed(inMobiAdRequestStatus);
                }
            });

            AdRequestBuilderFactory factory = new AdRequestBuilderFactory(targetingInfo);
//            adView.loadAd(factory.createAdRequestBuilder().build());
            List testDevices = factory.getTargetingInfoArrayList("testDevices", targetingInfo.get("testDevices"));
            if (testDevices != null) {
                for (Object deviceValue : testDevices) {
                    String device = factory.getTargetingInfoString("testDevices element", deviceValue);
                    //todo 添加测试设备
//                    if (device != null) Inmo.addTestDevice(device);
                }
            }
            adView.load();
        }

        @Override
        void show() {
            if (status == Status.LOADING) {
                status = Status.PENDING;
                return;
            }
            if (status != Status.LOADED) return;

            if (activity.findViewById(id) == null) {
                LinearLayout content = new LinearLayout(activity);
                content.setId(id);
                content.setOrientation(LinearLayout.VERTICAL);
                content.setGravity(anchorType);
                content.addView(adView);
                final float scale = activity.getResources().getDisplayMetrics().density;

                int left = horizontalCenterOffset > 0 ? (int) (horizontalCenterOffset * scale) : 0;
                int right =
                        horizontalCenterOffset < 0 ? (int) (Math.abs(horizontalCenterOffset) * scale) : 0;
                if (anchorType == Gravity.BOTTOM) {
                    content.setPadding(left, 0, right, (int) (anchorOffset * scale));
                } else {
                    content.setPadding(left, (int) (anchorOffset * scale), right, 0);
                }

                activity.addContentView(
                        content,
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }

        @Override
        void dispose() {
            super.dispose();

            adView.destroy();

            View contentView = activity.findViewById(id);
            if (contentView == null || !(contentView.getParent() instanceof ViewGroup)) return;

            ViewGroup contentParent = (ViewGroup) (contentView.getParent());
            contentParent.removeView(contentView);
        }
    }

    static class Interstitial extends MobileAd {
        private InMobiInterstitial interstitial = null;

        public Interstitial(int id, Activity activity, MethodChannel channel) {
            super(id, activity, channel);
        }

        @Override
        public String getMediationAdapterClassName() {
            return "";
//            try {
//                return interstitial != null && interstitial.isLoaded() ? interstitial.getMediationAdapterClassName() : "";
//            } catch (Exception e) {
//                return "";
//            }
        }

        private Map<String, Object> argumentsMap(Object... args) {
            Map<String, Object> arguments = new HashMap<String, Object>();
            arguments.put("id", id);
            for (int i = 0; i < args.length; i += 2) arguments.put(args[i].toString(), args[i + 1]);
            return arguments;
        }

        @Override
        void load(String adUnitId, Map<String, Object> targetingInfo) {
            status = Status.LOADING;

            interstitial = new InMobiInterstitial(activity, Long.parseLong(adUnitId), new InterstitialAdEventListener() {
                @Override
                public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial) {
                    super.onAdLoadSucceeded(inMobiInterstitial);
                    boolean statusWasPending = status == Status.PENDING;
                    status = Status.LOADED;
                    channel.invokeMethod("onAdLoaded", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
                    if (statusWasPending) show();
                }

                @Override
                public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus inMobiAdRequestStatus) {
                    super.onAdLoadFailed(inMobiInterstitial, inMobiAdRequestStatus);
                    Log.w(TAG, "onAdFailedToLoad: " + inMobiAdRequestStatus.getMessage() + "," + inMobiAdRequestStatus.getStatusCode());

                    status = Status.FAILED;
                    channel.invokeMethod("onAdFailedToLoad", argumentsMap("errorCode", getIndex(inMobiAdRequestStatus.getStatusCode())));
                }

                @Override
                public void onAdReceived(InMobiInterstitial inMobiInterstitial) {
                    super.onAdReceived(inMobiInterstitial);
                }

                @Override
                public void onAdClicked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                    super.onAdClicked(inMobiInterstitial, map);
                    channel.invokeMethod("onAdClicked", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
                }

                @Override
                public void onAdWillDisplay(InMobiInterstitial inMobiInterstitial) {
                    super.onAdWillDisplay(inMobiInterstitial);
                }

                @Override
                public void onAdDisplayed(InMobiInterstitial inMobiInterstitial) {
                    super.onAdDisplayed(inMobiInterstitial);
                    channel.invokeMethod("onAdOpened", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
                }

                @Override
                public void onAdDisplayFailed(InMobiInterstitial inMobiInterstitial) {
                    super.onAdDisplayFailed(inMobiInterstitial);
                }

                @Override
                public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
                    super.onAdDismissed(inMobiInterstitial);
                    channel.invokeMethod("onAdClosed", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
                }

                @Override
                public void onUserLeftApplication(InMobiInterstitial inMobiInterstitial) {
                    super.onUserLeftApplication(inMobiInterstitial);
                    channel.invokeMethod("onAdLeftApplication", argumentsMap("adResource", getAdForId(id).getMediationAdapterClassName()));
                }

                @Override
                public void onRewardsUnlocked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                    super.onRewardsUnlocked(inMobiInterstitial, map);
                }

                @Override
                public void onRequestPayloadCreated(byte[] bytes) {
                    super.onRequestPayloadCreated(bytes);
                }

                @Override
                public void onRequestPayloadCreationFailed(InMobiAdRequestStatus inMobiAdRequestStatus) {
                    super.onRequestPayloadCreationFailed(inMobiAdRequestStatus);
                }
            });
//            interstitial.setAdUnitId(adUnitId);

//            interstitial.setAdListener(this);
            AdRequestBuilderFactory factory = new AdRequestBuilderFactory(targetingInfo);
//            interstitial.loadAd(factory.createAdRequestBuilder().build());
            List testDevices = factory.getTargetingInfoArrayList("testDevices", targetingInfo.get("testDevices"));
            if (testDevices != null) {
                for (Object deviceValue : testDevices) {
                    String device = factory.getTargetingInfoString("testDevices element", deviceValue);
                    //todo 添加测试设备
//                    if (device != null) Inmo.addTestDevice(device);
                }
            }
            interstitial.load();
        }

        @Override
        void show() {
            if (status == Status.LOADING) {
                status = Status.PENDING;
                return;
            }
            interstitial.show();
        }

        // It is not possible to hide/remove/destroy an AdMob interstitial Ad.
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

}
