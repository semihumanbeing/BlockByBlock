package com.dahee.blockbyblock.presentation.me.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import blockbyblock.shared.generated.resources.Res
import blockbyblock.shared.generated.resources.profile_avatar_alien
import blockbyblock.shared.generated.resources.profile_avatar_cat
import blockbyblock.shared.generated.resources.profile_avatar_chef
import blockbyblock.shared.generated.resources.profile_avatar_person
import com.dahee.blockbyblock.domain.model.ProfileAvatarType
import org.jetbrains.compose.resources.painterResource

/**
 * Renders a 3D Toy Block Profile Avatar from the 3d_profile.png reference.
 */
@Composable
fun BlockAvatarView(
    avatarType: ProfileAvatarType,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    contentDescription: String? = null
) {
    val drawableRes = when (avatarType) {
        ProfileAvatarType.PERSON -> Res.drawable.profile_avatar_person
        ProfileAvatarType.CHEF -> Res.drawable.profile_avatar_chef
        ProfileAvatarType.ALIEN -> Res.drawable.profile_avatar_alien
        ProfileAvatarType.CAT -> Res.drawable.profile_avatar_cat
    }

    Image(
        painter = painterResource(drawableRes),
        contentDescription = contentDescription ?: avatarType.name,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size)
    )
}
