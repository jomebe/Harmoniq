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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jomebe.harmoniq.ui.theme.Cyan
import com.jomebe.harmoniq.ui.theme.InkRaised
import com.jomebe.harmoniq.ui.theme.TextSecondary

@Composable
fun ProfileScreen(onClearHistory: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Harmoniq", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(22.dp))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF302451), Color(0xFF182538))))
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Headphones, null, tint = Cyan, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(14.dp))
            Text("계정 없이 바로 듣기", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(7.dp))
            Text("Audius의 공개 음악 카탈로그를 사용합니다. API 키나 유료 구독이 필요하지 않습니다.", color = TextSecondary)
        }
        Spacer(Modifier.height(28.dp))
        SettingCard(Icons.Default.CloudDone, "추천 데이터", "앱에서 들은 아티스트와 장르를 기기에서 분석")
        SettingCard(Icons.Default.Headphones, "백그라운드 재생", "화면을 끄거나 다른 앱을 사용해도 계속 재생")
        SettingCard(Icons.Default.Lock, "개인정보", "검색·재생 기록을 외부 서버에 저장하지 않음")
        SettingCard(Icons.Default.History, "재생 기록 초기화", "기기에 저장된 취향 데이터를 삭제", onClearHistory)
        Spacer(Modifier.weight(1f))
        Text("Harmoniq 2.0.0", color = TextSecondary, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text("Music catalog powered by Audius", color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun SettingCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(20.dp))
            .background(InkRaised).padding(16.dp),
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
