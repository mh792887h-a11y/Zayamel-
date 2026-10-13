package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Category
import com.example.data.model.Zamil

@Composable
fun AddEditZamilDialog(
    zamilToEdit: Zamil?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        performer: String,
        description: String?,
        categoryId: String?,
        audioPath: String,
        coverPath: String?,
        durationSeconds: Int,
        isPublished: Boolean,
        audioPathLow: String?,
        audioPathHigh: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(zamilToEdit?.title ?: "") }
    var performer by remember { mutableStateOf(zamilToEdit?.performer ?: "") }
    var description by remember { mutableStateOf(zamilToEdit?.description ?: "") }
    var selectedCategoryId by remember { mutableStateOf(zamilToEdit?.categoryId ?: categories.firstOrNull()?.id) }
    var audioPath by remember {
        mutableStateOf(zamilToEdit?.audioPath ?: "https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg")
    }
    var audioPathLow by remember { mutableStateOf(zamilToEdit?.audioPathLow ?: "") }
    var audioPathHigh by remember { mutableStateOf(zamilToEdit?.audioPathHigh ?: "") }
    var coverPath by remember {
        mutableStateOf(zamilToEdit?.coverPath ?: "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600")
    }
    var durationSecondsText by remember { mutableStateOf((zamilToEdit?.durationSeconds ?: 210).toString()) }
    var isPublished by remember { mutableStateOf(zamilToEdit?.isPublished ?: true) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "اختر التصنيف"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("add_edit_zamil_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // شريط العنوان
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (zamilToEdit == null) "إضافة زامل جديد" else "تعديل بيانات الزامل",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_edit_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إلغاء",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (errorText != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = errorText!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // اسم الزامل
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorText = null },
                    label = { Text("اسم الزامل *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("zamil_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // المنشد
                OutlinedTextField(
                    value = performer,
                    onValueChange = { performer = it; errorText = null },
                    label = { Text("اسم المنشد *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("zamil_performer_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // اختيار التصنيف
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategoryName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("التصنيف") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            Text(
                                text = "تغيير ▼",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable { categoryDropdownExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryDropdownExpanded = true }
                            .testTag("category_dropdown_trigger")
                    )

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    categoryDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("category_option_${cat.id}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // رابط أو مسار الملف الصوتي الرئيسي (الجودة المتوسطة)
                OutlinedTextField(
                    value = audioPath,
                    onValueChange = { audioPath = it; errorText = null },
                    label = { Text("رابط الملف الصوتي القياسي (128kbps) *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AudioFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("audio_path_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // رابط الجودة المنخفضة (اختياري)
                OutlinedTextField(
                    value = audioPathLow,
                    onValueChange = { audioPathLow = it },
                    label = { Text("رابط الجودة المنخفضة (64kbps - توفير بيانات)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AudioFile, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("audio_path_low_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // رابط الجودة العالية (اختياري)
                OutlinedTextField(
                    value = audioPathHigh,
                    onValueChange = { audioPathHigh = it },
                    label = { Text("رابط الجودة العالية (320kbps - نقاء استوديو)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.HighQuality, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("audio_path_high_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // رابط صورة الغلاف
                OutlinedTextField(
                    value = coverPath,
                    onValueChange = { coverPath = it },
                    label = { Text("رابط صورة الغلاف (اختياري)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("cover_path_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // المدة بالثواني
                OutlinedTextField(
                    value = durationSecondsText,
                    onValueChange = { durationSecondsText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("المدة التقديرية بالثواني") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("duration_seconds_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // الوصف الاختياري
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف الزامل أو أبيات مختارة (اختياري)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("zamil_description_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // تبديل النشر أو الحفظ كمسودة مخفية
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPublished) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (isPublished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isPublished) "حالة النشر: منشور للعامة" else "حالة النشر: مسودة مخفية",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isPublished) "سيظهر لجميع المستخدمين" else "يظهر للمدير فقط",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isPublished,
                        onCheckedChange = { isPublished = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("publish_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // أزرار الحفظ والإلغاء
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("cancel_save_zamil_button")
                    ) {
                        Text("إلغاء")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorText = "يرجى إدخال اسم الزامل"
                                return@Button
                            }
                            if (performer.isBlank()) {
                                errorText = "يرجى إدخال اسم المنشد"
                                return@Button
                            }
                            if (audioPath.isBlank()) {
                                errorText = "يرجى تحديد مسار أو رابط الملف الصوتي"
                                return@Button
                            }
                            val dur = durationSecondsText.toIntOrNull() ?: 0
                            onSave(
                                title,
                                performer,
                                description.ifBlank { null },
                                selectedCategoryId,
                                audioPath,
                                coverPath.ifBlank { null },
                                dur,
                                isPublished,
                                audioPathLow.ifBlank { null },
                                audioPathHigh.ifBlank { null }
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("submit_save_zamil_button")
                    ) {
                        Text("حفظ الزامل")
                    }
                }
            }
        }
    }
}
