package com.nikolayux.masterchariot.feature.functions.ui

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import androidx.navigation.compose.rememberNavController
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme
import kotlin.collections.iterator


@Composable
fun formatId(id: Long): String {
    return when (id) {
        0L -> "Первый"
        1L -> "Второй"
        else -> (id + 1).toString()
    }
}

@Composable
fun FunctionListScreenRoute(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
    viewModel: FunctionViewModel = viewModel<FunctionViewModel>(),
//    navController: NavController = rememberNavController(),
    listState: LazyListState = rememberLazyListState()
) {

    viewModel.updateData()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is FunctionListEffect.ScrollTo -> {
                    viewModel.updateData()
                    var position = 0
                    for ((_, events) in viewModel.state.groupedFunctions) {
                        for (event in events) {
                            if (event.id == effect.index) {
                                listState.animateScrollToItem(position + 1)
                                return@collect
                            }
                            position++
                        }
                        position++
                    }
                }
            }
        }
    }

//    val context = LocalContext.current

    FunctionListScreen(
        viewModel.state, modifier, contentPadding, viewModel::action, listState,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FunctionListScreen(
    state: FunctionListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
    onEvent: (FunctionListMessage) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
) {
    val layoutDirection = LocalLayoutDirection.current

    val combinedPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(layoutDirection),
        end = contentPadding.calculateEndPadding(layoutDirection),
        top = contentPadding.calculateTopPadding(),
        bottom = contentPadding.calculateBottomPadding(),
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = combinedPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        state.groupedFunctions.forEach { (id, functions) ->
            stickyHeader {
                Text(
                    text = formatId(id),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp, start = 16.dp, bottom = 5.dp, end = 16.dp)
                )
            }
            items(items = functions, key = { it.id }) { function ->
                FunctionCard(
                    modifier = modifier
                        .animateItem(),
                    function = function,
                    likeClicked = { onEvent(FunctionListMessage.LikeClicked(function.id)) },
                )
            }
        }
    }

}

@Preview
@Composable
private fun FunctionListCardPreview() {
    MasterChariotTheme {
        FunctionListScreen(
            FunctionListState(
                listOf(
                    FunctionUiModel(
                        likes = 2,
                        likedByMe = true,
                    ),
                )

            )
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun FunctionListCardPreviewDark() {
    MasterChariotTheme {
        FunctionListScreen(
            FunctionListState(
                listOf(
                    FunctionUiModel(
                        likes = 2,
                        likedByMe = true,
                    )
                )
            )
        )
    }
}