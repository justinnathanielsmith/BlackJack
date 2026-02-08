package io.github.smithjustinn.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.back_content_description
import io.github.smithjustinn.resources.debug_add_1000
import io.github.smithjustinn.resources.debug_add_10000
import io.github.smithjustinn.resources.debug_ads_title
import io.github.smithjustinn.resources.debug_ad_status_idle
import io.github.smithjustinn.resources.debug_ad_status_loading
import io.github.smithjustinn.resources.debug_ad_status_showing
import io.github.smithjustinn.resources.debug_ad_status_reward_earned
import io.github.smithjustinn.resources.debug_ad_status_error
import io.github.smithjustinn.resources.debug_balance_format
import io.github.smithjustinn.resources.debug_economy_title
import io.github.smithjustinn.resources.debug_menu_title
import io.github.smithjustinn.resources.debug_reset_balance
import io.github.smithjustinn.resources.debug_show_rewarded_ad
import io.github.smithjustinn.ui.components.pokerBackground
import org.jetbrains.compose.resources.stringResource

@Composable
fun DebugContent(
    component: DebugComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .pokerBackground()
                .systemBarsPadding()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.debug_menu_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )

        Text(
            text = stringResource(Res.string.debug_balance_format, state.balance),
            style = MaterialTheme.typography.titleMedium,
            color = Color.Yellow,
        )

        Spacer(modifier = Modifier.height(16.dp))

        AdsSection(
            adStatus = state.adStatus,
            onShowRewardedAdClicked = component::onShowRewardedAdClicked,
        )

        Spacer(modifier = Modifier.height(16.dp))

        EconomySection(
            onAddCurrencyClicked = component::onAddCurrencyClicked,
            onResetCurrencyClicked = component::onResetCurrencyClicked,
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = component::onBackClicked) {
            Text(stringResource(Res.string.back_content_description))
        }
    }
}

@Composable
private fun AdsSection(
    adStatus: AdStatus,
    onShowRewardedAdClicked: () -> Unit,
) {
    Text(
        text = stringResource(Res.string.debug_ads_title),
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
    )
    Text(
        text =
            when (adStatus) {
                is AdStatus.Idle -> stringResource(Res.string.debug_ad_status_idle)
                is AdStatus.Loading -> stringResource(Res.string.debug_ad_status_loading)
                is AdStatus.Showing -> stringResource(Res.string.debug_ad_status_showing)
                is AdStatus.RewardEarned -> stringResource(Res.string.debug_ad_status_reward_earned, adStatus.amount)
                is AdStatus.Error -> stringResource(Res.string.debug_ad_status_error, adStatus.message)
            },
        style = MaterialTheme.typography.bodyMedium,
        color = Color.LightGray,
    )

    Button(onClick = onShowRewardedAdClicked) {
        Text(stringResource(Res.string.debug_show_rewarded_ad))
    }
}

@Composable
private fun EconomySection(
    onAddCurrencyClicked: (Long) -> Unit,
    onResetCurrencyClicked: () -> Unit,
) {
    Text(
        text = stringResource(Res.string.debug_economy_title),
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { onAddCurrencyClicked(1000) }) {
            Text(stringResource(Res.string.debug_add_1000))
        }
        Button(onClick = { onAddCurrencyClicked(10000) }) {
            Text(stringResource(Res.string.debug_add_10000))
        }
    }

    Button(onClick = onResetCurrencyClicked) {
        Text(stringResource(Res.string.debug_reset_balance))
    }
}
