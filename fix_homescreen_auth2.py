import re

with open('app/src/main/java/com/hyper/note/android/ui/HomeScreen.kt', 'r') as f:
    content = f.read()

replacement = """var currentUser by remember { mutableStateOf(authManager.currentUser) }
    
    androidx.compose.runtime.DisposableEffect(Unit) {
        var listener: com.google.firebase.auth.FirebaseAuth.AuthStateListener? = null
        try {
            listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { auth ->
                currentUser = auth.currentUser
            }
            com.google.firebase.auth.FirebaseAuth.getInstance().addAuthStateListener(listener!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onDispose {
            try {
                listener?.let {
                    com.google.firebase.auth.FirebaseAuth.getInstance().removeAuthStateListener(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }"""

# Replace currentUser state definition
content = re.sub(
    r'var currentUser by remember \{ mutableStateOf\(authManager\.currentUser\) \}.*?onDispose \{.*?\}',
    replacement,
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/hyper/note/android/ui/HomeScreen.kt', 'w') as f:
    f.write(content)
