import re

with open('app/src/main/java/com/hyper/note/android/ui/HomeScreen.kt', 'r') as f:
    content = f.read()

# Fix 1: Top bar avatar click
content = re.sub(
    r'scope\.launch \{\s*onNavigateToAuth\(\)\s*if \(success\) \{\s*currentUser = authManager\.currentUser\s*\}\s*\}',
    r'onNavigateToAuth()',
    content
)

# Fix 2: Vault Content button
content = re.sub(
    r'scope\.launch \{\s*onNavigateToAuth\(\)\s*if \(success\) \{\s*isAuthenticated = true\s*onAuthChange\(\)\s*\}\s*isAuthenticating = false\s*\}',
    r'onNavigateToAuth()',
    content
)

# Fix 3: SetupContent button
content = re.sub(
    r'scope\.launch \{\s*onNavigateToAuth\(\)\s*if \(success\) onAuthChange\(\)\s*\}',
    r'onNavigateToAuth()',
    content
)

# Fix 4: Advanced data management sync button
content = re.sub(
    r'if \(currentUser == null\) \{\s*onNavigateToAuth\(\)\s*if \(success\) \{\s*onAuthChange\(\)\s*showUploadDialog = true\s*\}\s*\} else \{',
    r'if (currentUser == null) { onNavigateToAuth() } else {',
    content
)

with open('app/src/main/java/com/hyper/note/android/ui/HomeScreen.kt', 'w') as f:
    f.write(content)
