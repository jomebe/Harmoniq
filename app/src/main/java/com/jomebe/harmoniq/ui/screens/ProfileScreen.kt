package com.jomebe.harmoniq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jomebe.harmoniq.auth.AuthState
import com.jomebe.harmoniq.ui.theme.Cyan
import com.jomebe.harmoniq.ui.theme.InkRaised
import com.jomebe.harmoniq.ui.theme.TextSecondary
import com.jomebe.harmoniq.ui.theme.Violet

@Composable
fun ProfileScreen(authState: AuthState, onSignIn: () -> Unit, onSignOut: () -> Unit, onClearHistory: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("프로필", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(22.dp))
        when (authState) {
            is AuthState.SignedIn -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        authState.profile.photoUrl,
                        null,
                        Modifier.size(68.dp).clip(CircleShape).background(Violet),
                        contentScale = ContentScale.Crop
                    )
                    Column(Modifier.padding(start = 16.dp).weight(1f)) {
                        Text(authState.profile.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(authState.profile.email, color = TextSecondary)
                    }
                    OutlinedButton(onClick = onSignOut) { Text("로그아웃") }
                }
            }
            AuthState.Loading -> Text("Google 계정을 확인하고 있어요…", color = TextSecondary)
            is AuthState.Error -> {
                Text(authState.message, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onSignIn) { Text("다시 로그인") }
            }
            else -> {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF302451), Color(0xFF182538))))
                        .padding(24.dp)
                ) {
                    Text("내 취향을 더 정확하게", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(7.dp))
                    Text("로그인하면 구독 채널과 좋아요 표시한 음악을 추천에 반영합니다.", color = TextSecondary)
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = onSignIn, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) {
                        Text("Google로 로그인")
                    }
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        SettingCard(Icons.Default.CloudDone, "추천 데이터", "구독·좋아요와 앱 내부 활동만 사용")
        SettingCard(Icons.Default.Lock, "개인정보", "재생 기록은 이 기기에만 저장")
        SettingCard(Icons.Default.History, "재생 기록 초기화", "기기에 저장된 취향 데이터를 삭제", onClearHistory)
        Spacer(Modifier.weight(1f))
        Text("Harmoniq 1.0.0", color = TextSecondary, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text("Playback powered by the official YouTube player", color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun SettingCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(20.dp))
            .background(InkRaised)
            .then(if (onClick != null) Modifier else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Cyan, modifier = Modifier.size(25.dp))
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        if (onClick != null) OutlinedButton(onClick = onClick) { Text("삭제") }
    }
}
