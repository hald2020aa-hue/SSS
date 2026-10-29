package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UiCustomizationProfile
import com.example.ui.components.SssStyledIcon
import com.example.ui.theme.LocalSssVisualConfig
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.SssNeonTheme
import com.example.ui.viewmodel.UiState

data class BuiltInStylePreset(
    val name: String,
    val subtitle: String,
    val theme: SssNeonTheme,
    val layoutMode: String,
    val iconStyle: String,
    val navStyle: String,
    val cornerDp: Int,
    val glow: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioCustomizationScreen(
    uiProfilesState: UiState<List<UiCustomizationProfile>>,
    onSelectNeonTheme: (SssNeonTheme) -> Unit,
    onSelectLayoutMode: (String) -> Unit,
    onSelectIconStyle: (String) -> Unit,
    onSelectNavigationBarStyle: (String) -> Unit,
    onSelectHomeSectionOrder: (String) -> Unit,
    onCornerRadiusChange: (Int) -> Unit,
    onGlowIntensityChange: (Int) -> Unit,
    onReaderModeChange: (String) -> Unit,
    onSaveCustomPreset: (String) -> Unit,
    onSyncNow: () -> Unit,
    onApplyCloudProfile: (UiCustomizationProfile) -> Unit,
    onDeleteCloudProfile: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val config = LocalSssVisualConfig.current
    val palette = config.neonTheme
    val cardRadius = config.cardCornerRadiusDp.dp
    var customPresetName by remember { mutableStateOf("") }

    val builtInPresets = remember {
        listOf(
            BuiltInStylePreset(
                name = "SSS Cyber Samurai",
                subtitle = "سيان متوهج • شبكة ثلاثية • شريط عائم",
                theme = SssNeonTheme.CYBER_CYAN,
                layoutMode = "GRID_3",
                iconStyle = "NEON_GLOW",
                navStyle = "FLOATING_PILL",
                cornerDp = 18,
                glow = 90
            ),
            BuiltInStylePreset(
                name = "Tokyo Magenta Pulse",
                subtitle = "ماجنتا نيون • بطاقات متدرجة • أيقونات صلبة",
                theme = SssNeonTheme.NEON_MAGENTA,
                layoutMode = "STAGGERED_CARDS",
                iconStyle = "SOLID_CYBER",
                navStyle = "FLOATING_PILL",
                cornerDp = 22,
                glow = 95
            ),
            BuiltInStylePreset(
                name = "Matrix Minimal Code",
                subtitle = "زمردي ماتريكس • قائمة مدمجة • خطوط هندسية",
                theme = SssNeonTheme.TOXIC_EMERALD,
                layoutMode = "COMPACT_LIST",
                iconStyle = "MINIMAL_OUTLINE",
                navStyle = "CLASSIC_DOCK",
                cornerDp = 10,
                glow = 65
            ),
            BuiltInStylePreset(
                name = "Crimson Void Tablet",
                subtitle = "ليزر قرمزي • شبكة عريضة • شريط جانبي",
                theme = SssNeonTheme.CRIMSON_LASER,
                layoutMode = "GRID_2",
                iconStyle = "DUOTONE_RING",
                navStyle = "COMPACT_RAIL",
                cornerDp = 16,
                glow = 85
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(horizontal = 16.dp)
            .testTag("studio_customization_screen"),
        contentPadding = PaddingValues(top = 14.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header + Cloud Sync Now
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SSS UI & CLOUD SYNC STUDIO",
                        fontFamily = OrbitronFontFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.secondaryNeon
                    )
                    Text(
                        text = "استوديو تخصيص الواجهة والمزامنة",
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.onBackground
                    )
                }
                Button(
                    onClick = onSyncNow,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primaryNeon,
                        contentColor = Color(0xFF04060E)
                    ),
                    modifier = Modifier.testTag("sync_now_button")
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مزامنة الآن", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 1. 6 Dark Neon Themes Selector
        item {
            StudioSectionCard(
                title = "1. الخلفيات الداكنة بألوان النيون الستة (6 Neon Themes)",
                subtitle = "اختر لوحة النيون الداكنة المفضلة لديك لتغيير هوية SSS بالكامل",
                icon = Icons.Default.Palette,
                cardRadius = cardRadius
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SssNeonTheme.entries.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { neon ->
                                val selected = neon == palette
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(neon.background)
                                        .border(
                                            width = if (selected) 2.2.dp else 1.dp,
                                            color = if (selected) neon.primaryNeon else neon.primaryNeon.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onSelectNeonTheme(neon) }
                                        .padding(12.dp)
                                        .testTag("studio_neon_${neon.id}")
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(neon.primaryNeon)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(neon.secondaryNeon)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(neon.tertiaryNeon)
                                                )
                                            }
                                            Text(
                                                text = neon.englishBadge,
                                                fontFamily = OrbitronFontFamily,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = neon.primaryNeon
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = neon.arabicName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = neon.onBackground
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Element Distribution & Layout Options
        item {
            StudioSectionCard(
                title = "2. توزيع العناصر وتخطيط الشاشات (Layout Distribution)",
                subtitle = "خصص طريقة عرض البطاقات، شريط التنقل، وترتيب الأقسام في الواجهة",
                icon = Icons.Default.ViewQuilt,
                cardRadius = cardRadius
            ) {
                Text(
                    text = "نمط توزيع بطاقات المانجا:",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                val layouts = listOf(
                    "GRID_3" to "شبكة ثلاثية (Kotatsu 3-Col)",
                    "GRID_2" to "شبكة عريضة (Wide 2-Col)",
                    "COMPACT_LIST" to "قائمة تفصيلية (Detailed List)",
                    "STAGGERED_CARDS" to "بطاقات متدرجة (Staggered)"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    layouts.forEach { (id, label) ->
                        SelectableOptionChip(
                            label = label,
                            selected = config.layoutMode == id,
                            onClick = { onSelectLayoutMode(id) },
                            tag = "layout_opt_$id"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "توزيع وموضع شريط التنقل:",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                val navStyles = listOf(
                    "FLOATING_PILL" to "كبسولة نيون عائمة (Floating Pill)",
                    "CLASSIC_DOCK" to "شريط سفلي كلاسيكي (Classic Dock)",
                    "COMPACT_RAIL" to "شريط جانبي احترافي (Side Rail)"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    navStyles.forEach { (id, label) ->
                        SelectableOptionChip(
                            label = label,
                            selected = config.navigationBarStyle == id,
                            onClick = { onSelectNavigationBarStyle(id) },
                            tag = "nav_style_$id"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "ترتيب أقسام شاشة الاستكشاف:",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                val sectionOrders = listOf(
                    "FEATURED_RECENT_POPULAR" to "المختارة ← الأحدث ← الأكثر شعبية",
                    "RECENT_FEATURED_POPULAR" to "الأحدث ← المختارة ← الأكثر شعبية",
                    "POPULAR_FEATURED_RECENT" to "الأكثر شعبية ← المختارة ← الأحدث"
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    sectionOrders.forEach { (id, label) ->
                        SelectableOptionChip(
                            label = label,
                            selected = config.homeSectionOrder == id,
                            onClick = { onSelectHomeSectionOrder(id) },
                            tag = "section_order_$id"
                        )
                    }
                }
            }
        }

        // 3. Icon Formatting & Visual Sliders
        item {
            StudioSectionCard(
                title = "3. تنسيق الأيقونات واللمسات البصرية (Icon & Glow Styling)",
                subtitle = "غير شكل إطارات الأيقونات، شدة توهج النيون، وانحناء الزوايا",
                icon = Icons.Default.DashboardCustomize,
                cardRadius = cardRadius
            ) {
                val iconStyles = listOf(
                    "NEON_GLOW" to "توهج نيون (Neon Glow)",
                    "MINIMAL_OUTLINE" to "إطار خطي (Minimal Outline)",
                    "SOLID_CYBER" to "مضلع سايبر (Solid Cyber)",
                    "DUOTONE_RING" to "حلقة مزدوجة (Duotone Ring)"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconStyles.forEach { (id, label) ->
                        SelectableOptionChip(
                            label = label,
                            selected = config.iconStyle == id,
                            onClick = { onSelectIconStyle(id) },
                            tag = "icon_style_$id"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("معاينة فورية للأيقونات:", color = palette.onSurfaceMuted)
                    SssStyledIcon(icon = Icons.Default.Favorite, contentDescription = null, isSelected = true)
                    SssStyledIcon(icon = Icons.Default.AutoAwesome, contentDescription = null, isSelected = false)
                    SssStyledIcon(icon = Icons.Default.Palette, contentDescription = null, isSelected = true)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "شدة توهج النيون (Neon Glow): ${config.neonGlowIntensity}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.onBackground
                )
                Slider(
                    value = config.neonGlowIntensity.toFloat(),
                    onValueChange = { onGlowIntensityChange(it.toInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = palette.primaryNeon,
                        activeTrackColor = palette.primaryNeon
                    )
                )

                Text(
                    text = "انحناء زوايا البطاقات (Corner Radius): ${config.cardCornerRadiusDp}dp",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.onBackground
                )
                Slider(
                    value = config.cardCornerRadiusDp.toFloat(),
                    onValueChange = { onCornerRadiusChange(it.toInt()) },
                    valueRange = 4f..32f,
                    colors = SliderDefaults.colors(
                        thumbColor = palette.secondaryNeon,
                        activeTrackColor = palette.secondaryNeon
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "نمط القارئ الافتراضي:",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                val readerModes = listOf(
                    "WEBTOON_VERTICAL" to "ويبتون عمودي مستمر",
                    "PAGED_RTL" to "صفحات مانجا (من اليمين لليسار)",
                    "PAGED_LTR" to "صفحات كوميكس (من اليسار لليمين)"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    readerModes.forEach { (id, label) ->
                        SelectableOptionChip(
                            label = label,
                            selected = config.readerMode == id,
                            onClick = { onReaderModeChange(id) },
                            tag = "reader_mode_$id"
                        )
                    }
                }
            }
        }

        // 4. Built-in Curated Custom Styles & Save User Preset
        item {
            StudioSectionCard(
                title = "4. الأنماط المخصصة الجاهزة والشخصية (Custom Style Presets)",
                subtitle = "اختر من الأنماط البصرية المصممة لمختلف الأذواق أو احفظ نمطك الخاص في السحابة",
                icon = Icons.Default.AutoAwesome,
                cardRadius = cardRadius
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    builtInPresets.forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.surfaceElevated)
                                .border(1.dp, preset.theme.primaryNeon.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectNeonTheme(preset.theme)
                                    onSelectLayoutMode(preset.layoutMode)
                                    onSelectIconStyle(preset.iconStyle)
                                    onSelectNavigationBarStyle(preset.navStyle)
                                    onCornerRadiusChange(preset.cornerDp)
                                    onGlowIntensityChange(preset.glow)
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = preset.theme.primaryNeon,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = preset.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.onSurfaceMuted
                                )
                            }
                            Text(
                                text = "تطبيق",
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.primaryNeon
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customPresetName,
                        onValueChange = { customPresetName = it },
                        placeholder = { Text("اسم النمط المخصص (مثال: نمط القراءة الليلي)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_preset_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.primaryNeon,
                            unfocusedBorderColor = palette.primaryNeon.copy(alpha = 0.35f),
                            focusedTextColor = palette.onBackground,
                            unfocusedTextColor = palette.onBackground
                        )
                    )
                    Button(
                        onClick = {
                            onSaveCustomPreset(customPresetName.ifBlank { "نمط SSS مخصص" })
                            customPresetName = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primaryNeon,
                            contentColor = Color(0xFF05070E)
                        ),
                        modifier = Modifier.testTag("save_preset_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ")
                    }
                }
            }
        }

        // 5. Cloud-Synced Device Profiles
        item {
            StudioSectionCard(
                title = "5. ملفات الواجهة المتزامنة سحابياً بين أجهزتك",
                subtitle = "تتم مزامنة هذه الملفات لحظياً عبر Firestore لتعمل على جميع أجهزتك",
                icon = Icons.Default.Devices,
                cardRadius = cardRadius
            ) {
                when (uiProfilesState) {
                    is UiState.Loading -> {
                        Text("جاري تحميل الملفات السحابية...", color = palette.primaryNeon)
                    }
                    is UiState.Error -> {
                        Text(uiProfilesState.message, color = Color(0xFFFF4D6D))
                    }
                    is UiState.Success -> {
                        if (uiProfilesState.data.isEmpty()) {
                            Text(
                                text = "لم يتم حفظ ملفات مخصصة بعد. اضغط «مزامنة الآن» أو احفظ نمطاً مخصصاً بالأعلى.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.onSurfaceMuted
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                uiProfilesState.data.forEach { prof ->
                                    val profTheme = SssNeonTheme.fromId(prof.neonThemeId)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(palette.surfaceElevated)
                                            .border(
                                                1.dp,
                                                profTheme.primaryNeon.copy(alpha = 0.45f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { onApplyCloudProfile(prof) }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            tint = profTheme.primaryNeon
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = prof.profileName,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = palette.onBackground,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "الجهاز: ${prof.deviceName} • ${profTheme.arabicName.substringBefore(" (")} • ${prof.layoutMode}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = palette.onSurfaceMuted
                                            )
                                        }
                                        IconButton(onClick = { onDeleteCloudProfile(prof.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "حذف",
                                                tint = palette.onSurfaceMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Account Sign-Out
        item {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_out_button"),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4D6D))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFFF4D6D))
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل الخروج من الحساب السحابي", color = Color(0xFFFF4D6D))
            }
        }
    }
}

@Composable
private fun StudioSectionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    cardRadius: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    val palette = LocalSssVisualConfig.current.neonTheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, palette.primaryNeon.copy(alpha = 0.32f), RoundedCornerShape(cardRadius)),
        shape = RoundedCornerShape(cardRadius),
        colors = CardDefaults.cardColors(containerColor = palette.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SssStyledIcon(icon = icon, contentDescription = null, isSelected = true, sizeDp = 38.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.onSurfaceMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SelectableOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val palette = LocalSssVisualConfig.current.neonTheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) palette.primaryNeon else palette.surfaceElevated)
            .border(
                1.dp,
                if (selected) palette.primaryNeon else palette.primaryNeon.copy(alpha = 0.25f),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color(0xFF04060E) else palette.onBackground
        )
    }
}
