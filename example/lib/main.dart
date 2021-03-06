// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// ignore_for_file: public_member_api_docs

import 'package:flutter/material.dart';
import 'package:flutter_inmobi/flutter_inmobi.dart';

// You can also test with your own ad unit IDs by registering your device as a
// test device. Check the logs for your device's ID value.
const String testDevice = 'YOUR_DEVICE_ID';

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  static const InmobiMobileAdTargetingInfo targetingInfo =
      InmobiMobileAdTargetingInfo(
    testDevices: testDevice != null ? <String>[testDevice] : null,
    keywords: <String>['foo', 'bar'],
    contentUrl: 'http://foo.com/bar.html',
    childDirected: true,
    nonPersonalizedAds: true,
  );

  InmobiBannerAd _bannerAd;
  InmobiInterstitialAd _interstitialAdtest;
  InmobiInterstitialAd _interstitialAd;
  int _coins = 0;
  String state0;
  String state1;
  String state2;

  InmobiBannerAd createBannerAd() {
    return InmobiBannerAd(
      adUnitId: /*BannerAd.testAdUnitId*/ "1583079773786",
      size: InmobiAdSize.banner,
      targetingInfo: targetingInfo,
      listener: (InmobiMobileAdEvent event, {String adResource}) {
        print("BannerAd event $event");
      },
    );
  }

  InmobiInterstitialAd createInterstitialAdtest() {
    return InmobiInterstitialAd(
      adUnitId: /*InterstitialAd.testAdUnitId*/ "1585427131512",
      targetingInfo: targetingInfo,
      listener: (InmobiMobileAdEvent event, {String adResource}) {
        print("InterstitialAd event $event");
        setState(() {
          state0 = event.toString();
        });
      },
    );
  }

  InmobiInterstitialAd createInterstitialAd() {
    return InmobiInterstitialAd(
      adUnitId: /*InterstitialAd.testAdUnitId*/ "1581746847763",
      targetingInfo: targetingInfo,
      listener: (InmobiMobileAdEvent event, {String adResource}) {
        print("InterstitialAd event $event");
        setState(() {
          state1 = event.toString();
        });
      },
    );
  }

  @override
  void initState() {
    super.initState();
    FlutterInmobi.instance.initialize(
        appId: /*FirebaseAdMob.testAppId*/ "eed13362e55b4b49b9e99ed27c736777");
//    _bannerAd = createBannerAd()..load();
    InmobiRewardedVideoAd.instance.listener = (InmobiRewardedVideoAdEvent event,
        {String rewardType, int rewardAmount, String adResource}) {
      print("RewardedVideoAd event $event");
      if (event == InmobiRewardedVideoAdEvent.rewarded) {
        setState(() {
          _coins += rewardAmount;
          state2 = event.toString();
        });
      }
    };
  }

  @override
  void dispose() {
    _bannerAd?.dispose();
    _interstitialAd?.dispose();
    _interstitialAdtest?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Inmobi example app'),
        ),
        body: SingleChildScrollView(
          child: Center(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                RaisedButton(
                  child: const Text('LOAD INTERSTITIAL(测试模式开启)'),
                  onPressed: () {
                    _interstitialAdtest?.dispose();
                    _interstitialAdtest = createInterstitialAdtest()..load();
                  },
                ),

                RaisedButton(
                  child: const Text('SHOW INTERSTITIAL'),
                  onPressed: () {
                    _interstitialAdtest?.show();
                  },
                ),

                Padding(
                  padding: EdgeInsets.only(bottom: 20),
                  child: Text("广告状态：$state0"),
                ),

                RaisedButton(
                  child: const Text('LOAD INTERSTITIAL(测试模式关闭)'),
                  onPressed: () {
                    _interstitialAd?.dispose();
                    _interstitialAd = createInterstitialAd()..load();
                  },
                ),

                RaisedButton(
                  child: const Text('SHOW INTERSTITIAL'),
                  onPressed: () {
                    _interstitialAd?.show();
                  },
                ),

                Text("广告状态：$state1"),

//                RaisedButton(
//                  child: const Text('LOAD REWARDED VIDEO（正式）'),
//                  onPressed: () {
//                    InmobiRewardedVideoAd.instance.load(
//                        adUnitId: '1579718203512'/*RewardedVideoAd.testAdUnitId*/,
//                        targetingInfo: targetingInfo);
//                  },
//                ),
//                Text("状态$state2"),
//                RaisedButton(
//                  child: const Text('SHOW REWARDED VIDEO'),
//                  onPressed: () {
//                    InmobiRewardedVideoAd.instance.show();
//                  },
//                ),
//                Text("You have $_coins coins."),
              ].map((Widget button) {
                return Padding(
                  padding: const EdgeInsets.symmetric(vertical: 16.0),
                  child: button,
                );
              }).toList(),
            ),
          ),
        ),
      ),
    );
  }
}

void main() {
  runApp(MyApp());
}
