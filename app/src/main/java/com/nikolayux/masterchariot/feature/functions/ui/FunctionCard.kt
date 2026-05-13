package com.nikolayux.masterchariot.feature.functions.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme

@Composable
fun FunctionCard(
    function: FunctionUiModel,
    modifier: Modifier = Modifier,
    likeClicked: () -> Unit = {},
) {
    Card(modifier) {
        Column(
            Modifier
                .padding(top = 12.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            TextButton(likeClicked) {
                Icon(
                    if (function.likedByMe) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    null,
                )

                Spacer(Modifier.width(width = 8.dp))

                Text(function.likes.toString())
            }
        }
    }
}

@Preview
@Composable
private fun FunctionCardPreview() {
    MasterChariotTheme {
        FunctionCard(
            FunctionUiModel(
                likes = 2,
                likedByMe = true,
            )
        )
    }
}