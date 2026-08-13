import re

with open('app/src/main/java/com/hyper/note/android/ui/HomeScreen.kt', 'r') as f:
    content = f.read()

# Replace currentUser state definition
content = content.replace(
    'var currentUser by remember { mutableStateOf(authManager.currentUser) }',
    """var currentUser by remember { mutableStateOf(authManager.currentUser) }
    
    androidx.compose.runtime.DisposableEffect(Unit) {
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        com.google.firebase.auth.FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose {
            com.google.firebase.auth.FirebaseAuth.getInstance().removeAuthStateListener(listener)
        }
    }"""
)

with open('app/src/main/java/com/hyper/note/android/ui/HomeScreen.kt', 'w') as f:
    f.write(content)
