package io.github.smithjustinn.ui.shop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.ShopItem
import io.github.smithjustinn.domain.models.ShopItemType
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.shop_back_description
import io.github.smithjustinn.resources.shop_balance_format
import io.github.smithjustinn.resources.shop_bankroll_description
import io.github.smithjustinn.resources.shop_item_active
import io.github.smithjustinn.resources.shop_item_equip
import io.github.smithjustinn.resources.shop_item_price_format
import io.github.smithjustinn.resources.shop_title
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticFeedbackType
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.assets.AssetProvider
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.components.AuroraEffect
import io.github.smithjustinn.ui.components.PokerButton
import io.github.smithjustinn.ui.components.ShopIcons
import io.github.smithjustinn.ui.components.pokerBackground
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShopContent(
    component: ShopComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticsService = LocalAppGraph.current.hapticsService
    val audioService = LocalAppGraph.current.audioService

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            val message =
                when (error) {
                    is ShopErrorMessage.Resource -> getString(error.res)
                    is ShopErrorMessage.Message -> error.text
                }
            snackbarHostState.showSnackbar(message)
            component.onClearError()
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .pokerBackground(),
    ) {
        AuroraEffect(
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
        ) {
            ShopHeader(
                balance = state.balance,
                onBackClicked = {
                    audioService.playEffect(AudioService.SoundEffect.CLICK)
                    component.onBackClicked()
                },
            )

            ShopItemsGrid(
                state = state,
                onBuyItem = {
                    hapticsService.performHapticFeedback(HapticFeedbackType.HEAVY)
                    component.onBuyItemClicked(it)
                },
                onEquipItem = {
                    hapticsService.performHapticFeedback(HapticFeedbackType.LIGHT)
                    component.onEquipItemClicked(it)
                },
                modifier = Modifier.weight(1f),
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ShopHeader(
    balance: Long,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(
                imageVector = AppIcons.ArrowBack,
                contentDescription = stringResource(Res.string.shop_back_description),
                tint = PokerTheme.colors.onBackground,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(Res.string.shop_title),
            style = MaterialTheme.typography.headlineMedium,
            color = PokerTheme.colors.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.weight(1f))

        // Bankroll Display
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ShopIcons.CasinoChip,
                contentDescription = stringResource(Res.string.shop_bankroll_description),
                tint = PokerTheme.colors.goldenYellow,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.shop_balance_format, balance),
                style = MaterialTheme.typography.titleMedium,
                color = PokerTheme.colors.goldenYellow,
            )
        }
    }
}

@Composable
private fun ShopItemsGrid(
    state: ShopState,
    onBuyItem: (ShopItem) -> Unit,
    onEquipItem: (ShopItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleItems = remember(state.items) { state.items.filter { it.isVisible } }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        items(visibleItems) { item ->
            val isEquipped =
                when (item.type) {
                    ShopItemType.THEME -> item.id == state.activeThemeId
                    ShopItemType.CARD_SKIN -> item.id == state.activeSkinId
                    else -> false
                }

            val isUnlocked = state.unlockedItemIds.contains(item.id)
            val shopItemState =
                when {
                    isEquipped -> ShopItemState.Equipped
                    isUnlocked -> ShopItemState.Owned
                    else -> ShopItemState.Locked(item.price, state.balance >= item.price)
                }

            ShopItemCard(
                item = item,
                shopItemState = shopItemState,
                onBuy = { onBuyItem(item) },
                onEquip = { onEquipItem(item) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    shopItemState: ShopItemState,
    onBuy: () -> Unit,
    onEquip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEquipped = shopItemState is ShopItemState.Equipped
    val isOwned = shopItemState is ShopItemState.Owned || isEquipped
    val canClick = !isEquipped

    AppCard(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(enabled = canClick) {
                    if (isOwned) onEquip() else onBuy()
                },
        backgroundColor = getCardBackgroundColor(shopItemState),
        border =
            if (isEquipped) {
                BorderStroke(2.dp, PokerTheme.colors.goldenYellow)
            } else {
                null
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp).heightIn(min = 280.dp),
        ) {
            ShopItemPreview(
                itemId = item.id,
                hexColor = item.hexColor,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            ShopItemInfo(
                name = item.name,
                description = item.description,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(16.dp))

            ShopActionButton(
                shopItemState = shopItemState,
                onBuy = onBuy,
                onEquip = onEquip,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ShopItemPreview(
    itemId: String,
    hexColor: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        AssetProvider.CardPreview(
            shopItemId = itemId,
            hexColor = hexColor,
            modifier = Modifier.fillMaxWidth(0.85f),
        )
    }
}

@Composable
private fun ShopItemInfo(
    name: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            color = PokerTheme.colors.goldenYellow,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelMedium,
                color = PokerTheme.colors.onSurface.copy(alpha = 0.6f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ShopActionButton(
    shopItemState: ShopItemState,
    onBuy: () -> Unit,
    onEquip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (shopItemState) {
        is ShopItemState.Equipped -> {
            PokerButton(
                text = stringResource(Res.string.shop_item_active),
                onClick = {},
                enabled = false,
                containerColor = PokerTheme.colors.bonusGreen.copy(alpha = 0.4f),
                contentColor = Color.White,
                modifier = modifier,
            )
        }
        is ShopItemState.Owned -> {
            PokerButton(
                text = stringResource(Res.string.shop_item_equip),
                onClick = onEquip,
                modifier = modifier,
            )
        }
        is ShopItemState.Locked -> {
            PokerButton(
                text = stringResource(Res.string.shop_item_price_format, shopItemState.price),
                leadingIcon = ShopIcons.CasinoChip,
                onClick = onBuy,
                modifier = modifier,
                contentColor = if (shopItemState.canAfford) PokerTheme.colors.goldenYellow else Color(0xFFE57373),
            )
        }
    }
}

@Composable
private fun getCardBackgroundColor(shopItemState: ShopItemState): Color =
    when (shopItemState) {
        is ShopItemState.Equipped -> PokerTheme.colors.surface.copy(alpha = 0.8f)
        is ShopItemState.Owned -> PokerTheme.colors.surface.copy(alpha = 0.5f)
        is ShopItemState.Locked -> PokerTheme.colors.surface
    }
