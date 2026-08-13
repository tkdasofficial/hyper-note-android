package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import com.example.data.UserPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.auth.AuthManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    notes: List<Note>,
    userPreferences: UserPreferences,
    authManager: AuthManager,
    onAddNote: () -> Unit,
    onNoteClick: (Note) -> Unit,
    onEraseAll: () -> Unit
) {
    var currentTab by remember { mutableStateOf("Notes") }
    val filters = listOf("All Notes", "Pinned", "Encrypted", "Cloud")
    var selectedFilter by remember { mutableStateOf(filters[0]) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf(authManager.currentUser) }

    Scaffold(
        topBar = {
            if (currentTab == "Notes" || currentTab == "Vault") {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSearchActive) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Search notes...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            IconButton(onClick = { 
                                isSearchActive = false 
                                searchQuery = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Search", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable {
                                            if (currentUser == null) {
                                                scope.launch {
                                                    val success = authManager.signInWithGoogle()
                                                    if (success) {
                                                        currentUser = authManager.currentUser
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    val firstChar = currentUser?.displayName?.firstOrNull()?.uppercase() ?: "G"
                                    Text(
                                        firstChar,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                                Column {
                                    val firstName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Guest"
                                    Text(
                                        text = "Hi, $firstName",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Welcome back!",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                            if (currentTab == "Notes" || currentTab == "Vault") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = { isSearchActive = true }) {
                                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                    IconButton(onClick = { currentTab = "Setup" }) {
                                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }

                    if (currentTab == "Notes" || currentTab == "Vault") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filters) { filter ->
                                FilterChip(
                                    selected = filter == selectedFilter,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = filter == selectedFilter,
                                        borderColor = Color.Transparent,
                                        selectedBorderColor = Color.Transparent
                                    ),
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentTab == "Notes" || currentTab == "Vault") {
                FloatingActionButton(
                    onClick = onAddNote,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.padding(bottom = 80.dp) // padding for bottom nav
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note")
                }
            }
        },
        bottomBar = {
            BottomNavBar(currentTab) { currentTab = it }
        }
    ) { paddingValues ->
        when (currentTab) {
            "Notes" -> {
                NotesContent(
                    notes = notes,
                    selectedFilter = selectedFilter,
                    searchQuery = searchQuery,
                    onNoteClick = onNoteClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            "Cloud" -> {
                CloudContent(modifier = Modifier.padding(paddingValues), notes = notes, onNoteClick = onNoteClick)
            }
            "Vault" -> {
                VaultContent(
                    notes = notes,
                    searchQuery = searchQuery,
                    selectedFilter = selectedFilter,
                    userPreferences = userPreferences,
                    authManager = authManager,
                    onNoteClick = onNoteClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            "Setup" -> {
                SetupContent(
                    modifier = Modifier.padding(paddingValues), 
                    notes = notes,
                    userPreferences = userPreferences, 
                    authManager = authManager,
                    currentUser = currentUser,
                    onAuthChange = { currentUser = authManager.currentUser },
                    onEraseAll = onEraseAll
                )
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.title.ifEmpty { "Untitled" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (note.isEncrypted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "AES-256",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                Text(
                    text = dateFormat.format(Date(note.timestamp)),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (note.isVoiceNote) "VOICE" else "SECURE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(currentTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
        modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Description, contentDescription = "Notes") },
            label = { Text("Notes", fontSize = 11.sp) },
            selected = currentTab == "Notes",
            onClick = { onTabSelected("Notes") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Cloud, contentDescription = "Cloud") },
            label = { Text("Cloud", fontSize = 11.sp) },
            selected = currentTab == "Cloud",
            onClick = { onTabSelected("Cloud") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Shield, contentDescription = "Vault") },
            label = { Text("Vault", fontSize = 11.sp) },
            selected = currentTab == "Vault",
            onClick = { onTabSelected("Vault") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun NotesContent(
    notes: List<Note>,
    selectedFilter: String,
    searchQuery: String = "",
    onNoteClick: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val filteredNotes = when (selectedFilter) {
            "Pinned" -> notes.filter { it.isPinned }
            "Cloud" -> notes // Assuming cloud notes are synced; for now show all or a specific flag
            "Encrypted" -> notes.filter { it.isEncrypted }
            else -> notes
        }.filter {
            searchQuery.isEmpty() || 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.content.contains(searchQuery, ignoreCase = true)
        }

        if (filteredNotes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                Text("No notes found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        filteredNotes.forEach { note ->
            NoteCard(note = note, onClick = { onNoteClick(note) })
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun CloudContent(modifier: Modifier = Modifier, notes: List<Note>, onNoteClick: (Note) -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Cloud Notes", color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Notes synced securely to the cloud.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
        
        if (notes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                Text("No cloud notes yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            notes.forEach { note ->
                NoteCard(note = note, onClick = { onNoteClick(note) })
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun VaultContent(
    notes: List<Note>,
    searchQuery: String,
    selectedFilter: String,
    userPreferences: com.example.data.UserPreferences,
    authManager: AuthManager,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    var unlocked by remember { mutableStateOf(false) }
    val encryptionKey by userPreferences.encryptionKey.collectAsState()
    val enableBiometrics by userPreferences.enableBiometrics.collectAsState()
    val scope = rememberCoroutineScope()
    var isAuthenticating by remember { mutableStateOf(false) }
    var isAuthenticated by remember { mutableStateOf(authManager.currentUser != null) }

    if (!isAuthenticated) {
        Column(
            modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Shield, contentDescription = "Vault", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("Sign In Required", color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Please sign in to access your Vault.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    isAuthenticating = true
                    scope.launch {
                        val success = authManager.signInWithGoogle()
                        if (success) {
                            isAuthenticated = true
                        }
                        isAuthenticating = false
                    }
                },
                enabled = !isAuthenticating
            ) {
                Text(if (isAuthenticating) "Signing in..." else "Sign in with Google")
            }
        }
        return
    }

    if (!unlocked) {
        AppLockScreen(
            correctPin = encryptionKey,
            enableBiometrics = enableBiometrics,
            isAuthenticated = authManager.currentUser != null,
            onUnlocked = { unlocked = true },
            onPinReset = { newPin -> userPreferences.saveEncryptionKey(newPin); unlocked = true },
            modifier = modifier
        )
    } else {
        NotesContent(notes = notes.filter { it.isEncrypted }, selectedFilter = "Encrypted", searchQuery = searchQuery, onNoteClick = onNoteClick, modifier = modifier)
    }
}

@Composable
fun SetupContent(modifier: Modifier = Modifier, notes: List<Note>, userPreferences: UserPreferences, authManager: AuthManager, currentUser: com.google.firebase.auth.FirebaseUser?, onAuthChange: () -> Unit, onEraseAll: () -> Unit) {
    val address by userPreferences.address.collectAsState()
    val appSecurityEnabled by userPreferences.appSecurityEnabled.collectAsState()
    val encryptionKey by userPreferences.encryptionKey.collectAsState()
    val theme by userPreferences.theme.collectAsState()
    val enableBiometrics by userPreferences.enableBiometrics.collectAsState()
    val autoLockTimeout by userPreferences.autoLockTimeout.collectAsState()
    val fontSize by userPreferences.fontSize.collectAsState()
    val analyticsEnabled by userPreferences.analyticsEnabled.collectAsState()
    val cloudUsageBytes by userPreferences.cloudUsageBytes.collectAsState()
    
    var editAddress by remember { mutableStateOf(address) }
    val scope = rememberCoroutineScope()
    
    var showUploadDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Settings & Profile", color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp, top = 24.dp))
        
        if (currentUser == null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sign in to Sync & Secure", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Log in with Google to enable cloud backups and advanced security recovery.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            scope.launch {
                                val success = authManager.signInWithGoogle()
                                if (success) onAuthChange()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Text("Continue with Google")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Linked Google Account", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(currentUser.displayName ?: "Unknown Name", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(currentUser.email ?: "No Email", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            authManager.signOut()
                            onAuthChange()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                    ) {
                        Text("Log Out")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Personal Details", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                OutlinedTextField(value = editAddress, onValueChange = { editAddress = it; userPreferences.saveAddress(it) }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Security & Privacy", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("AES-256 GCM is active. Your keys are secured in the Android Keystore.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                var showPinSetupDialog by remember { mutableStateOf(false) }
                var showPinDisableDialog by remember { mutableStateOf(false) }
                var firstPin by remember { mutableStateOf("") }
                var confirmPin by remember { mutableStateOf("") }
                var oldPinInput by remember { mutableStateOf("") }
                var pinError by remember { mutableStateOf("") }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("App Security Lock", color = MaterialTheme.colorScheme.onSurface)
                    androidx.compose.material3.Switch(
                        checked = appSecurityEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                firstPin = ""
                                confirmPin = ""
                                pinError = ""
                                showPinSetupDialog = true
                            } else {
                                oldPinInput = ""
                                pinError = ""
                                showPinDisableDialog = true
                            }
                        }
                    )
                }

                if (showPinSetupDialog) {
                    AlertDialog(
                        onDismissRequest = { showPinSetupDialog = false },
                        title = { Text("Enable App Security") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Set a 4-6 digit PIN to lock the app.", fontSize = 14.sp)
                                OutlinedTextField(
                                    value = firstPin,
                                    onValueChange = { firstPin = it },
                                    label = { Text("New PIN") },
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = confirmPin,
                                    onValueChange = { confirmPin = it },
                                    label = { Text("Confirm PIN") },
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (pinError.isNotEmpty()) {
                                    Text(pinError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (firstPin.length in 4..6) {
                                        if (firstPin == confirmPin) {
                                            userPreferences.saveEncryptionKey(firstPin)
                                            userPreferences.saveAppSecurityEnabled(true)
                                            showPinSetupDialog = false
                                        } else {
                                            pinError = "PINs do not match."
                                        }
                                    } else {
                                        pinError = "PIN must be 4-6 digits."
                                    }
                                }
                            ) {
                                Text("Enable")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPinSetupDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showPinDisableDialog) {
                    AlertDialog(
                        onDismissRequest = { showPinDisableDialog = false },
                        title = { Text("Disable App Security") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Please enter your current PIN to disable.", fontSize = 14.sp)
                                OutlinedTextField(
                                    value = oldPinInput,
                                    onValueChange = { oldPinInput = it },
                                    label = { Text("Current PIN") },
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (pinError.isNotEmpty()) {
                                    Text(pinError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (oldPinInput == encryptionKey) {
                                        userPreferences.saveAppSecurityEnabled(false)
                                        showPinDisableDialog = false
                                    } else {
                                        pinError = "Incorrect PIN."
                                    }
                                }
                            ) {
                                Text("Disable")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPinDisableDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Biometric Unlock", color = MaterialTheme.colorScheme.onSurface)
                    androidx.compose.material3.Switch(checked = enableBiometrics, onCheckedChange = { userPreferences.saveEnableBiometrics(it) })
                }
                
                Text("Auto-Lock Timeout", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val timeouts = listOf("Immediate", "1 Minute", "5 Minutes", "Never")
                    items(timeouts.size) { index ->
                        val t = timeouts[index]
                        FilterChip(
                            selected = autoLockTimeout == t,
                            onClick = { userPreferences.saveAutoLockTimeout(t) },
                            label = { Text(t) }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Appearance & Features", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("App Theme", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val themes = listOf("Dark", "Light", "System")
                    items(themes.size) { index ->
                        val t = themes[index]
                        FilterChip(
                            selected = theme == t,
                            onClick = { userPreferences.saveTheme(t) },
                            label = { Text(t) }
                        )
                    }
                }

                Text("Reading Font Size", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val sizes = listOf("Small", "Medium", "Large")
                    items(sizes.size) { index ->
                        val s = sizes[index]
                        FilterChip(
                            selected = fontSize == s,
                            onClick = { userPreferences.saveFontSize(s) },
                            label = { Text(s) }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Advanced Data Management", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Share Crash Analytics", color = MaterialTheme.colorScheme.onSurface)
                    androidx.compose.material3.Switch(checked = analyticsEnabled, onCheckedChange = { userPreferences.saveAnalyticsEnabled(it) })
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    val scope = rememberCoroutineScope()
                    var isSyncing by remember { mutableStateOf(false) }

                    Button(
                        onClick = { 
                            scope.launch {
                                isSyncing = true
                                if (currentUser == null) {
                                    val success = authManager.signInWithGoogle()
                                    if (success) {
                                        onAuthChange()
                                        showUploadDialog = true
                                    }
                                } else {
                                    showUploadDialog = true
                                }
                                isSyncing = false
                            }
                        }, 
                        modifier = Modifier.weight(1f), 
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Text(if (isSyncing) "Authenticating..." else "Upload To Cloud")
                    }
                    Button(
                        onClick = { showExportDialog = true }, 
                        modifier = Modifier.weight(1f), 
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Text("Export Data")
                    }
                }
                Text("Cloud Usage: ${cloudUsageBytes / 1024} KB / 10 MB", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onEraseAll, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)) {
            Text("Erase All Local Data")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
    
    if (showUploadDialog) {
        NoteSelectionDialog(
            notes = notes,
            title = "Upload To Cloud",
            actionText = "Upload",
            onDismiss = { showUploadDialog = false },
            onConfirm = { selected ->
                val MAX_BYTES = 10 * 1024 * 1024L
                val selectedSize = selected.sumOf { (it.title.toByteArray().size + it.content.toByteArray().size).toLong() }
                
                if (cloudUsageBytes + selectedSize > MAX_BYTES) {
                    android.widget.Toast.makeText(context, "Cloud limit of 10MB exceeded!", android.widget.Toast.LENGTH_LONG).show()
                } else {
                    userPreferences.saveCloudUsageBytes(cloudUsageBytes + selectedSize)
                    android.widget.Toast.makeText(context, "Uploaded to cloud successfully!", android.widget.Toast.LENGTH_SHORT).show()
                }
                showUploadDialog = false
            }
        )
    }

    if (showExportDialog) {
        NoteSelectionDialog(
            notes = notes,
            title = "Export Notes",
            actionText = "Export",
            onDismiss = { showExportDialog = false },
            onConfirm = { selected ->
                if (selected.size == 1) {
                    com.example.utils.ExportManager.exportAsPdf(context, selected.first())
                } else {
                    com.example.utils.ExportManager.exportAsZip(context, selected)
                }
                showExportDialog = false
            }
        )
    }
}

@Composable
fun NoteSelectionDialog(
    notes: List<com.example.data.Note>,
    title: String,
    actionText: String,
    onDismiss: () -> Unit,
    onConfirm: (List<com.example.data.Note>) -> Unit
) {
    var selectedNotes by remember { mutableStateOf(setOf<com.example.data.Note>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                items(notes) { note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedNotes.contains(note)) {
                                    selectedNotes = selectedNotes - note
                                } else {
                                    selectedNotes = selectedNotes + note
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedNotes.contains(note),
                            onCheckedChange = { checked ->
                                if (checked) {
                                    selectedNotes = selectedNotes + note
                                } else {
                                    selectedNotes = selectedNotes - note
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(note.title.ifEmpty { "Untitled" }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedNotes.toList()) },
                enabled = selectedNotes.isNotEmpty()
            ) {
                Text(actionText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
