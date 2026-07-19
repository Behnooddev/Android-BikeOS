package com.voidroot.bikeos.presentation.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.navigation.BikeOSDestinations
import com.voidroot.bikeos.core.theme.BikePrimary
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(navController: NavHostController, viewModel: OnboardingViewModel = hiltViewModel()) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    fun finish() {
        viewModel.onFinished {
            navController.navigate(BikeOSDestinations.SIGNUP) {
                popUpTo(BikeOSDestinations.ONBOARDING) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pageIndex ->
            val page = onboardingPages[pageIndex]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(page.gradientStart, page.gradientEnd))),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 48.dp)
                ) {
                    OnboardingGlyphIcon(glyph = page.glyph, tint = Color.White)
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 28.dp)
                    )
                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }
            }
        }

        // Skip - top right, always visible except on the last page (where it'd be redundant with Get Started)
        if (pagerState.currentPage < onboardingPages.lastIndex) {
            TextButton(
                onClick = { finish() },
                modifier = Modifier.align(Alignment.TopEnd).padding(20.dp)
            ) {
                Text("Skip", color = Color.White.copy(alpha = 0.8f))
            }
        }

        // Page indicator dots
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            onboardingPages.indices.forEach { index ->
                val isActive = index == pagerState.currentPage
                val width by animateDpAsState(if (isActive) 22.dp else 8.dp, label = "dotWidth")
                Box(
                    modifier = Modifier
                        .size(width = width, height = 8.dp)
                        .background(
                            if (isActive) Color.White else Color.White.copy(alpha = 0.35f),
                            CircleShape
                        )
                )
            }
        }

        // Next / Get Started
        Button(
            onClick = {
                if (pagerState.currentPage < onboardingPages.lastIndex) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    finish()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = BikePrimary),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .fillMaxWidth(0.7f)
        ) {
            Text(if (pagerState.currentPage < onboardingPages.lastIndex) "Next" else "Get Started")
        }
    }
}
