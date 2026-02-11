package dev.belalkhan.cutthenoise.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import dev.belalkhan.cutthenoise.R
import dev.belalkhan.cutthenoise.presentation.PersonaCardUi
import dev.belalkhan.cutthenoise.ui.theme.DividerDark
import dev.belalkhan.cutthenoise.ui.theme.TextSecondary

/**
 * Horizontal pager that allows users to swipe through the generated persona cards.
 * Includes page indicator dots and a "swipe" hint.
 */
@Composable
fun ResultCarousel(
    cards: List<PersonaCardUi>,
    modifier: Modifier = Modifier
) {
    if (cards.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { cards.size })

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            pageSpacing = 16.dp
        ) { page ->
            PersonaCardComposable(
                cardUi = cards[page],
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(cards.size) { index ->
                val isSelected = pagerState.currentPage == index
                val color = if (isSelected) {
                    Color(cards[index].persona.accentColor)
                } else {
                    DividerDark
                }
                val dotWidth = if (isSelected) 24.dp else 8.dp
                val dotHeight = 8.dp

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = dotWidth, height = dotHeight)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        if (cards.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.swipe_hint),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
