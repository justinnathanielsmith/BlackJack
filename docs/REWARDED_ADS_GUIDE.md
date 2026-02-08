# 📺 Rewarded Ads Implementation Guide

This guide outlines the next steps for completing the rewarded ads integration and provides configuration details for the feature.

## 🚀 Next Steps

### 1. UI Implementation
Create a "Panic Button" in the game screen for Time Attack mode:
- Show button when `state.canShowRewardedAd` is true.
- Display remaining cooldown time when unavailable.
- Show "Play 3 games to unlock" message for new players.
- Call `component.onShowRewardedAd()` on click.

### 2. iOS Setup
Add Google Mobile Ads SDK to the iOS project:
```ruby
# In iosApp/Podfile
pod 'Google-Mobile-Ads-SDK'
```

Update `Info.plist`:
```xml
<key>GADApplicationIdentifier</key>
<string>ca-app-pub-3940256099942544~1458002511</string>
<key>SKAdNetworkItems</key>
<array>
  <!-- AdMob SKAdNetwork IDs -->
</array>
```

### 3. Android Setup
Add AdMob App ID to `AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-3940256099942544~3347511713"/>
```

### 4. Testing Checklist
- [ ] Write unit tests for `TimeAttackLogic.addBonusTime()`.
- [ ] Update `GameComponentTest` to mock `AdService`.
- [ ] Test ad loading and display on Android.
- [ ] Test ad loading and display on iOS.
- [ ] Verify cooldown logic (10 minutes) works correctly.
- [ ] Verify initial delay (3 games) works correctly.

---

## ⚙️ Configuration

### Ad Unit ID
The project is currently configured to use `Res.string.ad_unit_id` for both Android and iOS. 

> [!IMPORTANT]
> Ensure you update `strings.xml` with your production ad unit IDs before release.

### Logic Parameters
- **Cooldown**: 10 minutes (defined in `DefaultGameComponent`).
- **Initial Delay**: 3 games played (defined in `GameModels.kt`).
- **Reward**: Bonus time added to the timer in Time Attack mode.

---

## 🛡️ Production Checklist
- [ ] Replace test ad unit IDs with production IDs.
- [ ] Test with real ads (not test ads).
- [ ] Verify GDPR/CCPA consent if required.
- [ ] Add analytics tracking for ad impressions.
- [ ] Consider adding a "Watch Ad" tutorial for first-time users.

## 💾 Database Migration Note
> [!WARNING]
> The `gamesPlayed` field was added to `GameStatsEntity`. The current database configuration uses `fallbackToDestructiveMigration`, so existing data will be lost on schema change. 
> 
> **For production, you should create a proper Room migration to preserve user data.**
